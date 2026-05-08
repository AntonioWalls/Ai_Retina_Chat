package com.antoniowalls.airetinachat.data.repository

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import com.antoniowalls.airetinachat.data.model.ChatSession
import com.antoniowalls.airetinachat.domain.repository.IHistoryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HistoryRepositoryImpl(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : IHistoryRepository {

    override fun getChatHistory(): Flow<List<ChatSession>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listenerRegistration = db.collection("users").document(userId).collection("chats")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val sessions = snapshot.documents.mapNotNull { doc ->
                        val id = doc.id
                        val title = doc.getString("title") ?: "Análisis de Retina"
                        val preview = doc.getString("preview") ?: "Sin mensajes..."
                        val isAlert = doc.getBoolean("isAlert") ?: false
                        val timestamp = doc.getLong("timestamp") ?: 0L

                        // ---CÁLCULO DINÁMICO DE FECHA Y HORA ---
                        val time = if (timestamp > 0L) {
                            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
                        } else {
                            doc.getString("time") ?: ""
                        }

                        //  AGRUPACIÓN INTELIGENTE (Hoy, Ayer, etc.) ---
                        val category = if (timestamp > 0L) {
                            val now = Calendar.getInstance()
                            val chatTime = Calendar.getInstance().apply { timeInMillis = timestamp }

                            val isSameYear = now.get(Calendar.YEAR) == chatTime.get(Calendar.YEAR)
                            val isSameDay = isSameYear && now.get(Calendar.DAY_OF_YEAR) == chatTime.get(Calendar.DAY_OF_YEAR)

                            now.add(Calendar.DAY_OF_YEAR, -1)
                            val isYesterday = now.get(Calendar.YEAR) == chatTime.get(Calendar.YEAR) &&
                                    now.get(Calendar.DAY_OF_YEAR) == chatTime.get(Calendar.DAY_OF_YEAR)

                            now.add(Calendar.DAY_OF_YEAR, -6) // Retrocedemos una semana
                            val isThisWeek = timestamp >= now.timeInMillis

                            when {
                                isSameDay -> "Hoy"
                                isYesterday -> "Ayer"
                                isThisWeek -> "Esta semana"
                                else -> "Anteriores"
                            }
                        } else {
                            // Fallback por si hay chats viejos sin timestamp
                            doc.getString("category") ?: "Hoy"
                        }

                        // --- SELECCIÓN DE ICONO ---
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

                    // Al ordenar por timestamp descendente, nuestro agrupador (groupBy)
                    // en el ViewModel respetará el orden de Hoy -> Ayer -> Esta semana.
                    val sortedSessions = sessions.sortedByDescending { it.first }.map { it.second }
                    trySend(sortedSessions)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }
}