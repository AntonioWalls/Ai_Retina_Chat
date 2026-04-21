package com.antoniowalls.airetinachat.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antoniowalls.airetinachat.ui.theme.AiRetinaChatTheme
import com.antoniowalls.airetinachat.ui.theme.FieldDark

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun DarkButtonPreview(){
    AiRetinaChatTheme {
        DarkButton(text = "Dark Button", onClick = {}, modifier = Modifier.padding(top = 36.dp, start = 16.dp, end = 16.dp))
    }
}


@Composable
fun DarkButton(text: String,
               modifier: Modifier = Modifier,
               onClick: () -> Unit){

    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = FieldDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(text = text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}