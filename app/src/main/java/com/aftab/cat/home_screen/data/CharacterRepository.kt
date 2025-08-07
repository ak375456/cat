package com.aftab.cat.home_screen.data

import android.content.SharedPreferences
import android.util.Log
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
            name = "Cat",
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
            id = "blu_cat",
            name = "Blu",
            category = CharacterCategory.ANIMALS,
            frameIds = listOf(
                R.drawable.blu_walk_01,
                R.drawable.blu_walk_02,
                R.drawable.blu_walk_03,
                R.drawable.blu_walk_04,
                R.drawable.blu_walk_05,
                R.drawable.blu_walk_06,
            ),
            width = 18,
            height = 18,
            speed = 3,
            animationDelay = 100L
        ),
        Characters(
            id = "walking_dog",
            name = "Dog",
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
        ),
        Characters(
            id = "walking_gangster",
            name = "Gangster",
            category = CharacterCategory.CARTOON,
            frameIds = listOf(
                R.drawable.gangsters_walk_01,
                R.drawable.gangsters_walk_02,
                R.drawable.gangsters_walk_03,
                R.drawable.gangsters_walk_04,
                R.drawable.gangsters_walk_05,
                R.drawable.gangsters_walk_06,
                R.drawable.gangsters_walk_07,
                R.drawable.gangsters_walk_08,
                R.drawable.gangsters_walk_09,
                R.drawable.gangsters_walk_10,
            ),
            width = 18,
            height = 18,
            speed = 4,
            animationDelay = 120L
        ),
        Characters(
            id = "walking_banana",
            name = "Banana Man",
            category = CharacterCategory.CARTOON,
            frameIds = listOf(
                R.drawable.banana_walk_01,
                R.drawable.banana_walk_02,
                R.drawable.banana_walk_03,
                R.drawable.banana_walk_04,
                R.drawable.banana_walk_05,
                R.drawable.banana_walk_06,
                R.drawable.banana_walk_07,
                R.drawable.banana_walk_08,
                R.drawable.banana_walk_09,
                R.drawable.banana_walk_10,

            ),
            width = 18,
            height = 18,
            speed = 4,
            animationDelay = 120L
        ),
        Characters(
            id = "walking_jerry",
            name = "Jerry",
            category = CharacterCategory.CARTOON,
            frameIds = listOf(
                R.drawable.jerry_03,
                R.drawable.jerry_04,
                R.drawable.jerry_05,
                R.drawable.jerry_06,
                R.drawable.jerry_08,
                R.drawable.jerry_10,
                R.drawable.jerry_12,
                R.drawable.jerry_14,
                ),
            width = 18,
            height = 18,
            speed = 4,
            animationDelay = 120L
        ),
        Characters(
            id = "jumping_pikachu",
            name = "Jumping Pikachu",
            category = CharacterCategory.ANIME,
            frameIds = listOf(
                R.drawable.pikachu_01,
                R.drawable.pikachu_02,
                R.drawable.pikachu_03,
                R.drawable.pikachu_04,
                R.drawable.pikachu_05,
                R.drawable.pikachu_06,
                R.drawable.pikachu_07,
                R.drawable.pikachu_08,
            ),
            width = 18,
            height = 18,
            speed = 4,
            animationDelay = 120L
        ),
        Characters(
            id = "morty_walking",
            name = "Morty",
            category = CharacterCategory.CARTOON,
            frameIds = listOf(
                R.drawable.morty_01,
                R.drawable.morty_02,
                R.drawable.morty_03,
                R.drawable.morty_04,
            ),
            width = 18,
            height = 18,
            speed = 4,
            animationDelay = 120L
        ),
        Characters(
            id = "peter_walking",
            name = "Peter",
            category = CharacterCategory.CARTOON,
            frameIds = listOf(
                R.drawable.peter_01,
                R.drawable.peter_02,
                R.drawable.peter_03,
                R.drawable.peter_04,
                R.drawable.peter_05,
                R.drawable.peter_06,
                R.drawable.peter_07,
            ),
            width = 18,
            height = 18,
            speed = 4,
            animationDelay = 120L
        ),
        Characters(
            id = "labubu_walking",
            name = "Labubu",
            category = CharacterCategory.CARTOON,
            frameIds = listOf(
                R.drawable.labubu_walk_01,
                R.drawable.labubu_walk_02,
                R.drawable.labubu_walk_03,
                R.drawable.labubu_walk_04,
                R.drawable.labubu_walk_05,
                R.drawable.labubu_walk_06,
            ),
            width = 18,
            height = 18,
            speed = 4,
            animationDelay = 120L
        ),
        Characters(
            id = "naruto_running",
            name = "Naruto",
            category = CharacterCategory.ANIME,
            frameIds = listOf(
                R.drawable.naruto_01,
                R.drawable.naruto_02,
                R.drawable.naruto_03,
                R.drawable.naruto_04,
                R.drawable.naruto_05,
                R.drawable.naruto_06,
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
        val characters = loadCharacters().values.toList()
        Log.d("CharacterRepo", "Loaded ${characters.size} characters: ${characters.map { it.name }}")
        return characters
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