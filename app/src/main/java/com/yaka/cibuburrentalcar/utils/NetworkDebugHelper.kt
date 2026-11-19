package com.yaka.cibuburrentalcar.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object NetworkDebugHelper {
    
    private const val TAG = "NetworkDebugHelper"
    
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                Log.d(TAG, "Connected via WiFi")
                true
            }
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                Log.d(TAG, "Connected via Cellular")
                true
            }
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                Log.d(TAG, "Connected via Ethernet")
                true
            }
            else -> {
                Log.d(TAG, "No network connection")
                false
            }
        }
    }
    
    suspend fun testApiConnection(baseUrl: String): NetworkTestResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Testing connection to: $baseUrl")
                
                val testUrls = listOf(
                    "${baseUrl}cars/get_all.php",
                    "${baseUrl}auth/login.php",
                    baseUrl.removeSuffix("/")
                )
                
                val results = mutableListOf<String>()
                
                for (testUrl in testUrls) {
                    try {
                        val url = URL(testUrl)
                        val connection = url.openConnection() as HttpURLConnection
                        connection.requestMethod = "GET"
                        connection.connectTimeout = 5000
                        connection.readTimeout = 5000
                        
                        val responseCode = connection.responseCode
                        val responseMessage = connection.responseMessage
                        
                        Log.d(TAG, "URL: $testUrl - Response: $responseCode $responseMessage")
                        results.add("✅ $testUrl: $responseCode $responseMessage")
                        
                        connection.disconnect()
                        
                        if (responseCode in 200..299) {
                            return@withContext NetworkTestResult.Success("Connection successful", results)
                        }
                        
                    } catch (e: Exception) {
                        Log.e(TAG, "Error testing $testUrl: ${e.message}")
                        results.add("❌ $testUrl: ${e.message}")
                    }
                }
                
                NetworkTestResult.Error("All connections failed", results)
                
            } catch (e: Exception) {
                Log.e(TAG, "General error testing connection: ${e.message}")
                NetworkTestResult.Error("Network test failed: ${e.message}", emptyList())
            }
        }
    }
    
    fun logNetworkConfig() {
        Log.d(TAG, "=== Network Configuration ===")
        Log.d(TAG, "Current Backend: Firebase Firestore")
        Log.d(TAG, "Network Security Config: Enabled")
        Log.d(TAG, "Cleartext Traffic: Allowed for development")
        Log.d(TAG, "==============================")
    }
    
    fun getRecommendedUrls(): List<String> {
        return listOf(
            "http://10.0.2.2/cibubur_rental_car/api/",           // Android Emulator
            "http://localhost/cibubur_rental_car/api/",          // Localhost
            "http://127.0.0.1/cibubur_rental_car/api/",         // Loopback
            "http://192.168.100.130/cibubur_rental_car/api/",   // Your current IP
            "http://192.168.1.100/cibubur_rental_car/api/",     // Common router IP
        )
    }
}

sealed class NetworkTestResult {
    data class Success(val message: String, val details: List<String>) : NetworkTestResult()
    data class Error(val message: String, val details: List<String>) : NetworkTestResult()
}
