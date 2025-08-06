package com.aftab.cat.home_screen.presentation


import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aftab.cat.OverlayService
import com.aftab.cat.home_screen.data.CharacterRepository
import com.aftab.cat.home_screen.data.model.Characters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.content.edit
import com.aftab.cat.home_screen.data.model.CharacterCategory
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.lang.ref.WeakReference


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val characterRepository: CharacterRepository
) : ViewModel() {

    private val _showPermissionDialog = MutableStateFlow(false)
    val showPermissionDialog: StateFlow<Boolean> = _showPermissionDialog.asStateFlow()

    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val PREFS_NAME = "overlay_pets_prefs"
        private const val KEY_DONT_SHOW_PERMISSION_DIALOG = "dont_show_permission_dialog"
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
        if (category == null) {
            characters
        } else {
            characters.filter { it.category == category }
        }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Track running characters using MutableStateFlow<Set>
    private val _runningCharacters = MutableStateFlow<Set<String>>(emptySet())
    val runningCharacters: StateFlow<Set<String>> = _runningCharacters.asStateFlow()

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
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            overlayServiceRef?.clear()
            overlayServiceRef = null
            serviceBound = false
        }
    }

    init {
        loadAllCharacters()
    }

    fun bindToService(context: Context) {
        val intent = Intent(context, OverlayService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindFromService(context: Context) {
        if (serviceBound) {
            context.unbindService(serviceConnection)
            serviceBound = false
            overlayServiceRef?.clear()
            overlayServiceRef = null
        }
    }

    private fun loadAllCharacters() {
        viewModelScope.launch {
            _allCharacters.value = characterRepository.getAllCharacters()
        }
    }

    fun startCharacter(context: Context, character: Characters) {
        OverlayService.startCharacter(context, character)
        _runningCharacters.value = _runningCharacters.value + character.id
    }

    fun stopCharacter(context: Context, characterId: String) {
        OverlayService.stopCharacter(context, characterId)
        _runningCharacters.value = _runningCharacters.value - characterId
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

    fun initializeDialogState(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
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

    // Category filtering methods
    fun selectCategory(category: CharacterCategory) {
        _selectedCategory.value = category
    }

    fun clearCategoryFilter() {
        _selectedCategory.value = null
    }



    override fun onCleared() {
        super.onCleared()
        overlayServiceRef?.clear()
        overlayServiceRef = null
    }
}