package com.lexur.yumo.navigation


sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object CharacterSettings : Screen("character_settings/{characterId}") {
        fun createRoute(characterId: String) = "character_settings/$characterId"
    }
    object CustomCharacterCreation : Screen("custom_character_creation")
}