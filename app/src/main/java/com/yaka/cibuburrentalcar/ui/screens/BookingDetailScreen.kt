package com.yaka.cibuburrentalcar.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.automirrored.filled.Notes
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yaka.cibuburrentalcar.R
import com.yaka.cibuburrentalcar.data.Car
import com.yaka.cibuburrentalcar.data.Rental
import com.yaka.cibuburrentalcar.data.RentalStatus
import com.yaka.cibuburrentalcar.ui.components.PrimaryButton
import com.yaka.cibuburrentalcar.ui.components.SecondaryButton
import com.yaka.cibuburrentalcar.ui.theme.EmeraldGreen
import com.yaka.cibuburrentalcar.ui.theme.MediumGray
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun BookingDetailScreen(
    booking: Rental,
    car: Car?,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    var showCancelDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header diseragamkan
        com.yaka.cibuburrentalcar.ui.components.AppTopBar(
            title = "Detail Booking",
            onBackClick = onBackClick,
            centerTitle = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Content dengan scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Car Information Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Car Image and Basic Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Car Image
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                        ) {
                            if (car != null) {
                                val context = LocalContext.current
                                val drawableId = context.resources.getIdentifier(
                                    car.imageUrl,
                                    "drawable",
                                    context.packageName
                                )
                                
                                if (drawableId != 0) {
                                    Image(
                                        painter = painterResource(id = drawableId),
                                        contentDescription = "${car.brand} ${car.name}",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsCar,
                                        contentDescription = "Car",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                        tint = Color.Gray
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Car Details
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = car?.let { "${it.brand} ${it.name}" } ?: "Unknown Car",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            if (car != null) {
                                Text(
                                    text = "${car.year} • ${car.transmission.name} • ${car.seats} Kursi",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MediumGray
                                )
                            }
                        }
                    }
                }
            }
            
            // Booking Details Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Status
                    StatusBadge(booking.status)
                    
                    // Booking Period
                    BookingDetailItem(
                        icon = Icons.Default.CalendarToday,
                        title = "Periode Sewa",
                        content = "${booking.startDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))} - ${booking.endDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))} (${booking.totalDays} hari)"
                    )
                    
                    // Total Price
                    BookingDetailItem(
                        icon = Icons.Default.DirectionsCar,
                        title = "Total Harga",
                        content = formatPrice(booking.totalPrice)
                    )
                    
                    // Notes
                    if (booking.notes.isNotEmpty()) {
                        BookingDetailItem(
                            icon = Icons.AutoMirrored.Filled.Notes,
                            title = "Catatan",
                            content = booking.notes
                        )
                    }
                }
            }
            
            // Action Buttons (only for MENUNGGU status)
            if (booking.status == RentalStatus.MENUNGGU) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecondaryButton(
                        text = "Batalkan",
                        onClick = { showCancelDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                    
                    PrimaryButton(
                        text = "Edit Booking",
                        onClick = onEditClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
    
    // Cancel Confirmation Dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Batalkan Booking") },
            text = { Text("Apakah Anda yakin ingin membatalkan booking ini?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        onCancelClick()
                    }
                ) {
                    Text("Ya, Batalkan")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCancelDialog = false }
                ) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun StatusBadge(status: RentalStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        RentalStatus.MENUNGGU -> Triple(
            Color(0xFFFF9800).copy(alpha = 0.1f),
            Color(0xFFFF9800),
            "Menunggu"
        )
        RentalStatus.AKTIF -> Triple(
            Color(0xFF4CAF50).copy(alpha = 0.1f),
            Color(0xFF4CAF50),
            "Aktif"
        )
        RentalStatus.SELESAI -> Triple(
            Color(0xFF2196F3).copy(alpha = 0.1f),
            Color(0xFF2196F3),
            "Selesai"
        )
        RentalStatus.DIBATALKAN -> Triple(
            Color(0xFFF44336).copy(alpha = 0.1f),
            Color(0xFFF44336),
            "Dibatalkan"
        )
    }
    
    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun BookingDetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = EmeraldGreen,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MediumGray,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun formatPrice(price: Int): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(price).replace("IDR", "Rp")
}
