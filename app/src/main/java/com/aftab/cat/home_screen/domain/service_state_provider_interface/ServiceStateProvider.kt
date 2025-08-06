package com.aftab.cat.home_screen.domain.service_state_provider_interface

interface ServiceStateProvider {
    fun getActiveCharacterIds(): Set<String>
    fun isCharacterActive(characterId: String): Boolean
}