package com.lexur.yumo.di

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
// import androidx.room.Room // No longer needed here
import com.google.firebase.firestore.FirebaseFirestore
import com.lexur.yumo.MotionSensorManager
import com.lexur.yumo.SimpleOverlayManager
import com.google.gson.Gson
import com.lexur.yumo.custom_character.domain.AppDatabase
import com.lexur.yumo.custom_character.domain.CustomCharacterDao
import com.lexur.yumo.util.ThemeManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        // Change this to call your static getDatabase method
        // which correctly adds the migration.
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideCustomCharacterDao(appDatabase: AppDatabase): CustomCharacterDao {
        return appDatabase.customCharacterDao()
    }


    @Provides
    @Singleton
    fun provideMotionSensorManager(): MotionSensorManager {
        return MotionSensorManager()
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideThemeManager(
        dataStore: DataStore<Preferences>
    ): ThemeManager = ThemeManager(dataStore)


    @Provides
    @Singleton
    fun provideSimpleOverlayManager(): SimpleOverlayManager {
        return SimpleOverlayManager(
            motionSensorManager = MotionSensorManager()
        )
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("character_settings", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }
}
