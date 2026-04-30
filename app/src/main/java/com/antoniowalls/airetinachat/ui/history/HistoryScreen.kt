package com.antoniowalls.airetinachat.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.antoniowalls.airetinachat.data.model.ChatSession
import com.antoniowalls.airetinachat.ui.theme.*
import com.antoniowalls.airetinachat.viewmodel.HistoryUiState
import com.antoniowalls.airetinachat.viewmodel.HistoryViewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.compose.koinViewModel

@Composable
fun HistoryScreen(
    // Igual que en ChatScreen: ViewModel nulo si es Preview
    viewModel: HistoryViewModel? = if (LocalInspectionMode.current) null else koinViewModel(),
    onNavigateToChat: (String?) -> Unit
) {
    val isPreview = LocalInspectionMode.current

    // Extraemos el estado de forma segura (con datos de ejemplo para el Preview)
    val uiState = if (isPreview) {
        HistoryUiState(
            groupedHistory = mapOf(
                "Hoy" to listOf(
                    ChatSession("1", "Análisis Retina", "Ojo sano con bordes definidos.", "14:20", "Hoy", Icons.Outlined.Psychology, true)
                )
            )
        )
    } else {
        viewModel?.uiState?.collectAsState()?.value ?: HistoryUiState()
    }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        Column(modifier = Modifier.fillMaxSize()) {
            HistoryTopBar()

            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { if (!isPreview) viewModel?.updateSearchQuery(it) }
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            } else if (uiState.errorMessage != null) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.errorMessage, color = Color.Red, textAlign = TextAlign.Center, modifier = Modifier.padding(24.dp))
                }
            } else if (uiState.groupedHistory.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(text = "No hay chats guardados aún.", color = TextGray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    uiState.groupedHistory.forEach { (category, sessions) ->
                        item {
                            Text(
                                text = category.uppercase(),
                                color = TextGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                            )
                        }
                        items(sessions) { session ->
                            HistoryCard(session = session, onClick = { onNavigateToChat(session.id) })
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { onNavigateToChat(null) },
            containerColor = PrimaryPurple,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 24.dp, end = 24.dp).size(60.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nuevo Chat", modifier = Modifier.size(32.dp))
        }
    }
}

// Componentes internos (se mantienen igual para orden)
@Composable
fun HistoryTopBar() {
    val user = if (LocalInspectionMode.current) null else FirebaseAuth.getInstance().currentUser

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Historial", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(CardDark),
            contentAlignment = Alignment.Center
        ) {
            if (user?.photoUrl != null) {
                AsyncImage(
                    model = user.photoUrl,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Outlined.Person, contentDescription = "Perfil", tint = TextGray, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(56.dp),
        placeholder = { Text("Buscar conversaciones...", color = TextGray, fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = TextGray) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = CardDark,
            unfocusedContainerColor = CardDark,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = PrimaryPurple
        ),
        shape = RoundedCornerShape(16.dp),
        singleLine = true
    )
}

@Composable
fun HistoryCard(session: ChatSession, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(CardDark, RoundedCornerShape(20.dp)).clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(if (session.isAlert) PrimaryPurple.copy(alpha = 0.15f) else FieldDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = session.icon, contentDescription = null, tint = if (session.isAlert) PrimaryPurple else Color(0xFF6B7280))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(session.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text(session.time, color = TextGray, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(session.preview, color = TextGray, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 20.sp)
        }
    }
}

// PREVIEW LIMPIO (Ahora llama a HistoryScreen directamente)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewHistoryScreen() {
    AiRetinaChatTheme {
        HistoryScreen(onNavigateToChat = {})
    }
}