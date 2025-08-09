package com.aftab.cat.character_screen.presentation

import com.aftab.cat.home_screen.data.CharacterRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aftab.cat.SimpleOverlayManager
import com.aftab.cat.home_screen.data.model.Characters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterSettingsViewModel @Inject constructor(
    private val characterRepository: CharacterRepository,
    private val overlayManager: SimpleOverlayManager,
) : ViewModel() {

    private val _character = MutableStateFlow<Characters?>(null)
    val character: StateFlow<Characters?> = _character

    private val _speed = MutableStateFlow(3)
    val speed: StateFlow<Int> = _speed

    private val _width = MutableStateFlow(18)
    val width: StateFlow<Int> = _width

    private val _height = MutableStateFlow(18)
    val height: StateFlow<Int> = _height

    private val _animationDelay = MutableStateFlow(100L)
    val animationDelay: StateFlow<Long> = _animationDelay

    private val _yPosition = MutableStateFlow(60)
    val yPosition: StateFlow<Int> = _yPosition

    private val _xPosition = MutableStateFlow(0)
    val xPosition: StateFlow<Int> = _xPosition

    private val _linkedDimensions = MutableStateFlow(true)
    val linkedDimensions: StateFlow<Boolean> = _linkedDimensions

    // Track if character is currently running for live updates
    private val _isCharacterRunning = MutableStateFlow(false)
    val isCharacterRunning: StateFlow<Boolean> = _isCharacterRunning

    fun loadCharacter(characterId: String) {
        viewModelScope.launch {
            val loadedCharacter = characterRepository.getCharacterById(characterId)
            _character.value = loadedCharacter
            loadedCharacter?.let { character ->
                _speed.value = character.speed
                _width.value = character.width
                _height.value = character.height
                _animationDelay.value = character.animationDelay
                _yPosition.value = character.yPosition
                _xPosition.value = character.xPosition
                // Set linked dimensions based on whether width equals height
                _linkedDimensions.value = character.width == character.height
            }
        }
    }

    fun setCharacterRunning(isRunning: Boolean) {
        _isCharacterRunning.value = isRunning
    }

    fun updateSpeed(newSpeed: Int) {
        _speed.value = newSpeed
        updateLiveCharacterIfRunning()
    }

    fun updateDimensions(newWidth: Int, newHeight: Int) {
        _width.value = newWidth
        _height.value = newHeight
        updateLiveCharacterIfRunning()
    }

    fun updateAnimationDelay(newDelay: Long) {
        _animationDelay.value = newDelay
        updateLiveCharacterIfRunning()
    }

    fun updateYPosition(newYPosition: Int) {
        _yPosition.value = newYPosition
        // Always update live position immediately for better UX
        updateLiveCharacterIfRunning()
    }

    fun updateXPosition(newXPosition: Int) {
        _xPosition.value = newXPosition
        // Always update live position immediately for better UX
        updateLiveCharacterIfRunning()
    }

    fun setLinkedDimensions(linked: Boolean) {
        _linkedDimensions.value = linked
        // If linking dimensions, make height equal to width
        if (linked) {
            _height.value = _width.value
            updateLiveCharacterIfRunning()
        }
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
                    width = _width.value,
                    height = _height.value,
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
                    width = _width.value,
                    height = _height.value,
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
                    _width.value = default.width
                    _height.value = default.height
                    _animationDelay.value = default.animationDelay
                    _yPosition.value = default.yPosition
                    _xPosition.value = default.xPosition
                    _linkedDimensions.value = default.width == default.height

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