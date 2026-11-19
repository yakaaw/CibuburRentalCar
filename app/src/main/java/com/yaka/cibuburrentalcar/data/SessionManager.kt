package com.yaka.cibuburrentalcar.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

class SessionManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val PREF_NAME = "CibuburRentalCarSession"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_LOGIN_TIME = "login_time"
        private const val KEY_AUTO_LOGIN_ENABLED = "auto_login_enabled"
        
        // Session expires after 30 days
        private const val SESSION_DURATION = 30L * 24 * 60 * 60 * 1000 // 30 days in milliseconds
    }
    
    /**
     * Save user login session
     */
    fun saveLoginSession(user: User, enableAutoLogin: Boolean = true) {
        try {
            val editor = prefs.edit()
            val userJson = gson.toJson(user)
            
            editor.putBoolean(KEY_IS_LOGGED_IN, true)
            editor.putString(KEY_USER_DATA, userJson)
            editor.putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
            editor.putBoolean(KEY_AUTO_LOGIN_ENABLED, enableAutoLogin)
            editor.apply()
            
            Log.d("SessionManager", "✅ Login session saved for user: ${user.name}")
        } catch (e: Exception) {
            Log.e("SessionManager", "❌ Error saving login session: ${e.message}", e)
        }
    }
    
    /**
     * Check if user is logged in and session is valid
     */
    fun isLoggedIn(): Boolean {
        try {
            val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
            val autoLoginEnabled = prefs.getBoolean(KEY_AUTO_LOGIN_ENABLED, false)
            val loginTime = prefs.getLong(KEY_LOGIN_TIME, 0)
            
            if (!isLoggedIn || !autoLoginEnabled) {
                Log.d("SessionManager", "❌ User not logged in or auto-login disabled")
                return false
            }
            
            // Check if session has expired
            val currentTime = System.currentTimeMillis()
            val sessionAge = currentTime - loginTime
            
            if (sessionAge > SESSION_DURATION) {
                Log.d("SessionManager", "❌ Session expired (${sessionAge / (24 * 60 * 60 * 1000)} days old)")
                clearSession()
                return false
            }
            
            Log.d("SessionManager", "✅ Valid session found (${sessionAge / (24 * 60 * 60 * 1000)} days old)")
            return true
            
        } catch (e: Exception) {
            Log.e("SessionManager", "❌ Error checking login status: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Get saved user data
     */
    fun getSavedUser(): User? {
        try {
            if (!isLoggedIn()) {
                return null
            }
            
            val userJson = prefs.getString(KEY_USER_DATA, null)
            if (userJson.isNullOrEmpty()) {
                Log.w("SessionManager", "❌ No user data found in session")
                return null
            }
            
            val user = gson.fromJson(userJson, User::class.java)
            Log.d("SessionManager", "✅ Retrieved user from session: ${user.name}")
            return user
            
        } catch (e: JsonSyntaxException) {
            Log.e("SessionManager", "❌ Error parsing user data: ${e.message}", e)
            clearSession()
            return null
        } catch (e: Exception) {
            Log.e("SessionManager", "❌ Error getting saved user: ${e.message}", e)
            return null
        }
    }
    
    /**
     * Update user data in session
     */
    fun updateUserData(user: User) {
        try {
            if (!isLoggedIn()) {
                Log.w("SessionManager", "❌ Cannot update user data - not logged in")
                return
            }
            
            val editor = prefs.edit()
            val userJson = gson.toJson(user)
            editor.putString(KEY_USER_DATA, userJson)
            editor.apply()
            
            Log.d("SessionManager", "✅ User data updated in session: ${user.name}")
        } catch (e: Exception) {
            Log.e("SessionManager", "❌ Error updating user data: ${e.message}", e)
        }
    }
    
    /**
     * Clear login session (logout)
     */
    fun clearSession() {
        try {
            val editor = prefs.edit()
            editor.clear()
            editor.apply()
            
            Log.d("SessionManager", "✅ Session cleared")
        } catch (e: Exception) {
            Log.e("SessionManager", "❌ Error clearing session: ${e.message}", e)
        }
    }
    
    /**
     * Enable/disable auto login
     */
    fun setAutoLoginEnabled(enabled: Boolean) {
        try {
            val editor = prefs.edit()
            editor.putBoolean(KEY_AUTO_LOGIN_ENABLED, enabled)
            editor.apply()
            
            Log.d("SessionManager", "✅ Auto login ${if (enabled) "enabled" else "disabled"}")
        } catch (e: Exception) {
            Log.e("SessionManager", "❌ Error setting auto login: ${e.message}", e)
        }
    }
    
    /**
     * Check if auto login is enabled
     */
    fun isAutoLoginEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_LOGIN_ENABLED, false)
    }
    
    /**
     * Get session info for debugging
     */
    fun getSessionInfo(): String {
        return try {
            val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
            val autoLoginEnabled = prefs.getBoolean(KEY_AUTO_LOGIN_ENABLED, false)
            val loginTime = prefs.getLong(KEY_LOGIN_TIME, 0)
            val sessionAge = System.currentTimeMillis() - loginTime
            val daysOld = sessionAge / (24 * 60 * 60 * 1000)
            
            "Session Info: LoggedIn=$isLoggedIn, AutoLogin=$autoLoginEnabled, Age=${daysOld}days"
        } catch (e: Exception) {
            "Session Info: Error - ${e.message}"
        }
    }
}
