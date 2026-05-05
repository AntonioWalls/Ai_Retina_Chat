package com.antoniowalls.airetinachat.data.repository

import android.net.Uri
import com.antoniowalls.airetinachat.data.network.ApiService
import com.antoniowalls.airetinachat.data.network.ChatResponse
import com.antoniowalls.airetinachat.domain.repository.IChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.UUID


class ChatRepositoryImpl(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val apiService: ApiService
) : IChatRepository {


    override val currentUserId: String? get() = auth.currentUser?.uid

    // Sube a Storage y retorna la URL
    override suspend fun uploadImageToCloud(chatId: String, imageUri: Uri): String {
        val userId = currentUserId ?: throw Exception("No hay un usuario autenticado")
        val imageRef = storage.reference.child("users/$userId/chats/$chatId/${UUID.randomUUID()}.jpg")
        imageRef.putFile(imageUri).await()
        return imageRef.downloadUrl.await().toString()
    }

    // Llama a la IA en Python (vía Retrofit)
    override suspend fun sendMessageToAI(historyJson: String, file: File?): ChatResponse {

        val builder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("history", historyJson) // Agrega el texto como un campo de formulario puro

        if (file != null) {
            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            builder.addFormDataPart("file", file.name, requestFile)
        }

        // Pasamos todo el bloque construido directamente a Retrofit
        return apiService.sendMessage(builder.build())
    }

    // Guarda el historial completo en Firestore
    override suspend fun saveChatSession(chatID: String, chatData: HashMap<String, Any>) {
        val userId = currentUserId ?: return
        db.collection("users").document(userId).collection("chats").document(chatID)
            .set(chatData, SetOptions.merge())
            .await()
    }

    override suspend fun getChatSession(chatId: String): Map<String, Any>? {
        val userId = currentUserId ?: return null
        val doc = db.collection("users").document(userId).collection("chats").document(chatId).get().await()
        return if (doc.exists()) doc.data else null
    }
}