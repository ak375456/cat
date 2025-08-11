package com.lexur.yumo.di

import android.content.Context
import android.content.SharedPreferences
import com.lexur.yumo.MotionSensorManager
import com.lexur.yumo.SimpleOverlayManager
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMotionSensorManager(): MotionSensorManager {
        return MotionSensorManager()
    }



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