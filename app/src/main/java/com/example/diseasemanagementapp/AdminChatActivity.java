package com.example.diseasemanagementapp;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton; // Corrected import for the send button
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChat;
    private EditText editTextMessage;
    private ImageButton buttonSend; // Changed to ImageButton for better UI
    private Toolbar toolbar;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private DatabaseReference messagesDbRef;
    private DatabaseReference metadataDbRef;
    private String userId, userName;
    private final String ADMIN_ID = "ADMIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chat);

        // Get user info from Intent, handle potential nulls
        userId = getIntent().getStringExtra("USER_ID");
        userName = getIntent().getStringExtra("USER_NAME");
        if (userId == null || userId.isEmpty()) {
            // Handle error: No user ID provided
            finish();
            return;
        }

        // Use the correct toolbar ID from activity_admin_chat.xml
        toolbar = findViewById(R.id.toolbar_admin_chat);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(userName != null ? userName : "Chat");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show back button
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());


        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);

        // Setup RecyclerView
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // So new messages appear at the bottom
        recyclerViewChat.setLayoutManager(layoutManager);
        recyclerViewChat.setAdapter(chatAdapter);

        // Firebase database references
        DatabaseReference chatRootRef = FirebaseDatabase.getInstance().getReference("chats").child(userId);
        messagesDbRef = chatRootRef.child("messages");
        metadataDbRef = chatRootRef.child("metadata");

        buttonSend.setOnClickListener(v -> sendMessage());
        listenForMessages();
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        String messageId = messagesDbRef.push().getKey();

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("text", messageText);
        messageData.put("senderId", ADMIN_ID);
        messageData.put("timestamp", ServerValue.TIMESTAMP);

        if (messageId != null) {
            // Send the message
            messagesDbRef.child(messageId).setValue(messageData);

            // Update the metadata for the conversation list screen
            Map<String, Object> metadataUpdate = new HashMap<>();
            metadataUpdate.put("lastMessage", messageText);
            metadataUpdate.put("timestamp", ServerValue.TIMESTAMP);
            metadataUpdate.put("userName", userName); // Also update username in case it's new
            metadataDbRef.updateChildren(metadataUpdate);
        }

        editTextMessage.setText("");
    }

    private void listenForMessages() {
        messagesDbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // This is where the ChatMessage class is crucial
                ChatMessage message = snapshot.getValue(ChatMessage.class);
                if (message != null) {
                    chatMessages.add(message);
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
                }
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String s) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String s) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {
                // Log error or show a toast
            }
});
}
}