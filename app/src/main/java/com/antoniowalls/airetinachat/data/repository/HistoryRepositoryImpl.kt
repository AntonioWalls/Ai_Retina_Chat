package com.antoniowalls.airetinachat.data.repository

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Visibility
import com.antoniowalls.airetinachat.data.model.ChatSession
import com.antoniowalls.airetinachat.domain.repository.IHistoryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class HistoryRepositoryImpl(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : IHistoryRepository {
    override fun getChatHistory(): Flow<List<ChatSession>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if(userId == null){
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listenerRegistration = db.collection("users").document(userId).collection("chats")
            .addSnapshotListener { snapshot, error ->
                if(error != null){
                    close(error)
                    return@addSnapshotListener
                }

                if(snapshot != null){
                    val sessions = snapshot.documents.mapNotNull { doc ->
                        val id = doc.id
                        val title = doc.getString("title") ?: "Análisis de Retina"
                        val preview = doc.getString("preview") ?: "Sin mensajes..."
                        val isAlert = doc.getBoolean("isAlert") ?: false
                        val time = doc.getString("time") ?: ""
                        val category = doc.getString("category") ?: "Hoy"
                        val timestamp = doc.getLong("timestamp") ?: 0L

                        val combinedText = "${title.lowercase()} ${preview.lowercase()}"
                        val icon = when {
                            combinedText.contains("corazón") || combinedText.contains("heart") -> Icons.Outlined.FavoriteBorder
                            combinedText.contains("dieta") || combinedText.contains("diet") -> Icons.Outlined.Restaurant
                            combinedText.contains("síntoma") || combinedText.contains("médic") -> Icons.Outlined.MedicalServices
                            combinedText.contains("mental") || combinedText.contains("psychology") -> Icons.Outlined.Psychology
                            combinedText.contains("código") || combinedText.contains("kotlin") -> Icons.Outlined.Code
                            combinedText.contains("viaje") || combinedText.contains("trip") -> Icons.Outlined.Flight
                            else -> Icons.Outlined.Visibility
                        }

                        Pair(timestamp, ChatSession(id, title, preview, time, category, icon, isAlert))
                    }

                    val sortedSessions = sessions.sortedByDescending { it.first }.map { it.second }
                    trySend(sortedSessions)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }
}