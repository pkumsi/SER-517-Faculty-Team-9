package com.example.carma_android_app;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carma_android_app.database.FeedbackEntity;
import com.example.carma_android_app.database.MessageEntity;
import com.google.android.material.chip.Chip;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Lists saved preview messages from Room ({@link MessageEntity}).
 */
public class SavedMessageAdapter extends RecyclerView.Adapter<SavedMessageAdapter.ViewHolder> {

    public interface OnMessageClickListener {
        void onMessageClick(MessageEntity message);
    }

    private List<MessageEntity> messages = new ArrayList<>();
    private Map<Long, String> feedbackTypeByMessageId = new HashMap<>();
    private final DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
    private OnMessageClickListener messageClickListener;

    public void setOnMessageClickListener(OnMessageClickListener listener) {
        this.messageClickListener = listener;
    }

    public void setData(List<MessageEntity> messages, Map<Long, String> feedbackTypeByMessageId) {
        this.messages = messages != null ? messages : new ArrayList<>();
        this.feedbackTypeByMessageId = feedbackTypeByMessageId != null ? feedbackTypeByMessageId : new HashMap<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessageEntity m = messages.get(position);
        String recipient = m.getRecipientName();
        holder.tvRecipient.setText(recipient != null && !recipient.isEmpty() ? recipient : "Unknown");

        holder.tvTimestamp.setText(timeFormat.format(m.getTimestamp()));

        String text = m.getMessageText();
        holder.tvPreview.setText(text != null ? text : "");

        String activity = safe(m.getContextActivity());
        String sender = safe(m.getContextSender());
        String urgency = safe(m.getContextUrgency());

        if (!activity.isEmpty()) {
            holder.chip1.setVisibility(View.VISIBLE);
            holder.chip1.setText(activity);
        } else {
            holder.chip1.setVisibility(View.GONE);
        }
        if (!sender.isEmpty()) {
            holder.chip2.setVisibility(View.VISIBLE);
            holder.chip2.setText(sender);
        } else if (!urgency.isEmpty()) {
            holder.chip2.setVisibility(View.VISIBLE);
            holder.chip2.setText(urgency);
        } else {
            holder.chip2.setVisibility(View.GONE);
        }
        holder.tvMoreTags.setVisibility(View.GONE);

        String status = m.getStatus() != null ? m.getStatus() : "pending";
        int barColor;
        if ("sent".equalsIgnoreCase(status)) {
            barColor = Color.parseColor("#4CAF50");
        } else if ("cancelled".equalsIgnoreCase(status)) {
            barColor = Color.parseColor("#F44336");
        } else {
            barColor = Color.parseColor("#BDBDBD");
        }
        holder.statusBar.setBackgroundColor(barColor);

        String fb = feedbackTypeByMessageId.get(m.getId());
        if (FeedbackEntity.THUMBS_UP.equals(fb)) {
            holder.ivFeedback.setVisibility(View.VISIBLE);
            holder.ivFeedback.setImageResource(R.drawable.ic_thumbs_up);
            holder.ivFeedback.setColorFilter(Color.parseColor("#4CAF50"));
            holder.tvNoFeedback.setVisibility(View.GONE);
        } else if (FeedbackEntity.THUMBS_DOWN.equals(fb)) {
            holder.ivFeedback.setVisibility(View.VISIBLE);
            holder.ivFeedback.setImageResource(R.drawable.ic_thumbs_down);
            holder.ivFeedback.setColorFilter(Color.parseColor("#F44336"));
            holder.tvNoFeedback.setVisibility(View.GONE);
        } else {
            holder.ivFeedback.setVisibility(View.GONE);
            holder.tvNoFeedback.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (messageClickListener != null) {
                messageClickListener.onMessageClick(m);
            }
        });
    }

    private static String safe(String s) {
        return s != null ? s.trim() : "";
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View statusBar;
        final TextView tvRecipient;
        final TextView tvTimestamp;
        final TextView tvPreview;
        final Chip chip1;
        final Chip chip2;
        final TextView tvMoreTags;
        final ImageView ivFeedback;
        final TextView tvNoFeedback;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            statusBar = itemView.findViewById(R.id.view_status_indicator);
            tvRecipient = itemView.findViewById(R.id.tv_recipient_name);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            tvPreview = itemView.findViewById(R.id.tv_message_preview);
            chip1 = itemView.findViewById(R.id.chip_context_1);
            chip2 = itemView.findViewById(R.id.chip_context_2);
            tvMoreTags = itemView.findViewById(R.id.tv_more_tags);
            ivFeedback = itemView.findViewById(R.id.iv_feedback_icon);
            tvNoFeedback = itemView.findViewById(R.id.tv_no_feedback);
        }
    }
}
