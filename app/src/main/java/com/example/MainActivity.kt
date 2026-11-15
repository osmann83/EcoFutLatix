package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.EcoScoreRepository
import com.example.data.repository.StudentProfileRepository
import com.example.ui.auth.AuthState
import com.example.ui.auth.AuthViewModel
import com.example.ui.screens.MainAppScaffold
import com.example.ui.screens.RegistrationScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.EcoFutLatixTheme
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Actividad Principal de EcoFutLatix.
 * Inicializa edge-to-edge, CredentialManager para Google Sign-In, repositorios de Firestore
 * y gestiona el flujo de autenticación, registro y navegación gamificada para estudiantes de 12 a 17 años.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EcoFutLatixTheme {
                EcoFutLatixApp()
            }
        }
    }
}

@Composable
fun EcoFutLatixApp() {
    val context = LocalContext.current
    val credentialManager = remember { CredentialManager.create(context) }

    // Resuelve el ID de base de datos aprovisionado en Firebase
    val databaseId = stringResource(R.string.firestore_database_id)
    val db = remember(databaseId) { FirebaseFirestore.getInstance(databaseId) }

    val profileRepository = remember(db) { StudentProfileRepository(db) }
    val scoreRepository = remember(db) { EcoScoreRepository(db) }

    val authViewModel: AuthViewModel = viewModel {
        AuthViewModel(profileRepository)
    }

    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    // Intento de auto-login silencioso al arrancar
    LaunchedEffect(Unit) {
        authViewModel.attemptAutoSignIn(context, credentialManager)
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (val state = authState) {
                is AuthState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                is AuthState.Unauthenticated -> {
                    WelcomeScreen(
                        isLoading = false,
                        onSignInClick = {
                            authViewModel.signInWithGoogle(context, credentialManager)
                        }
                    )
                }

                is AuthState.NeedsRegistration -> {
                    RegistrationScreen(
                        initialNickname = state.googleName,
                        onRegister = { nickname, school, year, month, day ->
                            authViewModel.registerStudentProfile(
                                nickname = nickname,
                                school = school,
                                birthYear = year,
                                birthMonth = month,
                                birthDay = day,
                                onSuccess = {
                                    Toast.makeText(
                                        context,
                                        "¡Bienvenido a EcoFutLatix, $nickname!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onError = { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    )
                }

                is AuthState.Authenticated -> {
                    MainAppScaffold(
                        userProfile = state.profile,
                        scoreRepository = scoreRepository,
                        profileRepository = profileRepository,
                        onSignOut = {
                            authViewModel.signOut(context, credentialManager)
                        }
                    )
                }

                is AuthState.Error -> {
                    LaunchedEffect(state) {
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    }
                    WelcomeScreen(
                        isLoading = false,
                        onSignInClick = {
                            authViewModel.signInWithGoogle(context, credentialManager)
                        }
                    )
                }
            }
        }
    }
}
