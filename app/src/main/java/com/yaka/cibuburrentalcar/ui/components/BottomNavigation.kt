package com.yaka.cibuburrentalcar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem(
        route = "home",
        title = "Beranda",
        selectedIcon = Icons.Filled.DirectionsCar,
        unselectedIcon = Icons.Outlined.DirectionsCar
    )

    object History : BottomNavItem(
        route = "history",
        title = "Riwayat",
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History
    )

    object Help : BottomNavItem(
        route = "help",
        title = "Bantuan",
        selectedIcon = Icons.AutoMirrored.Filled.Help,
        unselectedIcon = Icons.AutoMirrored.Outlined.Help
    )

    object Profile : BottomNavItem(
        route = "profile",
        title = "Profil",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
}

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.History,
        BottomNavItem.Help,
        BottomNavItem.Profile
    )

    // Floating navigation bar dengan background putih semi transparan
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Color.Black.copy(alpha = 0.1f),
                    spotColor = Color.Black.copy(alpha = 0.1f)
                ),
            shape = RoundedCornerShape(32.dp),
            color = Color.White.copy(alpha = 0.95f),
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = currentRoute == item.route
                    val interactionSource = remember { MutableInteractionSource() }

                    // Pill-shaped background untuk item aktif
                    val pillBackgroundColor by animateColorAsState(
                        targetValue = if (isSelected)
                            Color(0xFF1565C0) // Biru tua untuk highlight
                        else
                            Color.Transparent,
                        animationSpec = tween(300),
                        label = "pillBackground"
                    )

                    // Warna ikon dan teks
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected)
                            Color.White // Putih untuk kontras dengan background biru
                        else
                            Color(0xFF9E9E9E), // Abu-abu muda untuk item tidak aktif
                        animationSpec = tween(300),
                        label = "contentColor"
                    )

                    // Pill-shaped container untuk setiap item
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp)) // Pill shape
                            .background(pillBackgroundColor)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                onNavigate(item.route)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title,
                                tint = contentColor,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = item.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = contentColor
                            )
                        }
                    }
                }
            }
        }
    }
}
