package com.aftab.cat.home_screen.presentation

// HomeViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aftab.cat.home_screen.data.CharacterRepository
import com.aftab.cat.home_screen.data.model.Characters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val characterRepository: CharacterRepository
) : ViewModel() {

    private val _allCharacters = MutableStateFlow<List<Characters>>(emptyList())
    val allCharacters: StateFlow<List<Characters>> = _allCharacters.asStateFlow()

    // Track running characters using MutableStateFlow<Set>
    private val _runningCharacters = MutableStateFlow<Set<String>>(emptySet())
    val runningCharacters: StateFlow<Set<String>> = _runningCharacters.asStateFlow()

    init {
        loadAllCharacters()
    }

    private fun loadAllCharacters() {
        viewModelScope.launch {
            _allCharacters.value = characterRepository.getAllCharacters()
        }
    }

    fun startCharacter(characterId: String) {
        _runningCharacters.value = _runningCharacters.value + characterId
    }

    fun stopCharacter(characterId: String) {
        _runningCharacters.value = _runningCharacters.value - characterId
    }

    fun clearAllRunningCharacters() {
        _runningCharacters.value = emptySet()
    }

    fun isCharacterRunning(characterId: String): Boolean {
        return _runningCharacters.value.contains(characterId)
    }
}