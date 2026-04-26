package com.antoniowalls.airetinachat.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Visibility
import androidx.lifecycle.ViewModel
import com.antoniowalls.airetinachat.data.model.ChatSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HistoryViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Estado para capturar errores silenciosos de Firebase
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var allSessions: List<ChatSession> = emptyList()

    private val _groupedHistory = MutableStateFlow<Map<String, List<ChatSession>>>(emptyMap())
    val groupedHistory: StateFlow<Map<String, List<ChatSession>>> = _groupedHistory.asStateFlow()

    init {
        listenToRealHistory()
    }

    private fun listenToRealHistory() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).collection("chats")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _errorMessage.value = "Error Firebase: ${error.localizedMessage}"
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val realSessionsWithTime = snapshot.documents.mapNotNull { doc ->
                        val id = doc.id
                        val title = doc.getString("title") ?: "Análisis de Retina"
                        val preview = doc.getString("preview") ?: "Sin mensajes..."
                        val isAlert = doc.getBoolean("isAlert") ?: false
                        val time = doc.getString("time") ?: ""
                        val category = doc.getString("category") ?: "Hoy"
                        val timestamp = doc.getLong("timestamp") ?: 0L
                        // Lógica de Iconos Dinámicos
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
                        val session = ChatSession(id, title, preview, time, category, icon, isAlert)
                        Pair(timestamp, session) // Lo empaquetamos con su timestamp temporalmente para ordenar
                    }

                    // Ordenamos la lista localmente en Kotlin
                    allSessions = realSessionsWithTime
                        .sortedByDescending { it.first } // El más nuevo arriba
                        .map { it.second }

                    _errorMessage.value = null
                    updateSearchQuery(_searchQuery.value)
                }
            }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        val filtered = if (query.isBlank()) {
            allSessions
        } else {
            allSessions.filter {
                it.title.contains(query, ignoreCase = true) || it.preview.contains(query, ignoreCase = true)
            }
        }
        // Mantiene el orden por categorías
        _groupedHistory.value = filtered.groupBy { it.category }
    }
}