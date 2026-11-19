package com.yaka.cibuburrentalcar.ui.screens.admin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceDialog(
    blockedDates: Set<LocalDate>,
    removableDates: Set<LocalDate>,
    onDismiss: () -> Unit,
    onToggleSingleDate: (LocalDate, Boolean) -> Unit,
    onSubmitRange: (LocalDate, LocalDate) -> Unit
) {
    var tabIndex by remember { mutableStateOf(0) } // 0 = Range, 1 = Per Tanggal
    var displayMonth by remember { mutableStateOf(YearMonth.now()) }
    var rangeStart by remember { mutableStateOf(LocalDate.now()) }
    var rangeEnd by remember { mutableStateOf(LocalDate.now().plusDays(1)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Atur Maintenance", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TabRow(selectedTabIndex = tabIndex) {
                    Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Range") })
                    Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Per Tanggal") })
                }

                if (tabIndex == 0) {
                    // Range mode
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Pilih rentang tanggal (inkl.)")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { /* bisa ganti ke date picker */ }) {
                                Text("Mulai: ${rangeStart.format(DateTimeFormatter.ISO_DATE)}")
                            }
                            OutlinedButton(onClick = { /* bisa ganti ke date picker */ }) {
                                Text("Selesai: ${rangeEnd.format(DateTimeFormatter.ISO_DATE)}")
                            }
                        }
                        // Calendar view to assist visual selection as well
                        CalendarMonth(
                            month = displayMonth,
                            blockedDates = blockedDates,
                            removableDates = removableDates,
                            onPrevMonth = { displayMonth = displayMonth.minusMonths(1) },
                            onNextMonth = { displayMonth = displayMonth.plusMonths(1) },
                            onDayClick = { date ->
                                // In range tab, set start/end accordingly
                                if (date.isBefore(rangeStart) || date.isAfter(rangeEnd)) {
                                    if (date.isBefore(rangeStart)) rangeStart = date else rangeEnd = date
                                    if (rangeEnd.isBefore(rangeStart)) rangeEnd = rangeStart
                                } else {
                                    // click inside range: set nearest edge
                                    val distStart = kotlin.math.abs(date.toEpochDay() - rangeStart.toEpochDay())
                                    val distEnd = kotlin.math.abs(date.toEpochDay() - rangeEnd.toEpochDay())
                                    if (distStart <= distEnd) rangeStart = date else rangeEnd = date
                                }
                            }
                        )
                    }
                } else {
                    // Per-date toggle mode
                    CalendarMonth(
                        month = displayMonth,
                        blockedDates = blockedDates,
                        removableDates = removableDates,
                        onPrevMonth = { displayMonth = displayMonth.minusMonths(1) },
                        onNextMonth = { displayMonth = displayMonth.plusMonths(1) },
                        onDayClick = { date ->
                            val isBlocked = blockedDates.contains(date)
                            onToggleSingleDate(date, isBlocked)
                        }
                    )
                }
            }
        },
        confirmButton = {
            if (tabIndex == 0) {
                TextButton(onClick = { onSubmitRange(rangeStart, if (rangeEnd.isBefore(rangeStart)) rangeStart else rangeEnd) }) { Text("Simpan Range") }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Tutup") } }
    )
}

@Composable
private fun CalendarMonth(
    month: YearMonth,
    blockedDates: Set<LocalDate>,
    removableDates: Set<LocalDate>,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayClick: (LocalDate) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onPrevMonth) { Text("<") }
            Text(month.toString(), style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onNextMonth) { Text(">") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { Text(it, Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
        }
        val firstDay = month.atDay(1)
        val daysInMonth = month.lengthOfMonth()
        val leadingEmpty = (firstDay.dayOfWeek.value % 7) // Sunday = 0
        val cells = buildList<LocalDate?> {
            repeat(leadingEmpty) { add(null) }
            for (d in 1..daysInMonth) add(month.atDay(d))
        }
        LazyVerticalGrid(columns = GridCells.Fixed(7), userScrollEnabled = false, modifier = Modifier.heightIn(max = 300.dp)) {
            items(cells) { date ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(2.dp)
                        .let { m ->
                            val isBlocked = date != null && blockedDates.contains(date)
                            val isRemovable = date != null && removableDates.contains(date)
                            val bg = when {
                                date == null -> Color.Transparent
                                isBlocked && isRemovable -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                isBlocked -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            }
                            m.background(bg)
                        }
                        .clickable(enabled = date != null) { date?.let { onDayClick(it) } },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = date?.dayOfMonth?.toString() ?: "")
                }
            }
        }
        Text("Merah muda = bisa di-unblock (single-day). Ungu = diblokir oleh range.", style = MaterialTheme.typography.bodySmall)
    }
}

