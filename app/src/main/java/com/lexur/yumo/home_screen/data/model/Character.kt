package com.lexur.yumo.home_screen.data.model

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
    val xPosition: Int = 0, // Add X position field for hanging characters
    val previewWidth: Int = 80,
    val previewHeight: Int = 80,
    val speed: Int = 3, // pixels per frame
    val animationDelay: Long = 100L, // milliseconds
    val isCustom: Boolean = false,
    val imagePath: String? = null,
    val ropeResId: Int? = null,
    val ropeScale: Float = 1f,
    val ropeOffsetX: Float = 0f,
    val ropeOffsetY: Float = 0f
) : Parcelable {

    // Helper property to check if character is hanging (static)
    val isHanging: Boolean
        get() = id.contains("hanging", ignoreCase = true) ||
                name.contains("hanging", ignoreCase = true) ||
                category == CharacterCategory.HANGING
}

enum class CharacterCategory(val displayName: String) {
    ANIMATED("Animated"),
    HANGING("Hanging Characters")
}
