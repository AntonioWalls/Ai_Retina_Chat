package com.antoniowalls.airetinachat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antoniowalls.airetinachat.data.model.Resource
import com.antoniowalls.airetinachat.data.repository.AuthRepository
import com.google.firebase.auth.AuthCredential
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

    private val _resetPasswordState = MutableStateFlow<Resource<Unit>?>(null)
    val resetPasswordState: StateFlow<Resource<Unit>?> = _resetPasswordState.asStateFlow()

    fun login(email: String, pass: String) {
        // viewModelScope asegura que la tarea se cancele si la pantalla se cierra
        viewModelScope.launch {
            _authState.value = Resource.Loading
            val result = repository.loginWithEmail(email, pass)
            _authState.value = result

            if (result is Resource.Success) {
                _currentUser.value = result.data
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

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _resetPasswordState.value = Resource.Loading
            val result = repository.resetPassword(email)
            _resetPasswordState.value = result
        }
    }

    fun clearResetPasswordState() {
        _resetPasswordState.value = null
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