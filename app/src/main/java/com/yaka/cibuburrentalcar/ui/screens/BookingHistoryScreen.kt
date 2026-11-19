package com.yaka.cibuburrentalcar.ui.screens

import androidx.compose.foundation.Image
import coil.compose.AsyncImage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yaka.cibuburrentalcar.R
import com.yaka.cibuburrentalcar.data.Rental
import com.yaka.cibuburrentalcar.data.RentalStatus
import com.yaka.cibuburrentalcar.data.Car
import com.yaka.cibuburrentalcar.ui.viewmodel.BookingHistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingHistoryScreen(
    onBookingDetailClick: (Rental) -> Unit,
    viewModel: BookingHistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadBookingHistory()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header dengan border radius di bawah (diseragamkan)
        com.yaka.cibuburrentalcar.ui.components.AppTopBar(
            title = "Riwayat Penyewaan",
            onBackClick = null,
            centerTitle = true
        )


        // Content
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Memuat riwayat penyewaan...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Error icon dengan background
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.errorContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Text(
                            text = "Terjadi kesalahan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error
                        )

                        Text(
                            text = uiState.error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = { viewModel.loadBookingHistory() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Coba Lagi")
                        }
                    }
                }
            }
            uiState.bookingHistory.isEmpty() -> {
                EmptyStateView()
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Section Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Riwayat Booking",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Text(
                                text = "${uiState.bookingHistory.size} booking",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Booking Cards
                    items(uiState.bookingHistory) { booking ->
                        BookingHistoryCard(
                            booking = booking,
                            car = uiState.carDetails[booking.carId],
                            onClick = {
                                // Navigate to booking detail
                                onBookingDetailClick(booking)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterTabs(
    selectedStatus: RentalStatus?,
    onStatusSelected: (RentalStatus?) -> Unit
) {
    val tabs = listOf(
        "Semua" to null,
        "Menunggu" to RentalStatus.MENUNGGU,
        "Aktif" to RentalStatus.AKTIF,
        "Selesai" to RentalStatus.SELESAI,
        "Dibatalkan" to RentalStatus.DIBATALKAN
    )

    // Konsisten dengan FilterRow di HomeScreen
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        items(tabs) { (title, status) ->
            val isSelected = selectedStatus == status

            FilterChip(
                onClick = { onStatusSelected(status) },
                label = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                selected = isSelected,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f)
                    },
                    labelColor = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    },
                    selectedContainerColor = MaterialTheme.colorScheme.onPrimary,
                    selectedLabelColor = MaterialTheme.colorScheme.primary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                    selectedBorderColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingHistoryCard(
    booking: Rental,
    car: Car? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Car Image
            CarImageSection(booking = booking, car = car)

            // Booking Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Car Name and Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = if (car != null) "${car.brand} ${car.name}" else "Car ID: ${booking.carId}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    StatusBadge(status = booking.status.name)
                }

                // Car Details
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${booking.totalDays} hari • Rp ${booking.pricePerDay}/hari",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Date Range
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${booking.startDate} - ${booking.endDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Duration and Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${booking.totalDays} hari",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Text(
                        text = "Rp ${booking.totalPrice}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun CarImageSection(booking: Rental, car: Car? = null) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (car != null && car.imageUrl.isNotEmpty()) {
            // Get drawable resource ID
            val context = LocalContext.current
            val drawableId = context.resources.getIdentifier(
                car.imageUrl,
                "drawable",
                context.packageName
            )

            if (drawableId != 0) {
                // Use Image for drawable resources
                Image(
                    painter = painterResource(id = drawableId),
                    contentDescription = "${car.brand} ${car.name}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Fallback icon if drawable not found
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = car?.let { "${it.brand} ${it.name}" } ?: "Car ${booking.carId}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(40.dp)
                )
            }
        } else {
            // Placeholder icon for car
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = car?.let { "${it.brand} ${it.name}" } ?: "Car ${booking.carId}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (backgroundColor, textColor, text) = when (status.uppercase()) {
        "MENUNGGU" -> Triple(
            Color(0xFFFF9800).copy(alpha = 0.1f),
            Color(0xFFFF9800),
            "Menunggu"
        )
        "AKTIF" -> Triple(
            Color(0xFF4CAF50).copy(alpha = 0.1f),
            Color(0xFF4CAF50),
            "Aktif"
        )
        "SELESAI" -> Triple(
            Color(0xFF2196F3).copy(alpha = 0.1f),
            Color(0xFF2196F3),
            "Selesai"
        )
        "DIBATALKAN" -> Triple(
            Color(0xFFF44336).copy(alpha = 0.1f),
            Color(0xFFF44336),
            "Dibatalkan"
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            status
        )
    }

    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EmptyStateView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon dengan background circle
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(40.dp)
                )
            }

            Text(
                text = "Belum ada riwayat penyewaan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Riwayat penyewaan mobil Anda akan muncul di sini setelah melakukan booking",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Helper function to get car image resource
private fun getCarImageResource(imageUrl: String): Int? {
    return when {
        imageUrl.contains("brio", ignoreCase = true) -> R.drawable.brio1
        imageUrl.contains("xenia", ignoreCase = true) -> R.drawable.xenia1
        imageUrl.contains("innova", ignoreCase = true) || imageUrl.contains("zenix", ignoreCase = true) -> R.drawable.zenix1
        imageUrl.contains("fortuner", ignoreCase = true) || imageUrl.contains("vrz", ignoreCase = true) -> R.drawable.vrz1
        imageUrl.contains("jazz", ignoreCase = true) -> R.drawable.jazz1
        imageUrl.contains("calya", ignoreCase = true) -> R.drawable.calya1
        imageUrl.contains("sigra", ignoreCase = true) -> R.drawable.sigra1
        imageUrl.contains("hrv", ignoreCase = true) -> R.drawable.hrv1
        imageUrl.contains("brv", ignoreCase = true) -> R.drawable.brv1
        else -> null
    }
}
