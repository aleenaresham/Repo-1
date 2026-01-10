package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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

    // ✅ Interstitial Ad Variable
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

        // ✅ Initialize AdMob
        initializeAdMob();

        // Setup button listeners
        setupButtonListeners();

        // ✅ Load first interstitial ad
        loadInterstitialAd();
    }

    private void initializeAdMob() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                tvAdStatus.setText("Status: AdMob Initialized • Test Mode Active");

                // ✅ Banner Ad Load
                loadBannerAd();

                // ✅ Setup Banner Ad Listener
                setupBannerAdListener();
            }
        });
    }

    private void loadBannerAd() {
        // ✅ Test Banner Ad
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
        // ✅ Load Interstitial Ad
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

                        // ✅ Setup Interstitial Callback
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
                // ✅ Ad closed, load next ad
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
        // ✅ Toggle Banner Ad
        btnToggleBanner.setOnClickListener(v -> toggleBannerAd());

        // ✅ Show Interstitial Ad
        btnInterstitial.setOnClickListener(v -> showInterstitialAd());

        // ✅ Test Ad Information (Updated with Scrollable Dialog)
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
            // ✅ Display Real Interstitial Ad
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

    // ✅ UPDATED: Scrollable Dialog for Test Ad Info
    private void showTestAdInfo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📱 AdMob Test Configuration");

        // Create the text view with detailed information
        TextView message = new TextView(this);
        String infoText =
                "✅ BANNER AD (TEST MODE)\n" +
                        "--------------------------------\n" +
                        "Ad Unit ID: ca-app-pub-3940256099942544/6300978111\n" +
                        "Ad Size: 320x50 pixels (Banner)\n" +
                        "Refresh Rate: 60 seconds\n" +
                        "Location: Bottom of screen\n\n" +

                        "✅ INTERSTITIAL AD (TEST MODE)\n" +
                        "--------------------------------\n" +
                        "Ad Unit ID: ca-app-pub-3940256099942544/1033173712\n" +
                        "Ad Type: Full-screen interstitial\n" +
                        "Trigger: Manual (Button click)\n" +
                        "Loading: Background pre-load\n\n" +

                        "✅ IMPORTANT NOTES\n" +
                        "--------------------------------\n" +
                        "• These are TEST Ad Unit IDs for development only\n" +
                        "• Do NOT use these IDs in production apps\n" +
                        "• Create your own Ad Unit IDs in Google AdMob Console\n" +
                        "• Test ads are acceptable during development\n" +
                        "• Real ads require proper AdMob account setup\n\n" +

                        "✅ CURRENT AD STATUS\n" +
                        "--------------------------------\n" +
                        "• Banner Ad: " + (isBannerVisible ? "Visible" : "Hidden") + "\n" +
                        "• Interstitial Ad: " + (mInterstitialAd != null ? "Ready to Show" : "Loading...") + "\n" +
                        "• Total Ads Shown: " + adCount + "\n" +
                        "• Test Mode: ACTIVE\n\n" +

                        "✅ NEXT STEPS FOR PRODUCTION\n" +
                        "--------------------------------\n" +
                        "1. Create AdMob account at admob.google.com\n" +
                        "2. Add your app in AdMob Console\n" +
                        "3. Create real Ad Unit IDs\n" +
                        "4. Replace test IDs with your real IDs\n" +
                        "5. Submit app for review\n" +
                        "6. Start monetizing!";

        message.setText(infoText);
        message.setTextSize(14);
        message.setTextColor(getResources().getColor(android.R.color.black));
        message.setPadding(40, 30, 40, 30);

        // Make text scrollable
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(message);

        builder.setView(scrollView);

        // Add Copy IDs button
        builder.setNeutralButton("Copy IDs", (dialog, which) -> {
            String adIds = "Banner Test ID: ca-app-pub-3940256099942544/6300978111\n" +
                    "Interstitial Test ID: ca-app-pub-3940256099942544/1033173712";

            android.content.ClipboardManager clipboard =
                    (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText(
                    "AdMob Test IDs",
                    adIds
            );
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Test IDs copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        // Add OK button
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
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