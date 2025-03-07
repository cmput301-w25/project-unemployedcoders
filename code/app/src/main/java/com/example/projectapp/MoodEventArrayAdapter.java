package com.example.projectapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class MoodEventArrayAdapter extends ArrayAdapter<MoodEvent> {

    private ArrayList<MoodEvent> events;
    private Context context;
    private OnMoodEventClickListener listener;

    public interface OnMoodEventClickListener {
        void onEditMoodEvent(MoodEvent event, int position);
        void onDeleteMoodEvent(MoodEvent event, int position);
    }

    public MoodEventArrayAdapter(Context context, ArrayList<MoodEvent> events, OnMoodEventClickListener listener) {
        super(context, 0, events);
        this.events = events;
        this.context = context;
        this.listener = listener;
    }

    static class ViewHolder {
        ImageButton profilePic;
        TextView usernameText;
        TextView timeText;
        TextView emotionalStateText;
        TextView triggerText;
        TextView socialSituationText;
        ConstraintLayout background;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.mood_event_layout, parent, false);
            holder = new ViewHolder();

            holder.profilePic = convertView.findViewById(R.id.profile_pic);
            holder.usernameText = convertView.findViewById(R.id.username_text);
            holder.timeText = convertView.findViewById(R.id.time_text);
            holder.emotionalStateText = convertView.findViewById(R.id.emotional_state_text);
            holder.triggerText = convertView.findViewById(R.id.trigger_text);
            holder.socialSituationText = convertView.findViewById(R.id.social_situation_text);
            holder.background = convertView.findViewById(R.id.mood_event_background);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        MoodEvent moodEvent = events.get(position);

        holder.usernameText.setText("username");

        String[] tokens = moodEvent.getDate().toString().split(" ");
        String dateStr = tokens[1] + " " + tokens[2] + ", " + tokens[5];

        holder.timeText.setText(dateStr);

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

        // Click listener for editing a mood event
        convertView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditMoodEvent(moodEvent, position);
            }
        });

        // Long click listener for deleting a mood event
        convertView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onDeleteMoodEvent(moodEvent, position);
            }
            return true;
        });

        return convertView;
    }
}
