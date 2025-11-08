package com.lexur.yumo.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingResult as GoogleBillingResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BillingRepository @Inject constructor(
    @param: ApplicationContext private val context: Context
) : PurchasesUpdatedListener {

    private val TAG = "BillingRepository"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _billingState = MutableStateFlow(BillingState())
    val billingState: Flow<BillingState> = _billingState.asStateFlow()

    private lateinit var billingClient: BillingClient
    private var isConnectionInProgress = false
    private var connectionRetryCount = 0
    private val maxRetries = 3

    // Cache for product details to avoid repeated queries
    private var cachedProductDetails: ProductDetails? = null

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
        if (isConnectionInProgress) {
            Log.d(TAG, "Connection already in progress, skipping")
            return
        }

        if (billingClient.isReady) {
            Log.d(TAG, "Billing client already ready")
            queryPurchases()
            queryProductDetails()
            return
        }

        isConnectionInProgress = true
        Log.d(TAG, "Starting billing connection (attempt ${connectionRetryCount + 1})")

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: GoogleBillingResult) {
                isConnectionInProgress = false

                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing setup successful")
                    connectionRetryCount = 0

                    // Query purchases first, then product details
                    queryPurchases()
                    queryProductDetails()
                } else {
                    val errorMsg = "Billing setup failed: ${billingResult.debugMessage} (Code: ${billingResult.responseCode})"
                    Log.e(TAG, errorMsg)

                    _billingState.value = _billingState.value.copy(
                        error = errorMsg,
                        isLoading = false
                    )

                    // Retry with exponential backoff for certain errors
                    if (shouldRetryConnection(billingResult.responseCode) && connectionRetryCount < maxRetries) {
                        scheduleConnectionRetry()
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                isConnectionInProgress = false
                Log.w(TAG, "Billing service disconnected")

                // Retry connection with backoff
                if (connectionRetryCount < maxRetries) {
                    scheduleConnectionRetry()
                }
            }
        })
    }

    private fun shouldRetryConnection(responseCode: Int): Boolean {
        return when (responseCode) {
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
            BillingClient.BillingResponseCode.ERROR,
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> true
            else -> false
        }
    }

    private fun scheduleConnectionRetry() {
        connectionRetryCount++
        val delayMs = (1000L * connectionRetryCount * connectionRetryCount).coerceAtMost(30000L)

        Log.d(TAG, "Scheduling connection retry in ${delayMs}ms")

        scope.launch {
            delay(delayMs)
            startBillingConnection()
        }
    }

    private fun queryProductDetails() {
        if (!billingClient.isReady) {
            Log.w(TAG, "Billing client not ready for product query")
            return
        }

        Log.d(TAG, "Querying product details")

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
                if (productDetailsList.isEmpty()) {
                    Log.e(TAG, "Product details list is empty")
                    _billingState.value = _billingState.value.copy(
                        error = "Product not found. Please check your internet connection and try again."
                    )
                    return@queryProductDetailsAsync
                }

                // Cache the product details
                cachedProductDetails = productDetailsList[0]

                val products = productDetailsList.map { details ->
                    ProductInfo(
                        productId = details.productId,
                        title = details.name,
                        description = details.description,
                        price = details.oneTimePurchaseOfferDetails?.formattedPrice ?: ""
                    )
                }

                Log.d(TAG, "Product details loaded: ${products.size} products")

                _billingState.value = _billingState.value.copy(
                    availableProducts = products,
                    error = null
                )
            } else {
                val errorMsg = "Failed to load product details: ${billingResult.debugMessage}"
                Log.e(TAG, errorMsg)

                _billingState.value = _billingState.value.copy(
                    error = "Unable to load product information. Please try again."
                )
            }
        }
    }

    private fun queryPurchases() {
        if (!billingClient.isReady) {
            Log.w(TAG, "Billing client not ready for purchase query")
            return
        }

        Log.d(TAG, "Querying purchases")

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasPremium = purchases.any { purchase ->
                    purchase.products.contains(BillingConstants.PREMIUM_PRODUCT_ID) &&
                            purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }

                Log.d(TAG, "Purchase query complete. Has premium: $hasPremium")

                _billingState.value = _billingState.value.copy(
                    isPremiumOwned = hasPremium,
                    isLoading = false
                )

                // Acknowledge unacknowledged purchases
                purchases.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                        acknowledgePurchase(purchase)
                    }
                }
            } else {
                Log.e(TAG, "Purchase query failed: ${billingResult.debugMessage}")
                _billingState.value = _billingState.value.copy(
                    isLoading = false
                )
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        Log.d(TAG, "Acknowledging purchase")

        billingClient.acknowledgePurchase(params) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Purchase acknowledged successfully")
                _billingState.value = _billingState.value.copy(
                    purchaseSuccess = true
                )
            } else {
                Log.e(TAG, "Failed to acknowledge purchase: ${billingResult.debugMessage}")
            }
        }
    }

    suspend fun launchPurchaseFlow(activity: Activity): BillingResult {
        Log.d(TAG, "Launch purchase flow requested")

        // Ensure billing client is ready
        if (!billingClient.isReady) {
            Log.w(TAG, "Billing client not ready, attempting to reconnect")

            // Try to establish connection with timeout
            try {
                withTimeout(10000L) {
                    ensureBillingClientReady()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to establish billing connection", e)
                return BillingResult.Error("Unable to connect to billing service. Please check your internet connection and try again.")
            }
        }

        _billingState.value = _billingState.value.copy(isLoading = true, error = null)

        // Use cached product details if available, otherwise query
        val productDetails = cachedProductDetails ?: run {
            Log.d(TAG, "No cached product details, querying now")
            queryProductDetailsSync()
        }

        if (productDetails == null) {
            _billingState.value = _billingState.value.copy(
                isLoading = false,
                error = "Product not available. Please check your internet connection and try again."
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

        Log.d(TAG, "Launching billing flow")
        val result = billingClient.launchBillingFlow(activity, billingFlowParams)

        return when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Log.d(TAG, "Billing flow launched successfully")
                BillingResult.Loading // Actual result comes in onPurchasesUpdated
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "User cancelled billing flow")
                _billingState.value = _billingState.value.copy(isLoading = false)
                BillingResult.UserCancelled
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Log.d(TAG, "Item already owned")
                _billingState.value = _billingState.value.copy(
                    isLoading = false,
                    isPremiumOwned = true
                )
                BillingResult.PremiumOwned
            }
            else -> {
                Log.e(TAG, "Billing flow launch failed: ${result.debugMessage}")
                _billingState.value = _billingState.value.copy(
                    isLoading = false,
                    error = result.debugMessage
                )
                BillingResult.Error(result.debugMessage)
            }
        }
    }

    private suspend fun ensureBillingClientReady() = suspendCancellableCoroutine { continuation ->
        if (billingClient.isReady) {
            continuation.resume(Unit)
            return@suspendCancellableCoroutine
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: GoogleBillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(Unit)
                } else {
                    continuation.cancel(Exception("Billing setup failed: ${billingResult.debugMessage}"))
                }
            }

            override fun onBillingServiceDisconnected() {
                continuation.cancel(Exception("Billing service disconnected"))
            }
        })
    }

    private suspend fun queryProductDetailsSync(): ProductDetails? = suspendCancellableCoroutine { continuation ->
        if (!billingClient.isReady) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

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
                cachedProductDetails = productDetailsList[0]
                continuation.resume(productDetailsList[0])
            } else {
                Log.e(TAG, "Failed to query product details: ${billingResult.debugMessage}")
                continuation.resume(null)
            }
        }
    }

    override fun onPurchasesUpdated(
        billingResult: GoogleBillingResult,
        purchases: MutableList<Purchase>?
    ) {
        Log.d(TAG, "onPurchasesUpdated called with responseCode: ${billingResult.responseCode}")

        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases.isNullOrEmpty()) {
                    Log.w(TAG, "Purchase list is empty")
                    _billingState.value = _billingState.value.copy(
                        isLoading = false
                    )
                    return
                }

                purchases.forEach { purchase ->
                    Log.d(TAG, "Processing purchase: ${purchase.products}, state: ${purchase.purchaseState}")

                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        if (!purchase.isAcknowledged) {
                            acknowledgePurchase(purchase)
                        }

                        if (purchase.products.contains(BillingConstants.PREMIUM_PRODUCT_ID)) {
                            _billingState.value = _billingState.value.copy(
                                isPremiumOwned = true,
                                isLoading = false,
                                purchaseSuccess = true,
                                error = null
                            )
                        }
                    } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                        Log.d(TAG, "Purchase is pending")
                        _billingState.value = _billingState.value.copy(
                            isLoading = false,
                            error = "Purchase is pending. It will be completed shortly."
                        )
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "User cancelled purchase")
                _billingState.value = _billingState.value.copy(
                    isLoading = false,
                    error = null
                )
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Log.d(TAG, "Item already owned")
                _billingState.value = _billingState.value.copy(
                    isLoading = false,
                    isPremiumOwned = true,
                    error = null
                )
            }
            else -> {
                Log.e(TAG, "Purchase failed: ${billingResult.debugMessage}")
                _billingState.value = _billingState.value.copy(
                    isLoading = false,
                    error = "Purchase failed: ${billingResult.debugMessage}"
                )
            }
        }
    }

    fun refreshPurchases() {
        Log.d(TAG, "Manual refresh purchases requested")
        queryPurchases()
    }

    fun refreshProductDetails() {
        Log.d(TAG, "Manual refresh product details requested")
        cachedProductDetails = null
        queryProductDetails()
    }

    fun resetPurchaseSuccess() {
        _billingState.value = _billingState.value.copy(purchaseSuccess = false)
    }

    fun clearError() {
        _billingState.value = _billingState.value.copy(error = null)
    }

    fun destroy() {
        if (::billingClient.isInitialized && billingClient.isReady) {
            Log.d(TAG, "Ending billing connection")
            billingClient.endConnection()
        }
    }
}