package com.example.projectapp;

//import static androidx.appcompat.graphics.drawable.DrawableContainerCompat.Api21Impl.getResources;

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

import java.util.ArrayList;

public class MoodEventArrayAdapter extends ArrayAdapter<MoodEvent> {

    private ArrayList<MoodEvent> events;
    private Context context;

    public MoodEventArrayAdapter(Context context, ArrayList<MoodEvent> events){
        super(context, 0, events);
        this.events = events;
        this.context = context;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){

        View view = convertView;
        if (view == null){
            view = LayoutInflater.from(context).inflate(R.layout.mood_event_layout, parent, false);
        }

        MoodEvent moodEvent = events.get(position);

        ImageButton profilePic = view.findViewById(R.id.profile_pic);
        TextView usernameText = view.findViewById(R.id.username_text);
        TextView timeText = view.findViewById(R.id.time_text);
        TextView emotionalStateText = view.findViewById(R.id.emotional_state_text);
        TextView triggerText = view.findViewById(R.id.trigger_text);
        TextView socialSituationText = view.findViewById(R.id.social_situation_text);
        ConstraintLayout background = view.findViewById(R.id.mood_event_background);

        usernameText.setText("username"); // need navigability back to userProfile
        timeText.setText(moodEvent.getDate().toString());
        String emotionalString = moodEvent.getEmotionalState() + " " + context.getResources().getString(moodEvent.getEmoticonResource());
        emotionalStateText.setText(emotionalString);
        triggerText.setText(moodEvent.getTrigger());
        socialSituationText.setText(moodEvent.getSocialSituation());

        int color;
        color = moodEvent.getColorResource();
        background.setBackgroundColor(context.getResources().getColor(color, context.getTheme()));

        return view;
    }
}
