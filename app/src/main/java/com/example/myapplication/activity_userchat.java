package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class activity_userchat extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText inputMessage;
    private ImageButton btnSend;
    private Button btnClearChat, btnSwitchUser;
    private TextView tvStatus, tvLastMessage, tvCurrentUser;

    private List<ChatMessage> messageList;
    private userchatadapter adapter;
    private DatabaseReference databaseRef;

    private String chatRoomId = "user1_user2_chat";
    private String currentUserId = "user1";
    private String otherUserId = "user2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userchat);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        inputMessage = findViewById(R.id.inputMessage);
        btnSend = findViewById(R.id.btnSend);
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

        // Setup RecyclerView
        adapter = new userchatadapter(messageList, currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Load sample messages
        loadSampleMessages();

        // Setup button listeners
        setupButtonListeners();

        // Scroll to bottom
        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void initializeFirebase() {
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            databaseRef = database.getReference("chats").child(chatRoomId);
            tvStatus.setText("Status: Firebase Connected • Real-time active");
            loadMessagesFromFirebase();
        } catch (Exception e) {
            tvStatus.setText("Status: Using Local Demo");
            Toast.makeText(this, "Firebase: Local mode", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMessagesFromFirebase() {
        if (databaseRef != null) {
            databaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    messageList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ChatMessage message = snapshot.getValue(ChatMessage.class);
                        if (message != null) {
                            messageList.add(message);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    if (!messageList.isEmpty()) {
                        updateLastMessageTime(messageList.get(messageList.size() - 1).getTimestamp());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    tvStatus.setText("Status: Error loading");
                }
            });
        }
    }

    private void loadSampleMessages() {
        long currentTime = System.currentTimeMillis();

        // Sample messages
        addMessage("Hello! Testing real-time chat", otherUserId, currentTime - 600000);
        addMessage("Yes! Firebase messages work", currentUserId, currentTime - 300000);
        addMessage("Messages are timestamped", otherUserId, currentTime - 120000);
        addMessage("Perfect! Real-time demo", currentUserId, currentTime);
    }

    private void addMessage(String text, String sender, long time) {
        ChatMessage msg = new ChatMessage();
        msg.setMessage(text);
        msg.setSender(sender);
        msg.setTimestamp(time);
        messageList.add(msg);
    }

    private void setupButtonListeners() {
        btnSend.setOnClickListener(v -> sendMessage());
        btnClearChat.setOnClickListener(v -> clearChat());
        btnSwitchUser.setOnClickListener(v -> switchUser());

        inputMessage.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == 66) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void sendMessage() {
        String messageText = inputMessage.getText().toString().trim();
        if (!messageText.isEmpty()) {
            long timestamp = System.currentTimeMillis();

            ChatMessage message = new ChatMessage();
            message.setMessage(messageText);
            message.setSender(currentUserId);
            message.setReceiver(otherUserId);
            message.setTimestamp(timestamp);

            // Save to Firebase
            if (databaseRef != null) {
                String key = databaseRef.push().getKey();
                message.setMessageId(key);
                databaseRef.child(key).setValue(message);
            }

            // Add to local list
            messageList.add(message);
            adapter.notifyItemInserted(messageList.size() - 1);
            inputMessage.setText("");

            updateLastMessageTime(timestamp);
            Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearChat() {
        messageList.clear();
        adapter.notifyDataSetChanged();

        // Clear Firebase
        if (databaseRef != null) {
            databaseRef.removeValue();
        }

        // Add system message
        ChatMessage systemMsg = new ChatMessage();
        systemMsg.setMessage("Chat cleared");
        systemMsg.setSender("system");
        systemMsg.setTimestamp(System.currentTimeMillis());
        messageList.add(systemMsg);
        adapter.notifyItemInserted(0);

        tvLastMessage.setText("Last: Chat cleared");
        Toast.makeText(this, "Chat cleared", Toast.LENGTH_SHORT).show();
    }

    private void switchUser() {
        String temp = currentUserId;
        currentUserId = otherUserId;
        otherUserId = temp;

        adapter.setCurrentUserId(currentUserId);
        adapter.notifyDataSetChanged();
        updateUserInfo();
        Toast.makeText(this, "Switched to: " + currentUserId, Toast.LENGTH_SHORT).show();
    }

    private void updateUserInfo() {
        tvCurrentUser.setText("User: " + currentUserId);
    }

    private void updateLastMessageTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String time = sdf.format(new Date(timestamp));
        tvLastMessage.setText("Last: " + time);
    }
}