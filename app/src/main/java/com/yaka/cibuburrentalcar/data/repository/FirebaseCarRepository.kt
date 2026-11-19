package com.yaka.cibuburrentalcar.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.yaka.cibuburrentalcar.data.Car
import com.yaka.cibuburrentalcar.data.CarType
import com.yaka.cibuburrentalcar.data.FuelType
import com.yaka.cibuburrentalcar.data.TransmissionType
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirebaseCarRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val carsCollection = firestore.collection("cars")

    companion object {
        private const val TAG = "FirebaseCarRepository"
    }

    suspend fun getCars(
        type: String? = null,
        search: String? = null,
        availableOnly: Boolean = true
    ): NetworkResult<List<Car>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔥 Fetching cars from Firebase Firestore")

                var query: Query = carsCollection

                // Filter by availability
                if (availableOnly) {
                    query = query.whereEqualTo("isAvailable", true)
                }

                // Filter by type
                if (!type.isNullOrEmpty() && type != "Semua" && type != "All") {
                    query = query.whereEqualTo("type", type.uppercase())
                }

                // Order by rating and review count
                query = query.orderBy("rating", Query.Direction.DESCENDING)

                val snapshot = query.get().await()
                val cars = mutableListOf<Car>()

                for (document in snapshot.documents) {
                    try {
                        val car = documentToCar(document.id, document.data ?: emptyMap())

                        // Apply search filter (client-side since Firestore has limited text search)
                        if (search.isNullOrEmpty() ||
                            car.name.contains(search, ignoreCase = true) ||
                            car.brand.contains(search, ignoreCase = true)) {
                            cars.add(car)
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "⚠️ Error parsing car document ${document.id}: ${e.message}")
                    }
                }

                Log.d(TAG, "✅ Successfully fetched ${cars.size} cars from Firebase")
                NetworkResult.Success(cars)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error fetching cars from Firebase: ${e.message}", e)
                NetworkResult.Error("Failed to fetch cars: ${e.message}")
            }
        }
    }

    suspend fun getCarById(carId: String): NetworkResult<Car> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔥 Fetching car $carId from Firebase")

                val document = carsCollection.document(carId).get().await()

                if (document.exists()) {
                    val car = documentToCar(document.id, document.data ?: emptyMap())
                    Log.d(TAG, "✅ Successfully fetched car: ${car.name}")
                    NetworkResult.Success(car)
                } else {
                    Log.w(TAG, "⚠️ Car $carId not found")
                    NetworkResult.Error("Car not found")
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error fetching car $carId: ${e.message}", e)
                NetworkResult.Error("Failed to fetch car: ${e.message}")
            }
        }
    }

    private fun documentToCar(id: String, data: Map<String, Any>): Car {
        val images = (data["imageUrls"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
        return Car(
            id = id,
            name = data["name"] as? String ?: "",
            brand = data["brand"] as? String ?: "",
            year = (data["year"] as? Long)?.toInt() ?: 2020,
            type = parseCarType(data["type"] as? String),
            transmission = parseTransmissionType(data["transmission"] as? String),
            fuelType = parseFuelType(data["fuelType"] as? String),
            seats = (data["seats"] as? Long)?.toInt() ?: 5,
            pricePerDay = (data["pricePerDay"] as? Long)?.toInt() ?: 0,
            imageUrl = data["imageUrl"] as? String ?: (images.firstOrNull() ?: ""),
            imageUrls = images,
            rating = (data["rating"] as? Double)?.toFloat() ?: 0f,
            reviewCount = (data["reviewCount"] as? Long)?.toInt() ?: 0,
            features = (data["features"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            isAvailable = data["isAvailable"] as? Boolean ?: true,
            location = data["location"] as? String ?: "Jakarta",
            maintenanceUntil = (data["maintenanceUntil"] as? Long)
        )
    }

    private fun parseCarType(typeString: String?): CarType {
        return try {
            CarType.valueOf(typeString?.uppercase() ?: "MPV")
        } catch (e: IllegalArgumentException) {
            CarType.MPV
        }
    }

    private fun parseTransmissionType(transmissionString: String?): TransmissionType {
        return try {
            TransmissionType.valueOf(transmissionString?.uppercase() ?: "MANUAL")
        } catch (e: IllegalArgumentException) {
            TransmissionType.MANUAL
        }
    }

    private fun parseFuelType(fuelString: String?): FuelType {
        return try {
            FuelType.valueOf(fuelString?.uppercase() ?: "GASOLINE")
        } catch (e: IllegalArgumentException) {
            FuelType.GASOLINE
        }
    }

    // Function to add sample data (call this once to populate Firestore)
    suspend fun addSampleData(): NetworkResult<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔥 Adding sample data to Firebase")

                val sampleCars = getSampleCarsData()

                for (car in sampleCars) {
                    val carData = carToMap(car)
                    carsCollection.document(car.id).set(carData).await()
                }

                Log.d(TAG, "✅ Successfully added ${sampleCars.size} sample cars")
                NetworkResult.Success("Sample data added successfully")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error adding sample data: ${e.message}", e)
                NetworkResult.Error("Failed to add sample data: ${e.message}")
            }
        }
    }

    private fun carToMap(car: Car): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "name" to car.name,
            "brand" to car.brand,
            "year" to car.year,
            "type" to car.type.name,
            "transmission" to car.transmission.name,
            "fuelType" to car.fuelType.name,
            "seats" to car.seats,
            "pricePerDay" to car.pricePerDay,
            "imageUrl" to car.imageUrl,
            "imageUrls" to car.imageUrls,
            "rating" to car.rating,
            "reviewCount" to car.reviewCount,
            "features" to car.features,
            "isAvailable" to car.isAvailable,
            "location" to car.location
        )
        car.maintenanceUntil?.let { map["maintenanceUntil"] = it }
        return map
    }

    private fun getSampleCarsData(): List<Car> {
        // Use the same data as SampleCarData for consistency
        return com.yaka.cibuburrentalcar.data.SampleCarData.cars
    }

    suspend fun addCar(car: Car): NetworkResult<String> = withContext(Dispatchers.IO) {
        try {
            val docRef = carsCollection.document()
            val carData = carToMap(car.copy(id = docRef.id))
            docRef.set(carData).await()
            NetworkResult.Success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error adding car: ${e.message}", e)
            NetworkResult.Error("Failed to add car: ${e.message}")
        }
    }

    suspend fun updateCar(car: Car): NetworkResult<Unit> = withContext(Dispatchers.IO) {
        try {
            require(car.id.isNotBlank()) { "Car ID is required" }
            val carData = carToMap(car)
            carsCollection.document(car.id).update(carData).await()
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating car: ${e.message}", e)
            NetworkResult.Error("Failed to update car: ${e.message}")
        }
    }

    suspend fun deleteCar(carId: String): NetworkResult<Unit> = withContext(Dispatchers.IO) {
        try {
            carsCollection.document(carId).delete().await()
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error deleting car: ${e.message}", e)
            NetworkResult.Error("Failed to delete car: ${e.message}")
        }
    }
}
