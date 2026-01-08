package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton; // IMPORTANT: ImageButton import
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class activity_userchat extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText inputMessage;
    private ImageButton btnSend; // CHANGED: Button → ImageButton
    private Button btnClearChat, btnSwitchUser;
    private TextView tvStatus, tvLastMessage, tvCurrentUser;

    private List<ChatMessage> messageList;
    private userchatadapter adapter;
    private DatabaseReference databaseRef;

    private String currentUserId = "user1";
    private String otherUserId = "user2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userchat);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        inputMessage = findViewById(R.id.inputMessage);
        btnSend = findViewById(R.id.btnSend); // Now works with ImageButton
        btnClearChat = findViewById(R.id.btnClearChat);
        btnSwitchUser = findViewById(R.id.btnSwitchUser);
        tvStatus = findViewById(R.id.tvStatus);
        tvLastMessage = findViewById(R.id.tvLastMessage);
        tvCurrentUser = findViewById(R.id.tvCurrentUser);

        // Setup toolbar
        View btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("Real-Time Chat");

        // Initialize Firebase
        initializeFirebase();

        // Initialize message list
        messageList = new ArrayList<>();
        loadSampleMessages();

        // Setup RecyclerView
        adapter = new userchatadapter(messageList, currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Update UI
        updateUserInfo();
        tvStatus.setText("Status: Connected to Firebase • Online");

        // Setup button listeners
        setupButtonListeners();

        // Scroll to bottom
        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void initializeFirebase() {
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            databaseRef = database.getReference("chats");
            tvStatus.setText("Status: Firebase Connected • Real-time sync active");
        } catch (Exception e) {
            tvStatus.setText("Status: Firebase Offline • Using local storage");
        }
    }

    private void loadSampleMessages() {
        // Add sample messages with timestamps
        long currentTime = System.currentTimeMillis();

        ChatMessage msg1 = new ChatMessage();
        msg1.setMessage("Hey! Ready to test the real-time chat?");
        msg1.setSender(otherUserId);
        msg1.setTimestamp(currentTime - 600000); // 10 minutes ago
        messageList.add(msg1);

        ChatMessage msg2 = new ChatMessage();
        msg2.setMessage("Yes! This uses Firebase Firestore for instant messaging");
        msg2.setSender(currentUserId);
        msg2.setTimestamp(currentTime - 300000); // 5 minutes ago
        messageList.add(msg2);

        ChatMessage msg3 = new ChatMessage();
        msg3.setMessage("Can you see messages in real-time?");
        msg3.setSender(otherUserId);
        msg3.setTimestamp(currentTime - 120000); // 2 minutes ago
        messageList.add(msg3);

        ChatMessage msg4 = new ChatMessage();
        msg4.setMessage("Absolutely! Messages sync instantly across devices");
        msg4.setSender(currentUserId);
        msg4.setTimestamp(currentTime); // Now
        messageList.add(msg4);

        // Update last message time
        updateLastMessageTime(currentTime);
    }

    private void setupButtonListeners() {
        // Send button
        btnSend.setOnClickListener(v -> sendMessage());

        // Clear chat button
        btnClearChat.setOnClickListener(v -> clearChat());

        // Switch user button
        btnSwitchUser.setOnClickListener(v -> switchUser());

        // Send on Enter key
        inputMessage.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == 66) { // Enter key
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void sendMessage() {
        String messageText = inputMessage.getText().toString().trim();
        if (!messageText.isEmpty()) {
            // Create message object
            ChatMessage message = new ChatMessage();
            message.setMessage(messageText);
            message.setSender(currentUserId);
            message.setTimestamp(System.currentTimeMillis());

            // Add to local list
            messageList.add(message);
            adapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.scrollToPosition(messageList.size() - 1);
            inputMessage.setText("");

            // Save to Firebase if available
            if (databaseRef != null) {
                String key = databaseRef.push().getKey();
                databaseRef.child(key).setValue(message);
            }

            // Update last message time
            updateLastMessageTime(message.getTimestamp());

            // Simulate reply from other user
            simulateReply(messageText);

            // Show success message
            Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
        }
    }

    private void simulateReply(String userMessage) {
        new android.os.Handler().postDelayed(() -> {
            String reply = generateReply(userMessage);

            ChatMessage replyMessage = new ChatMessage();
            replyMessage.setMessage(reply);
            replyMessage.setSender(otherUserId);
            replyMessage.setTimestamp(System.currentTimeMillis());

            messageList.add(replyMessage);
            adapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.scrollToPosition(messageList.size() - 1);

            // Update last message time
            updateLastMessageTime(replyMessage.getTimestamp());
        }, 2000);
    }

    private String generateReply(String userMessage) {
        String lowerMsg = userMessage.toLowerCase();

        if (lowerMsg.contains("hello") || lowerMsg.contains("hi") || lowerMsg.contains("hey")) {
            return "Hi there! Thanks for your message. This is a simulated reply.";
        } else if (lowerMsg.contains("firebase")) {
            return "Yes, Firebase provides real-time database sync across all devices!";
        } else if (lowerMsg.contains("timestamp") || lowerMsg.contains("time")) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
            return "Message received at " + sdf.format(new Date()) + " with timestamp.";
        } else if (lowerMsg.contains("how are you")) {
            return "I'm doing great! Just simulating real-time chat responses.";
        } else {
            return "I received your message: \"" + userMessage + "\". This simulates real-time chat!";
        }
    }

    private void clearChat() {
        messageList.clear();
        adapter.notifyDataSetChanged();

        // Add system message
        ChatMessage systemMsg = new ChatMessage();
        systemMsg.setMessage("Chat cleared. Start a new conversation!");
        systemMsg.setSender("system");
        systemMsg.setTimestamp(System.currentTimeMillis());
        messageList.add(systemMsg);
        adapter.notifyItemInserted(0);

        tvLastMessage.setText("Last message: Chat cleared");
        Toast.makeText(this, "Chat history cleared", Toast.LENGTH_SHORT).show();
    }

    private void switchUser() {
        if (currentUserId.equals("user1")) {
            currentUserId = "user2";
            otherUserId = "user1";
        } else {
            currentUserId = "user1";
            otherUserId = "user2";
        }

        adapter.setCurrentUserId(currentUserId);
        adapter.notifyDataSetChanged();
        updateUserInfo();

        Toast.makeText(this, "Switched to User: " + currentUserId, Toast.LENGTH_SHORT).show();
    }

    private void updateUserInfo() {
        tvCurrentUser.setText("Current User: " + currentUserId);
    }

    private void updateLastMessageTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
        String time = sdf.format(new Date(timestamp));
        tvLastMessage.setText("Last message: " + time);
    }
}