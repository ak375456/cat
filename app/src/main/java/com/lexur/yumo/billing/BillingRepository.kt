package com.lexur.yumo.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingResult as GoogleBillingResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BillingRepository @Inject constructor(
    @param: ApplicationContext private val context: Context
) : PurchasesUpdatedListener {

    private val _billingState = MutableStateFlow(BillingState())
    val billingState: Flow<BillingState> = _billingState.asStateFlow()

    private lateinit var billingClient: BillingClient
    private var isInitialized = false

    init {
        setupBillingClient()
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        startBillingConnection()
    }

    private fun startBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: GoogleBillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    isInitialized = true
                    queryPurchases()
                    queryProductDetails()
                } else {
                    _billingState.value = _billingState.value.copy(
                        error = "Billing setup failed: ${billingResult.debugMessage}",
                        isLoading = false
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                isInitialized = false
                // Retry connection
                startBillingConnection()
            }
        })
    }

    private fun queryProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(BillingConstants.PREMIUM_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val products = productDetailsList.map { details ->
                    ProductInfo(
                        productId = details.productId,
                        title = details.name,
                        description = details.description,
                        price = details.oneTimePurchaseOfferDetails?.formattedPrice ?: ""
                    )
                }
                _billingState.value = _billingState.value.copy(
                    availableProducts = products
                )
            }
        }
    }

    private fun queryPurchases() {
        if (!isInitialized) return

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasPremium = purchases.any { purchase ->
                    purchase.products.contains(BillingConstants.PREMIUM_PRODUCT_ID) &&
                            purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }

                _billingState.value = _billingState.value.copy(
                    isPremiumOwned = hasPremium,
                    isLoading = false
                )

                // Acknowledge purchases if needed
                purchases.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                        acknowledgePurchase(purchase)
                    }
                }
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _billingState.value = _billingState.value.copy(
                    purchaseSuccess = true
                )
            }
        }
    }

    suspend fun launchPurchaseFlow(activity: Activity): BillingResult {
        if (!isInitialized) {
            return BillingResult.Error("Billing not initialized")
        }

        _billingState.value = _billingState.value.copy(isLoading = true, error = null)

        // Query product details first
        val productDetails = suspendCancellableCoroutine { continuation ->
            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(BillingConstants.PREMIUM_PRODUCT_ID)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
                    productDetailsList.isNotEmpty()) {
                    continuation.resume(productDetailsList[0])
                } else {
                    continuation.resume(null)
                }
            }
        }

        if (productDetails == null) {
            _billingState.value = _billingState.value.copy(
                isLoading = false,
                error = "Product not available"
            )
            return BillingResult.Error("Product not available")
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val result = billingClient.launchBillingFlow(activity, billingFlowParams)

        return when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> BillingResult.Success()
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _billingState.value = _billingState.value.copy(isLoading = false)
                BillingResult.UserCancelled
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _billingState.value = _billingState.value.copy(
                    isLoading = false,
                    isPremiumOwned = true
                )
                BillingResult.PremiumOwned
            }
            else -> {
                _billingState.value = _billingState.value.copy(
                    isLoading = false,
                    error = result.debugMessage
                )
                BillingResult.Error(result.debugMessage)
            }
        }
    }

    override fun onPurchasesUpdated(
        billingResult: GoogleBillingResult,
        purchases: MutableList<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        if (!purchase.isAcknowledged) {
                            acknowledgePurchase(purchase)
                        }
                        _billingState.value = _billingState.value.copy(
                            isPremiumOwned = true,
                            isLoading = false,
                            purchaseSuccess = true
                        )
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _billingState.value = _billingState.value.copy(
                    isLoading = false,
                    error = null
                )
            }
            else -> {
                _billingState.value = _billingState.value.copy(
                    isLoading = false,
                    error = "Purchase failed: ${billingResult.debugMessage}"
                )
            }
        }
    }

    fun resetPurchaseSuccess() {
        _billingState.value = _billingState.value.copy(purchaseSuccess = false)
    }

    fun clearError() {
        _billingState.value = _billingState.value.copy(error = null)
    }

    fun destroy() {
        if (::billingClient.isInitialized) {
            billingClient.endConnection()
        }
    }
}