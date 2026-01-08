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

public class userchatadapter extends RecyclerView.Adapter<userchatadapter.BaseViewHolder> {

    private List<ChatMessage> messages;
    private String currentUserId;

    public userchatadapter(List<ChatMessage> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        String sender = messages.get(position).getSender();
        if (sender.equals(currentUserId)) return 1; // sent
        if (sender.equals("system")) return 0; // system
        return 2; // received
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == 1) { // sent
            View view = inflater.inflate(R.layout.item_message_sent, parent, false);
            return new SentViewHolder(view);
        } else if (viewType == 2) { // received
            View view = inflater.inflate(R.layout.item_message_received, parent, false);
            return new ReceivedViewHolder(view);
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

    // Sent Message ViewHolder
    static class SentViewHolder extends BaseViewHolder {
        TextView tvMessage, tvTime;

        SentViewHolder(@NonNull View itemView) {
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

    // Received Message ViewHolder
    static class ReceivedViewHolder extends BaseViewHolder {
        TextView tvMessage, tvTime, tvSender;

        ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvSender = itemView.findViewById(R.id.tvSender);
        }

        @Override
        public void bind(ChatMessage message) {
            tvMessage.setText(message.getMessage());
            tvTime.setText(formatTime(message.getTimestamp()));
            tvSender.setText("User " + message.getSender());
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

    private static String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}