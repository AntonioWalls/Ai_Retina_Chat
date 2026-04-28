package com.antoniowalls.airetinachat.domain.repository

import com.antoniowalls.airetinachat.data.model.ChatSession
import kotlinx.coroutines.flow.Flow

interface IHistoryRepository {
    fun getChatHistory(): Flow<List<ChatSession>>
}