package com.antoniowalls.airetinachat.ui.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.antoniowalls.airetinachat.ui.components.BottomNavigationBar
import com.antoniowalls.airetinachat.ui.theme.*
import com.antoniowalls.airetinachat.viewmodel.ChatMessage
import com.antoniowalls.airetinachat.viewmodel.ChatViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ChatScreenPreview() {
    AiRetinaChatTheme {
        ChatScreen()
    }
}


@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel(), chatId: String? = null) {
    // Observamos los estados del ViewModel
        val messages by viewModel.messages.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        val chatTitle by viewModel.chatTitle.collectAsState()

    //efecto para cargar o resetear el chat
    LaunchedEffect(chatId) {
        if (chatId != null) {
            viewModel.loadChat(chatId)
        } else {
            viewModel.resetChat()
        }
    }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgDark)
        ) {
            ChatTopBar(title = chatTitle)

            // Área de mensajes o Estado Vacío
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (messages.isEmpty()) {
                    ChatEmptyState()
                } else {
                    // Lista de mensajes (el chat en sí)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        reverseLayout = false // Los mensajes nuevos van abajo
                    ) {
                        items(messages) { message ->
                            ChatMessageBubble(message)
                        }
                        if (isLoading) {
                            item {
                                LoadingBubble()
                            }
                        }
                    }
                }
            }
            val context = LocalContext.current
            ChatInputBar(
                isLoading = isLoading,
                onSendMessage = { text, imageUri ->
                    viewModel.sendMessage(context, text, imageUri)
                }
            )

            // Texto de "Powered By"
            Text(
                text = "POWERED BY RETINA AI NEURAL ARCHITECTURE",
                color = Color.DarkGray,
                fontSize = 9.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )
        }
}

@Composable
fun ChatMessageBubble(message: ChatMessage) {
    val isUser = message.isFromUser

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = if (isUser) PrimaryPurple else CardDark,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .padding(12.dp)
        ) {
            // Si el mensaje tiene una imagen adjunta, la mostramos
            if (message.imageUri != null) {
                AsyncImage(
                    model = message.imageUri,
                    contentDescription = "Imagen enviada",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .padding(bottom = 8.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Text(
                text = message.text,
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun LoadingBubble() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = CardDark,
                    shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
                )
                .padding(16.dp)
        ) {
            CircularProgressIndicator(
                color = PrimaryPurple,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        }
    }
}

@Composable
fun ChatTopBar(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar circular placeholder
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(CardDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Person, contentDescription = "Perfil", tint = TextGray, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            color = PrimaryPurple,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = { /* TODO: Ajustes */ }) {
            Icon(Icons.Default.Settings, contentDescription = "Ajustes", tint = TextGray)
        }
    }
}

@Composable
fun ChatEmptyState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 32.dp)
    ) {
        // Icono Central (Ojo / Retina)
        Icon(
            imageVector = Icons.Outlined.Visibility,
            contentDescription = "Ojo",
            tint = PrimaryPurple.copy(alpha = 0.8f),
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Hola, ¿cómo puedo ayudar a tus pacientes hoy?",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Inicia un análisis de retinografía o hazme cualquier pregunta oftalmológica.",
            color = TextGray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Chips de sugerencias
        SuggestionChip(text = "Analizar retinografía")
        Spacer(modifier = Modifier.height(12.dp))
        SuggestionChip(text = "Detección temprana de glaucoma")
        Spacer(modifier = Modifier.height(12.dp))
        SuggestionChip(text = "Revisar historial de análisis")
    }
}

@Composable
fun SuggestionChip(text: String) {
    Box(
        modifier = Modifier
            .border(1.dp, Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .background(CardDark.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = text,
            color = Color.LightGray,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ChatInputBar(
    isLoading: Boolean,
    onSendMessage: (String, Uri?) -> Unit
) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Lanzador para abrir la galería del teléfono
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Column {
        // Vista previa de la imagen seleccionada ANTES de enviarla
        if (selectedImageUri != null) {
            Box(modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Vista previa",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                // Botón para quitar la imagen seleccionada
                IconButton(
                    onClick = { selectedImageUri = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .padding(2.dp)
                ) {
                    Icon(Icons.Default.Close, "Quitar", tint = Color.White)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .background(CardDark, RoundedCornerShape(32.dp))
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. ICONO PARA ADJUNTAR FOTO
            IconButton(
                onClick = { galleryLauncher.launch("image/*") },
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = "Adjuntar Foto",
                    tint = if (isLoading) Color.DarkGray else TextGray
                )
            }

            // 2. CAMPO DE TEXTO
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                cursorBrush = SolidColor(PrimaryPurple),
                enabled = !isLoading,
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text(
                            text = if (selectedImageUri != null) "Añade un comentario..." else "Pregúntame algo...",
                            color = Color.DarkGray,
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }
            )

            // 3. BOTÓN DE ENVIAR
            IconButton(
                onClick = {
                    // Solo enviamos si hay texto o una imagen
                    if (text.isNotBlank() || selectedImageUri != null) {
                        onSendMessage(text, selectedImageUri)
                        text = "" // Limpiamos el texto después de enviar
                        selectedImageUri = null // Limpiamos la imagen
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(if (isLoading) Color.DarkGray else PrimaryPurple, CircleShape),
                enabled = !isLoading && (text.isNotBlank() || selectedImageUri != null)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Enviar",
                    tint = Color.White
                )
            }
        }
    }
}