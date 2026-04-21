package com.antoniowalls.airetinachat.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antoniowalls.airetinachat.data.model.Resource
import com.antoniowalls.airetinachat.ui.components.CustomTextField
import com.antoniowalls.airetinachat.ui.components.GradientButton
import com.antoniowalls.airetinachat.ui.components.SocialLoginSection
import com.antoniowalls.airetinachat.ui.theme.*
import com.antoniowalls.airetinachat.viewmodel.AuthViewModel

/**
 * OPCIÓN SENCILLA: Una sola función para todo.
 * Si 'viewModel' es null, significa que estamos en el Preview.
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel? = null,
    onBack: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    // 1. Detectamos si estamos en el editor de Android Studio (Preview)
    val isPreview = LocalInspectionMode.current

    // 2. Si es preview, usamos un estado falso. Si no, escuchamos al ViewModel.
    val authState = if (isPreview) {
        Resource.Success(Unit)
    } else {
        viewModel?.authState?.collectAsState()?.value
    }

    // Estados locales de la UI (estos no rompen el preview)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "cerrar", tint = TextGray)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "RETINA AI",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            // Body
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(CardDark)
                    .padding(32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(30.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.DarkGray)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Bienvenido de nuevo",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Accede a tu espacio neuronal y continúa la evolución",
                    color = TextGray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                CustomTextField(
                    label = "Correo Electrónico",
                    value = email,
                    onvalueChange = { email = it },
                    placeholder = "nombre@dominio.com"
                )

                Spacer(modifier = Modifier.height(16.dp))

                CustomTextField(
                    label = "Contraseña",
                    value = password,
                    onvalueChange = { password = it },
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onTogglePassword = { passwordVisible = !passwordVisible },
                    trailingText = "Olvidé mi contraseña"
                )

                Spacer(modifier = Modifier.height(32.dp))

                val isLoading = authState is Resource.Loading

                GradientButton(
                    text = if (isLoading) "Cargando..." else "Iniciar Sesión",
                    isLoading = isLoading
                ) {
                    // Solo llamamos al login si no estamos en preview y el viewModel existe
                    if (!isPreview) {
                        viewModel?.login(email, password)
                    }
                }

                if (authState is Resource.Error) {
                    Text(
                        text = authState.exception.localizedMessage ?: "Error",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                SocialLoginSection()
                Spacer(modifier = Modifier.height(32.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text("¿No tienes una cuenta? ", color = TextGray)
                    Text(
                        text = "Regístrate",
                        color = PrimaryPurple,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToSignUp() }
                    )
                }
            }
        }
    }
}

/**
 * PREVIEW: Ahora es súper simple.
 * Pasamos 'null' en el ViewModel y la función se encarga del resto.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLoginScreen() {
    AiRetinaChatTheme {
        LoginScreen(
            viewModel = null,
            onBack = {},
            onNavigateToSignUp = {}
        )
    }
}