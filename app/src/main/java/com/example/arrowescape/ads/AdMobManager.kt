package com.example.arrowescape.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * AdMobManager handles integration with Google Mobile Ads SDK.
 * Uses official AdMob App ID and Ad Unit IDs provided by developer.
 */
object AdMobManager {

    private const val TAG = "AdMobManager"

    // Developer Production IDs
    const val ADMOB_APP_ID = "ca-app-pub-3059174574936158~8445823968"
    const val PROD_BANNER_AD_UNIT_ID = "ca-app-pub-3059174574936158/6139399833"
    const val PROD_REWARDED_AD_UNIT_ID = "ca-app-pub-3059174574936158/2200154824"

    // Google Official Sample Test IDs (used as backup if new ad unit is in <1hr propagation window)
    private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    private var rewardedAd: RewardedAd? = null
    private var isLoadingRewarded = false
    private var isInitialized = false

    /**
     * Initializes Google Mobile Ads SDK.
     */
    fun init(context: Context) {
        if (isInitialized) return
        try {
            MobileAds.initialize(context) { initializationStatus ->
                isInitialized = true
                Log.i(TAG, "Google Mobile Ads SDK Initialized: $initializationStatus")
                preloadRewardedAd(context)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error initializing MobileAds", e)
        }
    }

    /**
     * Preloads a Rewarded Ad so it is ready when player taps Hint, Extra Life, or Free Coins.
     */
    fun preloadRewardedAd(context: Context) {
        if (isLoadingRewarded || rewardedAd != null) return

        isLoadingRewarded = true
        val adRequest = AdRequest.Builder().build()

        // First attempt with developer's production unit ID
        RewardedAd.load(
            context,
            PROD_REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoadingRewarded = false
                    Log.i(TAG, "AdMob Rewarded Ad loaded successfully with Prod ID!")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "Prod Rewarded Ad failed to load (${loadAdError.code}: ${loadAdError.message}). Trying test unit while ad unit propagates...")
                    // If newly created ad unit is in propagation window, load test unit so emulator/testing works
                    loadFallbackRewardedAd(context)
                }
            }
        )
    }

    private fun loadFallbackRewardedAd(context: Context) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            TEST_REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoadingRewarded = false
                    Log.i(TAG, "AdMob Test Rewarded Ad loaded successfully!")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    isLoadingRewarded = false
                    Log.w(TAG, "Both prod and test Rewarded Ads failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    /**
     * Displays a Rewarded Video Ad to the user.
     * If ad is ready, displays full Google AdMob video.
     * If ad is still loading / offline, invokes onFallbackSimulation.
     */
    fun showRewardedAd(
        activity: Activity,
        rewardName: String,
        onRewardEarned: () -> Unit,
        onFallbackSimulation: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    preloadRewardedAd(activity.applicationContext)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Failed to show Rewarded Ad: ${adError.message}")
                    rewardedAd = null
                    preloadRewardedAd(activity.applicationContext)
                    onFallbackSimulation()
                }
            }

            ad.show(activity) { rewardItem ->
                Log.i(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                activity.runOnUiThread {
                    onRewardEarned()
                }
            }
        } else {
            // No ad ready yet - trigger the fallback simulation so gameplay is never blocked
            Log.d(TAG, "Rewarded Ad not ready yet, falling back to simulated rewarded flow.")
            preloadRewardedAd(activity.applicationContext)
            onFallbackSimulation()
        }
    }

    /**
     * Loads and attaches a Google AdMob Banner Ad into the specified container.
     */
    fun loadBannerAd(
        container: ViewGroup,
        fallbackView: View? = null,
        activity: Activity
    ): AdView {
        val adView = AdView(activity).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = PROD_BANNER_AD_UNIT_ID
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        }

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.i(TAG, "AdMob Banner Ad loaded successfully!")
                fallbackView?.visibility = View.GONE
                adView.visibility = View.VISIBLE
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                Log.w(TAG, "AdMob Banner failed to load (${loadAdError.code}: ${loadAdError.message}). Trying test unit while new ad unit propagates...")
                // If newly created ad unit is still propagating (can take up to 1 hr per Google), load sample test ad
                loadFallbackBanner(adView, fallbackView)
            }
        }

        container.addView(adView)
        adView.loadAd(AdRequest.Builder().build())
        return adView
    }

    private fun loadFallbackBanner(adView: AdView, fallbackView: View?) {
        try {
            val fallbackAdView = AdView(adView.context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = TEST_BANNER_AD_UNIT_ID
                layoutParams = adView.layoutParams
            }
            val parent = adView.parent as? ViewGroup
            fallbackAdView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Log.i(TAG, "AdMob Test Banner loaded successfully!")
                    fallbackView?.visibility = View.GONE
                    fallbackAdView.visibility = View.VISIBLE
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Fallback banner failed to load: ${error.message}")
                    fallbackView?.visibility = View.VISIBLE
                }
            }
            parent?.addView(fallbackAdView)
            fallbackAdView.loadAd(AdRequest.Builder().build())
        } catch (e: Throwable) {
            Log.e(TAG, "Error in loadFallbackBanner", e)
        }
    }
}
