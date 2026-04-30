package com.antoniowalls.airetinachat

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.antoniowalls.airetinachat.ui.auth.AuthNavHost
import com.antoniowalls.airetinachat.ui.navigation.MainScreen
import com.antoniowalls.airetinachat.ui.theme.AiRetinaChatTheme
import com.antoniowalls.airetinachat.ui.theme.BgDark
import com.antoniowalls.airetinachat.viewmodel.AuthViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AiRetinaChatTheme {
                // Koin busca automáticamente las dependencias y nos entrega el ViewModel listo para usarse
                val authViewModel: AuthViewModel = koinViewModel()

                // Observamos al usuario actual para decidir qué pantalla mostrar
                val currentUser by authViewModel.currentUser.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = BgDark
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        if (currentUser != null) {
                            // Si el usuario ya inició sesión correctamente, lo mandamos directo al Chat
                            MainScreen()
                        } else {
                            // Si no hay usuario, mostramos el flujo de Bienvenida/Login/Registro
                            AuthNavHost(
                                viewModel = authViewModel,
                                onLoginSuccess = {
                                    Toast.makeText(this@MainActivity, "¡Ingreso exitoso!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}