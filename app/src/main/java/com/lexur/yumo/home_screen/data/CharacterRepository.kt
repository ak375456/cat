package com.lexur.yumo.home_screen.data

import com.lexur.yumo.home_screen.data.model.CharacterCategory
import com.lexur.yumo.home_screen.data.model.Characters
import com.lexur.yumo.R
import com.lexur.yumo.custom_character.domain.CustomCharacter
import com.lexur.yumo.custom_character.domain.CustomCharacterDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterRepository @Inject constructor(
    private val customCharacterDao: CustomCharacterDao
) {

    // Default characters - these are the base templates
    private val defaultCharacters = listOf(
        Characters(
            id = "zombie_crawling",
            name = "Zombie",
            category = CharacterCategory.ANIMATED,
            frameIds = listOf(
                R.drawable.zombie_01,
                R.drawable.zombie_02,
                R.drawable.zombie_03,
                R.drawable.zombie_04,
                R.drawable.zombie_05,
                R.drawable.zombie_06,
                R.drawable.zombie_07,
                R.drawable.zombie_08,
            ),
            width = 18,
            height = 18,
            speed = 4,
            animationDelay = 120L
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
            id = "mustang_flying",
            name = "Mustang",
            category = CharacterCategory.ANIMATED,
            frameIds = listOf(
                R.drawable.mustang_01,
                R.drawable.mustang_02,
                R.drawable.mustang_03,
                R.drawable.mustang_04,
                R.drawable.mustang_05,
                R.drawable.mustang_06,
                R.drawable.mustang_07,
                R.drawable.mustang_08,
                R.drawable.mustang_09,
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
            id = "android_hanging",
            name = "Android",
            category = CharacterCategory.HANGING,
            frameIds = listOf(
                R.drawable.android,
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
            id = "sunny_flying",
            name = "Sunny",
            category = CharacterCategory.ANIMATED,
            frameIds = listOf(
                R.drawable.sunny_01,
                R.drawable.sunny_02,
                R.drawable.sunny_03,
                R.drawable.sunny_04,
                R.drawable.sunny_05,
                R.drawable.sunny_06,
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
            id = "choco",
            name = "Choco",
            category = CharacterCategory.HANGING,
            frameIds = listOf(
                R.drawable.choco,
            ),
            width = 30,
            height = 30,

            yPosition = 20,
            xPosition = 150,
            speed = 0,
            animationDelay = 0L
        ),
    )


    suspend fun insertCustomCharacter(character: CustomCharacter) {
        customCharacterDao.insertCharacter(character)
    }

    fun getAllCharacters(): Flow<List<Characters>> {
        return customCharacterDao.getAllCharacters().map { customCharacters ->
            val customMapped = customCharacters.map {
                Characters(
                    id = "custom_${it.id}",
                    name = it.name,
                    category = CharacterCategory.HANGING, // Assuming custom are hanging for now
                    frameIds = emptyList(), // Will be loaded from path
                    imagePath = it.imagePath,
                    isCustom = true,
                    // Default new properties for custom characters
                    atBottom = false,
                    rotation = 0f
                )
            }
            defaultCharacters + customMapped
        }
    }

    suspend fun getCharacterById(id: String): Characters? {
        // This now correctly finds a character by its ID from the combined list
        // of default and custom characters.
        return getAllCharacters().first().find { it.id == id }
    }

    // Method to get default character (useful for reset functionality)
    fun getDefaultCharacter(characterId: String): Characters? {
        return defaultCharacters.find { it.id == characterId }
    }
}