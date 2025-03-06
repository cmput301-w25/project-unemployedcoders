package com.example.projectapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MoodEventRecyclerAdapter extends RecyclerView.Adapter<MoodEventRecyclerAdapter.ViewHolder> {

    private List<MoodEvent> events;
    private Context context;
    private OnMoodEventClickListener listener;
    private int selectedTab = 0; // 0 for For You, 1 for Following

    public interface OnMoodEventClickListener {
        void onEditMoodEvent(MoodEvent event, int position);
        void onDeleteMoodEvent(MoodEvent event, int position);
    }

    public MoodEventRecyclerAdapter(Context context, List<MoodEvent> forYouEvents, List<MoodEvent> followingEvents, OnMoodEventClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.events = forYouEvents; // Default to For You
    }

    public void switchTab(int tabIndex, List<MoodEvent> newEvents) {
        selectedTab = tabIndex;
        this.events = newEvents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.mood_event_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MoodEvent moodEvent = events.get(position);

        holder.usernameText.setText("username"); // Placeholder; replace with actual username logic
        holder.timeText.setText(moodEvent.getDate().toString());

        String moodText = moodEvent.getEmotionalState();
        int emoticonResId = moodEvent.getEmoticonResource();
        if (moodText != null && emoticonResId != 0) {
            String emoticon = context.getResources().getString(emoticonResId);
            holder.emotionalStateText.setText(emoticon + " " + moodText);
        } else {
            holder.emotionalStateText.setText("Unknown Mood");
        }

        holder.triggerText.setText(moodEvent.getTrigger() != null ? moodEvent.getTrigger() : "No Trigger");
        holder.socialSituationText.setText(moodEvent.getSocialSituation() != null ? moodEvent.getSocialSituation() : "No Social Situation");

        int color = moodEvent.getColorResource();
        holder.background.setBackgroundColor(context.getResources().getColor(color, context.getTheme()));

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditMoodEvent(moodEvent, position);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onDeleteMoodEvent(moodEvent, position);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageButton profilePic;
        TextView usernameText, timeText, emotionalStateText, triggerText, socialSituationText;
        ConstraintLayout background;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.profile_pic);
            usernameText = itemView.findViewById(R.id.username_text);
            timeText = itemView.findViewById(R.id.time_text);
            emotionalStateText = itemView.findViewById(R.id.emotional_state_text);
            triggerText = itemView.findViewById(R.id.trigger_text);
            socialSituationText = itemView.findViewById(R.id.social_situation_text);
            background = itemView.findViewById(R.id.mood_event_background);
        }
    }
}