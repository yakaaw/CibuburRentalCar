// Test Firebase Connection
// Add this to MainActivity temporarily to test Firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

// Add this function to MainActivity
private fun testFirebaseConnection() {
    try {
        // Test Firestore
        val firestore = FirebaseFirestore.getInstance()
        android.util.Log.d("Firebase", "✅ Firestore instance created successfully")
        
        // Test Auth
        val auth = FirebaseAuth.getInstance()
        android.util.Log.d("Firebase", "✅ Auth instance created successfully")
        
        // Test write to Firestore
        val testData = mapOf(
            "test" to "Firebase connection working!",
            "timestamp" to System.currentTimeMillis()
        )
        
        firestore.collection("test")
            .document("connection_test")
            .set(testData)
            .addOnSuccessListener {
                android.util.Log.d("Firebase", "🔥 Firebase write test SUCCESS!")
            }
            .addOnFailureListener { e ->
                android.util.Log.e("Firebase", "❌ Firebase write test FAILED: ${e.message}")
            }
            
    } catch (e: Exception) {
        android.util.Log.e("Firebase", "❌ Firebase connection test failed: ${e.message}")
    }
}

// Call this in onCreate() after setContent:
// testFirebaseConnection()
