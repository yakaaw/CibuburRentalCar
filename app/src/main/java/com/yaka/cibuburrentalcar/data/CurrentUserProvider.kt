package com.yaka.cibuburrentalcar.data

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.yaka.cibuburrentalcar.data.repository.AuthRepository

object CurrentUserProvider {
    
    private var _currentUser by mutableStateOf<User?>(null)
    private var authRepository: AuthRepository? = null
    
    val currentUser: User
        get() {
            val user = _currentUser ?: SampleUserData.currentUser
            // Ensure user ID is never empty
            return if (user.id.isEmpty()) {
                SampleUserData.currentUser
            } else {
                user
            }
        }
    
    fun initialize(context: Context) {
        authRepository = AuthRepository(context)
        refreshCurrentUser()
    }
    
    fun refreshCurrentUser() {
        _currentUser = authRepository?.getStoredUser()
        android.util.Log.d("CurrentUserProvider", "Current user refreshed: ${_currentUser?.name ?: "null"}")
    }
    
    fun setCurrentUser(user: User) {
        _currentUser = user
        android.util.Log.d("CurrentUserProvider", "Current user set: ${user.name}")
    }
    
    fun clearCurrentUser() {
        _currentUser = null
        android.util.Log.d("CurrentUserProvider", "Current user cleared")
    }
    
    fun isLoggedIn(): Boolean {
        return authRepository?.checkLoginStatus() ?: false
    }
}
