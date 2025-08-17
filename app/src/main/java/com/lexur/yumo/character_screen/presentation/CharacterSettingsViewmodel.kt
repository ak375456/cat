package com.lexur.yumo.character_screen.presentation

import com.lexur.yumo.home_screen.data.CharacterRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lexur.yumo.SimpleOverlayManager
import com.lexur.yumo.home_screen.data.model.Characters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.SharedPreferences
import androidx.core.content.edit

@HiltViewModel
class CharacterSettingsViewModel @Inject constructor(
    private val characterRepository: CharacterRepository,
    private val overlayManager: SimpleOverlayManager,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    companion object {
        private const val MOTION_SENSING_KEY = "motion_sensing_enabled"
        private const val USE_BUTTON_CONTROLS_KEY = "use_button_controls"
    }

    private val _character = MutableStateFlow<Characters?>(null)
    val character: StateFlow<Characters?> = _character

    private val _speed = MutableStateFlow(3)
    val speed: StateFlow<Int> = _speed

    private val _size = MutableStateFlow(18)
    val size: StateFlow<Int> = _size

    private val _animationDelay = MutableStateFlow(100L)
    val animationDelay: StateFlow<Long> = _animationDelay

    private val _yPosition = MutableStateFlow(60)
    val yPosition: StateFlow<Int> = _yPosition

    private val _xPosition = MutableStateFlow(0)
    val xPosition: StateFlow<Int> = _xPosition

    // Track if character is currently running for live updates
    private val _isCharacterRunning = MutableStateFlow(false)
    val isCharacterRunning: StateFlow<Boolean> = _isCharacterRunning

    // Motion sensing toggle
    private val _motionSensingEnabled = MutableStateFlow(true)
    val motionSensingEnabled: StateFlow<Boolean> = _motionSensingEnabled

    // Button controls toggle
    private val _useButtonControls = MutableStateFlow(false)
    val useButtonControls: StateFlow<Boolean> = _useButtonControls

    init {
        // Load preferences
        _motionSensingEnabled.value = sharedPreferences.getBoolean(MOTION_SENSING_KEY, true)
        _useButtonControls.value = sharedPreferences.getBoolean(USE_BUTTON_CONTROLS_KEY, false)
    }

    fun loadCharacter(characterId: String) {
        viewModelScope.launch {
            val loadedCharacter = characterRepository.getCharacterById(characterId)
            _character.value = loadedCharacter
            loadedCharacter?.let { character ->
                _speed.value = character.speed
                _size.value = character.width // Use width as the single size value
                _animationDelay.value = character.animationDelay
                _yPosition.value = character.yPosition
                _xPosition.value = character.xPosition
            }
        }
    }

    fun setCharacterRunning(isRunning: Boolean) {
        _isCharacterRunning.value = isRunning
    }

    fun setMotionSensingEnabled(enabled: Boolean) {
        _motionSensingEnabled.value = enabled
        // Save preference
        sharedPreferences.edit {
            putBoolean(MOTION_SENSING_KEY, enabled)
        }
        // Update overlay manager immediately
        overlayManager.setMotionSensingEnabled(enabled)
    }

    fun setUseButtonControls(useButtons: Boolean) {
        _useButtonControls.value = useButtons
        // Save preference
        sharedPreferences.edit {
            putBoolean(USE_BUTTON_CONTROLS_KEY, useButtons)
        }
    }

    fun updateSpeed(newSpeed: Int) {
        _speed.value = newSpeed
        updateLiveCharacterIfRunning()
    }

    fun updateSize(newSize: Int) {
        _size.value = newSize
        updateLiveCharacterIfRunning()
    }

    fun updateAnimationDelay(newDelay: Long) {
        _animationDelay.value = newDelay
        updateLiveCharacterIfRunning()
    }

    fun updateYPosition(newYPosition: Int) {
        _yPosition.value = newYPosition
        updateLiveCharacterIfRunning()
    }

    fun updateXPosition(newXPosition: Int) {
        _xPosition.value = newXPosition
        updateLiveCharacterIfRunning()
    }

    fun movePosition(deltaX: Int, deltaY: Int) {
        val newX = (_xPosition.value + deltaX).coerceIn(0, 1000)
        val newY = (_yPosition.value + deltaY).coerceIn(0, 300)

        _xPosition.value = newX
        _yPosition.value = newY
        updateLiveCharacterIfRunning()
    }

    private fun updateLiveCharacterIfRunning() {
        if (_isCharacterRunning.value) {
            updateLiveCharacter()
        }
    }

    private fun updateLiveCharacter() {
        viewModelScope.launch {
            _character.value?.let { current ->
                val updated = current.copy(
                    speed = _speed.value,
                    width = _size.value,
                    height = _size.value, // Use same value for both width and height
                    animationDelay = _animationDelay.value,
                    yPosition = _yPosition.value,
                    xPosition = _xPosition.value
                )

                // Save to repository for persistence
                characterRepository.updateCharacter(updated)
                _character.value = updated

                // Send live update to service
                sendLiveUpdateToService(updated)
            }
        }
    }

    private fun sendLiveUpdateToService(character: Characters) {
        try {
            overlayManager.updateCharacterSettings(character.id, character)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            _character.value?.let { current ->
                val updated = current.copy(
                    speed = _speed.value,
                    width = _size.value,
                    height = _size.value, // Use same value for both width and height
                    animationDelay = _animationDelay.value,
                    yPosition = _yPosition.value,
                    xPosition = _xPosition.value
                )
                characterRepository.updateCharacter(updated)
                _character.value = updated

                // Send final update to service if running
                if (_isCharacterRunning.value) {
                    sendLiveUpdateToService(updated)
                }
            }
        }
    }

    fun startCharacterTest() {
        viewModelScope.launch {
            _character.value?.let { character ->
                overlayManager.addCharacter(character)
                _isCharacterRunning.value = true
            }
        }
    }

    fun stopCharacterTest() {
        viewModelScope.launch {
            _character.value?.let { character ->
                overlayManager.removeCharacter(character.id)
                _isCharacterRunning.value = false
            }
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            _character.value?.id?.let { characterId ->
                val defaultCharacter = characterRepository.getDefaultCharacter(characterId)
                defaultCharacter?.let { default ->
                    _speed.value = default.speed
                    _size.value = default.width // Use width as the single size value
                    _animationDelay.value = default.animationDelay
                    _yPosition.value = default.yPosition
                    _xPosition.value = default.xPosition

                    // Also save the reset values
                    characterRepository.resetCharacterToDefault(characterId)
                    _character.value = default

                    // Send live update to service if running
                    if (_isCharacterRunning.value) {
                        sendLiveUpdateToService(default)
                    }
                }
            }
        }
    }
}