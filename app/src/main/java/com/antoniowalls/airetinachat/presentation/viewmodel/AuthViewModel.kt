package com.antoniowalls.airetinachat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antoniowalls.airetinachat.data.model.Resource
import com.antoniowalls.airetinachat.domain.usecase.auth.*
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val loginWithEmailUseCase: LoginWithEmailUseCase,
    private val registerWithEmailUseCase: RegisterWithEmailUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    // Estado de autenticación
    private val _authState = MutableStateFlow<Resource<FirebaseUser>?>(null)
    val authState: StateFlow<Resource<FirebaseUser>?> = _authState.asStateFlow()

    // Estado del usuario (se inicializa pidiendo el usuario actual al caso de uso)
    private val _currentUser = MutableStateFlow<FirebaseUser?>(getCurrentUserUseCase())
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _resetPasswordState = MutableStateFlow<Resource<Unit>?>(null)
    val resetPasswordState: StateFlow<Resource<Unit>?> = _resetPasswordState.asStateFlow()

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading
            // Usamos el invoke() del caso de uso
            val result = loginWithEmailUseCase(email, pass)
            _authState.value = result

            if (result is Resource.Success) {
                _currentUser.value = result.data
            }
        }
    }

    fun register(name: String, email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading
            val result = registerWithEmailUseCase(name, email, pass)
            _authState.value = result

            if (result is Resource.Success) {
                _currentUser.value = result.data
            }
        }
    }

    fun loginWithGoogle(credential: AuthCredential) {
        viewModelScope.launch {
            _authState.value = Resource.Loading
            val result = loginWithGoogleUseCase(credential)
            _authState.value = result
            if (result is Resource.Success) {
                _currentUser.value = result.data
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _resetPasswordState.value = Resource.Loading
            val result = resetPasswordUseCase(email)
            _resetPasswordState.value = result
        }
    }

    fun clearResetPasswordState() {
        _resetPasswordState.value = null
    }

    fun logout() {
        logoutUseCase()
        _currentUser.value = null
        _authState.value = null
    }

    fun resetAuthState() {
        _authState.value = null
    }
}