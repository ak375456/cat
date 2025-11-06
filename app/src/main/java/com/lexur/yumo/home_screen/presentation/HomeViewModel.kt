package com.lexur.yumo.home_screen.presentation

import android.app.Application // Import Application
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
import dagger.hilt.android.qualifiers.ApplicationContext // Import ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.lang.ref.WeakReference

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val characterRepository: CharacterRepository,
    private val sharedPreferences: SharedPreferences,
    @param: ApplicationContext private val application: Context // Inject application context
) : ViewModel() {

    private val _showPermissionDialog = MutableStateFlow(false)
    val showPermissionDialog: StateFlow<Boolean> = _showPermissionDialog.asStateFlow()

    private var isDialogStateInitialized = false

    private val _showCreationDialog = MutableStateFlow(false)
    val showCreationDialog = _showCreationDialog.asStateFlow()

    companion object {
        private const val PREFS_NAME = "overlay_pets_prefs"
        private const val KEY_DONT_SHOW_PERMISSION_DIALOG = "dont_show_permission_dialog"
        private const val KEY_ENABLE_IN_LANDSCAPE = "enable_in_landscape" // New key

        // Keys for character-specific settings
        private const val SPEED_SUFFIX = "_speed"
        private const val SIZE_SUFFIX = "_size"
        private const val ANIMATION_DELAY_SUFFIX = "_animation_delay"
        private const val Y_POSITION_SUFFIX = "_y_position"
        private const val X_POSITION_SUFFIX = "_x_position"
    }

    private val _allCharacters = MutableStateFlow<List<Characters>>(emptyList())

    // Selected category for filtering
    private val _selectedCategory = MutableStateFlow<CharacterCategory?>(null)
    val selectedCategory: StateFlow<CharacterCategory?> = _selectedCategory.asStateFlow()

    // Filtered characters based on selected category
    val filteredCharacters: StateFlow<List<Characters>> = combine(
        _allCharacters,
        _selectedCategory
    ) { characters, category ->

        val filtered = when (category) {
            null -> {
                characters
            }
            CharacterCategory.HANGING -> {
                // For hanging characters, check both category and isHanging property
                val hangingChars = characters.filter { character ->
                    val isHanging = character.category == CharacterCategory.HANGING || character.isHanging
                    isHanging
                }
                hangingChars
            }
            else -> {
                val categoryChars = characters.filter { it.category == category }
                categoryChars
            }
        }

        filtered
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Track running characters using MutableStateFlow<Set>
    private val _runningCharacters = MutableStateFlow<Set<String>>(emptySet())
    val runningCharacters: StateFlow<Set<String>> = _runningCharacters.asStateFlow()

    // --- New StateFlow for landscape setting ---
    private val _enableInLandscape = MutableStateFlow(
        sharedPreferences.getBoolean(KEY_ENABLE_IN_LANDSCAPE, false)
    )
    val enableInLandscape: StateFlow<Boolean> = _enableInLandscape.asStateFlow()
    // ---

    // Use WeakReference to avoid memory leak
    private var overlayServiceRef: WeakReference<OverlayService>? = null
    private var serviceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as OverlayService.OverlayServiceBinder
            val overlayService = binder.getService()
            overlayServiceRef = WeakReference(overlayService)
            serviceBound = true

            // Update running characters based on service state
            updateRunningCharactersFromService()

            // --- Pass current landscape setting to service ---
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
        bindToService() // Call bindToService in init
    }

    // Modified bindToService to not take context
    fun bindToService() {
        // Initialize dialog state only once when binding to service for the first time
        if (!isDialogStateInitialized) {
            initializeDialogState(application) // Use application context
            isDialogStateInitialized = true
        }

        val intent = Intent(application, OverlayService::class.java) // Use application context
        try {
            application.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE) // Use application context
        } catch (e: Exception) {
            // Handle potential exceptions if service binding fails
            e.printStackTrace()
        }
    }

    // Modified unbindFromService to not take context
    fun unbindFromService() {
        if (serviceBound) {
            try {
                application.unbindService(serviceConnection) // Use application context
            } catch (e: Exception) {
                // Handle exceptions during unbind (e.g., service not registered)
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
                // Load character-specific settings from SharedPreferences
                val characterWithCustomSettings = loadCharacterCustomSettings(character)

                // Use the static companion method from OverlayService
                OverlayService.startCharacter(context, characterWithCustomSettings)
                _runningCharacters.update { it + character.id }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadCharacterCustomSettings(character: Characters): Characters {
        val characterId = character.id

        // Load character-specific settings with fallback to original values
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

        // Return character with custom settings applied
        return character.copy(
            speed = customSpeed,
            width = customSize,
            height = customSize, // Use same value for both width and height
            animationDelay = customAnimationDelay,
            yPosition = customYPosition,
            xPosition = customXPosition
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

    // Call this when screen becomes visible to sync with service
    fun syncWithService() {
        if (serviceBound) {
            updateRunningCharactersFromService()
        }
    }

    // --- New function to update landscape preference ---
    fun setEnableInLandscape(enabled: Boolean) {
        viewModelScope.launch {
            sharedPreferences.edit()
                .putBoolean(KEY_ENABLE_IN_LANDSCAPE, enabled)
                .apply()
            _enableInLandscape.value = enabled

            // Pass the setting to the service immediately if bound
            overlayServiceRef?.get()?.overlayManager?.setEnableInLandscape(enabled)
        }
    }
    // ---

    private fun initializeDialogState(context: Context) {
        val dialogPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val shouldShow = !dialogPrefs.getBoolean(KEY_DONT_SHOW_PERMISSION_DIALOG, false)
        _showPermissionDialog.value = shouldShow
    }

    fun dismissPermissionDialog() {
        _showPermissionDialog.value = false
    }

    fun setDontShowPermissionDialogAgain() {
        val dialogPrefs = sharedPreferences.edit()
        dialogPrefs.putBoolean(KEY_DONT_SHOW_PERMISSION_DIALOG, true)
        dialogPrefs.apply()
        _showPermissionDialog.value = false
    }

    fun showPermissionDialogManually() {
        _showPermissionDialog.value = true
    }

    // Category filtering methods
    fun selectCategory(category: CharacterCategory) {
        _selectedCategory.value = category
    }



    fun clearCategoryFilter() {
        _selectedCategory.value = null
    }

    fun onFabClicked() {
        _showCreationDialog.value = true
    }

    fun onDismissCreationDialog() {
        _showCreationDialog.value = false
    }

    override fun onCleared() {
        unbindFromService() // Call unbindFromService in onCleared
        super.onCleared()
        overlayServiceRef?.clear()
        overlayServiceRef = null
    }
}