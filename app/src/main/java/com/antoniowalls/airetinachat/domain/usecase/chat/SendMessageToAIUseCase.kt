package com.antoniowalls.airetinachat.domain.usecase.chat

import com.antoniowalls.airetinachat.data.network.ChatResponse
import com.antoniowalls.airetinachat.domain.repository.IChatRepository
import java.io.File

class SendMessageToAIUseCase(private val repository: IChatRepository) {
    suspend operator fun invoke(historyJson: String, file: File?): ChatResponse {
        return repository.sendMessageToAI(historyJson, file)
    }
}