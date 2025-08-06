package com.aftab.cat.home_screen.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Characters(
    val id: String,
    val name: String,
    val category: CharacterCategory,
    val frameIds: List<Int>,
    val width: Int = 18,
    val height: Int = 18,
    val yPosition: Int = 60,
    val previewWidth: Int = 80,
    val previewHeight: Int = 80,
    val speed: Int = 3, // pixels per frame
    val animationDelay: Long = 100L // milliseconds
) : Parcelable

enum class CharacterCategory(val displayName: String) {
    ANIMALS("Animals"),
    ANIME("Anime Characters"),
    CARTOON("Cartoon Characters"),
}

