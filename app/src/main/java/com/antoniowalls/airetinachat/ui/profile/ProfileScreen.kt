package com.antoniowalls.airetinachat.ui.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.antoniowalls.airetinachat.ui.components.GradientButton
import com.antoniowalls.airetinachat.ui.theme.AiRetinaChatTheme
import com.antoniowalls.airetinachat.ui.theme.BgDark
import com.antoniowalls.airetinachat.ui.theme.CardDark
import com.antoniowalls.airetinachat.ui.theme.PrimaryPurple
import com.antoniowalls.airetinachat.ui.theme.TextGray
import com.antoniowalls.airetinachat.viewmodel.ProfileUiState
import com.antoniowalls.airetinachat.viewmodel.ProfileViewModel
import org.koin.androidx.compose.koinViewModel

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewProfileScreen() {
    AiRetinaChatTheme {
        ProfileScreen(onNavigateBack = {}, onLogout = {})
    }
}

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel? = if(LocalInspectionMode.current) null else koinViewModel(),
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val uiState = if (isPreview) ProfileUiState(lastLoginDays = 12, isFetching = false, fullName = "Julian Sterling", email = "j.sterling@obsidian.ai") else viewModel?.uiState?.collectAsState()?.value ?: ProfileUiState()

    // Solo guardamos en 'remember' las contraseñas, por temas de seguridad no van al ViewModel
    var newPassword by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }

    // El launcher actualiza el estado directamente en el ViewModel
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) viewModel?.onPhotoUriChange(uri)
    }

    // Manejo de Toasts
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel?.clearMessages()
        }
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel?.clearMessages()
        }
    }

    // DIÁLOGO DE RE-AUTENTICACIÓN (Para cuando cambian la contraseña)
    if (uiState.showReAuthDialog) {
        AlertDialog(
            onDismissRequest = { viewModel?.hideReAuthDialog() },
            containerColor = CardDark,
            title = { Text("Verifica tu identidad", color = Color.White) },
            text = {
                Column {
                    Text("Por seguridad, Firebase requiere tu contraseña actual para autorizar el cambio.", color = TextGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    ProfileTextField(
                        label = "CONTRASEÑA ACTUAL",
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        icon = Icons.Outlined.Lock,
                        isPassword = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel?.confirmReAuthAndChangePassword(currentPassword, newPassword)
                    newPassword = ""
                    currentPassword = ""
                }) {
                    Text("Confirmar", color = PrimaryPurple)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel?.hideReAuthDialog() }) {
                    Text("Cancelar", color = TextGray)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(rememberScrollState())
    ) {
        //Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Regresar",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Perfil",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                viewModel?.logout(); onLogout()
            }) {
                Icon(Icons.Default.Logout,
                    contentDescription = "Cerrar sesión",
                    tint = Color.Red.copy(alpha = 0.8f)
                )
            }
        }

        // Mostrar indicador de carga mientras trae los datos de Firestore
        if (uiState.isFetching) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryPurple)
            }
        } else {
            // Sección del Avatar
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.padding(top = 16.dp)) {
                    Box(
                        modifier = Modifier.size(120.dp).clip(CircleShape).background(CardDark),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.photoUri != null) {
                            AsyncImage(
                                model = uiState.photoUri,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else{
                            Icon(imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = TextGray,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }
                    //Boton editar foto
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .offset(x = (-4).dp, y= (-4).dp)
                            .clip(CircleShape)
                            .background(PrimaryPurple)
                            .clickable{galleryLauncher.launch("image/*")},
                        contentAlignment = Alignment.Center
                    ){
                        Icon(Icons.Default.Edit,
                            contentDescription = "Editar foto",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = uiState.fullName,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold)
                Text(text = "Especialista en Retina AI", color = TextGray, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            // Sección del formulario
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                ProfileTextField(
                    label = "NOMBRE COMPLETO",
                    value = uiState.fullName,
                    onValueChange = { viewModel?.onFullNameChange(it) },
                    icon = Icons.Outlined.Person
                )
                Spacer(modifier = Modifier.height(24.dp))

                //Género
                Text(
                    text = "GÉNERO",
                    color = PrimaryPurple,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GenderChip("Femenino", uiState.gender == "Female") { viewModel?.onGenderChange("Female") }
                    GenderChip("Masculino", uiState.gender == "Male") { viewModel?.onGenderChange("Male") }
                    GenderChip("Otro", uiState.gender == "Other") { viewModel?.onGenderChange("Other") }
                }
                Spacer(modifier = Modifier.height(24.dp))

                //Correo eléctronico
                ProfileTextField(
                    label = "CORREO ELECTRÓNICO",
                    value = uiState.email,
                    onValueChange = {},
                    icon = Icons.Outlined.Email,
                    enabled = false
                )
                Spacer(modifier = Modifier.height(24.dp))

                //Teléfono
                ProfileTextField(
                    label = "TELÉFONO",
                    value = uiState.phone,
                    onValueChange = { viewModel?.onPhoneChange(it) },
                    icon = Icons.Outlined.Phone,
                    keyboardType = KeyboardType.Phone
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Contraseña (Oculta si ingresó con Google)
                if (!uiState.isGoogleSignIn) {
                    ProfileTextField(
                        label = "NUEVA CONTRASEÑA (Opcional)",
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        icon = Icons.Outlined.Lock,
                        isPassword = true,
                        placeholder = "Escribe para cambiarla"
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Botón de Guardar
                GradientButton(
                    text = if (uiState.isLoading) "Guardando..." else "Guardar Cambios",
                    isLoading = uiState.isLoading
                ) {
                    if (!isPreview) {
                        viewModel?.saveProfileChanges(newPassword)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Seguridad de la cuenta",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Administra ajustes de seguridad y tokens",
                                    color = TextGray,
                                    fontSize = 12.sp
                                )
                            }
                            Icon(Icons.Outlined.VerifiedUser,
                                contentDescription = null,
                                tint = PrimaryPurple
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Divider(color = Color.DarkGray.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Autenticación en 2 pasos",
                                color = Color.White,
                                fontSize = 14.sp
                            )

                            // NUEVO: Switch Interactivo
                            Switch(
                                checked = uiState.is2FAEnabled,
                                onCheckedChange = { viewModel?.toggle2FA(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PrimaryPurple,
                                    uncheckedThumbColor = TextGray,
                                    uncheckedTrackColor = CardDark,
                                    uncheckedBorderColor = Color.DarkGray
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Divider(color = Color.DarkGray.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "último inicio de sesión",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(text = "hace ${uiState.lastLoginDays} días",
                                color = TextGray,
                                fontSize = 12.sp)
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun RowScope.GenderChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val containerColor = if (isSelected) PrimaryPurple.copy(alpha = 0.15f) else CardDark
    val textColor = if (isSelected) PrimaryPurple else TextGray
    val borderColor = if (isSelected) PrimaryPurple.copy(alpha = 0.5f) else Color.Transparent

    Box(
        modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean = true,
    isPassword: Boolean = false,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = PrimaryPurple, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = enabled,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CardDark,
                unfocusedContainerColor = CardDark,
                disabledContainerColor = CardDark.copy(alpha = 0.5f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                disabledTextColor = TextGray,
                cursorColor = PrimaryPurple
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            placeholder = { Text(placeholder, color = Color.DarkGray) },
            trailingIcon = { Icon(icon, contentDescription = null, tint = TextGray) }
        )
    }
}