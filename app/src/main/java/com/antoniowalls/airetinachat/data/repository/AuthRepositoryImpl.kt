package com.antoniowalls.airetinachat.data.repository

import android.net.Uri
import com.antoniowalls.airetinachat.data.model.Resource
import com.antoniowalls.airetinachat.domain.repository.IAuthRepository
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) : IAuthRepository {

    override val currentUser: FirebaseUser? get() = auth.currentUser

    override suspend fun loginWithEmail(email: String, pass: String): Resource<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            Resource.Success(result.user!!)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun registerWithEmail(name: String, email: String, pass: String): Resource<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user!!

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            user.updateProfile(profileUpdates).await()

            val userData = hashMapOf(
                "name" to name,
                "email" to email,
                "gender" to "Male",
                "phone" to "",
                "photoUrl" to ""
            )
            db.collection("users").document(user.uid).set(userData).await()

            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun loginWithGoogle(credential: AuthCredential): Resource<FirebaseUser> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            Resource.Success(result.user!!)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun resetPassword(email: String): Resource<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun getExtraProfileData(): Map<String, Any>? {
        return try {
            val userId = currentUser?.uid ?: return null
            val doc = db.collection("users").document(userId).get().await()
            if (doc.exists()) doc.data else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateProfile(name: String, phone: String, gender: String, photoUri: Uri?): Resource<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("Usuario no autenticado")
            var remotePhotoUrl: String? = user.photoUrl?.toString()

            if (photoUri != null && photoUri.toString().startsWith("content://")) {
                val ref = storage.reference.child("users/${user.uid}/profile_avatar.jpg")
                ref.putFile(photoUri).await()
                remotePhotoUrl = ref.downloadUrl.await().toString()
            }

            val builder = UserProfileChangeRequest.Builder().setDisplayName(name)
            if (remotePhotoUrl != null) {
                builder.setPhotoUri(Uri.parse(remotePhotoUrl))
            }
            user.updateProfile(builder.build()).await()

            val userData = hashMapOf(
                "name" to name,
                "phone" to phone,
                "gender" to gender,
                "photoUrl" to (remotePhotoUrl ?: "")
            )
            db.collection("users").document(user.uid).set(userData, SetOptions.merge()).await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun changePassword(newPass: String): Resource<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("Usuario no autenticado")
            user.updatePassword(newPass).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun update2FAState(isEnabled: Boolean): Resource<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("Usuario no autenticado")
            db.collection("users").document(userId).set(hashMapOf("is2FAEnabled" to isEnabled), SetOptions.merge()).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun reAuthenticateAndChangePassword(currentPass: String, newPass: String): Resource<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("Usuario no autenticado")
            val email = user.email ?: throw Exception("Email no encontrado")

            val credential = EmailAuthProvider.getCredential(email, currentPass)
            user.reauthenticate(credential).await()

            user.updatePassword(newPass).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override fun getLastSignInTimestamp(): Long = auth.currentUser?.metadata?.lastSignInTimestamp ?: 0L

    override fun logout() = auth.signOut()
}
