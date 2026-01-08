package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class activity_qr extends AppCompatActivity {

    private TextView tvResult;
    private Button btnScan, btnBack, btnLogout, btnGotoDashboard;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize SharedPreferences for session
        sharedPreferences = getSharedPreferences("MyAppSession", MODE_PRIVATE);

        // Initialize views
        tvResult = findViewById(R.id.tv_result);
        btnScan = findViewById(R.id.btn_scan);
        btnBack = findViewById(R.id.btn_back);
        btnLogout = findViewById(R.id.btn_logout);
        btnGotoDashboard = findViewById(R.id.btn_goto_dashboard); // Initialize dashboard button

        // Dashboard button click
        btnGotoDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Dashboard Activity
                Intent intent = new Intent(activity_qr.this, activity_dashboard.class);
                startActivity(intent);
                finish(); // Close current activity
            }
        });

        // Logout button click
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        // Scan button click
        btnScan.setOnClickListener(v -> {
            checkPermissionAndScan();
        });

        // Back button click
        btnBack.setOnClickListener(v -> {
            finish();
        });
    }

    // LOGOUT METHOD
    private void logoutUser() {
        // Clear session from SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Firebase se logout
        mAuth.signOut();

        // Success message
        Toast.makeText(this, "✅ User logged out successfully!", Toast.LENGTH_SHORT).show();

        // Login page par redirect
        Intent intent = new Intent(activity_qr.this, Loginpage.class);
        startActivity(intent);
        finish();
    }

    // CHECK IF USER IS LOGGED IN (Optional security check)
    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            // User not logged in, redirect to login
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(activity_qr.this, Loginpage.class);
            startActivity(intent);
            finish();
        }
    }

    private void checkPermissionAndScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    200);
        } else {
            startQRScan();
        }
    }

    private void startQRScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Scan a QR Code");
        integrator.setOrientationLocked(true);
        integrator.setBeepEnabled(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
                tvResult.setText("Scan cancelled");
            } else {
                String scannedData = result.getContents();
                tvResult.setText("Scanned: " + scannedData);
                Toast.makeText(this, "Scanned: " + scannedData, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startQRScan();
        }
    }
}