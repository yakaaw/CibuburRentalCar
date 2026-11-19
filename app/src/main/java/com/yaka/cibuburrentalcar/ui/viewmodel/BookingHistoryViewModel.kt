package com.yaka.cibuburrentalcar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yaka.cibuburrentalcar.data.Rental
import com.yaka.cibuburrentalcar.data.RentalStatus
import com.yaka.cibuburrentalcar.data.repository.BookingRepository
import com.yaka.cibuburrentalcar.data.repository.CarRepository
import com.yaka.cibuburrentalcar.data.Car
import com.yaka.cibuburrentalcar.data.repository.NetworkResult
import com.yaka.cibuburrentalcar.data.CurrentUserProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BookingHistoryUiState(
    val isLoading: Boolean = false,
    val bookingHistory: List<Rental> = emptyList(),
    val error: String? = null,
    val hasMore: Boolean = false,
    val carDetails: Map<String, Car> = emptyMap() // Cache car details by carId
)

class BookingHistoryViewModel : ViewModel() {

    private val bookingRepository = BookingRepository()
    private val carRepository = CarRepository()
    
    private val _uiState = MutableStateFlow(BookingHistoryUiState())
    val uiState: StateFlow<BookingHistoryUiState> = _uiState.asStateFlow()
    
    private var allBookingHistory: List<Rental> = emptyList()
    
    init {
        loadBookingHistory()
    }
    
    fun loadBookingHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Get current user ID (Firebase uses String IDs)
                val currentUser = CurrentUserProvider.currentUser
                val userId = currentUser.id // Now guaranteed to be non-empty

                android.util.Log.d("BookingHistoryViewModel", "🔄 Loading booking history for user: $userId")
                android.util.Log.d("BookingHistoryViewModel", "👤 Current user details: ID=${currentUser.id}, Name=${currentUser.name}, Email=${currentUser.email}")
                android.util.Log.d("BookingHistoryViewModel", "🔐 Is user logged in: ${CurrentUserProvider.isLoggedIn()}")

                // Check if using sample data
                if (currentUser.id == "U001") {
                    android.util.Log.w("BookingHistoryViewModel", "⚠️ Using sample user data - user might not be logged in properly")
                }

                when (val result = bookingRepository.getUserBookings(userId)) {
                    is NetworkResult.Success -> {
                        allBookingHistory = result.data ?: emptyList()
                        android.util.Log.d("BookingHistoryViewModel", "✅ Loaded ${allBookingHistory.size} bookings for user $userId")

                        if (allBookingHistory.isEmpty()) {
                            android.util.Log.d("BookingHistoryViewModel", "📭 No booking history found - database is empty or user has no bookings")
                        } else {
                            android.util.Log.d("BookingHistoryViewModel", "📋 Booking history details: $allBookingHistory")
                        }

                        // Apply current filter
                        applyFilter()

                        // Load car details for all bookings
                        loadCarDetails()

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                    is NetworkResult.Error<*> -> {
                        android.util.Log.e("BookingHistoryViewModel", "❌ Error loading booking history: ${result.message}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    is NetworkResult.Loading<*> -> {
                        // Keep loading state
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("BookingHistoryViewModel", "❌ Exception in loadBookingHistory: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Terjadi kesalahan: ${e.message}"
                )
            }
        }
    }
    
    // Filter dihapus sesuai permintaan product: selalu tampilkan semua booking
    
    private fun applyFilter() {
        // Filter dihapus → selalu gunakan semua riwayat, urut terbaru di atas
        _uiState.value = _uiState.value.copy(
            bookingHistory = allBookingHistory.sortedByDescending { it.createdAt }
        )
    }
    
    fun refreshBookingHistory() {
        android.util.Log.d("BookingHistoryViewModel", "🔄 Refreshing booking history")
        loadBookingHistory()
    }
    
    fun loadMoreBookings() {
        // TODO: Implement pagination if needed
        android.util.Log.d("BookingHistoryViewModel", "📄 Load more bookings requested")
    }
    
    fun retryLoadBookingHistory() {
        android.util.Log.d("BookingHistoryViewModel", "🔄 Retrying to load booking history")
        loadBookingHistory()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    // Helper function to get booking statistics
    fun getBookingStatistics(): BookingStatistics {
        return BookingStatistics(
            totalBookings = allBookingHistory.size,
            activeBookings = allBookingHistory.count { it.status == RentalStatus.AKTIF },
            completedBookings = allBookingHistory.count { it.status == RentalStatus.SELESAI },
            cancelledBookings = allBookingHistory.count { it.status == RentalStatus.DIBATALKAN },
            totalSpent = allBookingHistory.filter { it.status == RentalStatus.SELESAI }
                .sumOf { it.totalPrice.toDouble() }
        )
    }

    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            try {
                val result = bookingRepository.updateBookingStatus(bookingId, RentalStatus.DIBATALKAN)
                when (result) {
                    is NetworkResult.Success -> {
                        // Refresh booking history after cancellation
                        loadBookingHistory()
                    }
                    is NetworkResult.Error -> {
                        // Handle error silently or show toast
                        android.util.Log.e("BookingHistoryViewModel", "Error cancelling booking: ${result.message}")
                    }
                    is NetworkResult.Loading -> {
                        // Keep current state
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("BookingHistoryViewModel", "Error cancelling booking: ${e.message}", e)
            }
        }
    }

    private fun loadCarDetails() {
        viewModelScope.launch {
            try {
                val carIds = allBookingHistory.map { it.carId }.distinct()
                val carDetailsMap = mutableMapOf<String, Car>()

                android.util.Log.d("BookingHistoryViewModel", "🚗 Loading car details for IDs: $carIds")

                for (carId in carIds) {
                    when (val result = carRepository.getCarById(carId)) {
                        is NetworkResult.Success -> {
                            result.data?.let { car ->
                                carDetailsMap[carId] = car
                                android.util.Log.d("BookingHistoryViewModel", "✅ Loaded car: ${car.name} (ID: $carId)")
                            }
                        }
                        is NetworkResult.Error -> {
                            android.util.Log.e("BookingHistoryViewModel", "❌ Failed to load car $carId: ${result.message}")
                        }
                        is NetworkResult.Loading -> {
                            // Handle loading state if needed
                        }
                    }
                }

                _uiState.value = _uiState.value.copy(carDetails = carDetailsMap)
                android.util.Log.d("BookingHistoryViewModel", "🎯 Car details loaded: ${carDetailsMap.keys}")

            } catch (e: Exception) {
                android.util.Log.e("BookingHistoryViewModel", "❌ Error loading car details: ${e.message}", e)
            }
        }
    }


}

data class BookingStatistics(
    val totalBookings: Int,
    val activeBookings: Int,
    val completedBookings: Int,
    val cancelledBookings: Int,
    val totalSpent: Double
)
