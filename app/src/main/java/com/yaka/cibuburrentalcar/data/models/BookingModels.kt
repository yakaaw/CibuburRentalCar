package com.yaka.cibuburrentalcar.data.models

import com.google.gson.annotations.SerializedName

data class BookingRequest(
    @SerializedName("user_id")
    val userId: Long,
    
    @SerializedName("car_id")
    val carId: String,
    
    @SerializedName("car_name")
    val carName: String,
    
    @SerializedName("start_date")
    val startDate: String,
    
    @SerializedName("end_date")
    val endDate: String,
    
    @SerializedName("total_days")
    val totalDays: Int,
    
    @SerializedName("price_per_day")
    val pricePerDay: Double,
    
    @SerializedName("total_price")
    val totalPrice: Double
)

data class BookingResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("booking_id")
    val bookingId: Long? = null
)

data class AvailabilityRequest(
    @SerializedName("car_id")
    val carId: String,
    
    @SerializedName("start_date")
    val startDate: String,
    
    @SerializedName("end_date")
    val endDate: String
)

data class AvailabilityResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("available")
    val available: Boolean,
    
    @SerializedName("message")
    val message: String? = null
)

data class CarAvailability(
    @SerializedName("car_id")
    val carId: String,
    
    @SerializedName("available")
    val available: Boolean
)
