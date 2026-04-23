package com.antoniowalls.airetinachat.data.model

import androidx.compose.ui.graphics.vector.ImageVector

data class ChatSession(
    val id: String,
    val title: String,
    val preview: String,
    val time: String,
    val category: String,
    val icon: ImageVector,
    val isAlert: Boolean = false
)
