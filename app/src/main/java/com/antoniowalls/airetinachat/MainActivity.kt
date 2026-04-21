package com.antoniowalls.airetinachat

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antoniowalls.airetinachat.ui.auth.AuthNavHost
import com.antoniowalls.airetinachat.ui.components.GradientButton
import com.antoniowalls.airetinachat.ui.theme.AiRetinaChatTheme
import com.antoniowalls.airetinachat.ui.theme.BgDark
import com.antoniowalls.airetinachat.viewmodel.AuthViewModel
import com.antoniowalls.airetinachat.viewmodel.AuthViewModelFactory

class MainActivity : ComponentActivity() {

    // Instanciamos el ViewModel usando el Factory que ya creaste
    private val authViewModel: AuthViewModel by viewModels { AuthViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AiRetinaChatTheme {
                // Observamos al usuario actual para decidir qué pantalla mostrar
                val currentUser by authViewModel.currentUser.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = BgDark
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        if (currentUser != null) {
                            // PANTALLA TEMPORAL DE ÉXITO
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(BgDark)
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "¡BIENVENIDO A LA EVOLUCIÓN!",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))

                                // ¡Comprobamos que guardó el Nombre y el Correo!
                                Text(
                                    text = "Dr. ${currentUser?.displayName ?: "Usuario"}",
                                    color = Color(0xFFA87FFB), // PrimaryPurple
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${currentUser?.email}",
                                    color = Color.LightGray,
                                    fontSize = 16.sp
                                )

                                Spacer(modifier = Modifier.height(48.dp))

                                // Botón para desloguearse y seguir haciendo pruebas
                                GradientButton(text = "Cerrar Sesión") {
                                    authViewModel.logout()
                                }
                            }
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