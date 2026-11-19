package com.yaka.cibuburrentalcar

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.yaka.cibuburrentalcar.data.CurrentUserProvider
import com.yaka.cibuburrentalcar.data.repository.FirebaseCarRepository
import com.yaka.cibuburrentalcar.data.repository.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.yaka.cibuburrentalcar.navigation.CibuburRentalCarNavigation
import com.yaka.cibuburrentalcar.ui.theme.CibuburRentalCarTheme
import com.yaka.cibuburrentalcar.utils.FirebaseSetup

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install and configure splash screen to minimize system splash
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val splashScreen = installSplashScreen()
            splashScreen.setKeepOnScreenCondition { false } // Dismiss immediately
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize CurrentUserProvider
        CurrentUserProvider.initialize(this)

        // Populate Firestore with sample car data (run once)
        populateSampleCarData()

        // Firebase test disabled temporarily to debug crash
        android.util.Log.d("MainActivity", "🚀 MainActivity started successfully!")


        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            CibuburRentalCarTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CibuburRentalCarNavigation()
                }
            }
        }
    }

    private fun populateSampleCarData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val carRepository = FirebaseCarRepository()
                val result = carRepository.addSampleData()

                when (result) {
                    is NetworkResult.Success -> {
                        android.util.Log.d("MainActivity", "✅ Sample car data populated successfully")
                    }
                    is NetworkResult.Error -> {
                        android.util.Log.e("MainActivity", "❌ Failed to populate sample data: ${result.message}")
                    }
                    is NetworkResult.Loading -> {
                        android.util.Log.d("MainActivity", "🔄 Populating sample data...")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "❌ Error populating sample data: ${e.message}", e)
            }
        }
    }
}