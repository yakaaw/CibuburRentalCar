package com.yaka.cibuburrentalcar.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yaka.cibuburrentalcar.data.Car
import com.yaka.cibuburrentalcar.data.FuelType
import com.yaka.cibuburrentalcar.R
import com.yaka.cibuburrentalcar.ui.theme.EmeraldGreen
import com.yaka.cibuburrentalcar.ui.theme.MediumGray
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarCard(
    car: Car,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = car.isAvailable) { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (car.isAvailable) 2.dp else 1.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (car.isAvailable) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            // Left side - Car Image
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // Car Image - Prefer network image if provided
                val firstUrl = car.imageUrls.firstOrNull { it.isNotBlank() && it.startsWith("http", ignoreCase = true) }
                    ?: car.imageUrl.takeIf { it.startsWith("http", ignoreCase = true) }

                if (firstUrl != null) {
                    AsyncImage(
                        model = firstUrl,
                        contentDescription = "${car.brand} ${car.name}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = if (car.isAvailable) 1f else 0.5f
                    )
                } else {
                    // Local drawable fallback: resolve resource by name (supports values like "camry1" or "camry1.jpg")
                    val context = LocalContext.current
                    val baseName = car.imageUrl
                        .substringBeforeLast('.')
                        .lowercase(Locale.ROOT)
                        .replace(" ", "")
                        .replace("-", "")
                        .replace("/", "")
                    val drawableId = context.resources.getIdentifier(
                        baseName,
                        "drawable",
                        context.packageName
                    )

                    if (drawableId != 0) {
                        Image(
                            painter = painterResource(id = drawableId),
                            contentDescription = "${car.brand} ${car.name}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alpha = if (car.isAvailable) 1f else 0.5f
                        )
                    } else {
                        // Generic placeholder if nothing matches
                        Image(
                            painter = painterResource(id = R.drawable.xenia1),
                            contentDescription = "placeholder",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alpha = if (car.isAvailable) 1f else 0.5f
                        )
                    }
                }


                // Availability Badge
                if (!car.isAvailable) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .background(
                                MaterialTheme.colorScheme.error,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Tidak Tersedia",
                            color = MaterialTheme.colorScheme.onError,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right side - Car Details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (car.isAvailable) 1f else 0.6f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Car Name
                Text(
                    text = car.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 16.sp
                )

                // Car Details Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CarDetailChip(
                        text = car.transmission.name.lowercase().replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                        }
                    )
                    CarDetailChip(text = "${car.seats} Kursi")
                    CarDetailChip(text = car.type.name.lowercase().replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    })
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Price and Button Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Price
                    Column {
                        Text(
                            text = formatPrice(car.pricePerDay),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "/hari",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }

                    // Detail Button
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Lihat",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CarDetailChip(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 12.sp,
        modifier = modifier
    )
}

fun formatPrice(price: Int): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(price).replace("IDR", "Rp")
}
