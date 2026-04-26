package com.antoniowalls.airetinachat.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.antoniowalls.airetinachat.ui.chat.ChatScreen
import com.antoniowalls.airetinachat.ui.components.BottomNavigationBar
import com.antoniowalls.airetinachat.ui.history.HistoryScreen
import com.antoniowalls.airetinachat.ui.theme.BgDark

@Composable
fun MainScreen(){
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    // Extraemos la ruta real limpiando los parámetros
    val currentRoute = navBackStackEntry?.destination?.route?.substringBefore("?") ?: "chat"

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
            // 1. Ruta del Chat que acepta el ID
            composable(
                route = "chat?chatId={chatId}",
                arguments = listOf(navArgument("chatId") {
                    type = NavType.StringType
                    nullable = true
                })
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId")
                ChatScreen(chatId = chatId)
            }
            composable(route = "history") {
                HistoryScreen(
                    onNavigateToChat = { selectedChatId ->
                        if (selectedChatId != null) {
                            navController.navigate("chat?chatId=$selectedChatId") {
                                launchSingleTop = true
                            }
                        } else {
                            navController.navigate("chat") {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
            composable("insights") {}
            composable("profile") {}
        }

    }
}