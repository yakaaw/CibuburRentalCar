package com.yaka.cibuburrentalcar.data

import androidx.compose.runtime.Immutable
import java.time.LocalDate
import java.time.LocalDateTime

@Immutable
data class Rental(
    val id: String,
    val carId: String,
    val userId: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val pickupLocation: String,
    val dropoffLocation: String,
    val totalDays: Int,
    val pricePerDay: Int,
    val totalPrice: Int,
    val status: RentalStatus,
    val createdAt: LocalDateTime,
    val notes: String = ""
)

enum class RentalStatus {
    MENUNGGU,    // Sebelum tanggal pesan (PENDING)
    AKTIF,       // Di hari H sesuai tanggal pesanan (ACTIVE)
    SELESAI,     // Setelah pesanan melewati hari sewa (COMPLETED)
    DIBATALKAN   // Jika booking dibatalkan (CANCELLED)
}

/**
 * Extension function untuk menghitung status otomatis berdasarkan tanggal
 */
fun Rental.getAutoStatus(): RentalStatus {
    // Jika sudah dibatalkan, tetap dibatalkan
    if (status == RentalStatus.DIBATALKAN) {
        return RentalStatus.DIBATALKAN
    }

    val today = LocalDate.now()

    return when {
        // Sebelum tanggal mulai sewa
        today.isBefore(startDate) -> RentalStatus.MENUNGGU

        // Di antara tanggal mulai dan selesai sewa (inclusive)
        (today.isEqual(startDate) || today.isAfter(startDate)) &&
        (today.isEqual(endDate) || today.isBefore(endDate.plusDays(1))) -> RentalStatus.AKTIF

        // Setelah tanggal selesai sewa
        today.isAfter(endDate) -> RentalStatus.SELESAI

        // Default fallback
        else -> RentalStatus.MENUNGGU
    }
}

// RentalRequest moved to separate file for better organization

// Sample data removed - Full Firebase implementation
