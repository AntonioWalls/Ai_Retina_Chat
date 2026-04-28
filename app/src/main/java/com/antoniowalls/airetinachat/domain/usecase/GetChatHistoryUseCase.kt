package com.antoniowalls.airetinachat.domain.usecase

import com.antoniowalls.airetinachat.data.model.ChatSession
import com.antoniowalls.airetinachat.domain.repository.IHistoryRepository
import kotlinx.coroutines.flow.Flow

class GetChatHistoryUseCase(
    private val historyRepository: IHistoryRepository
) {
    operator fun invoke(): Flow<List<ChatSession>> {
        return historyRepository.getChatHistory()
    }
}