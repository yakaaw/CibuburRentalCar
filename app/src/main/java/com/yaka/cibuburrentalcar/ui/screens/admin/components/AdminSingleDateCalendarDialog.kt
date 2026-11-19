package com.yaka.cibuburrentalcar.ui.screens.admin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSingleDateCalendarDialog(
    bookingBlockedDates: List<String>, // merah (user bookings)
    maintenanceDates: List<String>,    // oranye (admin blocks)
    onToggleDate: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Prev Month")
                }
                Text(
                    text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Month")
                }
            }
        },
        text = {
            CustomAdminCalendarGrid(
                currentMonth = currentMonth,
                bookingBlockedDates = bookingBlockedDates,
                maintenanceDates = maintenanceDates,
                onDayClick = onToggleDate
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        }
    )
}

@Composable
private fun CustomAdminCalendarGrid(
    currentMonth: LocalDate,
    bookingBlockedDates: List<String>, // merah
    maintenanceDates: List<String>,    // oranye
    onDayClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentMonth.withDayOfMonth(1)
    val lastDayOfMonth = currentMonth.withDayOfMonth(currentMonth.lengthOfMonth())

    val firstDayOfWeek = when (firstDayOfMonth.dayOfWeek.value) { // Sunday=0
        7 -> 0
        else -> firstDayOfMonth.dayOfWeek.value
    }
    val daysInMonth = currentMonth.lengthOfMonth()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("M", "S", "S", "R", "K", "J", "S").forEach {
                Text(text = it, style = MaterialTheme.typography.labelMedium)
            }
        }

        val totalCells = firstDayOfWeek + daysInMonth
        val totalRows = ((totalCells + 6) / 7)

        for (week in 0 until totalRows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayIndex in 0 until 7) {
                    val cellIndex = week * 7 + dayIndex
                    val dayNumber = cellIndex - firstDayOfWeek + 1

                    val date = if (dayNumber in 1..daysInMonth) {
                        firstDayOfMonth.withDayOfMonth(dayNumber)
                    } else null

                    AdminCalendarDayCell(
                        date = date,
                        bookingBlockedDates = bookingBlockedDates,
                        maintenanceDates = maintenanceDates,
                        onDayClick = onDayClick
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminCalendarDayCell(
    date: LocalDate?,
    bookingBlockedDates: List<String>,
    maintenanceDates: List<String>,
    onDayClick: (LocalDate) -> Unit
) {
    Box(
        modifier = Modifier.size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            val dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val isBookingBlocked = bookingBlockedDates.contains(dateString)
            val isMaintenance = maintenanceDates.contains(dateString)
            val isPastDate = date.isBefore(LocalDate.now())

            val showBlocked = isBookingBlocked && !isPastDate
            val showMaintenance = isMaintenance && !isPastDate
            val bgColor = when {
                showBlocked -> Color(0xFFE53935).copy(alpha = 0.35f) // merah lembut
                showMaintenance -> Color(0xFFFFA726).copy(alpha = 0.35f) // orange lembut
                date == LocalDate.now() -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                else -> Color.Transparent
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(bgColor)
                    .clickable(enabled = !isPastDate) {
                        // Toggle hanya untuk maintenance; booking tidak bisa diubah dari sini
                        if (!isBookingBlocked) onDayClick(date)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isMaintenance || isBookingBlocked || date == LocalDate.now()) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isBookingBlocked || isMaintenance -> Color.White
                        isPastDate -> Color.Gray.copy(alpha = 0.4f)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }
}

