package com.antoniowalls.airetinachat.ui.auth

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.antoniowalls.airetinachat.R
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
import androidx.compose.ui.platform.LocalContext
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewSignUpScreen() {
    AiRetinaChatTheme {
        SignUpScreen(
            viewModel = null,
            onBack = {},
            onNavigateToLogin = {}
        )
    }
}

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel? = null,
    onBack: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    //detectamos si estamos en el preview de Android Studio
    val isPreview = LocalInspectionMode.current
    val context = LocalContext.current
    //estado de atenticación simulado para el preview o real del viewmodel
    val authState = if (isPreview) {
        Resource.Success(Unit)
    } else {
        viewModel?.authState?.collectAsState()?.value
    }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(if (isPreview) "" else context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { idToken ->
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                if (!isPreview) viewModel?.loginWithGoogle(credential)
            }
        } catch (e: ApiException) {
            Toast.makeText(context, "Error en Google Sign In: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            //Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextGray)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "RETINA AI",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.weight(1.5f))
            }
            Spacer(modifier = Modifier.weight(1f))

            //Formulario
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(CardDark)
                    .padding(32.dp)
            ) {
                //Handle del BottomSheet
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.DarkGray)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Crear Cuenta",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "únete al futuro de la inteligencia hoy.",
                    color = TextGray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                CustomTextField(
                    label = "NOMBRE COMPLETO",
                    value = fullName,
                    onvalueChange = { fullName = it },
                    placeholder = "Juan Pérez"
                )
                Spacer(modifier = Modifier.height(16.dp))

                CustomTextField(
                    label = "CORREO ELECTRÓNICO",
                    value = email,
                    onvalueChange = { email = it },
                    placeholder = "nombre@dominio.com"
                )
                Spacer(modifier = Modifier.height(16.dp))

                CustomTextField(
                    label = "CONTRASEÑA",
                    value = password,
                    onvalueChange = { password = it },
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onTogglePassword = { passwordVisible = !passwordVisible }
                )

                Spacer(modifier = Modifier.height(32.dp))

                val isLoading = authState is Resource.Loading

                GradientButton(
                    text = if (isLoading) "Creando..." else "Registrate",
                    isLoading = isLoading
                ) {
                    if (!isPreview) {
                        viewModel?.register(fullName, email, password)
                    }
                }

                if (authState is Resource.Error) {
                    Text(
                        text = authState.exception.localizedMessage ?: "Error al registrar",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                SocialLoginSection(
                    onGoogleClick = {
                        googleAuthLauncher.launch(googleSignInClient.signInIntent)
                    },
                    onAppleClick = {
                        Toast.makeText(context, "Apple Sign-In requiere cuenta de desarrollador ($99/año). No disponible en esta demo.", Toast.LENGTH_LONG).show()
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("¿Ya tienes una cuenta? ", color = TextGray)
                    Text(
                        text = "Iniciar Sesión",
                        color = PrimaryPurple,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                }
            }
        }
    }
}
