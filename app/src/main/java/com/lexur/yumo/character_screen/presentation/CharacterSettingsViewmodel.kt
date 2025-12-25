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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow

@HiltViewModel
class CharacterSettingsViewModel @Inject constructor(
    private val characterRepository: CharacterRepository,
    private val overlayManager: SimpleOverlayManager,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    companion object {
        private const val MOTION_SENSING_KEY = "motion_sensing_enabled"
        private const val USE_BUTTON_CONTROLS_KEY = "use_button_controls"

        private const val SPEED_SUFFIX = "_speed"
        private const val SIZE_SUFFIX = "_size"
        private const val ANIMATION_DELAY_SUFFIX = "_animation_delay"
        private const val Y_POSITION_SUFFIX = "_y_position"
        private const val X_POSITION_SUFFIX = "_x_position"
        private const val AT_BOTTOM_SUFFIX = "_at_bottom"
        private const val ROTATION_SUFFIX = "_rotation"
        private const val ENABLE_FULL_SCREEN_Y_SUFFIX = "_enable_full_screen_y"
        private const val ENABLE_IN_LANDSCAPE_SUFFIX = "_enable_in_landscape"

        private const val SLIDER_DEBOUNCE_MS = 150L
        private const val BUTTON_THROTTLE_MS = 16L

        private const val MAX_Y_POSITION = 3000
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

    private val _atBottom = MutableStateFlow(false)
    val atBottom: StateFlow<Boolean> = _atBottom

    private val _rotation = MutableStateFlow(0f)
    val rotation: StateFlow<Float> = _rotation

    private val _enableFullScreenY = MutableStateFlow(false)
    val enableFullScreenY: StateFlow<Boolean> = _enableFullScreenY

    private val _enableInLandscape = MutableStateFlow(true)
    val enableInLandscape: StateFlow<Boolean> = _enableInLandscape

    private val _maxXPosition = MutableStateFlow(1080)
    val maxXPosition: StateFlow<Int> = _maxXPosition

    private val _isCharacterRunning = MutableStateFlow(false)
    val isCharacterRunning: StateFlow<Boolean> = _isCharacterRunning

    private val _motionSensingEnabled = MutableStateFlow(true)
    val motionSensingEnabled: StateFlow<Boolean> = _motionSensingEnabled

    private val _useButtonControls = MutableStateFlow(false)
    val useButtonControls: StateFlow<Boolean> = _useButtonControls

    private var speedUpdateJob: Job? = null
    private var sizeUpdateJob: Job? = null
    private var animationUpdateJob: Job? = null
    private var positionUpdateJob: Job? = null

    private val positionUpdateChannel = Channel<Pair<Int, Int>>(Channel.CONFLATED)

    // NEW: Track if character has been loaded to prevent reloading on rotation
    private var isCharacterLoaded = false

    init {
        _motionSensingEnabled.value = sharedPreferences.getBoolean(MOTION_SENSING_KEY, true)
        _useButtonControls.value = sharedPreferences.getBoolean(USE_BUTTON_CONTROLS_KEY, false)

        _maxXPosition.value = overlayManager.getCurrentScreenWidth()

        viewModelScope.launch {
            positionUpdateChannel.consumeAsFlow()
                .collect { (x, y) ->
                    _xPosition.value = x
                    _yPosition.value = y
                    updateLiveCharacterImmediate()
                    delay(BUTTON_THROTTLE_MS)
                }
        }
    }

    fun loadCharacter(characterId: String) {
        // NEW: Only load if not already loaded (prevents reload on rotation)
        if (isCharacterLoaded && _character.value?.id == characterId) {
            return
        }

        viewModelScope.launch {
            val loadedCharacter = characterRepository.getCharacterById(characterId)
            _character.value = loadedCharacter
            loadedCharacter?.let { character ->
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
                _atBottom.value = sharedPreferences.getBoolean(
                    characterId + AT_BOTTOM_SUFFIX,
                    character.atBottom
                )
                _rotation.value = sharedPreferences.getFloat(
                    characterId + ROTATION_SUFFIX,
                    character.rotation
                )
                _enableFullScreenY.value = sharedPreferences.getBoolean(
                    characterId + ENABLE_FULL_SCREEN_Y_SUFFIX,
                    false
                )
                _enableInLandscape.value = sharedPreferences.getBoolean(
                    characterId + ENABLE_IN_LANDSCAPE_SUFFIX,
                    character.enableInLandscape
                )

                // NEW: Mark as loaded
                isCharacterLoaded = true
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
            putBoolean(characterId + AT_BOTTOM_SUFFIX, _atBottom.value)
            putFloat(characterId + ROTATION_SUFFIX, _rotation.value)
            putBoolean(characterId + ENABLE_FULL_SCREEN_Y_SUFFIX, _enableFullScreenY.value)
            putBoolean(characterId + ENABLE_IN_LANDSCAPE_SUFFIX, _enableInLandscape.value)
        }
    }

    fun setCharacterRunning(isRunning: Boolean) {
        // Only update if character is already loaded, or if we're setting it to true
        // This prevents the parameter from overriding the actual running state on rotation
        if (isCharacterLoaded || isRunning) {
            _isCharacterRunning.value = isRunning
        }
    }

    fun setMotionSensingEnabled(enabled: Boolean) {
        _motionSensingEnabled.value = enabled
        sharedPreferences.edit {
            putBoolean(MOTION_SENSING_KEY, enabled)
        }
        overlayManager.setMotionSensingEnabled(enabled)
    }

    fun setUseButtonControls(useButtons: Boolean) {
        _useButtonControls.value = useButtons
        sharedPreferences.edit {
            putBoolean(USE_BUTTON_CONTROLS_KEY, useButtons)
        }
    }

    fun setAtBottom(isAtBottom: Boolean, isHanging: Boolean) {
        _atBottom.value = isAtBottom
        if (isHanging) {
            _rotation.value = if (isAtBottom) 180f else 0f
        } else {
            _rotation.value = 0f
        }
        updateLiveCharacterImmediate()
    }

    fun setEnableFullScreenY(enabled: Boolean) {
        _enableFullScreenY.value = enabled
        if (!enabled && _yPosition.value > 300) {
            _yPosition.value = 300
            updateLiveCharacterImmediate()
        }
    }

    fun setEnableInLandscape(enabled: Boolean) {
        _enableInLandscape.value = enabled
        updateLiveCharacterImmediate()
    }

    fun updateSpeed(newSpeed: Int) {
        _speed.value = newSpeed
        updateLiveCharacterDebounced(speedUpdateJob) { job ->
            speedUpdateJob = job
        }
    }

    fun updateSize(newSize: Int) {
        _size.value = newSize
        updateLiveCharacterDebounced(sizeUpdateJob) { job ->
            sizeUpdateJob = job
        }
    }

    fun updateAnimationDelay(newDelay: Long) {
        _animationDelay.value = newDelay
        updateLiveCharacterDebounced(animationUpdateJob) { job ->
            animationUpdateJob = job
        }
    }

    fun updateYPosition(newYPosition: Int) {
        _yPosition.value = newYPosition
        updateLiveCharacterDebounced(positionUpdateJob) { job ->
            positionUpdateJob = job
        }
    }

    fun updateXPosition(newXPosition: Int) {
        _xPosition.value = newXPosition
        updateLiveCharacterDebounced(positionUpdateJob) { job ->
            positionUpdateJob = job
        }
    }

    fun movePosition(deltaX: Int, deltaY: Int) {
        val newX = (_xPosition.value + deltaX).coerceIn(0, _maxXPosition.value)
        val newY = (_yPosition.value + deltaY).coerceIn(0, MAX_Y_POSITION)

        positionUpdateChannel.trySend(newX to newY)
    }

    private fun updateLiveCharacterDebounced(currentJob: Job?, setJob: (Job) -> Unit) {
        if (!_isCharacterRunning.value) return

        currentJob?.cancel()
        val newJob = viewModelScope.launch {
            delay(SLIDER_DEBOUNCE_MS)
            updateLiveCharacterImmediate()
        }
        setJob(newJob)
    }

    private fun updateLiveCharacterImmediate() {
        if (!_isCharacterRunning.value) return

        viewModelScope.launch {
            _character.value?.let { current ->
                val updated = current.copy(
                    speed = _speed.value,
                    width = _size.value,
                    height = _size.value,
                    animationDelay = _animationDelay.value,
                    yPosition = _yPosition.value,
                    xPosition = _xPosition.value,
                    atBottom = _atBottom.value,
                    rotation = _rotation.value,
                    enableInLandscape = _enableInLandscape.value
                )

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
                saveCharacterSpecificSettings(current.id)

                val updated = current.copy(
                    speed = _speed.value,
                    width = _size.value,
                    height = _size.value,
                    animationDelay = _animationDelay.value,
                    yPosition = _yPosition.value,
                    xPosition = _xPosition.value,
                    atBottom = _atBottom.value,
                    rotation = _rotation.value,
                    enableInLandscape = _enableInLandscape.value
                )

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
                    xPosition = _xPosition.value,
                    atBottom = _atBottom.value,
                    rotation = _rotation.value,
                    enableInLandscape = _enableInLandscape.value
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
                sharedPreferences.edit {
                    remove(character.id + SPEED_SUFFIX)
                    remove(character.id + SIZE_SUFFIX)
                    remove(character.id + ANIMATION_DELAY_SUFFIX)
                    remove(character.id + Y_POSITION_SUFFIX)
                    remove(character.id + X_POSITION_SUFFIX)
                    remove(character.id + AT_BOTTOM_SUFFIX)
                    remove(character.id + ROTATION_SUFFIX)
                    remove(character.id + ENABLE_FULL_SCREEN_Y_SUFFIX)
                    remove(character.id + ENABLE_IN_LANDSCAPE_SUFFIX)
                }

                val defaultCharacter = characterRepository.getDefaultCharacter(character.id)
                defaultCharacter?.let { default ->
                    _speed.value = default.speed
                    _size.value = default.width
                    _animationDelay.value = default.animationDelay
                    _yPosition.value = default.yPosition
                    _xPosition.value = default.xPosition
                    _atBottom.value = default.atBottom
                    _rotation.value = default.rotation
                    _enableFullScreenY.value = false
                    _enableInLandscape.value = default.enableInLandscape

                    if (_isCharacterRunning.value) {
                        sendLiveUpdateToService(default)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        positionUpdateChannel.close()
        // NEW: Reset loaded flag when ViewModel is destroyed
        isCharacterLoaded = false
    }
}