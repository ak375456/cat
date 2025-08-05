package com.aftab.cat.home_screen.data

import android.content.SharedPreferences
import com.aftab.cat.R
import com.aftab.cat.home_screen.data.model.CharacterCategory
import com.aftab.cat.home_screen.data.model.Characters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CharacterRepository @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) {
    companion object {
        private const val CHARACTERS_KEY = "saved_characters"
    }

    // Default characters - these are the base templates
    private val defaultCharacters = listOf(
        Characters(
            id = "walking_cat",
            name = "Walking Cat",
            category = CharacterCategory.ANIMALS,
            frameIds = listOf(
                R.drawable.cat_walk_01,
                R.drawable.cat_walk_02,
                R.drawable.cat_walk_03,
                R.drawable.cat_walk_04,
                R.drawable.cat_walk_05,
                R.drawable.cat_walk_06
            ),
            width = 18,
            height = 18,
            speed = 3,
            animationDelay = 100L
        ),
        Characters(
            id = "walking_dog",
            name = "Walking Dog",
            category = CharacterCategory.ANIMALS,
            frameIds = listOf(
                R.drawable.dog_walk_01,
                R.drawable.dog_walk_02,
                R.drawable.dog_walk_03,
                R.drawable.dog_walk_04,
                R.drawable.dog_walk_05,
                R.drawable.dog_walk_06
            ),
            width = 18,
            height = 18,
            speed = 4,
            animationDelay = 120L
        )
        // Add more default characters here as needed
    )

    // Load characters from SharedPreferences or use defaults
    private fun loadCharacters(): Map<String, Characters> {
        val savedJson = sharedPreferences.getString(CHARACTERS_KEY, null)
        return if (savedJson != null) {
            try {
                val type = object : TypeToken<Map<String, Characters>>() {}.type
                gson.fromJson(savedJson, type) ?: getDefaultCharactersMap()
            } catch (e: Exception) {
                getDefaultCharactersMap()
            }
        } else {
            getDefaultCharactersMap()
        }
    }

    private fun getDefaultCharactersMap(): Map<String, Characters> {
        return defaultCharacters.associateBy { it.id }
    }

    // Save characters to SharedPreferences
    private fun saveCharacters(characters: Map<String, Characters>) {
        val json = gson.toJson(characters)
        sharedPreferences.edit { putString(CHARACTERS_KEY, json) }
    }

    fun getAllCharacters(): List<Characters> {
        return loadCharacters().values.toList()
    }

    fun getCharacterById(id: String): Characters? {
        val characters = loadCharacters()
        return characters[id]
    }

    fun getCharactersByCategory(category: CharacterCategory): List<Characters> {
        return loadCharacters().values.filter { it.category == category }
    }

    fun updateCharacter(updatedCharacter: Characters) {
        val characters = loadCharacters().toMutableMap()
        characters[updatedCharacter.id] = updatedCharacter.copy()
        saveCharacters(characters)
    }

    // Method to reset a character to its default settings
    fun resetCharacterToDefault(characterId: String) {
        val defaultCharacter = defaultCharacters.find { it.id == characterId }
        if (defaultCharacter != null) {
            updateCharacter(defaultCharacter)
        }
    }

    // Method to get default character (useful for reset functionality)
    fun getDefaultCharacter(characterId: String): Characters? {
        return defaultCharacters.find { it.id == characterId }
    }
}