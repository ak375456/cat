package com.lexur.yumo.billing

data class BillingState(
    val isLoading: Boolean = false,
    val isPremiumOwned: Boolean = false,
    val error: String? = null,
    val purchaseSuccess: Boolean = false,
    val availableProducts: List<ProductInfo> = emptyList()
)

data class ProductInfo(
    val productId: String,
    val title: String,
    val description: String,
    val price: String
)
