package com.lexur.yumo.custom_character.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_characters")
data class CustomCharacter(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val imagePath: String,
    val ropeResId: Int
)
