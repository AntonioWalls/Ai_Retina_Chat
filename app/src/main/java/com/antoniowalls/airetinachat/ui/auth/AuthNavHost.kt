package com.antoniowalls.airetinachat.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.antoniowalls.airetinachat.data.model.Resource
import com.antoniowalls.airetinachat.viewmodel.AuthViewModel

@Composable
fun AuthNavHost(viewModel: AuthViewModel, onLoginSuccess: () -> Unit){
    val navController = rememberNavController()
    val authState by viewModel.authState.collectAsState()

    //Si el login/registro es exitoso, se navega a la pantalla principal
    LaunchedEffect(authState) {
        if(authState is Resource.Success){
            onLoginSuccess()
        }
    }

    NavHost(navController = navController, startDestination = "welcome"){
        composable("welcome"){
            WelcomeScreen(
                onNavigateToLogin ={
                    viewModel.resetAuthState()
                    navController.navigate("login")
                },
                onNavigateToSignUp ={
                    viewModel.resetAuthState()
                    navController.navigate("signup")
                }
            )
        }
        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToSignUp = {
                    viewModel.resetAuthState()
                    navController.navigate("signup") {
                        popUpTo("welcome") // Evita un historial infinito de pantallas
                    }
                }
            )
        }
        composable("signup") {
            SignUpScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    viewModel.resetAuthState()
                    navController.navigate("login") {
                        popUpTo("welcome")
                    }
                }
            )
        }
    }
}

