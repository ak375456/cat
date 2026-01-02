package com.lexur.yumo.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AdMobManager(private val context: Context) {

    private val consentInformation: ConsentInformation by lazy {
        UserMessagingPlatform.getConsentInformation(context)
    }

    private var isInitialized = false

    companion object {
        private const val TAG = "AdMobManager"

        // Replace with your actual Ad Unit IDs from AdMob console
        const val BANNER_AD_UNIT_ID = "ca-app-pub-3026156143814055/7990692071"
        const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    }

    /**
     * Initialize AdMob SDK
     */
    suspend fun initialize(): Boolean = suspendCancellableCoroutine { continuation ->
        if (isInitialized) {
            continuation.resume(true)
            return@suspendCancellableCoroutine
        }

        try {
            // Check if WebView is available first
            try {
                android.webkit.WebView.getCurrentWebViewPackage()
            } catch (e: Exception) {
                Log.e(TAG, "WebView not available", e)
                continuation.resume(false)
                return@suspendCancellableCoroutine
            }

            MobileAds.initialize(context) { initializationStatus ->
                isInitialized = true
                Log.d(TAG, "AdMob initialized: ${initializationStatus.adapterStatusMap}")
                continuation.resume(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "AdMob initialization failed", e)
            continuation.resume(false)
        }
    }

    /**
     * Request consent information and show consent form if needed
     * This handles GDPR compliance automatically
     */
    fun requestConsentInformation(
        activity: Activity,
        onConsentGathered: (Boolean) -> Unit
    ) {
        val params = ConsentRequestParameters.Builder().build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                Log.d(TAG, "Consent info updated successfully")
                Log.d(TAG, "Consent status: ${consentInformation.consentStatus}")

                if (consentInformation.isConsentFormAvailable) {
                    loadConsentForm(activity, onConsentGathered)
                } else {
                    onConsentGathered(true)
                }
            },
            { formError ->
                Log.e(TAG, "Consent form error: ${formError.message}")
                onConsentGathered(false)
            }
        )
    }

    /**
     * Load and show consent form if required
     */
    private fun loadConsentForm(activity: Activity, onConsentGathered: (Boolean) -> Unit) {
        UserMessagingPlatform.loadConsentForm(
            context,
            { consentForm ->
                if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                    consentForm.show(activity) { formError ->
                        if (formError != null) {
                            Log.e(TAG, "Consent form show error: ${formError.message}")
                        }
                        // Load next form if available
                        loadConsentForm(activity, onConsentGathered)
                    }
                } else {
                    onConsentGathered(true)
                }
            },
            { formError ->
                Log.e(TAG, "Consent form load error: ${formError.message}")
                onConsentGathered(false)
            }
        )
    }

    /**
     * Show privacy options form (for users to change their consent)
     */
    fun showPrivacyOptionsForm(activity: Activity, onDismiss: () -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            if (formError != null) {
                Log.e(TAG, "Privacy options form error: ${formError.message}")
            }
            onDismiss()
        }
    }

    /**
     * Check if user can change privacy settings
     */
    fun canShowPrivacyOptions(): Boolean {
        return consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }
}