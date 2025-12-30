package com.lexur.yumo.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@Singleton
class ThemeManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    private val USE_SYSTEM_THEME_KEY = booleanPreferencesKey("use_system_theme")

    val isDarkMode = dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: false
    }

    val useSystemTheme = dataStore.data.map { preferences ->
        preferences[USE_SYSTEM_THEME_KEY] ?: true
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    suspend fun setUseSystemTheme(useSystem: Boolean) {
        dataStore.edit { preferences ->
            preferences[USE_SYSTEM_THEME_KEY] = useSystem
        }
    }
}

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeManager: ThemeManager
) : ViewModel() {

    // Convert Flow to StateFlow using stateIn
    val isDarkMode: StateFlow<Boolean> = themeManager.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val useSystemTheme: StateFlow<Boolean> = themeManager.useSystemTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            themeManager.setDarkMode(enabled)
        }
    }

    fun toggleSystemTheme(useSystem: Boolean) {
        viewModelScope.launch {
            themeManager.setUseSystemTheme(useSystem)
        }
    }
}