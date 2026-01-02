package com.lexur.yumo.ads.composables

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.lexur.yumo.ads.AdMobManager

@Composable
fun BannerAdView(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobManager.BANNER_AD_UNIT_ID,
    adSize: AdSize = AdSize.BANNER,
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(adSize)
                    this.adUnitId = adUnitId
                    loadAd(AdRequest.Builder().build())
                }
            },
            update = { adView ->
                // Refresh ad if needed
                adView.loadAd(AdRequest.Builder().build())
            }
        )
    }
}

/**
 * Adaptive banner ad that adjusts to screen width
 */
@Composable
fun AdaptiveBannerAdView(
    adUnitId: String = AdMobManager.BANNER_AD_UNIT_ID,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            factory = { ctx ->
                try {
                    AdView(ctx).apply {
                        val display = (ctx.getSystemService(android.content.Context.WINDOW_SERVICE)
                                as android.view.WindowManager).defaultDisplay
                        val outMetrics = android.util.DisplayMetrics()
                        display.getMetrics(outMetrics)
                        val widthPixels = outMetrics.widthPixels
                        val density = outMetrics.density
                        val adWidth = (widthPixels / density).toInt()

                        setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, adWidth))
                        this.adUnitId = adUnitId
                        loadAd(AdRequest.Builder().build())
                    }
                } catch (e: Exception) {
                    Log.e("BannerAdView", "Failed to create AdView", e)
                    // Return empty view if ad fails
                    android.view.View(ctx)
                }
            },
            update = { adView ->
                try {
                    if (adView is AdView) {
                        adView.loadAd(AdRequest.Builder().build())
                    }
                } catch (e: Exception) {
                    Log.e("BannerAdView", "Failed to load ad", e)
                }
            }
        )
    }
}