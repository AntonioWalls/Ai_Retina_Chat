package com.antoniowalls.airetinachat.data.repository
import com.antoniowalls.airetinachat.data.model.Resource
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class AuthRepository(private val auth: FirebaseAuth){
    //variable para saber si hay un usuario logueado
    val currentUser: FirebaseUser? get() = auth.currentUser

    //funcion suspendida para que no se congele la app mientras carga
    suspend fun loginWithEmail(email: String, pass: String): Resource<FirebaseUser>{
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            Resource.Success(result.user!!)
        } catch (e: Exception){
            Resource.Error(e)
        }
    }

    suspend fun registerWithEmail(name: String,email: String, pass: String): Resource<FirebaseUser>{
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user!!

            //añadimos el nombre completo
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            user.updateProfile(profileUpdates).await()
            Resource.Success(user)
        }catch (e: Exception){
            Resource.Error(e)
        }
    }

    //inicio de sesión con Google
    suspend fun loginWithGoogle(credential: AuthCredential): Resource<FirebaseUser>{
        return try{
            val result = auth.signInWithCredential(credential).await()
            Resource.Success(result.user!!)
        }catch (e: Exception){
            Resource.Error(e)
        }
    }
    fun logout(){
        auth.signOut()
    }
}