package com.antoniowalls.airetinachat.domain.usecase.auth

import com.antoniowalls.airetinachat.data.model.Resource
import com.antoniowalls.airetinachat.domain.repository.IAuthRepository
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser

class LoginWithGoogleUseCase(private val repository: IAuthRepository) {
    suspend operator fun invoke(credential: AuthCredential): Resource<FirebaseUser> {
        return repository.loginWithGoogle(credential)
    }
}