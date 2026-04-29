package com.antoniowalls.airetinachat.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antoniowalls.airetinachat.data.model.Resource
import com.antoniowalls.airetinachat.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isFetching: Boolean = true,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val lastLoginDays: Int = 0,
    val isGoogleSignIn: Boolean = false,
    val showReAuthDialog: Boolean = false,

    // DATOS DEL FORMULARIO CENTRALIZADOS AQUÍ
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val gender: String = "Male",
    val photoUri: Uri? = null,
    val is2FAEnabled: Boolean = false
)

class ProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        calculateLastLogin()
        fetchData()
    }

    private fun calculateLastLogin() {
        val lastSignInMs = authRepository.getLastSignInTimestamp()
        if (lastSignInMs > 0) {
            val diffMs = System.currentTimeMillis() - lastSignInMs
            val days = (diffMs / (1000 * 60 * 60 * 24)).toInt()
            _uiState.value = _uiState.value.copy(lastLoginDays = days)
        }
    }

    private fun fetchData() {
        viewModelScope.launch {
            val extraData = authRepository.getExtraProfileData()
            val user = authRepository.currentUser
            val isGoogle = user?.providerData?.any { it.providerId == "google.com" } == true

            _uiState.value = _uiState.value.copy(
                isFetching = false,
                isGoogleSignIn = isGoogle,
                fullName = user?.displayName ?: "",
                email = user?.email ?: "",
                photoUri = user?.photoUrl,
                phone = extraData?.get("phone") as? String ?: "",
                gender = extraData?.get("gender") as? String ?: "Male",
                is2FAEnabled = extraData?.get("is2FAEnabled") as? Boolean ?: false
            )
        }
    }

    // Funciones para actualizar el estado del formulario en tiempo real
    fun onFullNameChange(name: String) { _uiState.value = _uiState.value.copy(fullName = name) }
    fun onPhoneChange(phone: String) { _uiState.value = _uiState.value.copy(phone = phone) }
    fun onGenderChange(gender: String) { _uiState.value = _uiState.value.copy(gender = gender) }
    fun onPhotoUriChange(uri: Uri?) { _uiState.value = _uiState.value.copy(photoUri = uri) }

    fun toggle2FA(isEnabled: Boolean) {
        viewModelScope.launch {
            // Actualizamos la UI inmediatamente para que se sienta fluido
            _uiState.value = _uiState.value.copy(is2FAEnabled = isEnabled)

            // Guardamos en la nube
            val result = authRepository.update2FAState(isEnabled)
            if (result is Resource.Error) {
                // Si falla la conexión, regresamos el switch a su estado anterior y mostramos error
                _uiState.value = _uiState.value.copy(
                    is2FAEnabled = !isEnabled,
                    errorMessage = "Error al actualizar la seguridad 2FA"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    successMessage = if (isEnabled) "Autenticación en 2 pasos HABILITADA" else "Autenticación en 2 pasos DESHABILITADA"
                )
            }
        }
    }


        fun saveProfileChanges(newPassword: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            val currentState = _uiState.value

            // Guardamos todos los datos normales
            val profileResult = authRepository.updateProfile(
                name = currentState.fullName,
                phone = currentState.phone,
                gender = currentState.gender,
                photoUri = currentState.photoUri
            )

            if (profileResult is Resource.Error) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = profileResult.exception.localizedMessage)
                return@launch
            }

            // Si escribieron una contraseña, intentamos cambiarla
            if (newPassword.isNotBlank() && newPassword.length >= 6 && !currentState.isGoogleSignIn) {
                val passResult = authRepository.changePassword(newPassword)
                if (passResult is Resource.Error) {
                    //Si Firebase rechaza, mostramos el diálogo en lugar de fallar
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showReAuthDialog = true,
                        successMessage = "Perfil actualizado. Por seguridad, verifica tu identidad para cambiar la contraseña."
                    )
                    return@launch
                }
            }

            _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "¡Perfil actualizado exitosamente!")
        }
    }

    fun hideReAuthDialog() {
        _uiState.value = _uiState.value.copy(showReAuthDialog = false)
    }

    fun confirmReAuthAndChangePassword(currentPass: String, newPass: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, showReAuthDialog = false)
            val result = authRepository.reAuthenticateAndChangePassword(currentPass, newPass)

            if (result is Resource.Success) {
                _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "¡Contraseña actualizada exitosamente!")
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Error: Contraseña actual incorrecta.")
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }

    fun logout() {
        authRepository.logout()
    }
}