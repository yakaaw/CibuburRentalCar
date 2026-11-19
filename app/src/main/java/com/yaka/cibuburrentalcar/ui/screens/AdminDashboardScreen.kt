package com.yaka.cibuburrentalcar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CarRental
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yaka.cibuburrentalcar.ui.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateToCars: () -> Unit,
    onNavigateToBookings: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(title = "ADMIN DASHBOARD", onBackClick = null, centerTitle = true)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AdminMenuCard(
                    title = "Manajemen Mobil",
                    icon = Icons.Default.CarRental,
                    onClick = onNavigateToCars,
                    modifier = Modifier.weight(1f)
                )
                AdminMenuCard(
                    title = "Maintenance Calendar",
                    icon = Icons.Default.CalendarToday,
                    onClick = onNavigateToCalendar,
                    modifier = Modifier.weight(1f)
                )
            }
            AdminMenuCard(
                title = "Pengaturan Admin",
                icon = Icons.Default.Settings,
                onClick = onNavigateToSettings,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminMenuCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(imageVector = icon, contentDescription = null)
                Text(text = title, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

