package com.yaka.cibuburrentalcar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yaka.cibuburrentalcar.data.Car
import com.yaka.cibuburrentalcar.data.repository.CarRepository
import com.yaka.cibuburrentalcar.data.repository.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CarDetailUiState(
    val isLoading: Boolean = false,
    val car: Car? = null,
    val error: String? = null,
    val isBooking: Boolean = false
)

class CarDetailViewModel : ViewModel() {
    
    private val carRepository = CarRepository()
    
    private val _uiState = MutableStateFlow(CarDetailUiState())
    val uiState: StateFlow<CarDetailUiState> = _uiState.asStateFlow()
    
    fun loadCarDetail(carId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                android.util.Log.d("CarDetailViewModel", "🚗 Loading car detail for ID: $carId")
                
                when (val result = carRepository.getCarById(carId)) {
                    is NetworkResult.Success -> {
                        result.data?.let { car ->
                            android.util.Log.d("CarDetailViewModel", "✅ Car loaded: ${car.brand} ${car.name}")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                car = car,
                                error = null
                            )
                        } ?: run {
                            android.util.Log.e("CarDetailViewModel", "❌ Car data is null")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "Car data not found"
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        android.util.Log.e("CarDetailViewModel", "❌ Error loading car: ${result.message}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    is NetworkResult.Loading -> {
                        // Keep loading state
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CarDetailViewModel", "❌ Exception in loadCarDetail: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Terjadi kesalahan: ${e.message}"
                )
            }
        }
    }
    
    fun bookCar(car: Car) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBooking = true)
            
            try {
                android.util.Log.d("CarDetailViewModel", "📝 Booking car: ${car.brand} ${car.name}")
                
                // TODO: Implement actual booking logic
                // For now, just simulate booking process
                kotlinx.coroutines.delay(1000)
                
                android.util.Log.d("CarDetailViewModel", "✅ Car booked successfully")
                _uiState.value = _uiState.value.copy(isBooking = false)
                
            } catch (e: Exception) {
                android.util.Log.e("CarDetailViewModel", "❌ Error booking car: ${e.message}", e)
                _uiState.value = _uiState.value.copy(isBooking = false)
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
