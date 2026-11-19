package com.yaka.cibuburrentalcar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yaka.cibuburrentalcar.data.Rental

import com.yaka.cibuburrentalcar.ui.components.PrimaryButton
import com.yaka.cibuburrentalcar.ui.viewmodel.EditBookingViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun EditBookingScreen(
    booking: Rental,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: EditBookingViewModel = viewModel()
) {
    var startDate by remember { mutableStateOf(booking.startDate) }
    var endDate by remember { mutableStateOf(booking.endDate) }
    var notes by remember { mutableStateOf(booking.notes) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    // Calculate total days and price
    val totalDays = remember(startDate, endDate) {
        if (endDate.isAfter(startDate) || endDate.isEqual(startDate)) {
            startDate.until(endDate).days.toInt() + 1
        } else 1
    }

    val totalPrice = remember(totalDays) {
        totalDays * booking.pricePerDay
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSaveSuccess()
        }
    }

    // Load blocked dates when screen opens
    LaunchedEffect(booking.carId) {
        viewModel.loadBlockedDates(booking.carId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header dengan style konsisten seperti BookingHistoryScreen
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 4.dp,
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Header dengan back button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "Edit Booking",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "Ubah tanggal dan catatan pesanan",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Content dengan scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date Selection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Tanggal Sewa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Start Date
                    OutlinedCard(
                        onClick = {
                            // Refresh blocked dates before showing picker
                            viewModel.loadBlockedDates(booking.carId)
                            showStartDatePicker = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Start Date",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column {
                                Text(
                                    text = "Tanggal Mulai",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = startDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    
                    // End Date
                    OutlinedCard(
                        onClick = {
                            // Refresh blocked dates before showing picker
                            viewModel.loadBlockedDates(booking.carId)
                            showEndDatePicker = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "End Date",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column {
                                Text(
                                    text = "Tanggal Selesai",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = endDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    
                    // Total Days and Price
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$totalDays hari",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Rp ${String.format("%,d", totalPrice)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Blocked Dates Info
                    if (uiState.blockedDates.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Red.copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Blocked Dates",
                                    tint = Color.Red,
                                    modifier = Modifier.size(16.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "${uiState.blockedDates.size} tanggal tidak tersedia (ditampilkan merah di kalender)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Red,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
            
            // Notes Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Notes,
                            contentDescription = "Notes",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "Catatan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("Tambahkan catatan (opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
            
            // Error Message
            if (uiState.errorMessage.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Save Button
            PrimaryButton(
                text = if (uiState.isLoading) "Menyimpan..." else "Simpan Perubahan",
                onClick = {
                    viewModel.updateBooking(
                        bookingId = booking.id,
                        startDate = startDate,
                        endDate = endDate,
                        totalDays = totalDays,
                        totalPrice = totalPrice,
                        notes = notes
                    )
                },
                enabled = !uiState.isLoading && endDate.isAfter(startDate.minusDays(1)),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    
    // Date Pickers
    if (showStartDatePicker) {
        CustomCalendarDialog(
            selectedDate = startDate,
            blockedDates = uiState.blockedDates,
            isLoadingBlockedDates = uiState.isLoadingBlockedDates,
            onDateSelected = {
                startDate = it
                if (endDate.isBefore(it)) {
                    endDate = it
                }
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        CustomCalendarDialog(
            selectedDate = endDate,
            blockedDates = uiState.blockedDates,
            isLoadingBlockedDates = uiState.isLoadingBlockedDates,
            onDateSelected = {
                endDate = it
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

@Composable
private fun CustomCalendarDialog(
    selectedDate: LocalDate?,
    blockedDates: List<String> = emptyList(),
    isLoadingBlockedDates: Boolean = false,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(selectedDate ?: LocalDate.now()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Pilih Tanggal",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                // Month Navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { currentMonth = currentMonth.minusMonths(1) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous Month",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale("id", "ID"))),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = { currentMonth = currentMonth.plusMonths(1) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next Month",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Calendar Grid with loading state
                if (isLoadingBlockedDates) {
                    // Show loading indicator while blocked dates are being loaded
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Memuat tanggal tersedia...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    CustomCalendarGrid(
                        currentMonth = currentMonth,
                        selectedDate = selectedDate,
                        blockedDates = blockedDates,
                        onDateSelected = onDateSelected
                    )

                    // Calendar Legend
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CalendarLegendItem(
                            color = Color(0xFFE53E3E),
                            text = "Tidak tersedia"
                        )
                        CalendarLegendItem(
                            color = MaterialTheme.colorScheme.primary,
                            text = "Dipilih"
                        )
                        CalendarLegendItem(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            text = "Hari ini"
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
private fun CustomCalendarGrid(
    currentMonth: LocalDate,
    selectedDate: LocalDate?,
    blockedDates: List<String> = emptyList(),
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentMonth.withDayOfMonth(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = when (firstDayOfMonth.dayOfWeek.value) {
        7 -> 0 // Sunday
        else -> firstDayOfMonth.dayOfWeek.value // Monday=1, ..., Saturday=6
    }

    // Day headers
    val dayHeaders = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        dayHeaders.forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Calendar grid
    val totalCells = firstDayOfWeek + daysInMonth
    val totalWeeksNeeded = (totalCells + 6) / 7

    Column {
        for (week in 0 until totalWeeksNeeded) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayIndex in 0 until 7) {
                    val cellIndex = week * 7 + dayIndex
                    val dayNumber = cellIndex - firstDayOfWeek + 1

                    val date = if (dayNumber in 1..daysInMonth) {
                        firstDayOfMonth.withDayOfMonth(dayNumber)
                    } else {
                        null
                    }

                    CalendarDayCell(
                        date = date,
                        isSelected = date == selectedDate,
                        isToday = date == LocalDate.now(),
                        blockedDates = blockedDates,
                        onDateSelected = onDateSelected
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate?,
    isSelected: Boolean,
    isToday: Boolean,
    blockedDates: List<String> = emptyList(),
    onDateSelected: (LocalDate) -> Unit
) {
    Box(
        modifier = Modifier.size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            val dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val isBlocked = blockedDates.contains(dateString)
            val isPastDate = date.isBefore(LocalDate.now())
            val isDisabled = isBlocked || isPastDate

            val blockedVisible = isBlocked && !isPastDate
            val blockedColor = Color(0xFFE53935).copy(alpha = 0.35f)
            val todayColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            blockedVisible -> blockedColor
                            isPastDate -> Color.Transparent
                            isSelected -> MaterialTheme.colorScheme.primary
                            isToday -> todayColor
                            else -> Color.Transparent
                        }
                    )
                    .clickable(enabled = !isDisabled) {
                        if (!isDisabled) {
                            onDateSelected(date)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        blockedVisible -> Color.White
                        isPastDate -> Color.Gray.copy(alpha = 0.35f)
                        isSelected -> Color.White
                        isToday -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    fontWeight = when {
                        blockedVisible -> FontWeight.Bold
                        isSelected || isToday -> FontWeight.Bold
                        else -> FontWeight.Normal
                    }
                )
            }
        }
    }
}

@Composable
private fun CalendarLegendItem(
    color: Color,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
