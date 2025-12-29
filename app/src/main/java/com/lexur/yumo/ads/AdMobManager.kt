package com.lexur.yumo.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.ump.ConsentDebugSettings
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
        private const val TAG = "AdMobManager"

        // Replace with your actual Ad Unit IDs from AdMob console
        const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111" // Test ID
        const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712" // Test ID

        // For production, use:
        // const val BANNER_AD_UNIT_ID = "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy"
    }

    /**
     * Get the test device ID for GDPR testing
     * Call this once and copy the ID from Logcat
     */
    fun logTestDeviceId(activity: Activity) {
        val androidId = android.provider.Settings.Secure.getString(
            activity.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
        val md5 = java.security.MessageDigest.getInstance("MD5")
        val hashedId = md5.digest(androidId.toByteArray())
            .joinToString("") { "%02X".format(it) }

        Log.d(TAG, "════════════════════════════════════════")
        Log.d(TAG, "📱 YOUR TEST DEVICE ID FOR GDPR:")
        Log.d(TAG, "   $hashedId")
        Log.d(TAG, "════════════════════════════════════════")
        Log.d(TAG, "Copy this ID and paste it in AdMobManager.kt")
        Log.d(TAG, "in the addTestDeviceHashedId() method")
    }

    /**
     * Initialize AdMob SDK
     */
    suspend fun initialize(): Boolean = suspendCancellableCoroutine { continuation ->
        try {
            MobileAds.initialize(context) { initializationStatus ->
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
        isDebugMode: Boolean = false,
        onConsentGathered: (Boolean) -> Unit
    ) {
        val paramsBuilder = ConsentRequestParameters.Builder()

        if (isDebugMode) {
            val debugSettings = ConsentDebugSettings.Builder(context)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA) // Changed to EEA
                .addTestDeviceHashedId("CC6C6F640AE8A45D2EB609B69517E48B")
                .build()
            paramsBuilder.setConsentDebugSettings(debugSettings)

            Log.d(TAG, "Debug mode enabled - simulating EEA geography")
        }

        val params = paramsBuilder.build()
        Log.d(TAG, "Requesting consent info update...")

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                Log.d(TAG, "✅ Consent info updated successfully")
                Log.d(TAG, "Consent status: ${consentInformation.consentStatus}")
                Log.d(TAG, "Form available: ${consentInformation.isConsentFormAvailable}")
                Log.d(TAG, "Privacy options required: ${consentInformation.privacyOptionsRequirementStatus}")

                if (consentInformation.isConsentFormAvailable) {
                    Log.d(TAG, "Loading consent form...")
                    loadConsentForm(activity, onConsentGathered)
                } else {
                    Log.d(TAG, "No consent form available")
                    onConsentGathered(true)
                }
            },
            { formError ->
                Log.e(TAG, "❌ Consent form error: ${formError.message}")
                Log.e(TAG, "Error code: ${formError.errorCode}")
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
     * Reset consent for testing purposes
     */
    fun resetConsentForTesting() {
        consentInformation.reset()
    }

    /**
     * Check if user can change privacy settings
     */
    fun canShowPrivacyOptions(): Boolean {
        return consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    /**
     * Get consent status
     */
    fun getConsentStatus(): Int {
        return consentInformation.consentStatus
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