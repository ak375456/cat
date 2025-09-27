package com.lexur.yumo.custom_character.domain

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomCharacterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CustomCharacter)

    @Query("SELECT * FROM custom_characters")
    fun getAllCharacters(): Flow<List<CustomCharacter>>
}
