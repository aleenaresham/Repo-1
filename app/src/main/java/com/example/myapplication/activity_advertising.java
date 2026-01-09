package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.AdListener;

public class activity_advertising extends AppCompatActivity {

    private AdView bannerAdView;
    private Button btnInterstitial, btnToggleBanner, btnTestAdInfo;
    private FrameLayout adContainer;
    private TextView tvAdStatus, tvAdCounter;
    private int adCount = 0;
    private boolean isBannerVisible = true;

    // ✅ POINT 2: Interstitial Ad Variable
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertising);

        // Initialize views
        bannerAdView = findViewById(R.id.bannerAdView);
        btnInterstitial = findViewById(R.id.btnInterstitial);
        btnToggleBanner = findViewById(R.id.btnToggleBanner);
        btnTestAdInfo = findViewById(R.id.btnTestAdInfo);
        adContainer = findViewById(R.id.adContainer);
        tvAdStatus = findViewById(R.id.tvAdStatus);
        tvAdCounter = findViewById(R.id.tvAdCounter);

        // Setup toolbar
        View btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("Advertising (AdMob)");

        // ✅ POINT 1: Initialize AdMob
        initializeAdMob();

        // Setup button listeners
        setupButtonListeners();

        // ✅ POINT 3: Load first interstitial ad
        loadInterstitialAd();
    }

    private void initializeAdMob() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                tvAdStatus.setText("Status: AdMob Initialized • Test Mode Active");

                // ✅ POINT 4: Banner Ad Load
                loadBannerAd();

                // ✅ POINT 5: Setup Banner Ad Listener
                setupBannerAdListener();
            }
        });
    }

    private void loadBannerAd() {
        // ✅ POINT 6: Test Banner Ad
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAdView.loadAd(adRequest);
    }

    private void setupBannerAdListener() {
        bannerAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                tvAdStatus.setText("Status: Banner loaded • Test Mode");
                Toast.makeText(activity_advertising.this,
                        "✅ Banner ad loaded (Test Mode)",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                tvAdStatus.setText("Status: Banner failed • " + adError.getMessage());
            }
        });
    }

    private void loadInterstitialAd() {
        // ✅ POINT 7: Load Interstitial Ad
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this,
                "ca-app-pub-3940256099942544/1033173712", // Test Interstitial ID
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        tvAdStatus.setText("Status: Interstitial loaded • Ready");
                        btnInterstitial.setText("Show Interstitial Ad");
                        btnInterstitial.setEnabled(true);

                        // ✅ POINT 8: Setup Interstitial Callback
                        setupInterstitialCallback();
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        mInterstitialAd = null;
                        tvAdStatus.setText("Status: Interstitial failed • " + loadAdError.getMessage());
                        btnInterstitial.setText("Load Failed");
                    }
                });
    }

    private void setupInterstitialCallback() {
        mInterstitialAd.setFullScreenContentCallback(new com.google.android.gms.ads.FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                // ✅ POINT 9: Ad closed, load next ad
                tvAdStatus.setText("Status: Interstitial closed • Loading next");
                mInterstitialAd = null;
                loadInterstitialAd();

                // Increment counter
                adCount++;
                tvAdCounter.setText("Ads shown: " + adCount);
            }

            @Override
            public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                tvAdStatus.setText("Status: Failed to show • " + adError.getMessage());
                mInterstitialAd = null;
            }

            @Override
            public void onAdShowedFullScreenContent() {
                tvAdStatus.setText("Status: Interstitial showing");
            }
        });
    }

    private void setupButtonListeners() {
        // ✅ POINT 10: Toggle Banner Ad
        btnToggleBanner.setOnClickListener(v -> toggleBannerAd());

        // ✅ POINT 11: Show Interstitial Ad
        btnInterstitial.setOnClickListener(v -> showInterstitialAd());

        // ✅ POINT 12: Test Ad Information
        btnTestAdInfo.setOnClickListener(v -> showTestAdInfo());
    }

    private void toggleBannerAd() {
        if (isBannerVisible) {
            adContainer.setVisibility(View.GONE);
            btnToggleBanner.setText("Show Banner Ad");
            isBannerVisible = false;
            Toast.makeText(this, "Banner ad hidden", Toast.LENGTH_SHORT).show();
        } else {
            adContainer.setVisibility(View.VISIBLE);
            btnToggleBanner.setText("Hide Banner Ad");
            isBannerVisible = true;

            // Reload banner when showing
            loadBannerAd();
            Toast.makeText(this, "Banner ad displayed", Toast.LENGTH_SHORT).show();
        }
    }

    private void showInterstitialAd() {
        if (mInterstitialAd != null) {
            // ✅ POINT 13: Display Real Interstitial Ad
            mInterstitialAd.show(activity_advertising.this);
            btnInterstitial.setEnabled(false);
            btnInterstitial.setText("Showing ad...");
        } else {
            Toast.makeText(this,
                    "Ad not ready yet. Please wait...",
                    Toast.LENGTH_SHORT).show();

            // Try to load again
            loadInterstitialAd();
            btnInterstitial.setText("Loading...");
        }
    }

    private void showTestAdInfo() {
        Toast.makeText(this,
                "📱 AdMob Test Configuration:\n\n" +
                        "✅ Banner Ad (Test):\n" +
                        "ca-app-pub-3940256099942544/6300978111\n\n" +
                        "✅ Interstitial Ad (Test):\n" +
                        "ca-app-pub-3940256099942544/1033173712\n\n" +
                        "✅ Note:\n" +
                        "• Test ads are acceptable for development\n" +
                        "• Real ads need own Ad Unit IDs\n" +
                        "• Banner at bottom\n" +
                        "• Interstitial on button click",
                Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bannerAdView != null) {
            bannerAdView.resume();
        }
    }

    @Override
    protected void onPause() {
        if (bannerAdView != null) {
            bannerAdView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (bannerAdView != null) {
            bannerAdView.destroy();
        }
        super.onDestroy();
    }
}