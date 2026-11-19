package com.yaka.cibuburrentalcar.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun CategoryIcon(
    category: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val icon = getCategoryIcon(category)
    Icon(
        imageVector = icon,
        contentDescription = category,
        tint = tint,
        modifier = modifier
    )
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "semua" -> Icons.Default.DirectionsCar
        "mpv" -> Icons.Default.AirportShuttle  // Van-like icon for MPV
        "suv" -> Icons.Default.LocalShipping   // Truck-like icon for SUV
        "sedan" -> Icons.Default.DirectionsCar // Car icon for Sedan
        "hatchback" -> Icons.Default.DirectionsCar // Car icon for Hatchback
        else -> Icons.Default.DirectionsCar
    }
}

fun getCategoryGradient(category: String): Pair<Color, Color> {
    return when (category.lowercase()) {
        "semua" -> Pair(Color(0xFF1976D2), Color(0xFF42A5F5))
        "mpv" -> Pair(Color(0xFF388E3C), Color(0xFF66BB6A))
        "suv" -> Pair(Color(0xFFD32F2F), Color(0xFFEF5350))
        "sedan" -> Pair(Color(0xFF7B1FA2), Color(0xFFBA68C8))
        "hatchback" -> Pair(Color(0xFFFF8F00), Color(0xFFFFB74D))
        else -> Pair(Color(0xFF1976D2), Color(0xFF42A5F5))
    }
}

fun getCategoryDescription(category: String): String {
    return when (category.lowercase()) {
        "semua" -> "Semua jenis mobil"
        "mpv" -> "Multi Purpose Vehicle - Cocok untuk keluarga besar"
        "suv" -> "Sport Utility Vehicle - Tangguh untuk segala medan"
        "sedan" -> "Mobil mewah dan nyaman untuk perjalanan bisnis"
        "hatchback" -> "Mobil kompak dan efisien untuk perkotaan"
        else -> "Kategori mobil"
    }
}
