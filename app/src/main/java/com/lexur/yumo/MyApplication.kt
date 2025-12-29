package com.lexur.yumo

import android.app.Application
import com.lexur.yumo.ads.AdMobManager
import com.lexur.yumo.home_screen.data.CharacterRepository
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class MyApplication: Application(){
    @Inject
    lateinit var characterRepository: CharacterRepository

    @Inject
    lateinit var adMobManager: AdMobManager
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    override fun onCreate() {
        super.onCreate()

        // Initialize AdMob
        applicationScope.launch {
            adMobManager.initialize()
        }
    }
}