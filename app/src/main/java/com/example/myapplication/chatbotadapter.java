package com.example.myapplication;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class chatbotadapter extends RecyclerView.Adapter<chatbotadapter.BaseViewHolder> {

    private List<ChatMessage> messages;

    public chatbotadapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        String sender = messages.get(position).getSender();
        if (sender.equals("user")) return 1;
        if (sender.equals("bot")) return 2;
        if (sender.equals("typing")) return 3;
        return 0; // system
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == 1) { // user
            View view = inflater.inflate(R.layout.item_message_user, parent, false);
            return new UserViewHolder(view);
        } else if (viewType == 2) { // bot
            View view = inflater.inflate(R.layout.item_message_bot, parent, false);
            return new BotViewHolder(view);
        } else if (viewType == 3) { // typing
            View view = inflater.inflate(R.layout.item_message_typing, parent, false);
            return new TypingViewHolder(view);
        } else { // system
            View view = inflater.inflate(R.layout.item_message_system, parent, false);
            return new SystemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // Base ViewHolder class
    abstract static class BaseViewHolder extends RecyclerView.ViewHolder {
        public BaseViewHolder(@NonNull View itemView) {
            super(itemView);
        }
        public abstract void bind(ChatMessage message);
    }

    // User Message ViewHolder
    static class UserViewHolder extends BaseViewHolder {
        TextView tvMessage, tvTime;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        @Override
        public void bind(ChatMessage message) {
            tvMessage.setText(message.getMessage());
            tvTime.setText(formatTime(message.getTimestamp()));
        }
    }

    // Bot Message ViewHolder
    static class BotViewHolder extends BaseViewHolder {
        TextView tvMessage, tvTime, tvLabel;

        BotViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvLabel = itemView.findViewById(R.id.tvLabel);
        }

        @Override
        public void bind(ChatMessage message) {
            tvMessage.setText(message.getMessage());
            tvTime.setText(formatTime(message.getTimestamp()));
            tvLabel.setText("AI Assistant");
        }
    }

    // System Message ViewHolder
    static class SystemViewHolder extends BaseViewHolder {
        TextView tvMessage;

        SystemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }

        @Override
        public void bind(ChatMessage message) {
            tvMessage.setText(message.getMessage());
        }
    }

    // Typing ViewHolder
    static class TypingViewHolder extends BaseViewHolder {

        TypingViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void bind(ChatMessage message) {
            // Nothing to bind for typing indicator
        }
    }

    private static String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
