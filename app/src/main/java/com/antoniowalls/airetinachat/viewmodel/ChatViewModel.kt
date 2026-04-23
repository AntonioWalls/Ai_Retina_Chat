package com.antoniowalls.airetinachat.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antoniowalls.airetinachat.data.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Representa un "globo" de mensaje en la pantalla
data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val imageUri: Uri? = null
)

class ChatViewModel : ViewModel() {

    // Instancias de Firebase
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // ID del chat en la base de datos (se genera cuando mandas el primer mensaje)
    private var currentChatId: String? = null

    // Título dinámico del chat (Comienza por defecto como Retina AI)
    private val _chatTitle = MutableStateFlow("Retina AI")
    val chatTitle: StateFlow<String> = _chatTitle.asStateFlow()

    // Lista de mensajes (El historial de la conversación actual)
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    // Para mostrar el "Cargando..." cuando la IA está pensando
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendMessage(context: Context, text: String, imageUri: Uri?) {
        val userId = auth.currentUser?.uid ?: return // Seguridad: Solo logueados pueden chatear

        // Si es un chat nuevo, generamos un ID único en Firebase
        if (currentChatId == null) {
            currentChatId = db.collection("users").document(userId).collection("chats").document().id
        }

        // Magia: Generación automática del título en el primer mensaje
        if (_messages.value.isEmpty()) {
            val newTitle = if (imageUri != null && text.isBlank()) {
                "Análisis de Imagen"
            } else {
                if (text.length > 25) text.substring(0, 25).replaceFirstChar { it.uppercase() } + "..."
                else text.replaceFirstChar { it.uppercase() }
            }
            _chatTitle.value = newTitle
        }

        // 1. Agregar el mensaje del Doctor a la pantalla instantáneamente
        val userMessage = ChatMessage(text, isFromUser = true, imageUri = imageUri)
        _messages.value = _messages.value + userMessage
        _isLoading.value = true

        // 2. Llamar a la IA en segundo plano (Corrutina)
        viewModelScope.launch {
            try {
                // Preparamos el texto
                val promptBody = text.toRequestBody("text/plain".toMediaTypeOrNull())
                var imagePart: MultipartBody.Part? = null

                if (imageUri != null) {
                    val file = withContext(Dispatchers.IO) { uriToFile(context, imageUri) }
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    imagePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
                }

                // Disparamos la petición a Ngrok/Colab
                val response = RetrofitClient.apiService.sendMessage(promptBody, imagePart)

                // 3. Procesamos la respuesta de la IA
                if (response.success) {
                    val aiResponseText = response.response ?: "Sin respuesta"
                    val aiMessage = ChatMessage(aiResponseText, isFromUser = false)
                    _messages.value = _messages.value + aiMessage

                    // 4. ¡GUARDAMOS EL HISTORIAL EN FIREBASE!
                    saveChatToFirebase(userId, text, aiResponseText)
                } else {
                    val errorMessage = ChatMessage("Error del servidor: ${response.error}", isFromUser = false)
                    _messages.value = _messages.value + errorMessage
                }

            } catch (e: Exception) {
                val errorMessage = ChatMessage("Fallo de conexión. ¿Ngrok está corriendo y la URL es correcta?\nDetalle: ${e.localizedMessage}", isFromUser = false)
                _messages.value = _messages.value + errorMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun saveChatToFirebase(userId: String, userMessage: String, aiResponse: String) {
        val chatId = currentChatId ?: return

        // Formateamos la hora actual
        val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

        // Lógica súper inteligente: Si la IA menciona palabras de alerta, marcamos la tarjeta en ROJO en el historial
        val isAlert = aiResponse.contains("anomalía", ignoreCase = true) ||
                aiResponse.contains("patología", ignoreCase = true) ||
                aiResponse.contains("pathological", ignoreCase = true) ||
                aiResponse.contains("glaucoma", ignoreCase = true)

        // El texto previo será lo último que escribió el usuario, o la respuesta de la IA si el usuario solo mandó foto
        val previewText = if (userMessage.isNotBlank()) userMessage else aiResponse.take(50) + "..."

        val chatData = hashMapOf(
            "title" to _chatTitle.value,
            "preview" to previewText,
            "time" to time,
            "category" to "Hoy", // Todo lo nuevo entra a la categoría 'Hoy'
            "isAlert" to isAlert,
            "timestamp" to System.currentTimeMillis() // Permite ordenar del más nuevo al más viejo
        )

        // Guarda (o actualiza) el documento en la base de datos
        db.collection("users").document(userId).collection("chats").document(chatId)
            .set(chatData)
    }

    // Para cuando el usuario presione el botón '+' en el historial y quiera un chat limpio
    fun resetChat() {
        currentChatId = null
        _chatTitle.value = "Retina AI"
        _messages.value = emptyList()
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File(context.cacheDir, "temp_retina_upload.jpg")
        val outputStream = FileOutputStream(tempFile)
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }
}