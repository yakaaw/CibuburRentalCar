package com.yaka.cibuburrentalcar.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.yaka.cibuburrentalcar.data.AuthService
import com.yaka.cibuburrentalcar.data.LoginRequest
import com.yaka.cibuburrentalcar.data.RegisterRequest
import com.yaka.cibuburrentalcar.data.SessionManager
import com.yaka.cibuburrentalcar.data.User
// import com.yaka.cibuburrentalcar.data.api.ApiClient // Removed - Firebase only
import com.yaka.cibuburrentalcar.data.models.UpdateProfileRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val context: Context) {
    private val firebaseRepository = FirebaseAuthRepository()
    private val sessionManager = SessionManager(context)
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    
    suspend fun login(email: String, password: String): NetworkResult<User> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("AuthRepository", "🔄 Attempting login for: $email")

                // Primary: Try Firebase first
                android.util.Log.d("AuthRepository", "🔥 Attempting Firebase login")
                val firebaseResult = firebaseRepository.login(email, password)

                when (firebaseResult) {
                    is NetworkResult.Success -> {
                        android.util.Log.d("AuthRepository", "✅ Firebase login success")
                        firebaseResult.data?.let { user ->
                            // Save to both session manager and preferences
                            sessionManager.saveLoginSession(user, enableAutoLogin = true)
                            saveUserToPreferences(user)
                            android.util.Log.d("AuthRepository", "✅ Login session saved")
                        }
                        return@withContext firebaseResult
                    }
                    is NetworkResult.Error -> {
                        android.util.Log.e("AuthRepository", "❌ Firebase login failed: ${firebaseResult.message}")
                        // No fallback - return error for full online experience
                        return@withContext NetworkResult.Error("Login gagal. Periksa koneksi internet dan coba lagi.")
                    }
                    is NetworkResult.Loading -> {
                        return@withContext NetworkResult.Loading()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "Login error: ${e.message}", e)
                NetworkResult.Error("Login failed: ${e.message}")
            }
        }
    }
    
    suspend fun register(name: String, email: String, password: String, phone: String): NetworkResult<User> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("AuthRepository", "🔥 Attempting Firebase registration for: $email")

                // Primary: Try Firebase first
                val firebaseResult = firebaseRepository.register(name, email, phone, password)

                when (firebaseResult) {
                    is NetworkResult.Success -> {
                        android.util.Log.d("AuthRepository", "✅ Firebase registration successful")
                        firebaseResult.data?.let { user ->
                            saveUserToPreferences(user)
                        }
                        return@withContext firebaseResult
                    }
                    is NetworkResult.Error -> {
                        android.util.Log.e("AuthRepository", "❌ Firebase registration failed: ${firebaseResult.message}")
                        // No fallback - return error for full online experience
                        return@withContext NetworkResult.Error("Registrasi gagal. Periksa koneksi internet dan coba lagi.")
                    }
                    is NetworkResult.Loading -> {
                        return@withContext NetworkResult.Loading()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "❌ Registration exception: ${e.message}", e)
                NetworkResult.Error("Registration failed: ${e.message}")
            }
        }
    }

    // MySQL methods removed - Firebase only implementation



    // convertApiUserToAppUser removed - Firebase only implementation
    
    fun logout() {
        android.util.Log.d("AuthRepository", "User logged out")
        sharedPreferences.edit().clear().apply()
    }
    
    fun getCurrentUser(): User? {
        val userId = sharedPreferences.getString("user_id", null) ?: return null
        val userName = sharedPreferences.getString("user_name", null) ?: return null
        val userEmail = sharedPreferences.getString("user_email", null) ?: return null
        val userPhone = sharedPreferences.getString("user_phone", null) ?: return null
        val userAddress = sharedPreferences.getString("user_address", "") ?: ""
        val isVerified = sharedPreferences.getBoolean("user_verified", false)
        val memberSince = sharedPreferences.getString("user_member_since", "") ?: ""
        
        return User(
            id = userId,
            name = userName,
            email = userEmail,
            phone = userPhone,
            address = userAddress,
            isVerified = isVerified,
            memberSince = memberSince,
            role = sharedPreferences.getString("user_role", "user") ?: "user"
        )
    }

    suspend fun updateProfile(name: String, phone: String, address: String): NetworkResult<User> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("AuthRepository", "🔥 Starting Firebase updateProfile...")

                val currentUser = getCurrentUser()
                if (currentUser == null) {
                    android.util.Log.e("AuthRepository", "Current user is null")
                    return@withContext NetworkResult.Error("User tidak ditemukan")
                }

                // Use Firebase to update profile
                val firebaseResult = firebaseRepository.updateProfile(name, phone, address)

                when (firebaseResult) {
                    is NetworkResult.Success -> {
                        android.util.Log.d("AuthRepository", "✅ Firebase profile update successful")
                        firebaseResult.data?.let { user ->
                            saveUserToPreferences(user)
                        }
                        return@withContext firebaseResult
                    }
                    is NetworkResult.Error -> {
                        android.util.Log.e("AuthRepository", "❌ Firebase profile update failed: ${firebaseResult.message}")
                        return@withContext NetworkResult.Error("Update profil gagal. Periksa koneksi internet dan coba lagi.")
                    }
                    is NetworkResult.Loading -> {
                        return@withContext NetworkResult.Loading()
                    }
                }

            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "Error updating profile: ${e.message}", e)
                NetworkResult.Error("Error: ${e.message}")
            }
        }
    }

    private fun getAuthToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }

    private fun saveUserData(user: User, token: String) {
        sharedPreferences.edit().apply {
            putString("user_id", user.id)
            putString("user_name", user.name)
            putString("user_email", user.email)
            putString("user_phone", user.phone)
            putString("user_address", user.address)
            putBoolean("user_verified", user.isVerified)
            putString("user_member_since", user.memberSince)
            putString("user_role", user.role)
            putString("auth_token", token)
            apply()
        }
    }

    private fun saveUserToPreferences(user: User) {
        saveUserData(user, "firebase_token")
    }

    fun getStoredUser(): User? {
        val userId = sharedPreferences.getString("user_id", null)
        return if (userId != null && userId.isNotEmpty()) {
            User(
                id = userId,
                name = sharedPreferences.getString("user_name", "") ?: "",
                email = sharedPreferences.getString("user_email", "") ?: "",
                phone = sharedPreferences.getString("user_phone", "") ?: "",
                address = sharedPreferences.getString("user_address", "") ?: "",
                profileImageUrl = "",
                isVerified = sharedPreferences.getBoolean("user_verified", false),
                memberSince = sharedPreferences.getString("user_member_since", "") ?: "",
                role = sharedPreferences.getString("user_role", "user") ?: "user"
            )
        } else {
            // Check Firebase current user
            firebaseRepository.getCurrentUser()
        }
    }

    fun checkLoginStatus(): Boolean {
        val hasLocalUser = sharedPreferences.getString("user_id", null) != null
        val hasFirebaseUser = firebaseRepository.isLoggedIn()
        return hasLocalUser || hasFirebaseUser
    }

    /**
     * Check if user is already logged in (auto login)
     */
    fun isLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }

    /**
     * Get saved user from session
     */
    fun getSavedUser(): User? {
        return sessionManager.getSavedUser()
    }

    /**
     * Auto login - get user from saved session
     */
    suspend fun autoLogin(): NetworkResult<User> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("AuthRepository", "🔄 Checking auto login...")

                if (!sessionManager.isLoggedIn()) {
                    android.util.Log.d("AuthRepository", "❌ No valid session found")
                    return@withContext NetworkResult.Error("No saved session")
                }

                val savedUser = sessionManager.getSavedUser()
                if (savedUser != null) {
                    android.util.Log.d("AuthRepository", "✅ Auto login successful: ${savedUser.name}")
                    return@withContext NetworkResult.Success(savedUser)
                } else {
                    android.util.Log.d("AuthRepository", "❌ No user data in session")
                    return@withContext NetworkResult.Error("No user data found")
                }

            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "❌ Auto login error: ${e.message}", e)
                NetworkResult.Error("Auto login failed: ${e.message}")
            }
        }
    }

    fun performLogout() {
        // Clear session
        sessionManager.clearSession()
        // Clear SharedPreferences
        sharedPreferences.edit().clear().apply()
        // Logout from Firebase
        firebaseRepository.logout()
        android.util.Log.d("AuthRepository", "✅ Logout completed")
    }
}
