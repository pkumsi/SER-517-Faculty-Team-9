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
import org.json.JSONArray;
import org.json.JSONException;

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
        String statusLabel;
        if ("sent".equalsIgnoreCase(status)) {
            barColor = Color.parseColor("#4CAF50");
            statusLabel = "Sent";
        } else if ("cancelled".equalsIgnoreCase(status)) {
            barColor = Color.parseColor("#F44336");
            statusLabel = "Cancelled";
        } else {
            barColor = Color.parseColor("#BDBDBD");
            statusLabel = "Pending";
        }
        holder.statusBar.setBackgroundColor(barColor);
        holder.tvStatusLabel.setText(statusLabel);
        holder.tvStatusLabel.setTextColor(barColor);

        List<String> extraTags = parseAdditionalTags(m.getContextTags());
        int extraTagCount = extraTags.size();
        if (extraTagCount == 1) {
            holder.tvMoreTags.setVisibility(View.VISIBLE);
            holder.tvMoreTags.setText(extraTags.get(0));
        } else if (extraTagCount > 1) {
            holder.tvMoreTags.setVisibility(View.VISIBLE);
            holder.tvMoreTags.setText(extraTags.get(0) + " (+" + (extraTagCount - 1) + ")");
        } else {
            holder.tvMoreTags.setVisibility(View.GONE);
        }

        // Keep feedback in DB and dialog flow, but do not display like/dislike on the card list UI.
        holder.ivFeedback.setVisibility(View.GONE);
        holder.tvNoFeedback.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if (messageClickListener != null) {
                messageClickListener.onMessageClick(m);
            }
        });
    }

    private static String safe(String s) {
        return s != null ? s.trim() : "";
    }

    private static List<String> parseAdditionalTags(String contextTagsJson) {
        List<String> result = new ArrayList<>();
        String raw = safe(contextTagsJson);
        if (raw.isEmpty()) {
            return result;
        }
        try {
            JSONArray tags = new JSONArray(raw);
            for (int i = 0; i < tags.length(); i++) {
                String tag = tags.optString(i, "").trim();
                if (!tag.isEmpty()) {
                    result.add(tag);
                }
            }
        } catch (JSONException ignored) {
            // ignore malformed historical data
        }
        return result;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View statusBar;
        final TextView tvRecipient;
        final TextView tvTimestamp;
        final TextView tvStatusLabel;
        final TextView tvPreview;
        final Chip chip1;
        final Chip chip2;
        final Chip tvMoreTags;
        final ImageView ivFeedback;
        final TextView tvNoFeedback;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            statusBar = itemView.findViewById(R.id.view_status_indicator);
            tvRecipient = itemView.findViewById(R.id.tv_recipient_name);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            tvStatusLabel = itemView.findViewById(R.id.tv_status_label);
            tvPreview = itemView.findViewById(R.id.tv_message_preview);
            chip1 = itemView.findViewById(R.id.chip_context_1);
            chip2 = itemView.findViewById(R.id.chip_context_2);
            tvMoreTags = itemView.findViewById(R.id.tv_more_tags);
            ivFeedback = itemView.findViewById(R.id.iv_feedback_icon);
            tvNoFeedback = itemView.findViewById(R.id.tv_no_feedback);
        }
    }
}
