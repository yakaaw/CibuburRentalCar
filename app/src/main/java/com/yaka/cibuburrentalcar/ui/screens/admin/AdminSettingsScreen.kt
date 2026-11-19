package com.yaka.cibuburrentalcar.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.yaka.cibuburrentalcar.data.CurrentUserProvider
import com.yaka.cibuburrentalcar.data.repository.AuthRepository
import com.yaka.cibuburrentalcar.ui.components.AppTopBar

@Composable
fun AdminSettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val authRepository = remember { AuthRepository(context) }

    Scaffold(topBar = { AppTopBar(title = "Pengaturan Admin", onBackClick = onBack, centerTitle = true) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Kelola akun admin", style = MaterialTheme.typography.titleMedium)
            Button(onClick = {
                authRepository.performLogout()
                CurrentUserProvider.clearCurrentUser()
                onLogout()
            }) {
                Text("Logout")
            }
        }
    }
}

