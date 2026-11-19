package com.yaka.cibuburrentalcar.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.yaka.cibuburrentalcar.data.Rental
import com.yaka.cibuburrentalcar.data.RentalStatus
import com.yaka.cibuburrentalcar.data.getAutoStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar

class FirebaseBookingRepository {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val bookingsCollection = firestore.collection("bookings")
    
    companion object {
        private const val TAG = "FirebaseBookingRepository"
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
                Log.d(TAG, "🔥 Creating booking for car: $carId, user: $userId")
                
                val bookingId = bookingsCollection.document().id
                
                val booking = Rental(
                    id = bookingId,
                    carId = carId,
                    userId = userId,
                    startDate = startDate,
                    endDate = endDate,
                    pickupLocation = pickupLocation,
                    dropoffLocation = dropoffLocation,
                    totalDays = totalDays,
                    pricePerDay = pricePerDay,
                    totalPrice = totalPrice,
                    status = RentalStatus.MENUNGGU,
                    createdAt = LocalDateTime.now(),
                    notes = notes
                )
                
                val bookingData = rentalToMap(booking)
                bookingsCollection.document(bookingId).set(bookingData).await()

                // DON'T update car availability - car should remain visible in catalog
                // Only specific dates should be blocked, not the entire car

                Log.d(TAG, "✅ Booking created successfully: $bookingId")
                NetworkResult.Success(booking)
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error creating booking: ${e.message}", e)
                NetworkResult.Error("Gagal membuat booking: ${e.message}")
            }
        }
    }

    suspend fun getUserBookings(userId: String): NetworkResult<List<Rental>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔥 Fetching bookings for user: $userId")
                
                val querySnapshot = bookingsCollection
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()
                
                val bookings = querySnapshot.documents.mapNotNull { document ->
                    try {
                        val rental = documentToRental(document.id, document.data ?: emptyMap())
                        // Auto-update status berdasarkan tanggal
                        updateStatusIfNeeded(rental)
                    } catch (e: Exception) {
                        Log.w(TAG, "Error parsing booking ${document.id}: ${e.message}")
                        null
                    }
                }
                
                Log.d(TAG, "✅ Found ${bookings.size} bookings for user: $userId")
                NetworkResult.Success(bookings)
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error fetching user bookings: ${e.message}", e)
                NetworkResult.Error("Gagal memuat riwayat booking: ${e.message}")
            }
        }
    }

    suspend fun getBookingById(bookingId: String): NetworkResult<Rental> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔥 Fetching booking: $bookingId")
                
                val document = bookingsCollection.document(bookingId).get().await()
                
                if (document.exists()) {
                    val booking = documentToRental(document.id, document.data ?: emptyMap())
                    Log.d(TAG, "✅ Booking found: $bookingId")
                    NetworkResult.Success(booking)
                } else {
                    Log.w(TAG, "⚠️ Booking not found: $bookingId")
                    NetworkResult.Error("Booking tidak ditemukan")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error fetching booking: ${e.message}", e)
                NetworkResult.Error("Gagal memuat booking: ${e.message}")
            }
        }
    }

    suspend fun getBlockedDates(carId: String): NetworkResult<List<String>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔥 Getting blocked dates for car: $carId")

                // Get all active bookings for this car
                val querySnapshot = bookingsCollection
                    .whereEqualTo("carId", carId)
                    .whereIn("status", listOf("MENUNGGU", "AKTIF"))
                    .get()
                    .await()

                val blockedDates = mutableSetOf<String>()
                val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")

                for (document in querySnapshot.documents) {
                    try {
                        val data = document.data ?: continue
                        val startDateStr = data["startDate"] as? String
                        val endDateStr = data["endDate"] as? String

                        if (startDateStr != null && endDateStr != null) {
                            val startDate = java.time.LocalDate.parse(startDateStr)
                            val endDate = java.time.LocalDate.parse(endDateStr)

                            // Add all dates between start and end (inclusive)
                            var currentDate = startDate
                            while (!currentDate.isAfter(endDate)) {
                                blockedDates.add(currentDate.format(dateFormatter))
                                currentDate = currentDate.plusDays(1)
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Error parsing booking dates from document ${document.id}: ${e.message}")
                    }
                }

                val sortedBlockedDates = blockedDates.sorted()
                Log.d(TAG, "✅ Found ${sortedBlockedDates.size} blocked dates for car $carId: $sortedBlockedDates")
                NetworkResult.Success(sortedBlockedDates)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error getting blocked dates: ${e.message}", e)
                NetworkResult.Error("Gagal memuat tanggal terblokir: ${e.message}")
            }
        }
    }

    suspend fun updateBookingStatus(bookingId: String, status: RentalStatus): NetworkResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔥 Updating booking status: $bookingId -> $status")

                bookingsCollection.document(bookingId)
                    .update("status", status.name)
                    .await()

                // DON'T update car availability - dates will be automatically unblocked
                // when booking status changes to cancelled/completed

                Log.d(TAG, "✅ Booking status updated: $bookingId")
                NetworkResult.Success(true)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error updating booking status: ${e.message}", e)
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
                Log.d(TAG, "🔥 Updating booking: $bookingId")

                val updateData = mapOf(
                    "startDate" to startDate.toString(),
                    "endDate" to endDate.toString(),
                    "totalDays" to totalDays,
                    "totalPrice" to totalPrice,
                    "notes" to notes
                )

                bookingsCollection.document(bookingId)
                    .update(updateData)
                    .await()

                Log.d(TAG, "✅ Booking updated: $bookingId")
                NetworkResult.Success(true)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error updating booking: ${e.message}", e)
                NetworkResult.Error("Gagal update booking: ${e.message}")
            }
        }
    }

    private suspend fun updateCarAvailability(carId: String, isAvailable: Boolean) {
        try {
            firestore.collection("cars")
                .document(carId)
                .update("isAvailable", isAvailable)
                .await()
            Log.d(TAG, "✅ Car availability updated: $carId -> $isAvailable")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating car availability: ${e.message}", e)
        }
    }

    private fun rentalToMap(rental: Rental): Map<String, Any> {
        return mapOf(
            "carId" to rental.carId,
            "userId" to rental.userId,
            "startDate" to rental.startDate.toString(),
            "endDate" to rental.endDate.toString(),
            "pickupLocation" to rental.pickupLocation,
            "dropoffLocation" to rental.dropoffLocation,
            "totalDays" to rental.totalDays,
            "pricePerDay" to rental.pricePerDay,
            "totalPrice" to rental.totalPrice,
            "status" to rental.status.name,
            "createdAt" to rental.createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "notes" to rental.notes
        )
    }

    private fun documentToRental(id: String, data: Map<String, Any>): Rental {
        return Rental(
            id = id,
            carId = data["carId"] as? String ?: "",
            userId = data["userId"] as? String ?: "",
            startDate = LocalDate.parse(data["startDate"] as? String ?: LocalDate.now().toString()),
            endDate = LocalDate.parse(data["endDate"] as? String ?: LocalDate.now().toString()),
            pickupLocation = data["pickupLocation"] as? String ?: "",
            dropoffLocation = data["dropoffLocation"] as? String ?: "",
            totalDays = (data["totalDays"] as? Long)?.toInt() ?: 1,
            pricePerDay = (data["pricePerDay"] as? Long)?.toInt() ?: 0,
            totalPrice = (data["totalPrice"] as? Long)?.toInt() ?: 0,
            status = parseRentalStatus(data["status"] as? String),
            createdAt = LocalDateTime.parse(data["createdAt"] as? String ?: LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)),
            notes = data["notes"] as? String ?: ""
        )
    }

    private fun parseRentalStatus(status: String?): RentalStatus {
        return try {
            RentalStatus.valueOf(status ?: "MENUNGGU")
        } catch (e: Exception) {
            RentalStatus.MENUNGGU
        }
    }

    /**
     * Auto-update status berdasarkan tanggal dan return rental dengan status terbaru
     */
    private suspend fun updateStatusIfNeeded(rental: Rental): Rental {
        return try {
            // Import extension function
            val autoStatus = rental.getAutoStatus()

            // Jika status berbeda, update di Firebase
            if (rental.status != autoStatus) {
                Log.d(TAG, "🔄 Auto-updating status for booking ${rental.id}: ${rental.status} -> $autoStatus")

                bookingsCollection.document(rental.id)
                    .update("status", autoStatus.name)
                    .await()

                // Return rental dengan status yang sudah diupdate
                rental.copy(status = autoStatus)
            } else {
                // Status sudah benar, return as is
                rental
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error auto-updating status for booking ${rental.id}: ${e.message}")
            // Return original rental jika gagal update
            rental
        }
    }
}
