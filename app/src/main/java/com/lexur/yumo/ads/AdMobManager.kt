package com.lexur.yumo.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
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

    companion object {
        // TODO: Replace with your actual Ad Unit IDs from AdMob console
        const val BANNER_AD_UNIT_ID = "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy"
        const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy"
    }

    /**
     * Initialize AdMob SDK
     */
    suspend fun initialize(): Boolean = suspendCancellableCoroutine { continuation ->
        try {
            MobileAds.initialize(context) {
                continuation.resume(true)
            }
        } catch (e: Exception) {
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
                if (consentInformation.isConsentFormAvailable) {
                    loadConsentForm(activity, onConsentGathered)
                } else {
                    onConsentGathered(true)
                }
            },
            {
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
                    consentForm.show(activity) {
                        loadConsentForm(activity, onConsentGathered)
                    }
                } else {
                    onConsentGathered(true)
                }
            },
            {
                onConsentGathered(false)
            }
        )
    }

    /**
     * Show privacy options form (for users to change their consent)
     */
    fun showPrivacyOptionsForm(activity: Activity, onDismiss: () -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) {
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

    /**
     * Check if personalized ads are allowed
     */
    fun canShowPersonalizedAds(): Boolean {
        return consentInformation.consentStatus == ConsentInformation.ConsentStatus.OBTAINED
    }

    /**
     * Build ad request based on consent status
     */
    fun buildAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }
}