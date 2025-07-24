package com.example.diseasemanagementapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminChatListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ConversationAdapter adapter;
    private List<Conversation> conversationList;
    private DatabaseReference chatsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chat_list);

        recyclerView = findViewById(R.id.recyclerViewConversations);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        conversationList = new ArrayList<>();
        adapter = new ConversationAdapter(this, conversationList);
        recyclerView.setAdapter(adapter);

        chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        fetchConversations();
    }

    private void fetchConversations() {
        chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                conversationList.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    // The key of the snapshot is the user's ID
                    String userId = userSnapshot.getKey();

                    // Fetch data from the 'metadata' node
                    DataSnapshot metaSnapshot = userSnapshot.child("metadata");

                    Conversation conversation = new Conversation();
                    conversation.setUserId(userId);
                    conversation.setUserName(metaSnapshot.child("userName").getValue(String.class));
                    conversation.setLastMessage(metaSnapshot.child("lastMessage").getValue(String.class));
                    conversation.setTimestamp(metaSnapshot.child("timestamp").getValue(Long.class));

                    conversationList.add(conversation);
                }
                // Sort by timestamp, descending (most recent first)
                Collections.sort(conversationList, (c1, c2) -> Long.compare(c2.getTimestamp(), c1.getTimestamp()));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }
}