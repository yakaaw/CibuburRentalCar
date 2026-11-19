package com.yaka.cibuburrentalcar.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yaka.cibuburrentalcar.data.CurrentUserProvider
import com.yaka.cibuburrentalcar.data.repository.AuthRepository
import com.yaka.cibuburrentalcar.data.repository.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SplashUiState(
    val isLoading: Boolean = true,
    val statusMessage: String = "Memuat aplikasi...",
    val errorMessage: String = "",
    val navigationTarget: SplashViewModel.NavigationTarget = SplashViewModel.NavigationTarget.NONE
)

class SplashViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()
    
    enum class NavigationTarget {
        NONE,
        LOGIN,
        MAIN
    }
    
    fun checkAutoLogin(context: Context) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    statusMessage = "Memeriksa sesi login...",
                    errorMessage = "",
                    navigationTarget = NavigationTarget.NONE
                )
                
                val authRepository = AuthRepository(context)
                
                // Check if user has valid session
                if (authRepository.isLoggedIn()) {
                    _uiState.value = _uiState.value.copy(
                        statusMessage = "Memuat data pengguna..."
                    )
                    
                    // Try auto login
                    val result = authRepository.autoLogin()
                    when (result) {
                        is NetworkResult.Success -> {
                            // Set current user
                            result.data?.let { user ->
                                CurrentUserProvider.setCurrentUser(user)

                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    statusMessage = "Login berhasil!",
                                    navigationTarget = NavigationTarget.MAIN
                                )

                                android.util.Log.d("SplashViewModel", "✅ Auto login successful: ${user.name}")
                            } ?: run {
                                // No user data, go to login
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    statusMessage = "Silakan login",
                                    navigationTarget = NavigationTarget.LOGIN
                                )
                            }
                        }
                        is NetworkResult.Error -> {
                            // Auto login failed, go to login screen
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                statusMessage = "Silakan login",
                                navigationTarget = NavigationTarget.LOGIN
                            )

                            android.util.Log.d("SplashViewModel", "❌ Auto login failed: ${result.message ?: "Unknown error"}")
                        }
                        is NetworkResult.Loading -> {
                            // Keep loading
                        }
                    }
                } else {
                    // No valid session, go to login
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        statusMessage = "Silakan login",
                        navigationTarget = NavigationTarget.LOGIN
                    )
                    
                    android.util.Log.d("SplashViewModel", "❌ No valid session found")
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = "",
                    errorMessage = "Terjadi kesalahan: ${e.message}",
                    navigationTarget = NavigationTarget.LOGIN
                )
                
                android.util.Log.e("SplashViewModel", "❌ Error checking auto login: ${e.message}", e)
            }
        }
    }
    
    fun retryAutoLogin(context: Context) {
        checkAutoLogin(context)
    }
}
