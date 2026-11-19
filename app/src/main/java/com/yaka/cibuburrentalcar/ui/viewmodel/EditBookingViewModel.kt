package com.yaka.cibuburrentalcar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yaka.cibuburrentalcar.data.repository.BookingRepository
import com.yaka.cibuburrentalcar.data.repository.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class EditBookingUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String = "",
    val blockedDates: List<String> = emptyList(),
    val isLoadingBlockedDates: Boolean = false
)

class EditBookingViewModel(
    private val bookingRepository: BookingRepository = BookingRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EditBookingUiState())
    val uiState: StateFlow<EditBookingUiState> = _uiState.asStateFlow()
    
    fun updateBooking(
        bookingId: String,
        startDate: LocalDate,
        endDate: LocalDate,
        totalDays: Int,
        totalPrice: Int,
        notes: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = "",
                isSuccess = false
            )
            
            try {
                // Validate dates
                if (endDate.isBefore(startDate)) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Tanggal selesai tidak boleh sebelum tanggal mulai"
                    )
                    return@launch
                }
                
                if (startDate.isBefore(LocalDate.now())) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Tanggal mulai tidak boleh di masa lalu"
                    )
                    return@launch
                }
                
                // Update booking
                val result = bookingRepository.updateBooking(
                    bookingId = bookingId,
                    startDate = startDate,
                    endDate = endDate,
                    totalDays = totalDays,
                    totalPrice = totalPrice,
                    notes = notes
                )
                
                when (result) {
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSuccess = true,
                            errorMessage = ""
                        )
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Terjadi kesalahan"
                        )
                    }
                    is NetworkResult.Loading -> {
                        // Keep loading state
                    }
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Terjadi kesalahan: ${e.message}"
                )
            }
        }
    }
    
    fun loadBlockedDates(carId: String?) {
        if (carId == null) return

        viewModelScope.launch {
            try {
                // Set loading state
                _uiState.value = _uiState.value.copy(
                    isLoadingBlockedDates = true
                )

                val result = bookingRepository.getBlockedDates(carId)
                when (result) {
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            blockedDates = result.data ?: emptyList(),
                            isLoadingBlockedDates = false
                        )
                    }
                    is NetworkResult.Error -> {
                        // Silently fail for blocked dates
                        _uiState.value = _uiState.value.copy(
                            blockedDates = emptyList(),
                            isLoadingBlockedDates = false
                        )
                    }
                    is NetworkResult.Loading -> {
                        // Keep loading state
                    }
                }
            } catch (e: Exception) {
                // Silently fail for blocked dates
                _uiState.value = _uiState.value.copy(
                    blockedDates = emptyList(),
                    isLoadingBlockedDates = false
                )
            }
        }
    }
}
