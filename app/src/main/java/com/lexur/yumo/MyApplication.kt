package com.lexur.yumo

import android.app.Application
import com.lexur.yumo.home_screen.data.CharacterRepository
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject

@HiltAndroidApp
class MyApplication: Application(){
    @Inject
    lateinit var characterRepository: CharacterRepository
}