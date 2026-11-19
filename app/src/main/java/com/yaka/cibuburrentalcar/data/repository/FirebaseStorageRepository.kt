package com.yaka.cibuburrentalcar.data.repository

import android.net.Uri
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

class FirebaseStorageRepository {
    private val storage = Firebase.storage

    suspend fun uploadCarImage(carId: String, uri: Uri): String {
        val ref = storage.reference.child("cars/$carId/${System.currentTimeMillis()}.jpg")
        val uploadTask = ref.putFile(uri).await()
        // Get download URL
        return ref.downloadUrl.await().toString()
    }
}

