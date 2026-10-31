package com.mottainai.operacional.adapters;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mottainai.operacional.R;
import com.mottainai.operacional.databinding.ItemChatMessageBinding;
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
        return new ChatMessageViewHolder(ItemChatMessageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.message.setText(message.getContent());
        if (message.isSentByCurrentUser()) {
            holder.container.setGravity(Gravity.END);
            holder.sender.setGravity(Gravity.END);
            holder.sender.setText(R.string.chat_sender_user);
            holder.assistantAvatar.setVisibility(View.GONE);
            holder.message.setBackgroundResource(R.drawable.bg_chat_message_user);
            holder.message.setTextColor(holder.itemView.getContext().getColor(R.color.op_on_brand));
        } else {
            holder.container.setGravity(Gravity.START);
            holder.sender.setGravity(Gravity.START);
            holder.sender.setText(R.string.chat_sender_assistant);
            holder.assistantAvatar.setVisibility(View.VISIBLE);
            holder.message.setBackgroundResource(R.drawable.bg_chat_message_assistant);
            holder.message.setTextColor(holder.itemView.getContext().getColor(R.color.op_text));
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatMessageViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout container;
        final ImageView assistantAvatar;
        final TextView sender;
        final TextView message;

        ChatMessageViewHolder(@NonNull ItemChatMessageBinding binding) {
            super(binding.getRoot());
            container = binding.chatMessageContainer;
            assistantAvatar = binding.ivChatAssistantAvatar;
            sender = binding.tvChatSender;
            message = binding.tvChatMessage;
        }
    }
}
