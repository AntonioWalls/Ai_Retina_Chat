package com.antoniowalls.airetinachat.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antoniowalls.airetinachat.data.network.RetrofitClient
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

// Representa un "globo" de mensaje en la pantalla
data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val imageUri: Uri? = null
)

class ChatViewModel : ViewModel() {

    // Lista de mensajes (El historial de la conversación actual)
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    // Para mostrar el "Cargando..." cuando la IA está pensando
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendMessage(context: Context, text: String, imageUri: Uri?) {
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

                // Si hay foto, hacemos la magia de convertir Uri -> File físico -> MultipartBody
                if (imageUri != null) {
                    val file = withContext(Dispatchers.IO) { uriToFile(context, imageUri) }
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    imagePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
                }

                // Disparamos la petición a Ngrok/Colab
                val response = RetrofitClient.apiService.sendMessage(promptBody, imagePart)

                // 3. Procesamos la respuesta de la IA
                if (response.success) {
                    val aiMessage = ChatMessage(response.response ?: "Sin respuesta", isFromUser = false)
                    _messages.value = _messages.value + aiMessage
                } else {
                    val errorMessage = ChatMessage("Error del servidor: ${response.error}", isFromUser = false)
                    _messages.value = _messages.value + errorMessage
                }

            } catch (e: Exception) {
                val errorMessage = ChatMessage("Fallo de conexión. ¿Ngrok está corriendo?\nDetalle: ${e.localizedMessage}", isFromUser = false)
                _messages.value = _messages.value + errorMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Función hacker 👩‍💻: Android no nos deja enviar 'Uris' por internet,
    // así que copiamos la foto a la memoria caché temporal del teléfono y mandamos ese archivo.
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