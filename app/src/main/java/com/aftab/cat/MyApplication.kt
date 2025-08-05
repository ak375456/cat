package com.aftab.cat

import android.app.Application
import com.aftab.cat.home_screen.data.CharacterRepository
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject

@HiltAndroidApp
class MyApplication: Application(){
    @Inject
    lateinit var characterRepository: CharacterRepository
}