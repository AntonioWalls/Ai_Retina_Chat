package com.antoniowalls.airetinachat.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antoniowalls.airetinachat.ui.theme.AiRetinaChatTheme
import com.antoniowalls.airetinachat.ui.theme.FieldDark

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun SocialLoginSectionPreview(){
    AiRetinaChatTheme {
        SocialLoginSection()
    }
}

@Composable
fun SocialLoginSection() {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(color = Color.DarkGray, modifier = Modifier.weight(1f))
            Text(" O CONTINUAR CON ", color = Color.DarkGray, fontSize = 10.sp, letterSpacing = 1.sp, modifier = Modifier.padding(horizontal = 8.dp))
            HorizontalDivider(color = Color.DarkGray, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { /* Todo: Implementar Google SignIn aquí */ },
                modifier = Modifier.weight(1f).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FieldDark),
                shape = RoundedCornerShape(16.dp)
            ) {

                Text("Google", color = Color.White)
            }
            Button(
                onClick = { /* Todo: Implementar Apple SignIn aquí */ },
                modifier = Modifier.weight(1f).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FieldDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Apple", color = Color.White)
            }
        }
    }
}