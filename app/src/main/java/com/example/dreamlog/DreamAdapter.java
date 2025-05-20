package com.example.dreamlog;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DreamAdapter extends RecyclerView.Adapter<DreamAdapter.DreamViewHolder> {
    private List<Dream> dreamList;
    private OnDreamActionListener actionListener;

    // Interface untuk callback ke MainActivity
    public interface OnDreamActionListener {
        void onEditDream(Dream dream);
        void onDeleteDream(Dream dream);
    }

    public DreamAdapter(List<Dream> dreamList, OnDreamActionListener listener) {
        this.dreamList = dreamList;
        this.actionListener = listener;
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
        holder.emotionText.setText("Emosi: " + dream.getEmotion());

        LinearLayout layout = holder.itemView.findViewById(R.id.layout_inside_card);
        if (layout != null) {
            Drawable background = layout.getBackground();
            if (background instanceof GradientDrawable) {
                GradientDrawable drawable = (GradientDrawable) background;
                String emotion = dream.getEmotion() != null ? dream.getEmotion().toLowerCase() : "tidak diketahui";
                switch (emotion) {
                    case "senang":
                        drawable.setStroke(4, 0xFF00FF00);
                        break;
                    case "sedih":
                        drawable.setStroke(4, 0xFF0000FF);
                        break;
                    case "seram":
                        drawable.setStroke(4, 0xFFFF0000);
                        break;
                    case "aneh":
                        drawable.setStroke(4, 0xFFFF00FF);
                        break;
                    default:
                        drawable.setStroke(4, 0xFFFFFFFF);
                        break;
                }
            } else {
                Log.e("DreamAdapter", "Background is not a GradientDrawable");
            }
        } else {
            Log.e("DreamAdapter", "layout_inside_card not found");
        }

        //fungsi edit
        holder.btnEdit.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEditDream(dream);
            }
        });

        //fungsi hapus
        holder.btnDelete.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDeleteDream(dream);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dreamList != null ? dreamList.size() : 0;
    }

    static class DreamViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, descriptionText, emotionText;
        Button btnEdit, btnDelete;

        DreamViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            emotionText = itemView.findViewById(R.id.emotionText);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}