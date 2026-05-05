package com.antoniowalls.airetinachat.domain.usecase.chat

import com.antoniowalls.airetinachat.domain.repository.IChatRepository

class GetChatSessionUseCase(private val repository: IChatRepository) {
    suspend operator fun invoke(chatId: String): Map<String, Any>? {
        return repository.getChatSession(chatId)
    }
}