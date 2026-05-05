package com.antoniowalls.airetinachat.domain.usecase.chat

import com.antoniowalls.airetinachat.domain.repository.IChatRepository

class SaveChatSessionUseCase(private val repository: IChatRepository) {
    suspend operator fun invoke(chatId: String, chatData: HashMap<String, Any>) {
        repository.saveChatSession(chatId, chatData)
    }
}