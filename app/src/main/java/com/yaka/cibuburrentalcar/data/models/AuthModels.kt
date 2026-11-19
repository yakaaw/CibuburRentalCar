package com.yaka.cibuburrentalcar.data.models

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String
)

data class RegisterRequest(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("phone")
    val phone: String,
    
    @SerializedName("password")
    val password: String,
    
    @SerializedName("confirm_password")
    val confirmPassword: String
)

data class AuthResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("status")
    val status: Int? = null,

    @SerializedName("message")
    val message: String,

    @SerializedName("user")
    val user: UserModel? = null,

    @SerializedName("token")
    val token: String? = null,

    @SerializedName("data")
    val data: AuthData? = null
)

data class AuthData(
    @SerializedName("user")
    val user: UserModel? = null
)

data class UpdateProfileRequest(
    @SerializedName("user_id")
    val userId: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("address")
    val address: String
)

data class UserModel(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("address")
    val address: String? = null
)

