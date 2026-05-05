package com.antoniowalls.airetinachat.domain.repository

import android.net.Uri
import com.antoniowalls.airetinachat.data.network.ChatResponse
import java.io.File

interface IChatRepository {
    val currentUserId: String?

    suspend fun uploadImageToCloud(chatId: String, imageUri: Uri): String
    suspend fun sendMessageToAI(historyJson: String, file: File?): ChatResponse
    suspend fun saveChatSession(chatID: String, chatData: HashMap<String, Any>)
    suspend fun getChatSession(chatId: String): Map<String, Any>?
}