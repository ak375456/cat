package com.lexur.yumo.home_screen.data

import android.content.SharedPreferences
import android.util.Log
import com.lexur.yumo.home_screen.data.model.CharacterCategory
import com.lexur.yumo.home_screen.data.model.Characters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit
import com.lexur.yumo.R
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
            category = CharacterCategory.ANIMATED,
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
            category = CharacterCategory.ANIMATED,
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
            category = CharacterCategory.ANIMATED,
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
            category = CharacterCategory.ANIMATED,
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
            category = CharacterCategory.ANIMATED,
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
            id = "labubu_walking",
            name = "Labubu",
            category = CharacterCategory.ANIMATED,
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
            category = CharacterCategory.ANIMATED,
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
        ),
        Characters(
            id = "bird",
            name = "Bird",
            category = CharacterCategory.ANIMATED,
            frameIds = listOf(
                R.drawable.bird_01,
                R.drawable.bird_02,
                R.drawable.bird_03,
                R.drawable.bird_04,
                R.drawable.bird_05,
                R.drawable.bird_06,
                R.drawable.bird_07,
                R.drawable.bird_08,
                R.drawable.bird_09,
            ),
            width = 18,
            height = 18,
            speed = 4,
            animationDelay = 120L
        ),
        Characters(
            id = "girl_walk",
            name = "Girl",
            category = CharacterCategory.ANIMATED,
            frameIds = listOf(
                R.drawable.girl_walk_01,
                R.drawable.girl_walk_02,
                R.drawable.girl_walk_03,
                R.drawable.girl_walk_04,
                R.drawable.girl_walk_05,
                R.drawable.girl_walk_06,
            ),
            width = 18,
            height = 18,
            speed = 4,
            animationDelay = 120L
        ),
        Characters(
            id = "lightning_mcQueen",
            name = "McQueen",
            category = CharacterCategory.ANIMATED,
            frameIds = listOf(
                R.drawable.lightning_mcqueen_01,
            ),
            width = 18,
            height = 18,
            speed = 4,
            animationDelay = 120L
        ),
        Characters(
            id = "labubu_hanging",
            name = "Labubu Hanging",
            category = CharacterCategory.HANGING,
            frameIds = listOf(
                R.drawable.labubu_hanging,
            ),
            width = 18,
            height = 18,
            yPosition = 20,
            xPosition = 150,
            speed = 0, // No movement
            animationDelay = 0L // No animation
        ),
        Characters(
            id = "penguin_walking",
            name = "Penguin",
            category = CharacterCategory.ANIMATED,
            frameIds = listOf(
                R.drawable.penguin_walk_01,
                R.drawable.penguin_walk_02,
                R.drawable.penguin_walk_03,
                R.drawable.penguin_walk_04,
                R.drawable.penguin_walk_05,
                R.drawable.penguin_walk_06,
                R.drawable.penguin_walk_07,
                R.drawable.penguin_walk_08,
                R.drawable.penguin_walk_09,
                R.drawable.penguin_walk_10,
                R.drawable.penguin_walk_11,
            ),
            width = 18,
            height = 18,
            yPosition = 20,
            speed = 4,
            animationDelay = 120L
        ),
        Characters(
            id = "skullpanda_hanging",
            name = "SkullPanda",
            category = CharacterCategory.HANGING,
            frameIds = listOf(
                R.drawable.skullpanda_01,
            ),
            width = 18,
            height = 18,
            yPosition = 20,
            xPosition = 150,
            speed = 0, // No movement
            animationDelay = 0L // No animation
        ),
        Characters(
            id = "doraemon_walking",
            name = "Doraemon",
            category = CharacterCategory.ANIMATED,
            frameIds = listOf(
                R.drawable.doraemon_01,
                R.drawable.doraemon_02,
                R.drawable.doraemon_03,
                R.drawable.doraemon_04,
                R.drawable.doraemon_05,
                R.drawable.doraemon_05,
            ),
            width = 18,
            height = 18,
            yPosition = 20,
            speed = 4,
            animationDelay = 120L
        ),
        Characters(
            id = "wingman_hanging",
            name = "Wingman",
            category = CharacterCategory.HANGING,
            frameIds = listOf(
                R.drawable.wingman_hanging,
            ),
            width = 30,
            height = 30,
            yPosition = 20,
            xPosition = 150,
            speed = 0,
            animationDelay = 0L
        ),
        Characters(
            id = "fox_running",
            name = "Fox",
            category = CharacterCategory.ANIMATED,
            frameIds = listOf(
                R.drawable.wolf_running_01,
                R.drawable.wolf_running_02,
                R.drawable.wolf_running_03,
                R.drawable.wolf_running_04,
                R.drawable.wolf_running_05,
                R.drawable.wolf_running_06,
                R.drawable.wolf_running_07,
                R.drawable.wolf_running_08,
                R.drawable.wolf_running_09,
            ),
            width = 30,
            height = 30,
            yPosition = 20,
            xPosition = 150,
            speed = 4,
            animationDelay = 120L
        ),
        Characters(
            id = "bronco_running",
            name = "Bronco",
            category = CharacterCategory.ANIMATED,
            frameIds = listOf(
                R.drawable.bronco_01,
                R.drawable.bronco_02,
                R.drawable.bronco_03,
                R.drawable.bronco_04,
                R.drawable.bronco_05,
                R.drawable.bronco_06,
            ),
            width = 30,
            height = 30,
            yPosition = 20,
            xPosition = 150,
            speed = 4,
            animationDelay = 120L
        ),
        Characters(
            id = "zippy_running",
            name = "Zippy",
            category = CharacterCategory.ANIMATED,
            frameIds = listOf(
                R.drawable.zippy_01,
                R.drawable.zippy_02,
                R.drawable.zippy_03,
                R.drawable.zippy_04,
                R.drawable.zippy_05,
                R.drawable.zippy_06,
                R.drawable.zippy_07,
                R.drawable.zippy_08,
                R.drawable.zippy_09,
                R.drawable.zippy_10,
                R.drawable.zippy_11,
                R.drawable.zippy_12,
            ),
            width = 30,
            height = 30,
            yPosition = 20,
            xPosition = 150,
            speed = 4,
            animationDelay = 120L
        ),
        Characters(
            id = "bleepy_walking",
            name = "Blippy",
            category = CharacterCategory.ANIMATED,
            frameIds = listOf(
                R.drawable.bleepy_01,
                R.drawable.bleepy_02,
                R.drawable.bleepy_03,
                R.drawable.bleepy_04,
                R.drawable.bleepy_05,
                R.drawable.bleepy_06,
                R.drawable.bleepy_07,
                R.drawable.bleepy_08,
            ),
            width = 30,
            height = 30,
            yPosition = 20,
            xPosition = 150,
            speed = 4,
            animationDelay = 120L
        ),
        Characters(
            id = "flare_flying",
            name = "Flare",
            category = CharacterCategory.ANIMATED,
            frameIds = listOf(
                R.drawable.flare_01,
                R.drawable.flare_02,
                R.drawable.flare_03,
                R.drawable.flare_04,
                R.drawable.flare_05,
                R.drawable.flare_06,
                R.drawable.flare_07,
                R.drawable.flare_08,
            ),
            width = 30,
            height = 30,
            yPosition = 20,
            xPosition = 150,
            speed = 4,
            animationDelay = 120L
        ),
        Characters(
            id = "judy_hanging",
            name = "Judy Hopps",
            category = CharacterCategory.HANGING,
            frameIds = listOf(
                R.drawable.joddy_hops_hanging,
            ),
            width = 30,
            height = 30,

            yPosition = 20,
            xPosition = 150,
            speed = 0,
            animationDelay = 0L
        ),
        Characters(
            id = "minion_hanging",
            name = "Minion",
            category = CharacterCategory.HANGING,
            frameIds = listOf(
                R.drawable.minion,
            ),
            width = 30,
            height = 30,
            yPosition = 20,
            xPosition = 150,
            speed = 0,
            animationDelay = 0L
        ),
        Characters(
            id = "danglo_hanging",
            name = "Danglo",
            category = CharacterCategory.HANGING,
            frameIds = listOf(
                R.drawable.danglo_hanging
                ,
            ),
            width = 30,
            height = 30,
            yPosition = 20,
            xPosition = 150,
            speed = 0,
            animationDelay = 0L
        ),
        Characters(
            id = "tweeto_hanging",
            name = "Tweeto",
            category = CharacterCategory.HANGING,
            frameIds = listOf(
                R.drawable.tweeto
                ,
            ),
            width = 30,
            height = 30,
            yPosition = 20,
            xPosition = 150,
            speed = 0,
            animationDelay = 0L
        ),
        Characters(
            id = "puffin_hanging",
            name = "Puffin",
            category = CharacterCategory.HANGING,
            frameIds = listOf(
                R.drawable.puffin
                ,
            ),
            width = 30,
            height = 30,
            yPosition = 20,
            xPosition = 150,
            speed = 0,
            animationDelay = 0L
        ),
        Characters(
            id = "punch_hole_glow",
            name = "camera punch hole ring",
            category = CharacterCategory.HANGING,
            frameIds = listOf(
                R.drawable.punch_hole_glow
                ,
            ),
            width = 30,
            height = 30,
            yPosition = 20,
            xPosition = 150,
            speed = 0,
            animationDelay = 0L
        ),
        Characters(
            id = "saturn_ring_for_camera_notch",
            name = "Saturn Ring for Camera Notch",
            category = CharacterCategory.HANGING,
            frameIds = listOf(
                R.drawable.saturn_ring
                ,
            ),
            width = 30,
            height = 30,
            yPosition = 20,
            xPosition = 150,
            speed = 0,
            animationDelay = 0L
        ),
        Characters(
            id = "black_hole_for_camera_notch",
            name = "Black Whole for Camera Notch",
            category = CharacterCategory.HANGING,
            frameIds = listOf(
                R.drawable.black_hole
                ,
            ),
            width = 30,
            height = 30,
            yPosition = 20,
            xPosition = 150,
            speed = 0,
            animationDelay = 0L
        ),
    )

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

    // New method to get hanging characters specifically
    fun getHangingCharacters(): List<Characters> {
        return loadCharacters().values.filter { it.isHanging }
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