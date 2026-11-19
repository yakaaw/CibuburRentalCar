package com.yaka.cibuburrentalcar.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.yaka.cibuburrentalcar.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yaka.cibuburrentalcar.ui.viewmodel.SplashViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var startAnimation by remember { mutableStateOf(false) }

    // Logo animation: slide up from bottom with rotation
    val logoOffsetY by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 300f,
        animationSpec = tween(
            durationMillis = 1200,
            easing = FastOutSlowInEasing
        ),
        label = "logoOffset"
    )

    val logoRotation by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 180f,
        animationSpec = tween(
            durationMillis = 1200,
            easing = FastOutSlowInEasing
        ),
        label = "logoRotation"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            delayMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "logoAlpha"
    )

    // Text animation: fade in after logo
    val textAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            delayMillis = 800,
            easing = FastOutSlowInEasing
        ),
        label = "textAlpha"
    )

    // Start animation and check auto login
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(1500) // Animation delay
        viewModel.checkAutoLogin(context)
    }

    // Handle navigation based on state
    LaunchedEffect(uiState.navigationTarget) {
        when (uiState.navigationTarget) {
            SplashViewModel.NavigationTarget.LOGIN -> {
                delay(500) // Small delay for smooth transition
                onNavigateToLogin()
            }
            SplashViewModel.NavigationTarget.MAIN -> {
                delay(500) // Small delay for smooth transition
                onNavigateToMain()
            }
            SplashViewModel.NavigationTarget.NONE -> {
                // Still checking, do nothing
            }
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,                    // Deep Blue penuh di atas
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.9f), // Deep Blue 90% di tengah
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), // Deep Blue 80%
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f) // Light Blue tipis di bawah
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo dengan animasi berputar dari bawah - diperbesar
            Image(
                painter = painterResource(id = R.drawable.cibuburrentcar),
                contentDescription = "Cibubur Rental Car Logo",
                modifier = Modifier
                    .size(150.dp)
                    .offset(y = logoOffsetY.dp)
                    .rotate(logoRotation)
                    .alpha(logoAlpha)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // App Name - fade in setelah logo
            Text(
                text = "Cibubur Rental Car",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 28.sp,
                modifier = Modifier.alpha(textAlpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline - fade in setelah nama
            Text(
                text = "Sewa Mobil Mudah & Terpercaya",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                modifier = Modifier.alpha(textAlpha)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Loading indicator and status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = uiState.statusMessage,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                // Error message
                if (uiState.errorMessage.isNotEmpty()) {
                    Text(
                        text = uiState.errorMessage,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { viewModel.retryAutoLogin(context) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text("Coba Lagi")
                    }
                }
            }
        }
    }
}
