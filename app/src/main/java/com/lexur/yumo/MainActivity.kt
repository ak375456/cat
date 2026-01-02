package com.lexur.yumo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lexur.yumo.ads.AdMobManager
import com.lexur.yumo.character_screen.presentation.CharacterSettingsScreen
import com.lexur.yumo.custom_character.presentation.CustomCharacterCreationScreen
import com.lexur.yumo.home_screen.presentation.HomeScreen
import com.lexur.yumo.navigation.Screen
import com.lexur.yumo.navigation.SettingsScreen
import com.lexur.yumo.ui.theme.CatTheme
import com.lexur.yumo.util.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var adMobManager: AdMobManager
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        // Setup UI immediately (don't wait for consent)
        setupUI()

        // Handle consent and AdMob initialization in background
        lifecycleScope.launch {
            try {
                // Request consent first
                val consentSuccess = suspendCancellableCoroutine { continuation ->
                    adMobManager.requestConsentInformation(
                        activity = this@MainActivity,
                        onConsentGathered = { success ->
                            continuation.resume(success)
                        }
                    )
                }

                if (consentSuccess && !isFinishing && !isDestroyed) {
                    // Initialize AdMob after consent
                    val initialized = adMobManager.initialize()
                    Log.d("MainActivity", "AdMob ready: $initialized")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "AdMob setup error", e)
            }
        }
    }

    private fun setupUI() {
        setContent {
            val useSystemTheme by themeViewModel.useSystemTheme.collectAsState()
            val isDarkMode by themeViewModel.isDarkMode.collectAsState()
            val systemDarkMode = isSystemInDarkTheme()
            val shouldUseDarkMode = if (useSystemTheme) {
                systemDarkMode
            } else {
                isDarkMode
            }
            CatTheme(darkTheme = shouldUseDarkMode) {
                NavigationGraph(adMobManager = adMobManager)
            }
        }
    }
}

@Composable
fun NavigationGraph(adMobManager: AdMobManager) {
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
                onNavigateBack = { navController.popBackStack() },
                adMobManager = adMobManager
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
                navController = navController
            )
        }
    }
}