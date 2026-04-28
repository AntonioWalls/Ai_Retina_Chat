package com.antoniowalls.airetinachat.data.repository

import android.net.Uri
import com.antoniowalls.airetinachat.data.network.ApiService
import com.antoniowalls.airetinachat.data.network.ChatResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.UUID

class ChatRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val apiService: ApiService
) {
    val currentUserId: String? get() = auth.currentUser?.uid

    //sube a Storage y retorn la URL
    suspend fun uploadImageToCloud(chatId: String, imageUri: Uri): String {
        val userId = currentUserId ?: throw Exception("No hay un usuario autenticado")
        val imageRef = storage.reference.child("users/$userId/chats/$chatId/${UUID.randomUUID()}.jpg")
        imageRef.putFile(imageUri).await()
        return imageRef.downloadUrl.await().toString()
    }

    //Llama a la IA en python (vía retrofit)
    suspend fun sendMessageToAi(text: String, file: File?): ChatResponse{
        val promptBody = text.toRequestBody("text/plain".toMediaTypeOrNull())
        var imagePart: MultipartBody.Part? = null

        if (file != null){
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            imagePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
        }
        return apiService.sendMessage(promptBody, imagePart)
    }

    //Guarda el historial completo en Firestore
    suspend fun saveChatSession(chatID: String, chatData: HashMap<String, Any>){
        val userId = currentUserId ?: return
        db.collection("users").document(userId).collection("chats").document(chatID)
            .set(chatData, SetOptions.merge())
            .await()
    }

    suspend fun getChatSession(chatId: String): Map<String, Any>? {
        val userId = currentUserId ?: return null
        val doc = db.collection("users").document(userId).collection("chats").document(chatId).get().await()
        return if (doc.exists()) doc.data else null
    }
}