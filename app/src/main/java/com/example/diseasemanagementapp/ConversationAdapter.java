package com.example.diseasemanagementapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {

    private final Context context;
    private final List<Conversation> conversationList;

    public ConversationAdapter(Context context, List<Conversation> conversationList) {
        this.context = context;
        this.conversationList = conversationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation conversation = conversationList.get(position);
        holder.userName.setText(conversation.getUserName());
        holder.lastMessage.setText(conversation.getLastMessage());
        holder.timestamp.setText(conversation.getFormattedTimestamp());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminChatActivity.class);
            intent.putExtra("USER_ID", conversation.getUserId());
            intent.putExtra("USER_NAME", conversation.getUserName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName, lastMessage, timestamp;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.textViewUserName);
            lastMessage = itemView.findViewById(R.id.textViewLastMessage);
            timestamp = itemView.findViewById(R.id.textViewTimestamp);
        }
    }
}