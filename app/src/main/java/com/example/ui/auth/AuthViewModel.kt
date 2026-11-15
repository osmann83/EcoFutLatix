package com.example.ui.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.model.UserProfile
import com.example.data.repository.StudentProfileRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption

import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar

/**
 * Estados de la sesión del estudiante en la aplicación.
 */
sealed interface AuthState {
    data object Loading : AuthState
    data object Unauthenticated : AuthState
    data class NeedsRegistration(val googleName: String) : AuthState
    data class Authenticated(val profile: UserProfile) : AuthState
    data class Error(val message: String) : AuthState
}

/**
 * ViewModel encargado del flujo de autenticación mediante CredentialManager y Google Sign-In,
 * así como de la validación estricta de edad (12 a 17 años) para EcoFutLatix.
 */
class AuthViewModel(
    private val profileRepository: StudentProfileRepository
) : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkCurrentSession()
    }

    /**
     * Verifica si existe una sesión activa al iniciar la aplicación.
     */
    fun checkCurrentSession() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            loadUserProfile(currentUser.uid, currentUser.displayName ?: "Estudiante")
        }
    }

    private fun loadUserProfile(uid: String, fallbackName: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            profileRepository.getProfile(uid).fold(
                onSuccess = { profile ->
                    if (profile != null && profile.nickname.isNotBlank() && profile.school.isNotBlank()) {
                        _authState.value = AuthState.Authenticated(profile)
                    } else {
                        _authState.value = AuthState.NeedsRegistration(fallbackName)
                    }
                },
                onFailure = { error ->
                    Log.e("AuthViewModel", "Error al cargar perfil de usuario: ${error.message}", error)
                    _authState.value = AuthState.NeedsRegistration(fallbackName)
                }
            )
        }
    }

    /**
     * Intento silencioso de inicio de sesión con Google.
     */
    fun attemptAutoSignIn(context: Context, credentialManager: CredentialManager) {
        if (auth.currentUser != null) {
            checkCurrentSession()
            return
        }

        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            _authState.value = AuthState.Unauthenticated
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

        viewModelScope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        loadUserProfile(user.uid, user.displayName ?: "Estudiante")
                    } else {
                        _authState.value = AuthState.Unauthenticated
                    }
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                // Si falla el auto-login silencioso, se muestra la pantalla de inicio para login interactivo
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    /**
     * Inicio de sesión interactivo al pulsar "Ingresar con Google".
     */
    fun signInWithGoogle(context: Context, credentialManager: CredentialManager) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Configuración de Google Sign-In faltante: default_web_client_id no encontrado.")
            return
        }

        val signInOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = credentialManager.getCredential(context as Activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        loadUserProfile(user.uid, user.displayName ?: "Estudiante")
                    } else {
                        _authState.value = AuthState.Error("No se pudo obtener el usuario autenticado.")
                    }
                } else {
                    _authState.value = AuthState.Error("Tipo de credencial no esperado.")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("AuthViewModel", "Inicio de sesión cancelado por el usuario: ${e.message}", e)
                _authState.value = AuthState.Unauthenticated
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Fallo al iniciar sesión con Google", e)
                _authState.value = AuthState.Error(e.localizedMessage ?: "Error al iniciar sesión con Google.")
            }
        }
    }

    /**
     * Valida y guarda el registro del perfil del estudiante verificando que su edad
     * se encuentre estrictamente entre los 12 y 17 años de edad.
     */
    fun registerStudentProfile(
        nickname: String,
        school: String,
        birthYear: Int,
        birthMonth: Int, // 1-12
        birthDay: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val trimmedNick = nickname.trim()
        if (trimmedNick.length < 3) {
            onError("El nickname debe tener al menos 3 caracteres.")
            return
        }
        if (school.isBlank()) {
            onError("Por favor selecciona tu institución educativa pública.")
            return
        }

        // Validación estricta de edad: 12 a 17 años
        val today = Calendar.getInstance()
        var age = today.get(Calendar.YEAR) - birthYear
        if (today.get(Calendar.MONTH) + 1 < birthMonth ||
            (today.get(Calendar.MONTH) + 1 == birthMonth && today.get(Calendar.DAY_OF_MONTH) < birthDay)
        ) {
            age--
        }

        if (age !in 12..17) {
            onError("¡¡Fecha fuera de límites!! La aplicación está dirigida exclusivamente a estudiantes de 12 a 17 años (Edad calculada: $age años).")
            return
        }

        val birthDateFormatted = "%04d-%02d-%02d".format(birthYear, birthMonth, birthDay)

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            profileRepository.saveProfile(trimmedNick, school, birthDateFormatted).fold(
                onSuccess = {
                    val uid = auth.currentUser?.uid ?: ""
                    val updatedProfile = UserProfile(
                        userId = uid,
                        nickname = trimmedNick,
                        school = school,
                        birthDate = birthDateFormatted,
                        totalScore = 0L
                    )
                    _authState.value = AuthState.Authenticated(updatedProfile)
                    onSuccess()
                },
                onFailure = { error ->
                    _authState.value = AuthState.NeedsRegistration(trimmedNick)
                    onError(error.localizedMessage ?: "Error al registrar el perfil.")
                }
            )
        }
    }

    /**
     * Cierra la sesión activa en Firebase y limpia CredentialManager.
     */
    fun signOut(context: Context, credentialManager: CredentialManager) {
        auth.signOut()
        viewModelScope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error al limpiar credenciales: ${e.message}", e)
            } finally {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }
}
