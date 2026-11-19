package com.yaka.cibuburrentalcar.data.models

import com.google.gson.annotations.SerializedName
import androidx.compose.runtime.Immutable

/**
 * Data model untuk riwayat booking dengan detail mobil
 */
@Immutable
data class BookingHistory(
    @SerializedName("id")
    val id: Long,
    
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
    val totalPrice: Double,
    
    @SerializedName("status")
    val status: String, // ACTIVE, COMPLETED, CANCELLED
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String,
    
    // Detail mobil dari join dengan tabel cars
    @SerializedName("car_brand")
    val carBrand: String,
    
    @SerializedName("car_year")
    val carYear: Int,
    
    @SerializedName("car_type")
    val carType: String,
    
    @SerializedName("car_transmission")
    val carTransmission: String,
    
    @SerializedName("car_fuel_type")
    val carFuelType: String,
    
    @SerializedName("car_seats")
    val carSeats: Int,
    
    @SerializedName("car_image_url")
    val carImageUrl: String,
    
    @SerializedName("car_rating")
    val carRating: Float,
    
    @SerializedName("car_location")
    val carLocation: String
) {
    /**
     * Helper function untuk mendapatkan status dalam bahasa Indonesia
     */
    fun getStatusInIndonesian(): String {
        return when (status.uppercase()) {
            "ACTIVE" -> "Sedang Berlangsung"
            "COMPLETED" -> "Selesai"
            "CANCELLED" -> "Dibatalkan"
            else -> status
        }
    }
    
    /**
     * Helper function untuk mendapatkan warna status
     */
    fun getStatusColor(): androidx.compose.ui.graphics.Color {
        return when (status.uppercase()) {
            "ACTIVE" -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
            "COMPLETED" -> androidx.compose.ui.graphics.Color(0xFF2196F3) // Blue
            "CANCELLED" -> androidx.compose.ui.graphics.Color(0xFFF44336) // Red
            else -> androidx.compose.ui.graphics.Color.Gray
        }
    }
    
    /**
     * Helper function untuk format harga
     */
    fun getFormattedTotalPrice(): String {
        return "Rp ${String.format("%,.0f", totalPrice)}"
    }
    
    /**
     * Helper function untuk format tanggal
     */
    fun getFormattedDateRange(): String {
        return "$startDate s/d $endDate"
    }
    
    /**
     * Helper function untuk mendapatkan durasi dalam format yang mudah dibaca
     */
    fun getDurationText(): String {
        return "$totalDays hari"
    }
}

/**
 * Response model untuk API riwayat booking
 */
data class BookingHistoryResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: BookingHistoryData?
)

/**
 * Data wrapper untuk nested response structure
 */
data class BookingHistoryData(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: List<BookingHistory>?,

    @SerializedName("pagination")
    val pagination: BookingPagination?
)

/**
 * Pagination info
 */
data class BookingPagination(
    @SerializedName("total")
    val total: Int,

    @SerializedName("limit")
    val limit: Int,

    @SerializedName("offset")
    val offset: Int,

    @SerializedName("has_more")
    val hasMore: Boolean
)

/**
 * Enum untuk status booking
 */
enum class BookingStatus(val value: String, val displayName: String) {
    ACTIVE("ACTIVE", "Sedang Berlangsung"),
    COMPLETED("COMPLETED", "Selesai"),
    CANCELLED("CANCELLED", "Dibatalkan");
    
    companion object {
        fun fromValue(value: String): BookingStatus {
            return values().find { it.value.equals(value, ignoreCase = true) } ?: ACTIVE
        }
    }
}

/**
 * Filter untuk riwayat booking
 */
data class BookingHistoryFilter(
    val status: BookingStatus? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val carId: String? = null
)
