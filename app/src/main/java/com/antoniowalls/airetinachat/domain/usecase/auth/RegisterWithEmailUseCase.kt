package com.antoniowalls.airetinachat.domain.usecase.auth

import com.antoniowalls.airetinachat.data.model.Resource
import com.antoniowalls.airetinachat.domain.repository.IAuthRepository
import com.google.firebase.auth.FirebaseUser

class RegisterWithEmailUseCase(private val repository: IAuthRepository) {
    suspend operator fun invoke(name: String, email: String, pass: String): Resource<FirebaseUser> {
        return repository.registerWithEmail(name, email, pass)
    }
}