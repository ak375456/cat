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

    private val _purchaseResult = MutableStateFlow<PurchaseResultState>(PurchaseResultState.Idle)
    val purchaseResult: StateFlow<PurchaseResultState> = _purchaseResult.asStateFlow()

    init {
        observeBillingState()
    }

    private fun observeBillingState() {
        viewModelScope.launch {
            billingRepository.billingState.collect { state ->
                _billingState.value = state

                // Auto-dismiss dialog on successful purchase
                if (state.purchaseSuccess) {
                    _showPremiumDialog.value = false
                    _purchaseResult.value = PurchaseResultState.Success
                }
            }
        }
    }

    fun checkPremiumStatus(): Boolean {
        return _billingState.value.isPremiumOwned
    }

    fun showPremiumDialog() {
        // Clear any previous errors when opening dialog
        clearError()
        _showPremiumDialog.value = true
    }

    fun dismissPremiumDialog() {
        _showPremiumDialog.value = false
        clearError()
        _purchaseResult.value = PurchaseResultState.Idle
    }

    fun purchasePremium(activity: Activity) {
        viewModelScope.launch {
            _purchaseResult.value = PurchaseResultState.Loading

            val result = billingRepository.launchPurchaseFlow(activity)

            when (result) {
                is BillingResult.Loading -> {
                    // Purchase flow launched, waiting for callback
                    _purchaseResult.value = PurchaseResultState.Loading
                }
                is BillingResult.Success -> {
                    // This shouldn't happen with the fixed repository
                    _purchaseResult.value = PurchaseResultState.Success
                    _showPremiumDialog.value = false
                }
                is BillingResult.Error -> {
                    _purchaseResult.value = PurchaseResultState.Error(result.message)
                }
                is BillingResult.PremiumOwned -> {
                    _purchaseResult.value = PurchaseResultState.AlreadyOwned
                    _showPremiumDialog.value = false
                }
                is BillingResult.UserCancelled -> {
                    _purchaseResult.value = PurchaseResultState.Cancelled
                }
                else -> {
                    _purchaseResult.value = PurchaseResultState.Error("Unknown error occurred")
                }
            }
        }
    }

    fun retryPurchase(activity: Activity) {
        // Refresh product details and try again
        refreshBillingData()
        purchasePremium(activity)
    }

    fun refreshBillingData() {
        viewModelScope.launch {
            clearError()
            billingRepository.refreshProductDetails()
            billingRepository.refreshPurchases()
        }
    }

    fun resetPurchaseSuccess() {
        billingRepository.resetPurchaseSuccess()
        _purchaseResult.value = PurchaseResultState.Idle
    }

    fun clearError() {
        billingRepository.clearError()
        if (_purchaseResult.value is PurchaseResultState.Error) {
            _purchaseResult.value = PurchaseResultState.Idle
        }
    }

    fun getProductPrice(): String {
        return _billingState.value.availableProducts.firstOrNull()?.price ?: ""
    }

    override fun onCleared() {
        super.onCleared()
        billingRepository.destroy()
    }
}

// Sealed class for purchase result states
sealed class PurchaseResultState {
    data object Idle : PurchaseResultState()
    data object Loading : PurchaseResultState()
    data object Success : PurchaseResultState()
    data object AlreadyOwned : PurchaseResultState()
    data object Cancelled : PurchaseResultState()
    data class Error(val message: String) : PurchaseResultState()
}