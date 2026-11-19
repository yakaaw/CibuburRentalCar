package com.yaka.cibuburrentalcar.data

import androidx.compose.runtime.Immutable
import java.time.LocalDate

/**
 * Data class for rental booking request
 * Used for creating new bookings in Firebase
 * 
 * Sesuai dengan requirement:
 * - userId: String
 * - carId: String  
 * - startDate: String (converted from LocalDate)
 * - endDate: String (converted from LocalDate)
 * - totalDays: Int
 * - pricePerDay: Int
 * - totalPrice: Int
 * - notes: String
 * - status: String
 */
@Immutable
data class RentalRequest(
    val userId: String,
    val carId: String,
    val startDate: LocalDate, // Will be converted to String when needed
    val endDate: LocalDate,   // Will be converted to String when needed
    val pickupLocation: String,
    val dropoffLocation: String,
    val totalDays: Int,
    val pricePerDay: Int,
    val totalPrice: Int,
    val notes: String = "",
    val status: String = "MENUNGGU",
    
    // Additional fields for UI
    val customerName: String = "",
    val customerPhone: String = ""
) {
    // Helper properties for String conversion
    val startDateString: String
        get() = startDate.toString()
        
    val endDateString: String
        get() = endDate.toString()
}
