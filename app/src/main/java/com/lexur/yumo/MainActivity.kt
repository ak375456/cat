package com.lexur.yumo

import com.lexur.yumo.character_screen.presentation.CharacterSettingsScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lexur.yumo.custom_character.presentation.CustomCharacterCreationScreen
import com.lexur.yumo.navigation.Screen
import com.lexur.yumo.navigation.SettingsScreen
import com.lexur.yumo.home_screen.presentation.HomeScreen
import com.lexur.yumo.ui.theme.CatTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContent {

            CatTheme {
                NavigationGraph()
            }
        }
    }
}

@Composable
fun NavigationGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToCharacterSettings = { characterId ->
                    navController.navigate("character_settings/$characterId")
                },
                onNavigateToCustomCharacterCreation = {
                    navController.navigate(Screen.CustomCharacterCreation.route)
                },
                navController = navController
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "character_settings/{characterId}",
            arguments = listOf(navArgument("characterId") { type = NavType.StringType })
        ) { backStackEntry ->
            val characterId = backStackEntry.arguments?.getString("characterId") ?: ""
            CharacterSettingsScreen(
                characterId = characterId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CustomCharacterCreation.route) {
            CustomCharacterCreationScreen(
                navController = navController,
            )
        }
    }
}