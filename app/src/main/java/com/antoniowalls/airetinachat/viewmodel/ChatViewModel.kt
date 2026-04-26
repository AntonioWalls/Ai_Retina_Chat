package com.antoniowalls.airetinachat.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antoniowalls.airetinachat.data.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
import java.util.UUID

data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val imageUri: Uri? = null
)

class ChatViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var currentChatId: String? = null

    private val _chatTitle = MutableStateFlow("Retina AI")
    val chatTitle: StateFlow<String> = _chatTitle.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendMessage(context: Context, text: String, imageUri: Uri?) {
        val userId = auth.currentUser?.uid ?: return

        if (currentChatId == null) {
            currentChatId = db.collection("users").document(userId).collection("chats").document().id
        }

        if (_messages.value.isEmpty()) {
            val newTitle = if (imageUri != null && text.isBlank()) {
                "Análisis de Imagen"
            } else {
                if (text.length > 25) text.substring(0, 25).replaceFirstChar { it.uppercase() } + "..."
                else text.replaceFirstChar { it.uppercase() }
            }
            _chatTitle.value = newTitle
        }

        // Mostramos el mensaje en pantalla con la imagen local (rápido)
        val userMessage = ChatMessage(text, isFromUser = true, imageUri = imageUri)
        _messages.value = _messages.value + userMessage
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // SUBIR IMAGEN A LA NUBE
                var remoteImageUrl: String? = null
                if (imageUri != null) {
                    try {
                        // Creamos una carpeta: users/ID/chats/ID_CHAT/foto_aleatoria.jpg
                        val imageRef = storage.reference.child("users/$userId/chats/$currentChatId/${UUID.randomUUID()}.jpg")
                        imageRef.putFile(imageUri).await() // Sube el archivo
                        remoteImageUrl = imageRef.downloadUrl.await().toString() // Obtiene el link público

                        // Reemplazamos la ruta local por el link de la nube en la interfaz
                        val updatedMessages = _messages.value.toMutableList()
                        val lastMsgIndex = updatedMessages.indexOfLast { it.isFromUser && it.text == text }
                        if (lastMsgIndex != -1) {
                            updatedMessages[lastMsgIndex] = userMessage.copy(imageUri = Uri.parse(remoteImageUrl))
                            _messages.value = updatedMessages
                        }
                    } catch (e: Exception) {
                        e.printStackTrace() // Si falla la subida, imprimimos error pero dejamos que la IA siga respondiendo
                    }
                }

                //  ENVIAR A LA IA EN COLAB
                val promptBody = text.toRequestBody("text/plain".toMediaTypeOrNull())
                var imagePart: MultipartBody.Part? = null

                if (imageUri != null) {
                    val file = withContext(Dispatchers.IO) { uriToFile(context, imageUri) }
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    imagePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
                }

                val response = RetrofitClient.apiService.sendMessage(promptBody, imagePart)

                // GUARDAR RESULTADOS
                if (response.success) {
                    val aiResponseText = response.response ?: "Sin respuesta"
                    val aiMessage = ChatMessage(aiResponseText, isFromUser = false)
                    _messages.value = _messages.value + aiMessage

                    // GUARDAMOS EL HISTORIAL EN FIREBASE
                    saveChatToFirebase(userId, text, aiResponseText)
                } else {
                    val errorMessage = ChatMessage("Error del servidor: ${response.error}", isFromUser = false)
                    _messages.value = _messages.value + errorMessage
                }

            } catch (e: Exception) {
                val errorMessage = ChatMessage("Fallo de conexión.\nDetalle: ${e.localizedMessage}", isFromUser = false)
                _messages.value = _messages.value + errorMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun saveChatToFirebase(userId: String, userMessage: String, aiResponse: String) {
        val chatId = currentChatId ?: return
        val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

        val isAlert = aiResponse.contains("anomalía", ignoreCase = true) ||
                aiResponse.contains("patología", ignoreCase = true) ||
                aiResponse.contains("glaucoma", ignoreCase = true)

        // El texto previo será lo último que escribió el usuario, o la respuesta de la IA si el usuario solo mandó foto
        val previewText = if (userMessage.isNotBlank()) userMessage else aiResponse.take(50) + "..."

        // ☁️ AHORA GUARDAMOS EL LINK DE LA IMAGEN
        val messagesList = _messages.value.map {
            hashMapOf(
                "text" to it.text,
                "isFromUser" to it.isFromUser,
                "imageUrl" to (it.imageUri?.toString() ?: "") // Guardamos la URL remota
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

        // Guarda (o actualiza) el documento en la base de datos
        db.collection("users").document(userId).collection("chats").document(chatId)
            .set(chatData, SetOptions.merge())
    }

    fun loadChat(chatId: String) {
        if (currentChatId == chatId) return
        val userId = auth.currentUser?.uid ?: return

        _isLoading.value = true
        currentChatId = chatId

        db.collection("users").document(userId).collection("chats").document(chatId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    _chatTitle.value = doc.getString("title") ?: "Retina AI"

                    val rawMessages = doc.get("messages")

                    if (rawMessages is List<*>) {
                        val loadedMsgs = rawMessages.mapNotNull { item ->
                            if (item is Map<*, *>) {
                                // ☁️ RECUPERAMOS EL LINK Y LO CONVERTIMOS A URI PARA MOSTRARLO
                                val urlStr = item["imageUrl"] as? String
                                val imageUriFromCloud = if (!urlStr.isNullOrBlank()) Uri.parse(urlStr) else null

                                ChatMessage(
                                    text = item["text"] as? String ?: "",
                                    isFromUser = item["isFromUser"] as? Boolean ?: false,
                                    imageUri = imageUriFromCloud
                                )
                            } else null
                        }

                        if (loadedMsgs.isNotEmpty()) {
                            _messages.value = loadedMsgs
                        } else {
                            _messages.value = listOf(ChatMessage("⚠️ La base de datos dice que este chat está vacío.", false))
                        }
                    } else {
                        val preview = doc.getString("preview") ?: "Chat antiguo"
                        _messages.value = listOf(
                            ChatMessage("⚠️ Este es un chat de una versión antigua donde no se guardaba el historial.\n\nÚltimo mensaje guardado:\n\"$preview\"", false)
                        )
                    }
                }
                _isLoading.value = false
            }
            .addOnFailureListener {
                _isLoading.value = false
            }
    }

    fun resetChat() {
        if (currentChatId == null && _messages.value.isEmpty()) return
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