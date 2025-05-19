package com.example.dreamlog;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DreamAdapter extends RecyclerView.Adapter<DreamAdapter.DreamViewHolder> {
    private List<Dream> dreamList;

    public DreamAdapter(List<Dream> dreamList) {
        this.dreamList = dreamList;
    }

    @Override
    public DreamViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dream_item, parent, false);
        return new DreamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DreamViewHolder holder, int position) {
        Dream dream = dreamList.get(position);
        holder.titleText.setText(dream.getTitle());
        holder.descriptionText.setText(dream.getDescription());
        holder.emotionText.setText(dream.getEmotion());

        GradientDrawable drawable = (GradientDrawable) holder.itemView.getBackground();
        switch (dream.getEmotion().toLowerCase()) {
            case "happy":
                drawable.setStroke(4, 0xFF00FF00); // Neon hijau
                break;
            case "sad":
                drawable.setStroke(4, 0xFF0000FF); // Neon biru
                break;
            case "scary":
                drawable.setStroke(4, 0xFFFF0000); // Neon merah
                break;
            case "weird":
                drawable.setStroke(4, 0xFFFF00FF); // Neon magenta
                break;
            default:
                drawable.setStroke(4, 0xFFFFFFFF); // Putih
        }
    }

    @Override
    public int getItemCount() {
        return dreamList.size();
    }

    static class DreamViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, descriptionText, emotionText;

        DreamViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            emotionText = itemView.findViewById(R.id.emotionText);
        }
    }
}