package com.yaka.cibuburrentalcar

import android.app.Application
import com.yaka.cibuburrentalcar.data.CurrentUserProvider

class CibuburRentalCarApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize CurrentUserProvider with application context
        CurrentUserProvider.initialize(this)
    }
}