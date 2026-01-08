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

public class activity_advertising extends AppCompatActivity {

    private AdView bannerAdView;
    private Button btnInterstitial, btnToggleBanner, btnTestAdInfo;
    private FrameLayout adContainer;
    private TextView tvAdStatus, tvAdCounter;
    private int adCount = 0;
    private boolean isBannerVisible = true;

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

        // Initialize AdMob
        initializeAdMob();

        // Setup button listeners
        setupButtonListeners();
    }

    private void initializeAdMob() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                tvAdStatus.setText("Status: AdMob Initialized • Test Mode Active");
                loadBannerAd();
                setupInterstitialAd();
            }
        });
    }

    private void loadBannerAd() {
        // Load test banner ad
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAdView.loadAd(adRequest);

        // Show success message
        Toast.makeText(this, "Banner ad loaded (Test Mode)", Toast.LENGTH_SHORT).show();
    }

    private void setupInterstitialAd() {
        // Enable interstitial button
        btnInterstitial.setEnabled(true);
        btnInterstitial.setText("Show Interstitial Ad");
    }

    private void setupButtonListeners() {
        // Toggle Banner button
        btnToggleBanner.setOnClickListener(v -> toggleBannerAd());

        // Interstitial button
        btnInterstitial.setOnClickListener(v -> showInterstitialAd());

        // Test Ad Info button
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
            Toast.makeText(this, "Banner ad displayed", Toast.LENGTH_SHORT).show();
        }
    }

    private void showInterstitialAd() {
        // Increment ad counter
        adCount++;
        tvAdCounter.setText("Ads shown: " + adCount);

        // Show interstitial ad simulation
        Toast.makeText(this,
                "Interstitial Ad Displayed\n\n" +
                        "• Full-screen ad\n" +
                        "• Shown at natural app breaks\n" +
                        "• Test ad unit ID: ca-app-pub-3940256099942544/1033173712",
                Toast.LENGTH_LONG).show();

        // Update status
        tvAdStatus.setText("Status: Interstitial shown • Ready for next ad");

        // Disable button temporarily
        btnInterstitial.setEnabled(false);
        btnInterstitial.setText("Loading next ad...");

        // Re-enable after delay
        new android.os.Handler().postDelayed(() -> {
            btnInterstitial.setEnabled(true);
            btnInterstitial.setText("Show Interstitial Ad");
        }, 3000);
    }

    private void showTestAdInfo() {
        Toast.makeText(this,
                "Test Ad Information:\n\n" +
                        "Banner Ad Unit ID:\n" +
                        "ca-app-pub-3940256099942544/6300978111\n\n" +
                        "Interstitial Ad Unit ID:\n" +
                        "ca-app-pub-3940256099942544/1033173712\n\n" +
                        "Note: Test ads are acceptable for development",
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