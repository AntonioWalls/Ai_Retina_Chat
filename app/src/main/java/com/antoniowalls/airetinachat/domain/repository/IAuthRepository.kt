package com.antoniowalls.airetinachat.domain.repository

import android.net.Uri
import com.antoniowalls.airetinachat.data.model.Resource
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser


interface IAuthRepository {
    val currentUser: FirebaseUser?

    suspend fun loginWithEmail(email: String, pass: String): Resource<FirebaseUser>
    suspend fun registerWithEmail(name: String, email: String, pass: String): Resource<FirebaseUser>
    suspend fun loginWithGoogle(credential: AuthCredential): Resource<FirebaseUser>
    suspend fun resetPassword(email: String): Resource<Unit>
    suspend fun getExtraProfileData(): Map<String, Any>?
    suspend fun updateProfile(name: String, phone: String, gender: String, photoUri: Uri?): Resource<Unit>
    suspend fun changePassword(newPass: String): Resource<Unit>
    suspend fun update2FAState(isEnabled: Boolean): Resource<Unit>
    suspend fun reAuthenticateAndChangePassword(currentPass: String, newPass: String): Resource<Unit>

    fun getLastSignInTimestamp(): Long
    fun logout()
}