package com.yaka.cibuburrentalcar.data.repository

import com.yaka.cibuburrentalcar.data.Car
import com.yaka.cibuburrentalcar.data.SampleCarData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay

class CarRepository {

    private val firebaseRepository = FirebaseCarRepository()

    suspend fun getCars(
        type: String? = null,
        search: String? = null,
        availableOnly: Boolean = true,
        retryCount: Int = 0
    ): NetworkResult<List<Car>> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("CarRepository", "🔥 Getting cars from Firebase with fallback")

                // Try Firebase first, fallback to sample data if fails
                val firebaseResult = firebaseRepository.getCars(type, search, availableOnly)

                when (firebaseResult) {
                    is NetworkResult.Success -> {
                        android.util.Log.d("CarRepository", "✅ Firebase success: ${firebaseResult.data?.size ?: 0} cars")
                        return@withContext firebaseResult
                    }
                    is NetworkResult.Error -> {
                        android.util.Log.w("CarRepository", "⚠️ Firebase failed: ${firebaseResult.message}")
                        android.util.Log.d("CarRepository", "📱 Using sample data as fallback")
                        return@withContext getFallbackCars(type, search, availableOnly)
                    }
                    is NetworkResult.Loading -> {
                        return@withContext NetworkResult.Loading()
                    }
                }

            } catch (e: Exception) {
                android.util.Log.e("CarRepository", "Error in getCars: ${e.message}", e)
                return@withContext getFallbackCars(type, search, availableOnly)
            }
        }
    }

    private suspend fun getFallbackCars(
        type: String? = null,
        search: String? = null,
        availableOnly: Boolean = true
    ): NetworkResult<List<Car>> {
        return try {
            android.util.Log.d("CarRepository", "Using fallback sample data")

            var cars = SampleCarData.cars

            // Filter by availability
            if (availableOnly) {
                cars = cars.filter { it.isAvailable }
            }

            // Filter by type
            if (!type.isNullOrBlank() && type != "Semua") {
                cars = cars.filter { it.type.name.equals(type, ignoreCase = true) }
            }

            // Filter by search
            if (!search.isNullOrBlank()) {
                cars = cars.filter {
                    it.name.contains(search, ignoreCase = true) ||
                    it.brand.contains(search, ignoreCase = true)
                }
            }

            android.util.Log.d("CarRepository", "Fallback: Returning ${cars.size} cars")
            NetworkResult.Success(cars)
        } catch (e: Exception) {
            android.util.Log.e("CarRepository", "Error in fallback: ${e.message}", e)
            NetworkResult.Error("Error getting cars: ${e.message}")
        }
    }

    // Real-time refresh function
    suspend fun refreshCars(
        type: String? = null,
        search: String? = null,
        availableOnly: Boolean = true
    ): NetworkResult<List<Car>> {
        android.util.Log.d("CarRepository", "🔥 Refreshing cars from Firebase...")
        return getCars(type, search, availableOnly)
    }

    // Get car availability status from Firebase
    suspend fun getCarAvailability(carId: String): Boolean {
        return try {
            android.util.Log.d("CarRepository", "📱 Getting availability from sample data")
            SampleCarData.cars.find { it.id == carId }?.isAvailable ?: true
        } catch (e: Exception) {
            android.util.Log.e("CarRepository", "Error getting car availability: ${e.message}")
            true // Default to available
        }
    }

    suspend fun getCarById(id: String, retryCount: Int = 0): NetworkResult<Car> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("CarRepository", "🔥 Getting car by ID: $id")

                // Try Firebase first
                android.util.Log.d("CarRepository", "🔥 Trying Firebase for car: $id")
                val firebaseResult = firebaseRepository.getCarById(id)

                when (firebaseResult) {
                    is NetworkResult.Success -> {
                        android.util.Log.d("CarRepository", "✅ Firebase success: ${firebaseResult.data?.name}")
                        return@withContext firebaseResult
                    }
                    is NetworkResult.Error -> {
                        android.util.Log.w("CarRepository", "⚠️ Firebase failed: ${firebaseResult.message}")
                        android.util.Log.d("CarRepository", "📱 Fallback to sample data for car: $id")

                        // Fallback to sample data
                        val localCar = SampleCarData.cars.find { it.id == id }
                        if (localCar != null) {
                            android.util.Log.d("CarRepository", "✅ Found car in sample data: ${localCar.name}")
                            return@withContext NetworkResult.Success(localCar)
                        } else {
                            android.util.Log.e("CarRepository", "❌ Car $id not found in sample data")
                            return@withContext NetworkResult.Error("Car not found")
                        }
                    }
                    is NetworkResult.Loading -> {
                        return@withContext NetworkResult.Loading()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CarRepository", "Error getting car by id: ${e.message}", e)
                // Fallback to sample data
                val localCar = SampleCarData.cars.find { it.id == id }
                if (localCar != null) {
                    NetworkResult.Success(localCar)
                } else {
                    NetworkResult.Error("Error getting car: ${e.message}")
                }
            }
        }
    }

    suspend fun searchCars(query: String): NetworkResult<List<Car>> {
        return getCars(search = query)
    }

    suspend fun getCarsByType(type: String): NetworkResult<List<Car>> {
        return getCars(type = type)
    }

    suspend fun getAvailableCars(): NetworkResult<List<Car>> {
        return getCars(availableOnly = true)
    }
    suspend fun addCar(car: Car): NetworkResult<String> {
        return firebaseRepository.addCar(car)
    }

    suspend fun updateCar(car: Car): NetworkResult<Unit> {
        return firebaseRepository.updateCar(car)
    }

    suspend fun deleteCar(carId: String): NetworkResult<Unit> {
        return firebaseRepository.deleteCar(carId)
    }
}
