package com.example.projectapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class MoodHistoryAdapter extends RecyclerView.Adapter<MoodHistoryAdapter.ViewHolder> {

    private ArrayList<MoodEvent> moodEvents;
    private Context context;

    public MoodHistoryAdapter(Context context, ArrayList<MoodEvent> moodEvents) {
        this.context = context;
        this.moodEvents = moodEvents;
    }

    // Update the adapter's data and refresh the list
    public void setData(ArrayList<MoodEvent> moodEvents) {
        this.moodEvents = moodEvents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MoodHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the mood_event_layout.xml for each item
        View view = LayoutInflater.from(context).inflate(R.layout.mood_event_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodHistoryAdapter.ViewHolder holder, int position) {
        MoodEvent event = moodEvents.get(position);
        // Bind the data to the views.
        // Replace the hardcoded "username" with actual data if available.
        holder.usernameText.setText("username");
        holder.emotionalStateText.setText(event.getEmotionalState());
        holder.triggerText.setText(event.getTrigger() != null ? event.getTrigger() : "No Trigger");
        holder.timeText.setText(event.getDate().toString()); // Consider formatting the date
        holder.socialSituationText.setText(event.getSocialSituation() != null ? event.getSocialSituation() : "No Info");
    }

    @Override
    public int getItemCount() {
        return moodEvents != null ? moodEvents.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageButton profilePic;
        TextView usernameText;
        TextView emotionalStateText;
        TextView triggerText;
        TextView timeText;
        TextView socialSituationText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.profile_pic);
            usernameText = itemView.findViewById(R.id.username_text);
            emotionalStateText = itemView.findViewById(R.id.emotional_state_text);
            triggerText = itemView.findViewById(R.id.trigger_text);
            timeText = itemView.findViewById(R.id.time_text);
            socialSituationText = itemView.findViewById(R.id.social_situation_text);
        }
    }
}
