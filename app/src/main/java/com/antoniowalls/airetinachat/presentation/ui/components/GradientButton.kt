package com.antoniowalls.airetinachat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antoniowalls.airetinachat.ui.theme.AiRetinaChatTheme
import com.antoniowalls.airetinachat.ui.theme.GradientEnd
import com.antoniowalls.airetinachat.ui.theme.GradientStart

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GradientButtonPreview(){
    AiRetinaChatTheme {
        // Aquí le pones el padding que quieras ver en el Preview
        GradientButton(
            text = "Botonson",
            modifier = Modifier.padding(top = 64.dp, start = 16.dp, end = 16.dp),
            onClick = {}
        )
    }
}

@Composable
fun GradientButton(
    text: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    onClick: () -> Unit){

    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),
        enabled = !isLoading
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(listOf(GradientStart, GradientEnd)),
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ){
            Text(text = text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}