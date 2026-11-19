package com.yaka.cibuburrentalcar.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yaka.cibuburrentalcar.data.CalendarBlock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CalendarBlockRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth get() = FirebaseAuth.getInstance()

    private fun carBlocksCollection(carId: String) =
        firestore.collection("cars").document(carId).collection("blocks")

    private fun globalBlocksCollection() = firestore.collection("global_blocks")

    suspend fun listBlocks(carId: String): NetworkResult<List<CalendarBlock>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = carBlocksCollection(carId).get().await()
            val blocks = snapshot.documents.map { doc ->
                CalendarBlock(
                    id = doc.id,
                    startDate = doc.getString("startDate") ?: "",
                    endDate = doc.getString("endDate") ?: "",
                    reason = doc.getString("reason") ?: "",
                    createdBy = doc.getString("createdBy") ?: "",
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            }
            NetworkResult.Success(blocks)
        } catch (e: Exception) {
            NetworkResult.Error("Failed to load blocks: ${e.message}")
        }
    }

    suspend fun addBlock(carId: String, block: CalendarBlock): NetworkResult<String> = withContext(Dispatchers.IO) {
        try {
            val docRef = carBlocksCollection(carId).document()
            val uid = auth.currentUser?.uid ?: ""
            val data = mapOf(
                "startDate" to block.startDate,
                "endDate" to block.endDate,
                "reason" to block.reason,
                "createdBy" to (if (block.createdBy.isNotBlank()) block.createdBy else uid),
                "createdAt" to block.createdAt
            )
            docRef.set(data).await()
            NetworkResult.Success(docRef.id)
        } catch (e: Exception) {
            NetworkResult.Error("Failed to add block: ${e.message}")
        }
    }

    suspend fun removeBlock(carId: String, blockId: String): NetworkResult<Unit> = withContext(Dispatchers.IO) {
        try {
            carBlocksCollection(carId).document(blockId).delete().await()
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error("Failed to remove block: ${e.message}")
        }
    }

    // Global blocks
    suspend fun listGlobalBlocks(): NetworkResult<List<CalendarBlock>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = globalBlocksCollection().get().await()
            val blocks = snapshot.documents.map { doc ->
                CalendarBlock(
                    id = doc.id,
                    startDate = doc.getString("startDate") ?: "",
                    endDate = doc.getString("endDate") ?: "",
                    reason = doc.getString("reason") ?: "",
                    createdBy = doc.getString("createdBy") ?: "",
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            }
            NetworkResult.Success(blocks)
        } catch (e: Exception) {
            NetworkResult.Error("Failed to load global blocks: ${e.message}")
        }
    }

    suspend fun addGlobalBlock(block: CalendarBlock): NetworkResult<String> = withContext(Dispatchers.IO) {
        try {
            val docRef = globalBlocksCollection().document()
            val uid = auth.currentUser?.uid ?: ""
            val data = mapOf(
                "startDate" to block.startDate,
                "endDate" to block.endDate,
                "reason" to block.reason,
                "createdBy" to (if (block.createdBy.isNotBlank()) block.createdBy else uid),
                "createdAt" to block.createdAt
            )
            docRef.set(data).await()
            NetworkResult.Success(docRef.id)
        } catch (e: Exception) {
            NetworkResult.Error("Failed to add global block: ${e.message}")
        }
    }

    suspend fun removeGlobalBlock(blockId: String): NetworkResult<Unit> = withContext(Dispatchers.IO) {
        try {
            globalBlocksCollection().document(blockId).delete().await()
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error("Failed to remove global block: ${e.message}")
        }
    }
}

