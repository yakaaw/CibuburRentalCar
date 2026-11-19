package com.yaka.cibuburrentalcar.data.models

import com.google.gson.annotations.SerializedName

data class BlockedDatesResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: BlockedDatesData? = null
)

data class BlockedDatesData(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("car_id")
    val carId: String,
    
    @SerializedName("blocked_dates")
    val blockedDates: List<String>,
    
    @SerializedName("booking_ranges")
    val bookingRanges: List<BookingRange>,
    
    @SerializedName("total_blocked_days")
    val totalBlockedDays: Int
)

data class BookingRange(
    @SerializedName("start_date")
    val startDate: String,
    
    @SerializedName("end_date")
    val endDate: String,
    
    @SerializedName("status")
    val status: String
)
