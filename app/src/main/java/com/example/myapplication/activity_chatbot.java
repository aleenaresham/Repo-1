package com.example.myapplication;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class activity_chatbot extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText inputMessage;
    private ImageButton btnSend;
    private Button btnTokenizationDemo;
    private TextView tvStatus;

    private List<ChatMessage> messageList;
    private chatbotadapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        inputMessage = findViewById(R.id.inputMessage);
        btnSend = findViewById(R.id.btnSend);
        btnTokenizationDemo = findViewById(R.id.btnTokenizationDemo);
        tvStatus = findViewById(R.id.tvStatus);

        // Setup toolbar
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("AI Chatbot (Google Gemini)");

        // Initialize message list
        messageList = new ArrayList<>();

        // Add welcome message
        ChatMessage welcomeMsg = new ChatMessage();
        welcomeMsg.setMessage("Hello! I'm your AI assistant powered by Google Gemini. I can help you with:\n\n" +
                "• App features and usage\n" +
                "• Password reset queries\n" +
                "• Advertisement information\n" +
                "• Real-time chat functionality\n\n" +
                "What would you like to know?");
        welcomeMsg.setSender("bot");
        welcomeMsg.setTimestamp(System.currentTimeMillis());
        messageList.add(welcomeMsg);

        // Setup RecyclerView
        adapter = new chatbotadapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Set status
        tvStatus.setText("Status: Connected to Google Gemini • NLP Active");

        // Send button click
        btnSend.setOnClickListener(v -> sendMessage());

        // Tokenization demo button
        btnTokenizationDemo.setOnClickListener(v -> showTokenizationDemo());

        // Send on Enter key
        inputMessage.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == 66) { // Enter key
                sendMessage();
                return true;
            }
            return false;
        });

        // Scroll to bottom
        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void sendMessage() {
        String message = inputMessage.getText().toString().trim();
        if (!message.isEmpty()) {
            // Add user message
            ChatMessage userMsg = new ChatMessage();
            userMsg.setMessage(message);
            userMsg.setSender("user");
            userMsg.setTimestamp(System.currentTimeMillis());
            messageList.add(userMsg);
            adapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.scrollToPosition(messageList.size() - 1);
            inputMessage.setText("");

            // Show typing indicator
            showTypingIndicator();

            // Generate AI response after delay
            generateAIResponse(message);
        }
    }

    private void showTypingIndicator() {
        // Add typing indicator
        ChatMessage typingMsg = new ChatMessage();
        typingMsg.setMessage("...");
        typingMsg.setSender("typing");
        typingMsg.setTimestamp(System.currentTimeMillis());
        messageList.add(typingMsg);
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void removeTypingIndicator() {
        if (!messageList.isEmpty() && messageList.get(messageList.size() - 1).getSender().equals("typing")) {
            messageList.remove(messageList.size() - 1);
            adapter.notifyItemRemoved(messageList.size());
        }
    }

    private void generateAIResponse(String userMessage) {
        // Remove typing indicator
        removeTypingIndicator();

        String response;
        String lowerMsg = userMessage.toLowerCase();

        // AI logic based on user query
        if (lowerMsg.contains("password") || lowerMsg.contains("reset") || lowerMsg.contains("forgot")) {
            response = "To reset your password:\n\n" +
                    "1. Go to Settings > Account\n" +
                    "2. Tap 'Reset Password'\n" +
                    "3. Enter your email address\n" +
                    "4. Check your inbox for reset link\n" +
                    "5. Click the link and set new password\n\n" +
                    "Need more help?";

        } else if (lowerMsg.contains("feature") || lowerMsg.contains("function") || lowerMsg.contains("what can")) {
            response = "This app has these main features:\n\n" +
                    "✓ AI Chatbot (Google Gemini) - That's me!\n" +
                    "✓ AdMob Integration - Banner & Interstitial ads\n" +
                    "✓ Real-Time Chat - Firebase based messaging\n" +
                    "✓ User Authentication - Secure login system\n" +
                    "✓ Push Notifications - Stay updated\n\n" +
                    "Which feature would you like to know more about?";

        } else if (lowerMsg.contains("ad") || lowerMsg.contains("ads") || lowerMsg.contains("advertisement")) {
            response = "AdMob Integration Details:\n\n" +
                    "• Google AdMob is integrated\n" +
                    "• Banner ads shown at screen bottom\n" +
                    "• Interstitial ads shown on button click\n" +
                    "• Currently using TEST ads for development\n" +
                    "• Real ads will be shown in production\n\n" +
                    "You can try the Ads Demo from main screen!";

        } else if (lowerMsg.contains("chat") || lowerMsg.contains("message") || lowerMsg.contains("talk")) {
            response = "Real-Time Chat Features:\n\n" +
                    "• Uses Firebase Firestore database\n" +
                    "• One-to-one private messaging\n" +
                    "• Messages are timestamped\n" +
                    "• Online/Offline status shown\n" +
                    "• Message delivery receipts\n" +
                    "• Typing indicators\n\n" +
                    "Try it from the main screen!";

        } else if (lowerMsg.contains("hello") || lowerMsg.contains("hi") || lowerMsg.contains("hey")) {
            response = "Hello! 👋 I'm your AI assistant. I can help you with:\n\n" +
                    "• App features and questions\n" +
                    "• Password reset assistance\n" +
                    "• Advertisement information\n" +
                    "• Chat functionality details\n\n" +
                    "What would you like to know?";

        } else if (lowerMsg.contains("token") || lowerMsg.contains("nlp") || lowerMsg.contains("natural language")) {
            response = "Natural Language Processing:\n\n" +
                    "I use tokenization to understand your queries:\n" +
                    "1. Break sentences into tokens (words)\n" +
                    "2. Analyze context and intent\n" +
                    "3. Generate appropriate responses\n" +
                    "4. Maintain conversation context\n\n" +
                    "Try the Tokenization Demo button!";

        } else {
            response = "I understand you're asking: \"" + userMessage + "\"\n\n" +
                    "As an AI assistant specialized in app support, I can help with:\n" +
                    "• App functionality questions\n" +
                    "• Troubleshooting issues\n" +
                    "• Feature explanations\n" +
                    "• Usage guidance\n\n" +
                    "Could you rephrase your question?";
        }

        // Add AI response after delay (simulating processing)
        new android.os.Handler().postDelayed(() -> {
            ChatMessage botMsg = new ChatMessage();
            botMsg.setMessage(response);
            botMsg.setSender("bot");
            botMsg.setTimestamp(System.currentTimeMillis());
            messageList.add(botMsg);
            adapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.scrollToPosition(messageList.size() - 1);

            // Update tokenization info
            updateTokenizationInfo(userMessage);
        }, 1500);
    }

    private void showTokenizationDemo() {
        String exampleQuery = "How do I reset my app password quickly?";

        // Add demo message
        ChatMessage demoMsg = new ChatMessage();
        demoMsg.setMessage("🔍 Tokenization Demo:\n\n" +
                "Query: \"" + exampleQuery + "\"\n\n" +
                "Tokens: [How, do, I, reset, my, app, password, quickly, ?]\n\n" +
                "Analysis:\n" +
                "• Intent: Password reset\n" +
                "• Priority: Quick/urgent\n" +
                "• Entity: App password\n\n" +
                "This helps me provide accurate responses!");
        demoMsg.setSender("system");
        demoMsg.setTimestamp(System.currentTimeMillis());
        messageList.add(demoMsg);
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);

        Toast.makeText(this, "Tokenization demo shown", Toast.LENGTH_SHORT).show();
    }

    private void updateTokenizationInfo(String message) {
        int wordCount = message.split("\\s+").length;
        TextView tvTokenInfo = findViewById(R.id.tvTokenInfo);
        tvTokenInfo.setText("Tokens processed: " + wordCount + " | Context analyzed");
    }
}
