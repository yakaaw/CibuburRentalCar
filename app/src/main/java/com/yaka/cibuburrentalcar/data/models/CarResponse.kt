package com.yaka.cibuburrentalcar.data.models

import com.google.gson.annotations.SerializedName

data class CarResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: List<CarModel>? = null,
    
    @SerializedName("count")
    val count: Int = 0
)

data class CarModel(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("brand")
    val brand: String,
    
    @SerializedName("year")
    val year: Int,
    
    @SerializedName("type")
    val type: String,
    
    @SerializedName("seats")
    val seats: Int,
    
    @SerializedName("transmission")
    val transmission: String,
    
    @SerializedName("fuel_type")
    val fuelType: String,
    
    @SerializedName("price_per_day")
    val pricePerDay: Int,
    
    @SerializedName("image_url")
    val imageUrl: String,
    
    @SerializedName("location")
    val location: String,
    
    @SerializedName("is_available")
    val isAvailable: Boolean,
    
    @SerializedName("rating")
    val rating: Float,
    
    @SerializedName("review_count")
    val reviewCount: Int,
    
    @SerializedName("features")
    val features: List<String>
)

// Extension function to convert CarModel to Car (existing model)
fun CarModel.toCar(): com.yaka.cibuburrentalcar.data.Car {
    return com.yaka.cibuburrentalcar.data.Car(
        id = this.id,
        name = this.name,
        brand = this.brand,
        year = this.year,
        type = stringToCarType(this.type),
        transmission = stringToTransmissionType(this.transmission),
        fuelType = stringToFuelType(this.fuelType),
        seats = this.seats,
        pricePerDay = this.pricePerDay,
        imageUrl = this.imageUrl,
        rating = this.rating,
        reviewCount = this.reviewCount,
        features = this.features,
        isAvailable = this.isAvailable,
        location = this.location
    )
}

// Helper functions to convert strings to enum types
private fun stringToCarType(type: String): com.yaka.cibuburrentalcar.data.CarType {
    return when (type.uppercase()) {
        "MPV" -> com.yaka.cibuburrentalcar.data.CarType.MPV
        "SUV" -> com.yaka.cibuburrentalcar.data.CarType.SUV
        "SEDAN" -> com.yaka.cibuburrentalcar.data.CarType.SEDAN
        "HATCHBACK" -> com.yaka.cibuburrentalcar.data.CarType.HATCHBACK
        "CONVERTIBLE" -> com.yaka.cibuburrentalcar.data.CarType.CONVERTIBLE
        "COUPE" -> com.yaka.cibuburrentalcar.data.CarType.COUPE
        else -> com.yaka.cibuburrentalcar.data.CarType.MPV
    }
}

private fun stringToTransmissionType(transmission: String): com.yaka.cibuburrentalcar.data.TransmissionType {
    return when (transmission.uppercase()) {
        "MANUAL" -> com.yaka.cibuburrentalcar.data.TransmissionType.MANUAL
        "AUTOMATIC" -> com.yaka.cibuburrentalcar.data.TransmissionType.AUTOMATIC
        "CVT" -> com.yaka.cibuburrentalcar.data.TransmissionType.CVT
        else -> com.yaka.cibuburrentalcar.data.TransmissionType.MANUAL
    }
}

private fun stringToFuelType(fuelType: String): com.yaka.cibuburrentalcar.data.FuelType {
    return when (fuelType.uppercase()) {
        "BENSIN" -> com.yaka.cibuburrentalcar.data.FuelType.GASOLINE
        "GASOLINE" -> com.yaka.cibuburrentalcar.data.FuelType.GASOLINE
        "DIESEL" -> com.yaka.cibuburrentalcar.data.FuelType.DIESEL
        "HYBRID" -> com.yaka.cibuburrentalcar.data.FuelType.HYBRID
        "ELECTRIC" -> com.yaka.cibuburrentalcar.data.FuelType.ELECTRIC
        else -> com.yaka.cibuburrentalcar.data.FuelType.GASOLINE
    }
}


