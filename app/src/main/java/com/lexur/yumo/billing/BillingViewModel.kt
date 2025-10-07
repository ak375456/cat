package com.lexur.yumo.billing

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val billingRepository: BillingRepository
) : ViewModel() {

    private val _billingState = MutableStateFlow(BillingState())
    val billingState: StateFlow<BillingState> = _billingState.asStateFlow()

    private val _showPremiumDialog = MutableStateFlow(false)
    val showPremiumDialog: StateFlow<Boolean> = _showPremiumDialog.asStateFlow()

    init {
        observeBillingState()
    }

    private fun observeBillingState() {
        viewModelScope.launch {
            billingRepository.billingState.collect { state ->
                _billingState.value = state
            }
        }
    }

    fun checkPremiumStatus(): Boolean {
        return _billingState.value.isPremiumOwned
    }

    fun showPremiumDialog() {
        _showPremiumDialog.value = true
    }

    fun dismissPremiumDialog() {
        _showPremiumDialog.value = false
    }

    fun purchasePremium(activity: Activity) {
        viewModelScope.launch {
            val result = billingRepository.launchPurchaseFlow(activity)
            when (result) {
                is BillingResult.Success -> {
                    // Purchase flow launched successfully
                }
                is BillingResult.Error -> {
                    _billingState.value = _billingState.value.copy(
                        error = result.message
                    )
                }
                is BillingResult.PremiumOwned -> {
                    _billingState.value = _billingState.value.copy(
                        isPremiumOwned = true
                    )
                }
                else -> {
                    // Handle other cases
                }
            }
        }
    }

    fun resetPurchaseSuccess() {
        billingRepository.resetPurchaseSuccess()
    }

    fun clearError() {
        billingRepository.clearError()
    }

    override fun onCleared() {
        super.onCleared()
        billingRepository.destroy()
    }
}