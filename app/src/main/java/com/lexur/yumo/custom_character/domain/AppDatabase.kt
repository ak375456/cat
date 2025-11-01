package com.lexur.yumo.custom_character.domain

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [CustomCharacter::class], version = 2, exportSchema = false) // Changed version to 2
abstract class AppDatabase : RoomDatabase() {

    abstract fun customCharacterDao(): CustomCharacterDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add isEmoji column with default value false
                db.execSQL("ALTER TABLE custom_characters ADD COLUMN isEmoji INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yumo_database"
                )
                    .addMigrations(MIGRATION_1_2) // Add migration
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}