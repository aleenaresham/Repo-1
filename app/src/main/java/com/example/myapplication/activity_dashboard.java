package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class activity_dashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Find buttons
        Button btnChatbot = findViewById(R.id.btnChatbot);
        Button btnAdMob = findViewById(R.id.btnAdMob);
        Button btnRealTimeChat = findViewById(R.id.btnRealTimeChat);

        // Set click listeners
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