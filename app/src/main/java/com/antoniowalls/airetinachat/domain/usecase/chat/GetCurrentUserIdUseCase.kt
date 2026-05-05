package com.antoniowalls.airetinachat.domain.usecase.chat

import com.antoniowalls.airetinachat.domain.repository.IChatRepository

class GetCurrentUserIdUseCase(private val repository: IChatRepository) {
    operator fun invoke(): String? {
        return repository.currentUserId
    }
}