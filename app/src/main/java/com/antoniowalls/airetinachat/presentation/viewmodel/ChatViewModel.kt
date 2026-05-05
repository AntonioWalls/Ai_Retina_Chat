package com.antoniowalls.airetinachat.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antoniowalls.airetinachat.domain.usecase.chat.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import com.google.gson.Gson

data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val imageUri: Uri? = null
)

class ChatViewModel(
    private val uploadImageToCloudUseCase: UploadImageToCloudUseCase,
    private val sendMessageToAIUseCase: SendMessageToAIUseCase,
    private val saveChatSessionUseCase: SaveChatSessionUseCase,
    private val getChatSessionUseCase: GetChatSessionUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) : ViewModel() {

    private var currentChatId: String? = null

    private val _chatTitle = MutableStateFlow("Retina AI")
    val chatTitle: StateFlow<String> = _chatTitle.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendMessage(text: String, imageUri: Uri?, imageFile: File?) {
        // Usamos el caso de uso para obtener el ID en lugar del repositorio
        val userId = getCurrentUserIdUseCase() ?: return

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
                // Subir imagen usando el Caso de Uso
                var remoteImageUrl: String? = null
                if (imageUri != null) {
                    try {
                        remoteImageUrl = uploadImageToCloudUseCase(currentChatId!!, imageUri)

                        val updatedMessages = _messages.value.toMutableList()
                        val lastMsgIndex = updatedMessages.indexOfLast { it.isFromUser && it.text == text }
                        if (lastMsgIndex != -1) {
                            updatedMessages[lastMsgIndex] = userMessage.copy(imageUri = Uri.parse(remoteImageUrl))
                            _messages.value = updatedMessages
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }

                // Crear JSON del historial completo
                val historyList = _messages.value.map {
                    mapOf("text" to it.text, "isFromUser" to it.isFromUser)
                }
                val historyJson = Gson().toJson(historyList)

                // Llamada a la API de la IA usando el Caso de Uso
                val response = sendMessageToAIUseCase(historyJson, imageFile)

                if (response.success) {
                    val aiResponseText = response.response ?: "Sin respuesta"
                    _messages.value = _messages.value + ChatMessage(aiResponseText, isFromUser = false)

                    // Guardar historial
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

        val chatData = hashMapOf<String, Any>(
            "title" to _chatTitle.value,
            "preview" to previewText,
            "time" to time,
            "category" to "Hoy",
            "isAlert" to isAlert,
            "timestamp" to System.currentTimeMillis(),
            "messages" to messagesList
        )

        // Usamos el Caso de Uso en lugar del repositorio
        saveChatSessionUseCase(chatId, chatData)
    }

    fun loadChat(chatId: String) {
        if (currentChatId == chatId) return

        _isLoading.value = true
        currentChatId = chatId

        viewModelScope.launch {
            try {
                // Usamos el Caso de Uso para pedir los datos
                val data = getChatSessionUseCase(chatId)
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
                // Manejar error silenciosamente
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