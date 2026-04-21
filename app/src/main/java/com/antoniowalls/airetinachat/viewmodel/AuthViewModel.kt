package com.antoniowalls.airetinachat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.antoniowalls.airetinachat.data.model.Resource
import com.antoniowalls.airetinachat.data.repository.AuthRepository
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository): ViewModel(){
    //Estado de autenticación
    private val _authState = MutableStateFlow<Resource<FirebaseUser>?>(null)
    val authState: StateFlow<Resource<FirebaseUser>?> = _authState.asStateFlow()

    //Estado del usuario
    private val _currentUser = MutableStateFlow<FirebaseUser?>(repository.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    //funciones que llamará la interfaz de usuario

    fun login(email: String, pass: String) {
        // viewModelScope asegura que la tarea se cancele si la pantalla se cierra
        viewModelScope.launch {
            _authState.value = Resource.Loading // Le decimos a la UI que muestre el circulito de carga
            val result = repository.loginWithEmail(email, pass)
            _authState.value = result // Le mandamos a la UI el Éxito o el Error

            if (result is Resource.Success) {
                _currentUser.value = result.data // Actualizamos el usuario activo
            }
        }
    }

    fun register (name: String,email: String, pass: String){
        viewModelScope.launch {
            _authState.value = Resource.Loading
            val result = repository.registerWithEmail(name,email,pass)
            _authState.value = result

            if(result is Resource.Success){
                _currentUser.value = result.data
            }
        }
    }

    fun loginWithGoogle(credential: AuthCredential){
        viewModelScope.launch {
            _authState.value = Resource.Loading
            val result = repository.loginWithGoogle(credential)
            _authState.value = result
            if(result is Resource.Success){
                _currentUser.value = result.data
            }
        }
    }

    fun logout(){
        repository.logout()
        _currentUser.value = null
        _authState.value = null
    }

    fun resetAuthState(){
        _authState.value = null
    }
}

//Factory

class AuthViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            val auth = FirebaseAuth.getInstance()
            val repository = AuthRepository(auth)
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}