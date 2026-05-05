package com.antoniowalls.airetinachat.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antoniowalls.airetinachat.R
import com.antoniowalls.airetinachat.ui.components.DarkButton
import com.antoniowalls.airetinachat.ui.components.GradientButton
import com.antoniowalls.airetinachat.ui.theme.AiRetinaChatTheme
import com.antoniowalls.airetinachat.ui.theme.BgDark
import com.antoniowalls.airetinachat.ui.theme.CardDark
import com.antoniowalls.airetinachat.ui.theme.TextGray

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun WelcomeScreenPreview(){
    AiRetinaChatTheme {
        WelcomeScreen({},{})
    }
}


@Composable
fun WelcomeScreen(onNavigateToLogin: () -> Unit, onNavigateToSignUp: () -> Unit){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E2030), BgDark)
                    )
                ),
            contentAlignment = Alignment.Center
        ){
            Image(painterResource(R.drawable.ic_background), contentDescription = null)
        }

        //Tarjeta inferior
        // Tarjeta inferior
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(CardDark)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.DarkGray)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Bienvenido a Retina AI",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Experimenta el futuro del diagnóstico médico con nuestra arquitectura neuronal premium.",
                color = TextGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            GradientButton(text = "Crear Cuenta", onClick = onNavigateToSignUp)
            Spacer(modifier = Modifier.height(16.dp))
            DarkButton(text = "Iniciar Sesión", onClick = onNavigateToLogin)

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "MOTOR NEURONAL V4.0  •  TOTALMENTE ENCRIPTADO",
                color = Color.DarkGray,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}