package com.antoniowalls.airetinachat.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antoniowalls.airetinachat.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val imageUri: Uri? = null
)

class ChatViewModel(
    private val repository: ChatRepository // ¡Inyectado automáticamente por Koin!
) : ViewModel() {

    private var currentChatId: String? = null

    private val _chatTitle = MutableStateFlow("Retina AI")
    val chatTitle: StateFlow<String> = _chatTitle.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()


    fun sendMessage(text: String, imageUri: Uri?, imageFile: File?) {
        val userId = repository.currentUserId ?: return

        if (currentChatId == null) {
            currentChatId = UUID.randomUUID().toString()
        }

        if (_messages.value.isEmpty()) {
            val newTitle = if (imageUri != null && text.isBlank()) "Análisis de Imagen"
            else if (text.length > 25) text.substring(0, 25).replaceFirstChar { it.uppercase() } + "..."
            else text.replaceFirstChar { it.uppercase() }
            _chatTitle.value = newTitle
        }

        val userMessage = ChatMessage(text, isFromUser = true, imageUri = imageUri)
        _messages.value = _messages.value + userMessage
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // 1. Subir imagen a Firebase Storage usando el Repositorio
                var remoteImageUrl: String? = null
                if (imageUri != null) {
                    try {
                        remoteImageUrl = repository.uploadImageToCloud(currentChatId!!, imageUri)

                        // Actualizamos la UI local con la URL de la nube
                        val updatedMessages = _messages.value.toMutableList()
                        val lastMsgIndex = updatedMessages.indexOfLast { it.isFromUser && it.text == text }
                        if (lastMsgIndex != -1) {
                            updatedMessages[lastMsgIndex] = userMessage.copy(imageUri = Uri.parse(remoteImageUrl))
                            _messages.value = updatedMessages
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }

                // 2. Llamada a la API de la IA (Usando el Repositorio)
                val response = repository.sendMessageToAi(text, imageFile)

                if (response.success) {
                    val aiResponseText = response.response ?: "Sin respuesta"
                    _messages.value = _messages.value + ChatMessage(aiResponseText, isFromUser = false)

                    // 3. Guardar historial en Firestore
                    saveChatToFirebase(userMessage.text, aiResponseText)
                } else {
                    _messages.value = _messages.value + ChatMessage("Error: ${response.error}", isFromUser = false)
                }
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage("Fallo de conexión: ${e.localizedMessage}", isFromUser = false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun saveChatToFirebase(userMessage: String, aiResponse: String) {
        val chatId = currentChatId ?: return
        val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

        val isAlert = aiResponse.contains("anomalía", true) || aiResponse.contains("patología", true)

        val previewText = if (userMessage.isNotBlank()) userMessage else aiResponse.take(50) + "..."

        val messagesList = _messages.value.map {
            hashMapOf(
                "text" to it.text,
                "isFromUser" to it.isFromUser,
                "imageUrl" to (it.imageUri?.toString() ?: "")
            )
        }

        val chatData = hashMapOf(
            "title" to _chatTitle.value,
            "preview" to previewText,
            "time" to time,
            "category" to "Hoy",
            "isAlert" to isAlert,
            "timestamp" to System.currentTimeMillis(),
            "messages" to messagesList
        )

        // Usamos el repositorio para guardar los datos
        repository.saveChatSession(chatId, chatData)
    }

    fun loadChat(chatId: String) {
        if (currentChatId == chatId) return

        _isLoading.value = true
        currentChatId = chatId

        viewModelScope.launch {
            try {
                // Le pedimos los datos al Repositorio
                val data = repository.getChatSession(chatId)
                if (data != null) {
                    _chatTitle.value = data["title"] as? String ?: "Retina AI"

                    val rawMessages = data["messages"] as? List<*>
                    if (rawMessages != null) {
                        _messages.value = rawMessages.mapNotNull { item ->
                            if (item is Map<*, *>) {
                                val urlStr = item["imageUrl"] as? String
                                val imageUriFromCloud = if (!urlStr.isNullOrBlank()) Uri.parse(urlStr) else null
                                ChatMessage(item["text"] as? String ?: "", item["isFromUser"] as? Boolean ?: false, imageUriFromCloud)
                            } else null
                        }
                    } else {
                        val preview = data["preview"] as? String ?: "Chat antiguo"
                        _messages.value = listOf(ChatMessage("⚠️ Chat antiguo sin historial.\n\nÚltimo msj: \"$preview\"", false))
                    }
                }
            } catch (e: Exception) {
                // Manejar error silenciosamente o mandarlo al estado
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetChat() {
        if (currentChatId == null && _messages.value.isEmpty()) return
        currentChatId = null
        _chatTitle.value = "Retina AI"
        _messages.value = emptyList()
    }
}