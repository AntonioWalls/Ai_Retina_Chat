package com.antoniowalls.airetinachat.domain.usecase.chat

import android.net.Uri
import com.antoniowalls.airetinachat.domain.repository.IChatRepository

class UploadImageToCloudUseCase(private val repository: IChatRepository) {
    suspend operator fun invoke(chatId: String, imageUri: Uri): String {
        return repository.uploadImageToCloud(chatId, imageUri)
    }
}