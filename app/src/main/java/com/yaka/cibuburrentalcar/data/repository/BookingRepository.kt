package com.yaka.cibuburrentalcar.data.repository

import com.yaka.cibuburrentalcar.data.Rental
import com.yaka.cibuburrentalcar.data.RentalStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class BookingRepository {

    private val firebaseBookingRepository = FirebaseBookingRepository()

    suspend fun getUserBookings(userId: String): NetworkResult<List<Rental>> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("BookingRepository", "🔥 Getting user bookings from Firebase: $userId")
                firebaseBookingRepository.getUserBookings(userId)
            } catch (e: Exception) {
                android.util.Log.e("BookingRepository", "Error getting user bookings: ${e.message}", e)
                NetworkResult.Error("Gagal memuat riwayat booking: ${e.message}")
            }
        }
    }

    suspend fun createBooking(
        carId: String,
        userId: String,
        startDate: LocalDate,
        endDate: LocalDate,
        pickupLocation: String,
        dropoffLocation: String,
        totalDays: Int,
        pricePerDay: Int,
        totalPrice: Int,
        notes: String = ""
    ): NetworkResult<Rental> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("BookingRepository", "🔥 Creating booking in Firebase")
                firebaseBookingRepository.createBooking(
                    carId, userId, startDate, endDate, pickupLocation, dropoffLocation,
                    totalDays, pricePerDay, totalPrice, notes
                )
            } catch (e: Exception) {
                android.util.Log.e("BookingRepository", "Error creating booking: ${e.message}", e)
                NetworkResult.Error("Gagal membuat booking: ${e.message}")
            }
        }
    }

    suspend fun getBookingById(bookingId: String): NetworkResult<Rental> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("BookingRepository", "🔥 Getting booking by ID from Firebase: $bookingId")
                firebaseBookingRepository.getBookingById(bookingId)
            } catch (e: Exception) {
                android.util.Log.e("BookingRepository", "Error getting booking: ${e.message}", e)
                NetworkResult.Error("Gagal memuat booking: ${e.message}")
            }
        }
    }

    suspend fun updateBookingStatus(bookingId: String, status: RentalStatus): NetworkResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("BookingRepository", "🔥 Updating booking status in Firebase: $bookingId -> $status")
                firebaseBookingRepository.updateBookingStatus(bookingId, status)
            } catch (e: Exception) {
                android.util.Log.e("BookingRepository", "Error updating booking status: ${e.message}", e)
                NetworkResult.Error("Gagal update status booking: ${e.message}")
            }
        }
    }

    suspend fun updateBooking(
        bookingId: String,
        startDate: LocalDate,
        endDate: LocalDate,
        totalDays: Int,
        totalPrice: Int,
        notes: String
    ): NetworkResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("BookingRepository", "🔥 Updating booking in Firebase: $bookingId")
                firebaseBookingRepository.updateBooking(
                    bookingId = bookingId,
                    startDate = startDate,
                    endDate = endDate,
                    totalDays = totalDays,
                    totalPrice = totalPrice,
                    notes = notes
                )
            } catch (e: Exception) {
                android.util.Log.e("BookingRepository", "Error updating booking: ${e.message}", e)
                NetworkResult.Error("Gagal update booking: ${e.message}")
            }
        }
    }

    // Get blocked dates for a specific car (bookings + maintenance)
    suspend fun getBlockedDates(carId: String): NetworkResult<List<String>> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("BookingRepository", "🔥 Getting blocked dates for car: $carId (bookings + maintenance)")

                // 1) Blocked dates from active bookings (MENUNGGU/AKTIF)
                val bookingResult = firebaseBookingRepository.getBlockedDates(carId)
                val bookingBlocked: List<String> = when (bookingResult) {
                    is NetworkResult.Success -> bookingResult.data ?: emptyList()
                    is NetworkResult.Error -> emptyList()
                    is NetworkResult.Loading -> emptyList()
                }

                // 2) Maintenance blocks from car subcollection
                val blockRepo = CalendarBlockRepository()
                val maintenanceBlocked: List<String> = when (val blocksRes = blockRepo.listBlocks(carId)) {
                    is NetworkResult.Success -> {
                        val blocks = blocksRes.data.orEmpty()
                        val dates = mutableListOf<String>()
                        blocks.forEach { b ->
                            try {
                                if (b.startDate.isNotBlank() && b.endDate.isNotBlank()) {
                                    var d = LocalDate.parse(b.startDate)
                                    val end = LocalDate.parse(b.endDate)
                                    while (!d.isAfter(end)) {
                                        dates.add(d.toString())
                                        d = d.plusDays(1)
                                    }
                                }
                            } catch (_: Exception) { }
                        }
                        dates
                    }
                    else -> emptyList()
                }

                val combined = (bookingBlocked + maintenanceBlocked).toSet().sorted()
                android.util.Log.d("BookingRepository", "✅ Combined blocked dates: bookings=${bookingBlocked.size}, maintenance=${maintenanceBlocked.size}, total=${combined.size}")

                NetworkResult.Success(combined)

            } catch (e: Exception) {
                android.util.Log.e("BookingRepository", "❌ Exception getting blocked dates: ${e.message}", e)
                NetworkResult.Error("Gagal memuat tanggal terblokir: ${e.message}")
            }
        }
    }

    suspend fun hasActiveBooking(userIdentifier: String): NetworkResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("BookingRepository", "🔥 Checking active booking for: $userIdentifier")
                // Return false for now - user can always book
                NetworkResult.Success(false)
            } catch (e: Exception) {
                android.util.Log.e("BookingRepository", "Error checking active booking: ${e.message}", e)
                NetworkResult.Error("Gagal cek booking aktif: ${e.message}")
            }
        }
    }

    suspend fun getActiveBooking(userIdentifier: String): NetworkResult<Any?> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("BookingRepository", "🔥 Getting active booking for: $userIdentifier")
                // Return null for now
                NetworkResult.Success(null)
            } catch (e: Exception) {
                android.util.Log.e("BookingRepository", "Error getting active booking: ${e.message}", e)
                NetworkResult.Error("Gagal memuat booking aktif: ${e.message}")
            }
        }
    }

    suspend fun cancelBooking(userIdentifier: String): NetworkResult<String> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("BookingRepository", "🔥 Cancelling booking for: $userIdentifier")
                // Return success message
                NetworkResult.Success("Booking berhasil dibatalkan")
            } catch (e: Exception) {
                android.util.Log.e("BookingRepository", "Error cancelling booking: ${e.message}", e)
                NetworkResult.Error("Gagal batalkan booking: ${e.message}")
            }
        }
    }
}
