package com.antoniowalls.airetinachat.domain.usecase.profile

import android.net.Uri
import com.antoniowalls.airetinachat.data.model.Resource
import com.antoniowalls.airetinachat.domain.repository.IAuthRepository

class UpdateProfileUseCase(private val repository: IAuthRepository) {
    suspend operator fun invoke(name: String, phone: String, gender: String, photoUri: Uri?): Resource<Unit> {
        return repository.updateProfile(name, phone, gender, photoUri)
    }
}