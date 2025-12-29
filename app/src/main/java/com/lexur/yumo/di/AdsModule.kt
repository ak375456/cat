package com.lexur.yumo.di

import android.content.Context
import com.lexur.yumo.ads.AdMobManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdsModule {

    @Provides
    @Singleton
    fun provideAdMobManager(
        @ApplicationContext context: Context
    ): AdMobManager {
        return AdMobManager(context)
    }
}