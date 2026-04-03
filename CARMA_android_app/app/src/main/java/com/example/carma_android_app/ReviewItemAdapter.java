package com.example.carma_android_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ReviewItemAdapter extends RecyclerView.Adapter<ReviewItemAdapter.ViewHolder> {
    private List<String> reviewItems; // Replace String with your ReviewItem model if needed

    public ReviewItemAdapter(List<String> reviewItems) {
        this.reviewItems = reviewItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = reviewItems.get(position);
        holder.tvReviewText.setText(item);
    }

    @Override
    public int getItemCount() {
        return reviewItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvReviewText;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReviewText = itemView.findViewById(R.id.tv_review_text);
        }
    }
}
