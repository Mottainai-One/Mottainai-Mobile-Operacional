package com.mottainai.operacional.adapters;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mottainai.operacional.R;
import com.mottainai.operacional.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

/** Adaptador simples para mensagens do usuário e respostas da assistente. */
public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ChatMessageViewHolder> {

    private final List<ChatMessage> messages = new ArrayList<>();

    public void replaceMessages(List<ChatMessage> updatedMessages) {
        messages.clear();
        if (updatedMessages != null) {
            messages.addAll(updatedMessages);
        }
        notifyDataSetChanged();
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    @NonNull
    @Override
    public ChatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.message.setText(message.getContent());
        if (message.isSentByCurrentUser()) {
            holder.container.setGravity(Gravity.END);
            holder.message.setBackgroundResource(R.drawable.bg_chat_message_user);
            holder.message.setTextColor(holder.itemView.getContext().getColor(R.color.white));
        } else {
            holder.container.setGravity(Gravity.START);
            holder.message.setBackgroundResource(R.drawable.bg_chat_message_assistant);
            holder.message.setTextColor(holder.itemView.getContext().getColor(R.color.text_primary));
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatMessageViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout container;
        final TextView message;

        ChatMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.chat_message_container);
            message = itemView.findViewById(R.id.tv_chat_message);
        }
    }
}
