package com.example.diseasemanagementapp;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class UserChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChat;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private Toolbar toolbar;

    private UserChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private DatabaseReference messagesDbRef;
    private DatabaseReference metadataDbRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chat);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // If no user is logged in, close the activity
            finish();
            return;
        }

        toolbar = findViewById(R.id.toolbar_user_chat);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerViewChat = findViewById(R.id.recyclerViewUserChat);
        editTextMessage = findViewById(R.id.editTextUserMessage);
        buttonSend = findViewById(R.id.buttonUserSend);

        // Setup RecyclerView
        chatMessages = new ArrayList<>();
        chatAdapter = new UserChatAdapter(chatMessages); // Use the new UserChatAdapter
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewChat.setLayoutManager(layoutManager);
        recyclerViewChat.setAdapter(chatAdapter);

        // Set the database reference to the current user's chat node
        DatabaseReference chatRootRef = FirebaseDatabase.getInstance().getReference("chats").child(currentUser.getUid());
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
        messageData.put("senderId", currentUser.getUid()); // Send with the user's UID
        messageData.put("timestamp", ServerValue.TIMESTAMP);

        if (messageId != null) {
            messagesDbRef.child(messageId).setValue(messageData);

            // Update metadata so the admin sees the new message in their list
            Map<String, Object> metadataUpdate = new HashMap<>();
            metadataUpdate.put("lastMessage", messageText);
            metadataUpdate.put("timestamp", ServerValue.TIMESTAMP);
            // Use display name or a default name
            String userName = currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()
                    ? currentUser.getDisplayName()
                    : "User";
            metadataUpdate.put("userName", userName);
            metadataDbRef.updateChildren(metadataUpdate);
        }

        editTextMessage.setText("");
    }

    private void listenForMessages() {
        messagesDbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
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
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}