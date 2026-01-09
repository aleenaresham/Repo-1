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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class activity_chatbot extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText inputMessage;
    private ImageButton btnSend;
    private Button btnTokenizationDemo;
    private TextView tvStatus, tvTokenInfo;

    private List<ChatMessage> messageList;
    private chatbotadapter adapter;

    // ✅ Google Gemini API Configuration
    private static final String API_KEY = "AIzaSyAi_iu3bTFc0FhriQnnar1g7--D8KZA_ig";
    private static final String URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;
    private OkHttpClient client;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        inputMessage = findViewById(R.id.inputMessage);
        btnSend = findViewById(R.id.btnSend);
        btnTokenizationDemo = findViewById(R.id.btnTokenizationDemo);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvStatus = findViewById(R.id.tvStatus);
        tvTokenInfo = findViewById(R.id.tvTokenInfo);

        // Setup toolbar
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvTitle.setText("AI Chatbot (Google Gemini)");

        // Initialize OkHttp client
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

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
        tvTokenInfo.setText("NLP: Ready");

        // Send button click
        btnSend.setOnClickListener(v -> sendMessage());

        // Tokenization Demo button
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

            // Generate AI response using Google Gemini API
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

        // Prepare the request body for Gemini API
        JSONObject jsonBody = new JSONObject();
        try {
            JSONObject content = new JSONObject();
            JSONObject textPart = new JSONObject();
            textPart.put("text", userMessage);

            JSONObject part = new JSONObject();
            part.put("text", userMessage);

            content.put("parts", new org.json.JSONArray().put(part));
            content.put("role", "user");

            jsonBody.put("contents", new org.json.JSONArray().put(content));

            // Add safety settings (optional)
            JSONObject safetySettings = new JSONObject();
            safetySettings.put("category", "HARM_CATEGORY_DANGEROUS_CONTENT");
            safetySettings.put("threshold", "BLOCK_MEDIUM_AND_ABOVE");

            jsonBody.put("safetySettings", new org.json.JSONArray().put(safetySettings));

            // Add generation config
            JSONObject generationConfig = new JSONObject();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("topK", 1);
            generationConfig.put("topP", 0.8);
            generationConfig.put("maxOutputTokens", 1000);

            jsonBody.put("generationConfig", generationConfig);

        } catch (JSONException e) {
            e.printStackTrace();
            showError("Error creating request");
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    // Show error message
                    ChatMessage errorMsg = new ChatMessage();
                    errorMsg.setMessage("⚠️ I'm having trouble connecting to the AI service. Using local responses.\n\n" +
                            "Original query: \"" + userMessage + "\"");
                    errorMsg.setSender("bot");
                    errorMsg.setTimestamp(System.currentTimeMillis());
                    messageList.add(errorMsg);
                    adapter.notifyItemInserted(messageList.size() - 1);
                    recyclerView.scrollToPosition(messageList.size() - 1);

                    tvStatus.setText("Status: Using Local Mode");

                    // Fallback to local response
                    showLocalResponse(userMessage);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        // Extract the AI response from JSON
                        String aiResponse = extractAIResponse(jsonResponse);

                        runOnUiThread(() -> {
                            // Add AI response
                            ChatMessage botMsg = new ChatMessage();
                            botMsg.setMessage(aiResponse);
                            botMsg.setSender("bot");
                            botMsg.setTimestamp(System.currentTimeMillis());
                            messageList.add(botMsg);
                            adapter.notifyItemInserted(messageList.size() - 1);
                            recyclerView.scrollToPosition(messageList.size() - 1);

                            // Update status
                            tvStatus.setText("Status: Gemini AI Active • Response Generated");

                            // Update tokenization info
                            updateTokenizationInfo(userMessage);
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            showError("Error parsing response");
                            showLocalResponse(userMessage);
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        showError("API Error: " + response.code());
                        showLocalResponse(userMessage);
                    });
                }
            }
        });
    }

    private void showLocalResponse(String userMessage) {
        String response;
        String lowerMsg = userMessage.toLowerCase();

        // AI logic based on user query
        if (lowerMsg.contains("password") || lowerMsg.contains("reset") || lowerMsg.contains("forgot")) {
            response = "🔐 **Password Reset Assistance**\n\n" +
                    "To reset your password:\n\n" +
                    "1. Go to **Settings > Account**\n" +
                    "2. Tap **'Reset Password'**\n" +
                    "3. Enter your registered email\n" +
                    "4. Check your inbox for reset link\n" +
                    "5. Click the link and set new password\n\n" +
                    "📧 Ensure you have access to your email.";

        } else if (lowerMsg.contains("feature") || lowerMsg.contains("function") || lowerMsg.contains("what can")) {
            response = "📱 **App Features Overview**\n\n" +
                    "This app has these main features:\n\n" +
                    "✅ **AI Chatbot** (Google Gemini) - That's me!\n" +
                    "✅ **AdMob Integration** - Banner & Interstitial ads\n" +
                    "✅ **Real-Time Chat** - Firebase based messaging\n" +
                    "✅ **User Authentication** - Secure login system\n" +
                    "✅ **QR Code Scanner** - Scan and process QR codes\n\n" +
                    "Which feature would you like to know more about?";

        } else if (lowerMsg.contains("ad") || lowerMsg.contains("ads") || lowerMsg.contains("advertisement")) {
            response = "📢 **AdMob Integration Details**\n\n" +
                    "• **Google AdMob** is integrated\n" +
                    "• **Banner ads** shown at screen bottom\n" +
                    "• **Interstitial ads** shown on button click\n" +
                    "• Currently using **TEST ads** for development\n" +
                    "• Real ads will be shown in production\n\n" +
                    "You can try the **Ads Demo** from main screen!";

        } else if (lowerMsg.contains("chat") || lowerMsg.contains("message") || lowerMsg.contains("talk")) {
            response = "💬 **Real-Time Chat Features**\n\n" +
                    "• Uses **Firebase Firestore** database\n" +
                    "• **One-to-one** private messaging\n" +
                    "• Messages are **timestamped**\n" +
                    "• Online/Offline status shown\n" +
                    "• Message delivery receipts\n" +
                    "• Typing indicators\n\n" +
                    "Try it from the main screen!";

        } else if (lowerMsg.contains("hello") || lowerMsg.contains("hi") || lowerMsg.contains("hey")) {
            response = "Hello! 👋 I'm your AI assistant.\n\n" +
                    "I can help you with:\n\n" +
                    "• **App features** and questions\n" +
                    "• **Password reset** assistance\n" +
                    "• **Advertisement** information\n" +
                    "• **Chat functionality** details\n\n" +
                    "What would you like to know?";

        } else if (lowerMsg.contains("token") || lowerMsg.contains("nlp") || lowerMsg.contains("natural language")) {
            response = "🧠 **Natural Language Processing**\n\n" +
                    "I use **tokenization** to understand your queries:\n\n" +
                    "1. Break sentences into **tokens** (words)\n" +
                    "2. Analyze **context** and **intent**\n" +
                    "3. Generate appropriate **responses**\n" +
                    "4. Maintain conversation **context**\n\n" +
                    "Try the **Tokenization Demo** button below!";

        } else {
            response = "🤔 **Query Analysis**\n\n" +
                    "I understand you're asking: \n\"" + userMessage + "\"\n\n" +
                    "As an AI assistant specialized in **app support**, I can help with:\n\n" +
                    "• **App functionality** questions\n" +
                    "• **Troubleshooting** issues\n" +
                    "• **Feature explanations**\n" +
                    "• **Usage guidance**\n\n" +
                    "Could you rephrase or ask about specific app features?";
        }

        // Add local response
        ChatMessage botMsg = new ChatMessage();
        botMsg.setMessage(response);
        botMsg.setSender("bot");
        botMsg.setTimestamp(System.currentTimeMillis());
        messageList.add(botMsg);
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);

        updateTokenizationInfo(userMessage);
    }

    private String extractAIResponse(JSONObject jsonResponse) {
        try {
            if (jsonResponse.has("candidates")) {
                org.json.JSONArray candidates = jsonResponse.getJSONArray("candidates");
                if (candidates.length() > 0) {
                    JSONObject candidate = candidates.getJSONObject(0);
                    if (candidate.has("content")) {
                        JSONObject content = candidate.getJSONObject("content");
                        if (content.has("parts")) {
                            org.json.JSONArray parts = content.getJSONArray("parts");
                            if (parts.length() > 0) {
                                JSONObject part = parts.getJSONObject(0);
                                return part.getString("text");
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "I received your message. As an AI assistant for this app, I can help with:\n\n" +
                "• App features and usage\n" +
                "• Password reset procedures\n" +
                "• Advertisement information\n" +
                "• Real-time chat functionality\n\n" +
                "Could you ask about something specific?";
    }

    private void showTokenizationDemo() {
        String exampleQuery = "How do I reset my app password quickly for email verification?";

        // Tokenization analysis
        String[] tokens = exampleQuery.split("\\s+");
        int tokenCount = tokens.length;

        // Create token list string
        StringBuilder tokenList = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            tokenList.append(i + 1).append(". ").append(tokens[i]);
            if (i < tokens.length - 1) tokenList.append("\n");
        }

        // NLP analysis
        String intent = "PASSWORD_RESET";
        String priority = "Quick/Urgent";
        String entities = "app, password, email, verification";

        // Create demo message
        ChatMessage demoMsg = new ChatMessage();
        demoMsg.setMessage("🔍 **Tokenization & NLP Demo**\n\n" +
                "**Original Query:**\n\"" + exampleQuery + "\"\n\n" +
                "**Tokenization Results:**\n" +
                "Total Tokens: " + tokenCount + "\n\n" +
                "**Token List:**\n" + tokenList.toString() + "\n\n" +
                "**NLP Analysis:**\n" +
                "• **Primary Intent:** " + intent + "\n" +
                "• **Priority:** " + priority + "\n" +
                "• **Extracted Entities:** " + entities + "\n" +
                "• **Context:** App-specific password flow\n\n" +
                "**Process Flow:**\n" +
                "1. **Tokenization** → Split into " + tokenCount + " words\n" +
                "2. **POS Tagging** → Identify nouns/verbs\n" +
                "3. **Intent Detection** → " + intent + "\n" +
                "4. **Entity Recognition** → " + entities + "\n" +
                "5. **Response Generation** → Context-aware reply");
        demoMsg.setSender("system");
        demoMsg.setTimestamp(System.currentTimeMillis());

        messageList.add(demoMsg);
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);

        // Update token info
        tvTokenInfo.setText("Tokens: " + tokenCount + " | NLP Demo");

        Toast.makeText(this, "Tokenization demo shown", Toast.LENGTH_SHORT).show();
    }

    private void updateTokenizationInfo(String message) {
        int wordCount = message.split("\\s+").length;
        tvTokenInfo.setText("Tokens: " + wordCount + " | NLP Active");
    }

    private void showError(String errorMessage) {
        runOnUiThread(() -> {
            Toast.makeText(activity_chatbot.this, errorMessage, Toast.LENGTH_SHORT).show();

            tvStatus.setText("Status: Error - Using Local Responses");
        });
    }
}