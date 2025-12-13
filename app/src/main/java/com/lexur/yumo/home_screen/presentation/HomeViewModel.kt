package com.lexur.yumo.home_screen.presentation

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lexur.yumo.OverlayService
import com.lexur.yumo.home_screen.data.CharacterRepository
import com.lexur.yumo.home_screen.data.model.Characters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.lexur.yumo.home_screen.data.model.CharacterCategory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.lang.ref.WeakReference
import androidx.core.content.edit

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val characterRepository: CharacterRepository,
    private val sharedPreferences: SharedPreferences,
    @param: ApplicationContext private val application: Context,
) : ViewModel() {

    private val _showPermissionDialog = MutableStateFlow(false)
    val showPermissionDialog: StateFlow<Boolean> = _showPermissionDialog.asStateFlow()

    private var isDialogStateInitialized = false

    private val _showCreationDialog = MutableStateFlow(false)
    val showCreationDialog = _showCreationDialog.asStateFlow()

    companion object {
        private const val PREFS_NAME = "overlay_pets_prefs"
        private const val KEY_DONT_SHOW_PERMISSION_DIALOG = "dont_show_permission_dialog"
        private const val KEY_ENABLE_IN_LANDSCAPE = "enable_in_landscape"

        private const val SPEED_SUFFIX = "_speed"
        private const val SIZE_SUFFIX = "_size"
        private const val ANIMATION_DELAY_SUFFIX = "_animation_delay"
        private const val Y_POSITION_SUFFIX = "_y_position"
        private const val X_POSITION_SUFFIX = "_x_position"
        private const val AT_BOTTOM_SUFFIX = "_at_bottom"
        private const val ROTATION_SUFFIX = "_rotation"
    }

    private val _allCharacters = MutableStateFlow<List<Characters>>(emptyList())

    private val _selectedCategory = MutableStateFlow<CharacterCategory?>(null)
    val selectedCategory: StateFlow<CharacterCategory?> = _selectedCategory.asStateFlow()

    val filteredCharacters: StateFlow<List<Characters>> = combine(
        _allCharacters,
        _selectedCategory
    ) { characters, category ->
        val filtered = when (category) {
            null -> characters
            CharacterCategory.HANGING -> {
                characters.filter { character ->
                    character.category == CharacterCategory.HANGING || character.isHanging
                }
            }
            else -> {
                characters.filter { it.category == category }
            }
        }
        filtered
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _runningCharacters = MutableStateFlow<Set<String>>(emptySet())
    val runningCharacters: StateFlow<Set<String>> = _runningCharacters.asStateFlow()

    private val _enableInLandscape = MutableStateFlow(
        sharedPreferences.getBoolean(KEY_ENABLE_IN_LANDSCAPE, false)
    )
    val enableInLandscape: StateFlow<Boolean> = _enableInLandscape.asStateFlow()

    private var overlayServiceRef: WeakReference<OverlayService>? = null
    private var serviceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as OverlayService.OverlayServiceBinder
            val overlayService = binder.getService()
            overlayServiceRef = WeakReference(overlayService)
            serviceBound = true

            updateRunningCharactersFromService()
            overlayService.overlayManager.setEnableInLandscape(_enableInLandscape.value)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            overlayServiceRef?.clear()
            overlayServiceRef = null
            serviceBound = false
        }
    }

    init {
        loadAllCharacters()
        bindToService()
    }

    fun bindToService() {
        if (!isDialogStateInitialized) {
            initializeDialogState()
            isDialogStateInitialized = true
        }

        val intent = Intent(application, OverlayService::class.java)
        try {
            application.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun unbindFromService() {
        if (serviceBound) {
            try {
                application.unbindService(serviceConnection)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            serviceBound = false
            overlayServiceRef?.clear()
            overlayServiceRef = null
        }
    }

    private fun loadAllCharacters() {
        viewModelScope.launch {
            characterRepository.getAllCharacters().collect { allChars ->
                _allCharacters.value = allChars
            }
        }
    }

    fun startCharacter(context: Context, character: Characters) {
        viewModelScope.launch {
            try {
                val characterWithCustomSettings = loadCharacterCustomSettings(character)
                OverlayService.startCharacter(context, characterWithCustomSettings)
                _runningCharacters.update { it + character.id }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadCharacterCustomSettings(character: Characters): Characters {
        val characterId = character.id

        val customSpeed = sharedPreferences.getInt(
            characterId + SPEED_SUFFIX,
            character.speed
        )
        val customSize = sharedPreferences.getInt(
            characterId + SIZE_SUFFIX,
            character.width
        )
        val customAnimationDelay = sharedPreferences.getLong(
            characterId + ANIMATION_DELAY_SUFFIX,
            character.animationDelay
        )
        val customYPosition = sharedPreferences.getInt(
            characterId + Y_POSITION_SUFFIX,
            character.yPosition
        )
        val customXPosition = sharedPreferences.getInt(
            characterId + X_POSITION_SUFFIX,
            character.xPosition
        )
        val customAtBottom = sharedPreferences.getBoolean(
            characterId + AT_BOTTOM_SUFFIX,
            character.atBottom
        )
        val customRotation = sharedPreferences.getFloat(
            characterId + ROTATION_SUFFIX,
            character.rotation
        )

        return character.copy(
            speed = customSpeed,
            width = customSize,
            height = customSize,
            animationDelay = customAnimationDelay,
            yPosition = customYPosition,
            xPosition = customXPosition,
            atBottom = customAtBottom,
            rotation = customRotation
        )
    }

    fun stopCharacter(context: Context, characterId: String) {
        OverlayService.stopCharacter(context, characterId)
        _runningCharacters.update { it - characterId }
    }

    fun clearAllRunningCharacters(context: Context) {
        OverlayService.stopAllCharacters(context)
        _runningCharacters.value = emptySet()
    }

    private fun updateRunningCharactersFromService() {
        overlayServiceRef?.get()?.let { service ->
            _runningCharacters.value = service.getActiveCharacterIds()
        }
    }

    fun syncWithService() {
        if (serviceBound) {
            updateRunningCharactersFromService()
        }
    }

    fun setEnableInLandscape(enabled: Boolean) {
        viewModelScope.launch {
            sharedPreferences.edit {
                putBoolean(KEY_ENABLE_IN_LANDSCAPE, enabled)
            }
            _enableInLandscape.value = enabled
            overlayServiceRef?.get()?.overlayManager?.setEnableInLandscape(enabled)
        }
    }

    private fun initializeDialogState() {
        val shouldShow = !sharedPreferences.getBoolean(KEY_DONT_SHOW_PERMISSION_DIALOG, false)
        _showPermissionDialog.value = shouldShow
    }

    fun dismissPermissionDialog() {
        _showPermissionDialog.value = false
    }

    fun setDontShowPermissionDialogAgain() {
        sharedPreferences.edit {
            putBoolean(KEY_DONT_SHOW_PERMISSION_DIALOG, true)
        }
        _showPermissionDialog.value = false
    }

    fun showPermissionDialogManually() {
        _showPermissionDialog.value = true
    }

    fun selectCategory(category: CharacterCategory) {
        _selectedCategory.value = category
    }

    fun clearCategoryFilter() {
        _selectedCategory.value = null
    }

    fun onDismissCreationDialog() {
        _showCreationDialog.value = false
    }

    // NEW: Get count of premium characters
    fun getPremiumCharacterCount(): Int {
        return characterRepository.getPremiumCharacterCount()
    }

    override fun onCleared() {
        unbindFromService()
        super.onCleared()
        overlayServiceRef?.clear()
        overlayServiceRef = null
    }
}