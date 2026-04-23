package com.antoniowalls.airetinachat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antoniowalls.airetinachat.ui.theme.AiRetinaChatTheme
import com.antoniowalls.airetinachat.ui.theme.BgDark
import com.antoniowalls.airetinachat.ui.theme.PrimaryPurple
import com.antoniowalls.airetinachat.ui.theme.TextGray

@Preview(showBackground = true)
@Composable
fun BottomNavigationBarPreview() {
    AiRetinaChatTheme {
        BottomNavigationBar(currentRoute = "Chat", onNavigate = {})
    }
}
@Composable
fun BottomNavigationBar(currentRoute: String, onNavigate: (String) -> Unit){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgDark)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(
            icon = Icons.Filled.ChatBubble,
            label = "Chat",
            isSelected = currentRoute == "chat",
            onClick = {onNavigate("Chat")}
        )
        BottomNavItem(
            icon = Icons.Filled.History,
            label = "Historial",
            isSelected = currentRoute == "history",
            onClick = {onNavigate("history")}
        )
        BottomNavItem(
            icon = Icons.Filled.Insights,
            label = "Insights",
            isSelected = currentRoute == "insights",
            onClick = {onNavigate("insights")}
        )
        BottomNavItem(
            icon = Icons.Filled.Person,
            label = "Perfil",
            isSelected = currentRoute == "profile",
            onClick = {onNavigate("profile")}
        )
    }
}

@Composable
fun BottomNavItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val color = if (isSelected) PrimaryPurple else TextGray

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(PrimaryPurple, CircleShape)
            )
        }
    }
}


