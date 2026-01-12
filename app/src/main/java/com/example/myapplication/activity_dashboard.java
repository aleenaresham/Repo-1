package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class activity_dashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // ✅ Add Back Arrow button
        ImageButton btnBack = findViewById(R.id.btnBack);

        // Find other buttons
        Button btnChatbot = findViewById(R.id.btnChatbot);
        Button btnAdMob = findViewById(R.id.btnAdMob);
        Button btnRealTimeChat = findViewById(R.id.btnRealTimeChat);

        // ✅ Back arrow click listener - goes back to QR activity
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_dashboard.this, activity_qr.class);
                startActivity(intent);
                finish(); // Close dashboard
            }
        });

        // Set click listeners for other buttons
        btnChatbot.setOnClickListener(v -> {
            Intent intent = new Intent(activity_dashboard.this, activity_chatbot.class);
            startActivity(intent);
        });

        btnAdMob.setOnClickListener(v -> {
            Intent intent = new Intent(activity_dashboard.this, activity_advertising.class);
            startActivity(intent);
        });

        btnRealTimeChat.setOnClickListener(v -> {
            Intent intent = new Intent(activity_dashboard.this, activity_userchat.class);
            startActivity(intent);
        });
    }
}