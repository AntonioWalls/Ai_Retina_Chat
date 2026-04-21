package com.antoniowalls.airetinachat.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antoniowalls.airetinachat.ui.theme.AiRetinaChatTheme
import com.antoniowalls.airetinachat.ui.theme.FieldDark
import com.antoniowalls.airetinachat.ui.theme.PrimaryPurple
import com.antoniowalls.airetinachat.ui.theme.TextGray

@Preview(showBackground = true, backgroundColor = 0xFF000000) // Le puse fondo negro porque tu diseño es oscuro
@Composable
fun CustomTextFieldPreview() {
    AiRetinaChatTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            // Ejemplo 1: Email normal
            CustomTextField(
                label = "CORREO ELECTRÓNICO",
                value = "",
                onvalueChange = {},
                placeholder = "tu@correo.com"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Ejemplo 2: Password con texto a la derecha
            CustomTextField(
                label = "CONTRASEÑA",
                value = "123456",
                onvalueChange = {},
                isPassword = true,
                trailingText = "¿OLVIDASTE TU CONTRASEÑA?"
            )

        }
    }
}

@Composable
fun CustomTextField(
    label: String,
    value: String,
    onvalueChange: (String) -> Unit,
    placeholder: String = "",
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: () -> Unit = {},
    trailingText: String? = null,
    modifier: Modifier = Modifier) {

    Column(modifier = modifier.fillMaxWidth()){
        Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = label,
                color = TextGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            if(trailingText!=null){
                Text(
                    text = trailingText,
                    color = TextGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = value,
            onValueChange = onvalueChange,
            modifier = modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = FieldDark,
                unfocusedContainerColor = FieldDark,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = PrimaryPurple
            ),
            shape = RoundedCornerShape(16.dp),
            placeholder ={Text(placeholder, color = Color.DarkGray)},
            singleLine = true,
            visualTransformation = if(isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if(isPassword) KeyboardType.Password else KeyboardType.Email)
        )
    }
}