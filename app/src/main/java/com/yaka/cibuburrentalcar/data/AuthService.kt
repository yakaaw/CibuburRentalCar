package com.yaka.cibuburrentalcar.data

import kotlinx.coroutines.delay

// Data classes for auth requests/responses
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val confirmPassword: String
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val user: User? = null,
    val token: String? = null
)

object AuthService {

    // In-memory user storage for demo (in real app, this would be database)
    private val registeredUsers = mutableListOf<User>()

    // Simple password storage for demo (in real app, use proper password hashing)
    private val userPasswords = mutableMapOf<String, String>()
    
    init {
        // Add default admin user
        registeredUsers.add(
            User(
                id = "U001",
                name = "Admin",
                email = "admin@email.com",
                phone = "081234567890",
                address = "Cibubur, Jakarta Timur",
                profileImageUrl = "",
                isVerified = true,
                memberSince = "2024-01-01"
            )
        )

        // Store admin password
        userPasswords["admin@email.com"] = "password"
    }

    // Validate user password
    private fun validateUserPassword(email: String, password: String): Boolean {
        val storedPassword = userPasswords[email]
        return storedPassword != null && storedPassword == password
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        return try {
            // Simulate network delay
            delay(1000)
            
            android.util.Log.d("AuthService", "Login attempt for: ${request.email}")
            
            // Check credentials with proper password validation
            val user = registeredUsers.find { it.email == request.email }

            if (user != null) {
                // Validate password (for demo, we'll use simple validation)
                val isPasswordValid = when {
                    request.email == "admin@email.com" -> request.password == "password"
                    else -> {
                        // For registered users, we'll store password in a simple way for demo
                        // In real app, you would verify password hash
                        validateUserPassword(request.email, request.password)
                    }
                }

                if (isPasswordValid) {
                    android.util.Log.d("AuthService", "Login successful for: ${request.email}")
                    AuthResponse(
                        success = true,
                        message = "Login berhasil",
                        user = user,
                        token = "demo_token_${System.currentTimeMillis()}"
                    )
                } else {
                    android.util.Log.w("AuthService", "Invalid password for: ${request.email}")
                    AuthResponse(
                        success = false,
                        message = "Email atau password salah"
                    )
                }
            } else {
                android.util.Log.w("AuthService", "User not found: ${request.email}")
                AuthResponse(
                    success = false,
                    message = "Email atau password salah"
                )
            }
            
        } catch (e: Exception) {
            android.util.Log.e("AuthService", "Login error: ${e.message}", e)
            AuthResponse(
                success = false,
                message = "Terjadi kesalahan saat login"
            )
        }
    }
    
    suspend fun register(request: RegisterRequest): AuthResponse {
        return try {
            // Simulate network delay
            delay(1500)
            
            android.util.Log.d("AuthService", "Registration attempt for: ${request.email}")
            
            // Validate input
            if (request.name.isBlank() || request.email.isBlank() || 
                request.phone.isBlank() || request.password.isBlank()) {
                return AuthResponse(
                    success = false,
                    message = "Semua field harus diisi"
                )
            }
            
            if (request.password != request.confirmPassword) {
                return AuthResponse(
                    success = false,
                    message = "Password dan konfirmasi password tidak sama"
                )
            }
            
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(request.email).matches()) {
                return AuthResponse(
                    success = false,
                    message = "Format email tidak valid"
                )
            }
            
            // Check if email already exists
            val existingUser = registeredUsers.find { it.email == request.email }
            if (existingUser != null) {
                android.util.Log.w("AuthService", "Email already exists: ${request.email}")
                return AuthResponse(
                    success = false,
                    message = "Email sudah terdaftar. Silakan gunakan email lain."
                )
            }
            
            // Create new user
            val newUser = User(
                id = "U${String.format("%03d", registeredUsers.size + 1)}",
                name = request.name,
                email = request.email,
                phone = request.phone,
                address = "Cibubur, Jakarta Timur", // Default address
                profileImageUrl = "",
                isVerified = false,
                memberSince = java.time.LocalDate.now().toString()
            )
            
            // Add to registered users
            registeredUsers.add(newUser)

            // Store password for the new user
            userPasswords[request.email] = request.password

            android.util.Log.d("AuthService", "Registration successful for: ${request.email}")
            android.util.Log.d("AuthService", "Total registered users: ${registeredUsers.size}")
            android.util.Log.d("AuthService", "Password stored for: ${request.email}")
            
            AuthResponse(
                success = true,
                message = "Registrasi berhasil",
                user = newUser,
                token = "demo_token_${System.currentTimeMillis()}"
            )
            
        } catch (e: Exception) {
            android.util.Log.e("AuthService", "Registration error: ${e.message}", e)
            AuthResponse(
                success = false,
                message = "Terjadi kesalahan saat registrasi"
            )
        }
    }
    
    // Get all registered users (for debugging)
    fun getRegisteredUsers(): List<User> {
        return registeredUsers.toList()
    }
    
    // Clear all users except admin (for testing)
    fun clearUsers() {
        registeredUsers.clear()
        registeredUsers.add(
            User(
                id = "U001",
                name = "Admin",
                email = "admin@email.com",
                phone = "081234567890",
                address = "Cibubur, Jakarta Timur",
                profileImageUrl = "",
                isVerified = true,
                memberSince = "2024-01-01"
            )
        )
    }
}
