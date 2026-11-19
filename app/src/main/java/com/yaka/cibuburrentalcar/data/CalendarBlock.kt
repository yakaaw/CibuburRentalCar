package com.yaka.cibuburrentalcar.data

import androidx.compose.runtime.Immutable

@Immutable
data class CalendarBlock(
    val id: String,
    val startDate: String, // yyyy-MM-dd
    val endDate: String,   // yyyy-MM-dd
    val reason: String = "",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

