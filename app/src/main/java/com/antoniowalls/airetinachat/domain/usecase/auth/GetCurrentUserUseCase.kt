package com.antoniowalls.airetinachat.domain.usecase.auth

import com.antoniowalls.airetinachat.domain.repository.IAuthRepository
import com.google.firebase.auth.FirebaseUser

class GetCurrentUserUseCase(private val repository: IAuthRepository) {
    operator fun invoke(): FirebaseUser? {
        return repository.currentUser
    }
}