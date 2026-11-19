package com.yaka.cibuburrentalcar.data

import androidx.compose.runtime.Immutable

@Immutable
data class Car(
    val id: String,
    val name: String,
    val brand: String,
    val year: Int,
    val type: CarType,
    val transmission: TransmissionType,
    val fuelType: FuelType,
    val seats: Int,
    val pricePerDay: Int,
    // First image string kept for backward compatibility (e.g., existing Firestore docs)
    val imageUrl: String,
    // New: up to 4 images; use these to render galleries when available
    val imageUrls: List<String> = emptyList(),
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val features: List<String> = emptyList(),
    val isAvailable: Boolean = true,
    val location: String = "Jakarta",
    val maintenanceUntil: Long? = null
)

enum class CarType {
    SEDAN,
    SUV,
    MPV,
    HATCHBACK,
    CONVERTIBLE,
    COUPE
}

enum class TransmissionType {
    MANUAL,
    AUTOMATIC,
    CVT
}

enum class FuelType {
    GASOLINE,
    DIESEL,
    ELECTRIC,
    HYBRID
}

// Sample data untuk fallback jika Firebase gagal
object SampleCarData {
    val cars = listOf(
        // 1. Honda Brio
        Car(
            id = "1",
            name = "Brio",
            brand = "Honda",
            year = 2023,
            type = CarType.HATCHBACK,
            transmission = TransmissionType.AUTOMATIC,
            fuelType = FuelType.GASOLINE,
            seats = 5,
            pricePerDay = 280000,
            imageUrl = "brio1",
            rating = 4.3f,
            reviewCount = 85,
            features = listOf("AC", "Audio System", "Power Steering", "Central Lock"),
            isAvailable = true,
            location = "Cibubur"
        ),
        // 2. Honda BRV
        Car(
            id = "2",
            name = "BRV",
            brand = "Honda",
            year = 2022,
            type = CarType.SUV,
            transmission = TransmissionType.AUTOMATIC,
            fuelType = FuelType.GASOLINE,
            seats = 7,
            pricePerDay = 450000,
            imageUrl = "brv1",
            rating = 4.4f,
            reviewCount = 92,
            features = listOf("AC", "Audio System", "Power Steering", "Central Lock", "ABS"),
            isAvailable = true,
            location = "Cibubur"
        ),
        // 3. Toyota Calya
        Car(
            id = "3",
            name = "Calya",
            brand = "Toyota",
            year = 2023,
            type = CarType.MPV,
            transmission = TransmissionType.MANUAL,
            fuelType = FuelType.GASOLINE,
            seats = 7,
            pricePerDay = 320000,
            imageUrl = "calya1",
            rating = 4.2f,
            reviewCount = 78,
            features = listOf("AC", "Audio System", "Power Steering"),
            isAvailable = true,
            location = "Cibubur"
        ),
        // 4. Toyota Camry
        Car(
            id = "4",
            name = "Camry",
            brand = "Toyota",
            year = 2023,
            type = CarType.SEDAN,
            transmission = TransmissionType.AUTOMATIC,
            fuelType = FuelType.GASOLINE,
            seats = 5,
            pricePerDay = 800000,
            imageUrl = "camry1",
            rating = 4.8f,
            reviewCount = 145,
            features = listOf("AC", "Audio System", "Power Steering", "Central Lock", "ABS", "Airbags", "Leather Seats"),
            isAvailable = true,
            location = "Cibubur"
        ),
        // 5. Honda City RS
        Car(
            id = "5",
            name = "City RS",
            brand = "Honda",
            year = 2023,
            type = CarType.HATCHBACK,
            transmission = TransmissionType.CVT,
            fuelType = FuelType.GASOLINE,
            seats = 5,
            pricePerDay = 380000,
            imageUrl = "cityrs1",
            rating = 4.5f,
            reviewCount = 103,
            features = listOf("AC", "Audio System", "Power Steering", "Central Lock", "ABS"),
            isAvailable = true,
            location = "Cibubur"
        ),
        // 6. Suzuki Ertiga
        Car(
            id = "6",
            name = "Ertiga",
            brand = "Suzuki",
            year = 2022,
            type = CarType.MPV,
            transmission = TransmissionType.AUTOMATIC,
            fuelType = FuelType.GASOLINE,
            seats = 7,
            pricePerDay = 420000,
            imageUrl = "ertiga1",
            rating = 4.3f,
            reviewCount = 89,
            features = listOf("AC", "Audio System", "Power Steering", "Central Lock"),
            isAvailable = true,
            location = "Cibubur"
        ),
        // 7. Honda HRV
        Car(
            id = "7",
            name = "HRV",
            brand = "Honda",
            year = 2023,
            type = CarType.SUV,
            transmission = TransmissionType.CVT,
            fuelType = FuelType.GASOLINE,
            seats = 5,
            pricePerDay = 520000,
            imageUrl = "hrv1",
            rating = 4.6f,
            reviewCount = 134,
            features = listOf("AC", "Audio System", "Power Steering", "Central Lock", "ABS", "Airbags"),
            isAvailable = true,
            location = "Cibubur"
        ),
        // 8. Toyota Innova
        Car(
            id = "8",
            name = "Innova",
            brand = "Toyota",
            year = 2021,
            type = CarType.MPV,
            transmission = TransmissionType.AUTOMATIC,
            fuelType = FuelType.GASOLINE,
            seats = 8,
            pricePerDay = 650000,
            imageUrl = "innovag1",
            rating = 4.5f,
            reviewCount = 156,
            features = listOf("AC", "Audio System", "Power Steering", "Central Lock", "ABS"),
            isAvailable = true,
            location = "Cibubur"
        ),
        // 9. Honda Jazz
        Car(
            id = "9",
            name = "Jazz",
            brand = "Honda",
            year = 2023,
            type = CarType.HATCHBACK,
            transmission = TransmissionType.CVT,
            fuelType = FuelType.GASOLINE,
            seats = 5,
            pricePerDay = 350000,
            imageUrl = "jazz1",
            rating = 4.4f,
            reviewCount = 97,
            features = listOf("AC", "Audio System", "Power Steering", "Central Lock"),
            isAvailable = true,
            location = "Cibubur"
        ),
        // 10. Honda Mobilio
        Car(
            id = "10",
            name = "Mobilio",
            brand = "Honda",
            year = 2022,
            type = CarType.MPV,
            transmission = TransmissionType.AUTOMATIC,
            fuelType = FuelType.GASOLINE,
            seats = 7,
            pricePerDay = 400000,
            imageUrl = "mobilio1",
            rating = 4.2f,
            reviewCount = 76,
            features = listOf("AC", "Audio System", "Power Steering", "Central Lock"),
            isAvailable = true,
            location = "Cibubur"
        ),
        // 11. Toyota Innova G
        Car(
            id = "11",
            name = "Innova G",
            brand = "Toyota",
            year = 2020,
            type = CarType.MPV,
            transmission = TransmissionType.AUTOMATIC,
            fuelType = FuelType.GASOLINE,
            seats = 8,
            pricePerDay = 600000,
            imageUrl = "reborn1",
            rating = 4.4f,
            reviewCount = 123,
            features = listOf("AC", "Audio System", "Power Steering", "Central Lock", "ABS"),
            isAvailable = true,
            location = "Cibubur"
        ),
        // 12. Daihatsu Sigra
        Car(
            id = "12",
            name = "Sigra",
            brand = "Daihatsu",
            year = 2023,
            type = CarType.MPV,
            transmission = TransmissionType.MANUAL,
            fuelType = FuelType.GASOLINE,
            seats = 7,
            pricePerDay = 300000,
            imageUrl = "sigra1",
            rating = 4.1f,
            reviewCount = 67,
            features = listOf("AC", "Audio System", "Power Steering"),
            isAvailable = true,
            location = "Cibubur"
        ),
        // 13. Daihatsu Terios
        Car(
            id = "13",
            name = "Terios",
            brand = "Daihatsu",
            year = 2022,
            type = CarType.SUV,
            transmission = TransmissionType.AUTOMATIC,
            fuelType = FuelType.GASOLINE,
            seats = 7,
            pricePerDay = 480000,
            imageUrl = "terios1",
            rating = 4.3f,
            reviewCount = 91,
            features = listOf("AC", "Audio System", "Power Steering", "Central Lock", "ABS"),
            isAvailable = true,
            location = "Cibubur"
        ),
        // 14. Toyota Fortuner VRZ
        Car(
            id = "14",
            name = "Fortuner VRZ",
            brand = "Toyota",
            year = 2023,
            type = CarType.SUV,
            transmission = TransmissionType.AUTOMATIC,
            fuelType = FuelType.DIESEL,
            seats = 7,
            pricePerDay = 900000,
            imageUrl = "vrz1",
            rating = 4.7f,
            reviewCount = 178,
            features = listOf("AC", "Audio System", "Power Steering", "Central Lock", "ABS", "Airbags", "4WD"),
            isAvailable = true,
            location = "Cibubur"
        ),
        // 15. Daihatsu Xenia
        Car(
            id = "15",
            name = "Xenia",
            brand = "Daihatsu",
            year = 2023,
            type = CarType.MPV,
            transmission = TransmissionType.MANUAL,
            fuelType = FuelType.GASOLINE,
            seats = 7,
            pricePerDay = 350000,
            imageUrl = "xenia1",
            rating = 4.2f,
            reviewCount = 89,
            features = listOf("AC", "Audio System", "Power Steering", "Central Lock"),
            isAvailable = true,
            location = "Cibubur"
        ),
        // 16. Suzuki XL7
        Car(
            id = "16",
            name = "XL7",
            brand = "Suzuki",
            year = 2023,
            type = CarType.SUV,
            transmission = TransmissionType.AUTOMATIC,
            fuelType = FuelType.GASOLINE,
            seats = 7,
            pricePerDay = 550000,
            imageUrl = "xl7_1",
            rating = 4.5f,
            reviewCount = 112,
            features = listOf("AC", "Audio System", "Power Steering", "Central Lock", "ABS", "Airbags"),
            isAvailable = true,
            location = "Cibubur"
        ),
        // 17. Mitsubishi Xpander Cross
        Car(
            id = "17",
            name = "Xpander Cross",
            brand = "Mitsubishi",
            year = 2023,
            type = CarType.SUV,
            transmission = TransmissionType.AUTOMATIC,
            fuelType = FuelType.GASOLINE,
            seats = 7,
            pricePerDay = 580000,
            imageUrl = "xpandercross1",
            rating = 4.6f,
            reviewCount = 145,
            features = listOf("AC", "Audio System", "Power Steering", "Central Lock", "ABS", "Airbags"),
            isAvailable = true,
            location = "Cibubur"
        ),
        // 18. Toyota Yaris
        Car(
            id = "18",
            name = "Yaris",
            brand = "Toyota",
            year = 2023,
            type = CarType.HATCHBACK,
            transmission = TransmissionType.CVT,
            fuelType = FuelType.GASOLINE,
            seats = 5,
            pricePerDay = 380000,
            imageUrl = "yaris1",
            rating = 4.4f,
            reviewCount = 98,
            features = listOf("AC", "Audio System", "Power Steering", "Central Lock", "ABS"),
            isAvailable = true,
            location = "Cibubur"
        ),
        // 19. Toyota Innova Zenix
        Car(
            id = "19",
            name = "Innova Zenix",
            brand = "Toyota",
            year = 2023,
            type = CarType.MPV,
            transmission = TransmissionType.CVT,
            fuelType = FuelType.HYBRID,
            seats = 7,
            pricePerDay = 750000,
            imageUrl = "zenix1",
            rating = 4.8f,
            reviewCount = 167,
            features = listOf("AC", "Audio System", "Power Steering", "Central Lock", "ABS", "Airbags", "Hybrid Engine"),
            isAvailable = true,
            location = "Cibubur"
        ),
        // 20. Mitsubishi Pajero Sport
        Car(
            id = "20",
            name = "Pajero Sport",
            brand = "Mitsubishi",
            year = 2023,
            type = CarType.SUV,
            transmission = TransmissionType.AUTOMATIC,
            fuelType = FuelType.DIESEL,
            seats = 7,
            pricePerDay = 850000,
            imageUrl = "jaresopo1",
            rating = 4.7f,
            reviewCount = 189,
            features = listOf("AC", "Audio System", "Power Steering", "Central Lock", "ABS", "Airbags", "4WD"),
            isAvailable = true,
            location = "Cibubur"
        )
    )
}
