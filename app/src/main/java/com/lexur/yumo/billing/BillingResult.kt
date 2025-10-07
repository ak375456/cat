package com.lexur.yumo.billing

sealed class BillingResult {
    data object Loading : BillingResult()
    data class Success(val message: String = "Purchase successful") : BillingResult()
    data class Error(val message: String) : BillingResult()
    data object PremiumOwned : BillingResult()
    data object PremiumNotOwned : BillingResult()
    data object UserCancelled : BillingResult()
}