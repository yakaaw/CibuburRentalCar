package com.yaka.cibuburrentalcar.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.yaka.cibuburrentalcar.data.User
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirebaseAuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    
    companion object {
        private const val TAG = "FirebaseAuthRepository"
    }

    suspend fun login(email: String, password: String): NetworkResult<User> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔥 Firebase login attempt for: $email")
                
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = result.user
                
                if (firebaseUser != null) {
                    // Get user data from Firestore
                    val userDoc = usersCollection.document(firebaseUser.uid).get().await()
                    
                    val user = if (userDoc.exists()) {
                        // User data exists in Firestore
                        documentToUser(firebaseUser.uid, userDoc.data ?: emptyMap(), firebaseUser)
                    } else {
                        // Create user data in Firestore from Firebase Auth
                        val newUserData = mapOf(
                            "name" to (firebaseUser.displayName ?: "User"),
                            "email" to (firebaseUser.email ?: email),
                            "phone" to "",
                            "address" to "",
                            "profileImageUrl" to (firebaseUser.photoUrl?.toString() ?: ""),
                            "isVerified" to firebaseUser.isEmailVerified,
                            "memberSince" to System.currentTimeMillis(),
                            "role" to "user"
                        )

                        usersCollection.document(firebaseUser.uid).set(newUserData).await()
                        documentToUser(firebaseUser.uid, newUserData, firebaseUser)
                    }
                    
                    Log.d(TAG, "✅ Firebase login successful for: ${user.name}")
                    NetworkResult.Success(user)
                } else {
                    Log.w(TAG, "⚠️ Firebase login failed: No user returned")
                    NetworkResult.Error("Login failed")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Firebase login error: ${e.message}", e)
                NetworkResult.Error("Login failed: ${e.message}")
            }
        }
    }

    suspend fun register(
        name: String,
        email: String,
        phone: String,
        password: String,
        address: String = ""
    ): NetworkResult<User> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔥 Firebase register attempt for: $email")
                
                // Create Firebase Auth user
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = result.user
                
                if (firebaseUser != null) {
                    // Create user data in Firestore
                    val userData = mapOf(
                        "name" to name,
                        "email" to email,
                        "phone" to phone,
                        "address" to address,
                        "profileImageUrl" to "",
                        "isVerified" to false,
                        "memberSince" to System.currentTimeMillis(),
                        "role" to "user"
                    )
                    
                    usersCollection.document(firebaseUser.uid).set(userData).await()
                    
                    val user = documentToUser(firebaseUser.uid, userData, firebaseUser)
                    
                    Log.d(TAG, "✅ Firebase register successful for: ${user.name}")
                    NetworkResult.Success(user)
                } else {
                    Log.w(TAG, "⚠️ Firebase register failed: No user returned")
                    NetworkResult.Error("Registration failed")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Firebase register error: ${e.message}", e)
                NetworkResult.Error("Registration failed: ${e.message}")
            }
        }
    }

    suspend fun updateProfile(
        name: String,
        phone: String,
        address: String
    ): NetworkResult<User> {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    return@withContext NetworkResult.Error("User not logged in")
                }
                
                Log.d(TAG, "🔥 Firebase update profile for: ${currentUser.uid}")
                
                val updates = mapOf(
                    "name" to name,
                    "phone" to phone,
                    "address" to address
                )
                
                usersCollection.document(currentUser.uid).update(updates).await()
                
                // Get updated user data
                val userDoc = usersCollection.document(currentUser.uid).get().await()
                val user = documentToUser(currentUser.uid, userDoc.data ?: emptyMap(), currentUser)
                
                Log.d(TAG, "✅ Firebase profile update successful")
                NetworkResult.Success(user)
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Firebase update profile error: ${e.message}", e)
                NetworkResult.Error("Profile update failed: ${e.message}")
            }
        }
    }

    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            try {
                // Fetch from Firestore to get full profile including role
                val doc = usersCollection.document(firebaseUser.uid).get().result
                val data = doc?.data
                if (doc != null && doc.exists() && data != null) {
                    documentToUser(firebaseUser.uid, data, firebaseUser)
                } else {
                    // Fallback to minimal info
                    User(
                        id = firebaseUser.uid,
                        name = firebaseUser.displayName ?: "User",
                        email = firebaseUser.email ?: "",
                        phone = "",
                        address = "",
                        profileImageUrl = firebaseUser.photoUrl?.toString() ?: "",
                        isVerified = firebaseUser.isEmailVerified,
                        memberSince = "Firebase User",
                        role = "user"
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error getting current user profile: ${e.message}")
                User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "User",
                    email = firebaseUser.email ?: "",
                    phone = "",
                    address = "",
                    profileImageUrl = firebaseUser.photoUrl?.toString() ?: "",
                    isVerified = firebaseUser.isEmailVerified,
                    memberSince = "Firebase User",
                    role = "user"
                )
            }
        } else {
            null
        }
    }

    fun logout() {
        Log.d(TAG, "🔥 Firebase logout")
        auth.signOut()
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    private fun documentToUser(id: String, data: Map<String, Any>, firebaseUser: FirebaseUser): User {
        return User(
            id = id,
            name = data["name"] as? String ?: firebaseUser.displayName ?: "User",
            email = data["email"] as? String ?: firebaseUser.email ?: "",
            phone = data["phone"] as? String ?: "",
            address = data["address"] as? String ?: "",
            profileImageUrl = data["profileImageUrl"] as? String ?: firebaseUser.photoUrl?.toString() ?: "",
            isVerified = data["isVerified"] as? Boolean ?: firebaseUser.isEmailVerified,
            memberSince = formatMemberSince(data["memberSince"] as? Long),
            role = (data["role"] as? String) ?: "user"
        )
    }

    private fun formatMemberSince(timestamp: Long?): String {
        return if (timestamp != null) {
            val date = java.util.Date(timestamp)
            java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault()).format(date)
        } else {
            "Firebase User"
        }
    }
}
