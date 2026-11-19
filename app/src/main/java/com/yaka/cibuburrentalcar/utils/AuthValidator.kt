package com.yaka.cibuburrentalcar.utils

import android.util.Patterns

object AuthValidator {
    
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    fun isValidPassword(password: String): Boolean {
        return password.isNotBlank() && password.length >= 6
    }
    
    fun isValidName(name: String): Boolean {
        return name.isNotBlank() && name.length >= 2
    }
    
    fun isValidPhone(phone: String): Boolean {
        return phone.isNotBlank() && phone.length >= 10 && phone.all { it.isDigit() || it == '+' || it == '-' || it == ' ' }
    }
    
    fun doPasswordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }
    
    fun validateLoginForm(email: String, password: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Error("Email tidak boleh kosong")
            password.isBlank() -> ValidationResult.Error("Password tidak boleh kosong")
            !isValidEmail(email) -> ValidationResult.Error("Format email tidak valid")
            !isValidPassword(password) -> ValidationResult.Error("Password minimal 6 karakter")
            else -> ValidationResult.Success
        }
    }
    
    fun validateRegisterForm(
        name: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String
    ): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error("Nama tidak boleh kosong")
            email.isBlank() -> ValidationResult.Error("Email tidak boleh kosong")
            phone.isBlank() -> ValidationResult.Error("Nomor telepon tidak boleh kosong")
            password.isBlank() -> ValidationResult.Error("Password tidak boleh kosong")
            confirmPassword.isBlank() -> ValidationResult.Error("Konfirmasi password tidak boleh kosong")
            !isValidName(name) -> ValidationResult.Error("Nama minimal 2 karakter")
            !isValidEmail(email) -> ValidationResult.Error("Format email tidak valid")
            !isValidPhone(phone) -> ValidationResult.Error("Format nomor telepon tidak valid")
            !isValidPassword(password) -> ValidationResult.Error("Password minimal 6 karakter")
            !doPasswordsMatch(password, confirmPassword) -> ValidationResult.Error("Password dan konfirmasi password tidak sama")
            else -> ValidationResult.Success
        }
    }
    
    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }

    // Individual validation functions that return error message or null
    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email tidak boleh kosong"
            !isValidEmail(email) -> "Format email tidak valid"
            else -> null
        }
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password tidak boleh kosong"
            !isValidPassword(password) -> "Password minimal 6 karakter"
            else -> null
        }
    }

    fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "Nama tidak boleh kosong"
            !isValidName(name) -> "Nama minimal 2 karakter"
            else -> null
        }
    }

    fun validatePhone(phone: String): String? {
        return when {
            phone.isBlank() -> "Nomor telepon tidak boleh kosong"
            !isValidPhone(phone) -> "Format nomor telepon tidak valid"
            else -> null
        }
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isBlank() -> "Konfirmasi password tidak boleh kosong"
            !doPasswordsMatch(password, confirmPassword) -> "Password dan konfirmasi password tidak sama"
            else -> null
        }
    }
}
