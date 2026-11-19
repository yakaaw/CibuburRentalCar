package com.yaka.cibuburrentalcar.data

import androidx.compose.runtime.Immutable

@Immutable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val profileImageUrl: String = "",
    val isVerified: Boolean = false,
    val memberSince: String = "",
    val role: String = "user"
)

// Sample data untuk fallback
object SampleUserData {
    val currentUser = User(
        id = "U001",
        name = "Demo User",
        email = "demo@example.com",
        phone = "081234567890",
        address = "Cibubur, Jakarta Timur",
        profileImageUrl = "",
        isVerified = true,
        memberSince = "Januari 2024"
    )
}
