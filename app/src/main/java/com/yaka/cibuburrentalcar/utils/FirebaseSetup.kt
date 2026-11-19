package com.yaka.cibuburrentalcar.utils

import android.util.Log
import com.yaka.cibuburrentalcar.data.repository.FirebaseCarRepository
import com.yaka.cibuburrentalcar.data.repository.NetworkResult

object FirebaseSetup {
    private const val TAG = "FirebaseSetup"
    
    suspend fun initializeFirebaseData(): Boolean {
        return try {
            Log.d(TAG, "🔥 Initializing Firebase data...")
            
            val carRepository = FirebaseCarRepository()
            
            // Add sample cars data to Firestore
            val result = carRepository.addSampleData()
            
            when (result) {
                is NetworkResult.Success -> {
                    Log.d(TAG, "✅ Firebase data initialization successful")
                    true
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "❌ Firebase data initialization failed: ${result.message}")
                    false
                }
                is NetworkResult.Loading -> {
                    Log.d(TAG, "⏳ Firebase data initialization in progress")
                    false
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Firebase initialization error: ${e.message}", e)
            false
        }
    }
    
    fun checkFirebaseConnection(): Boolean {
        return try {
            // Simple check - if we can create Firebase instances, connection is likely OK
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
            com.google.firebase.auth.FirebaseAuth.getInstance()
            Log.d(TAG, "✅ Firebase connection check passed")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Firebase connection check failed: ${e.message}", e)
            false
        }
    }
}
