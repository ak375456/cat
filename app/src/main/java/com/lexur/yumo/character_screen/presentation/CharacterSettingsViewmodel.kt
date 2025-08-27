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

        // Keys for character-specific settings
        private const val SPEED_SUFFIX = "_speed"
        private const val SIZE_SUFFIX = "_size"
        private const val ANIMATION_DELAY_SUFFIX = "_animation_delay"
        private const val Y_POSITION_SUFFIX = "_y_position"
        private const val X_POSITION_SUFFIX = "_x_position"
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
        // Load global preferences
        _motionSensingEnabled.value = sharedPreferences.getBoolean(MOTION_SENSING_KEY, true)
        _useButtonControls.value = sharedPreferences.getBoolean(USE_BUTTON_CONTROLS_KEY, false)
    }

    fun loadCharacter(characterId: String) {
        viewModelScope.launch {
            val loadedCharacter = characterRepository.getCharacterById(characterId)
            _character.value = loadedCharacter
            loadedCharacter?.let { character ->
                // Load character-specific settings from SharedPreferences
                // If no saved settings exist, use the character's default values
                _speed.value = sharedPreferences.getInt(
                    characterId + SPEED_SUFFIX,
                    character.speed
                )
                _size.value = sharedPreferences.getInt(
                    characterId + SIZE_SUFFIX,
                    character.width
                )
                _animationDelay.value = sharedPreferences.getLong(
                    characterId + ANIMATION_DELAY_SUFFIX,
                    character.animationDelay
                )
                _yPosition.value = sharedPreferences.getInt(
                    characterId + Y_POSITION_SUFFIX,
                    character.yPosition
                )
                _xPosition.value = sharedPreferences.getInt(
                    characterId + X_POSITION_SUFFIX,
                    character.xPosition
                )
            }
        }
    }

    private fun saveCharacterSpecificSettings(characterId: String) {
        sharedPreferences.edit {
            putInt(characterId + SPEED_SUFFIX, _speed.value)
            putInt(characterId + SIZE_SUFFIX, _size.value)
            putLong(characterId + ANIMATION_DELAY_SUFFIX, _animationDelay.value)
            putInt(characterId + Y_POSITION_SUFFIX, _yPosition.value)
            putInt(characterId + X_POSITION_SUFFIX, _xPosition.value)
        }
    }

    fun setCharacterRunning(isRunning: Boolean) {
        _isCharacterRunning.value = isRunning
    }

    fun setMotionSensingEnabled(enabled: Boolean) {
        _motionSensingEnabled.value = enabled
        // Save global preference
        sharedPreferences.edit {
            putBoolean(MOTION_SENSING_KEY, enabled)
        }
        // Update overlay manager immediately
        overlayManager.setMotionSensingEnabled(enabled)
    }

    fun setUseButtonControls(useButtons: Boolean) {
        _useButtonControls.value = useButtons
        // Save global preference
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
                // Save character-specific settings to SharedPreferences
                saveCharacterSpecificSettings(current.id)

                val updated = current.copy(
                    speed = _speed.value,
                    width = _size.value,
                    height = _size.value, // Use same value for both width and height
                    animationDelay = _animationDelay.value,
                    yPosition = _yPosition.value,
                    xPosition = _xPosition.value
                )

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
                val testCharacter = character.copy(
                    speed = _speed.value,
                    width = _size.value,
                    height = _size.value,
                    animationDelay = _animationDelay.value,
                    yPosition = _yPosition.value,
                    xPosition = _xPosition.value
                )
                overlayManager.addCharacter(testCharacter)
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
            _character.value?.let { character ->
                // Remove character-specific settings from SharedPreferences
                sharedPreferences.edit {
                    remove(character.id + SPEED_SUFFIX)
                    remove(character.id + SIZE_SUFFIX)
                    remove(character.id + ANIMATION_DELAY_SUFFIX)
                    remove(character.id + Y_POSITION_SUFFIX)
                    remove(character.id + X_POSITION_SUFFIX)
                }

                // Reset to original character values
                val defaultCharacter = characterRepository.getDefaultCharacter(character.id)
                defaultCharacter?.let { default ->
                    _speed.value = default.speed
                    _size.value = default.width
                    _animationDelay.value = default.animationDelay
                    _yPosition.value = default.yPosition
                    _xPosition.value = default.xPosition

                    // Send live update to service if running
                    if (_isCharacterRunning.value) {
                        sendLiveUpdateToService(default)
                    }
                }
            }
        }
    }
}