package com.antoniowalls.airetinachat.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.antoniowalls.airetinachat.ui.chat.ChatScreen
import com.antoniowalls.airetinachat.ui.components.BottomNavigationBar
import com.antoniowalls.airetinachat.ui.history.HistoryScreen
import com.antoniowalls.airetinachat.ui.theme.BgDark

@Composable
fun MainScreen(){
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "chat"

    Scaffold(
        containerColor = BgDark,
        bottomBar = {
            BottomNavigationBar(
              currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) {saveState = true}
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "chat",
            modifier = Modifier.padding(innerPadding)
        ){
            composable("chat") { ChatScreen() }
            composable(route = "history") {
                HistoryScreen(
                    onNavigateToChat = {
                        navController.navigate("chat") {
                            launchSingleTop = true // Evita abrir múltiples chats uno sobre otro
                        }
                    }
                )
            }
            composable("insights") {}
            composable("profile") {}
        }

    }
}