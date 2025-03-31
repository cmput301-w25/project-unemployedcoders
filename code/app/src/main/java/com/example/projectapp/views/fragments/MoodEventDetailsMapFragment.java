package com.example.projectapp.views.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.projectapp.database_util.ProfileProvider;
import com.example.projectapp.R;
import com.example.projectapp.models.MoodEvent;
import com.google.firebase.firestore.FirebaseFirestore;

public class MoodEventDetailsMapFragment  extends DialogFragment {

    public interface EditMoodEventMapListener {
        void onMapMoodEventEdited(MoodEvent moodEvent);
    }

    private ImageButton profilePic;
    private TextView usernameText;
    private TextView timeText;
    private TextView emotionalStateText;
    private TextView socialSituationText;
    private TextView reasonText;
    private ConstraintLayout background;
    private EditMoodEventMapListener listener;

    private ImageView photo;
    public static MoodEventDetailsMapFragment newInstance(MoodEvent moodEvent) {
        Bundle args = new Bundle();
        args.putSerializable("moodEvent", moodEvent);
        MoodEventDetailsMapFragment fragment = new MoodEventDetailsMapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof EditMoodEventMapListener) {
            listener = (EditMoodEventMapListener) context;
        } else {
            throw new RuntimeException(context + " must Implement EditMoodEventListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.mood_event_layout, null);

        profilePic = view.findViewById(R.id.profile_pic);
        usernameText = view.findViewById(R.id.username_text);
        timeText = view.findViewById(R.id.time_text);
        emotionalStateText = view.findViewById(R.id.emotional_state_text);
        socialSituationText = view.findViewById(R.id.social_situation_text);
        background = view.findViewById(R.id.mood_event_background);
        reasonText = view.findViewById(R.id.reason_text);
        photo = view.findViewById(R.id.mood_event_photo);

        MoodEvent moodEvent = (MoodEvent) requireArguments().getSerializable("moodEvent");

        ProfileProvider provider = ProfileProvider.getInstance(FirebaseFirestore.getInstance());
        provider.listenForUpdates(new ProfileProvider.DataStatus() {
            @Override
            public void onDataUpdated() {
                if (moodEvent.getUserId() != null){
                    usernameText.setText("@" + provider.getProfileByUID(moodEvent.getUserId()).getUsername());

                }
            }

            @Override
            public void onError(String error) {
                // nothing for now
            }
        });


        String[] tokens = moodEvent.getDate().toString().split(" ");
        String dateStr = tokens[1] + " " + tokens[2] + ", " + tokens[5];

        timeText.setText(dateStr);

        String moodText = moodEvent.getEmotionalState();
        int emoticonResId = moodEvent.getEmoticonResource();
        if (moodText != null && emoticonResId != 0) {
            String emoticon = view.getResources().getString(emoticonResId);
            emotionalStateText.setText(emoticon + " " + moodText);
        } else {
            emotionalStateText.setText("Unknown Mood");
        }

        reasonText.setText(moodEvent.getReason() != null ? moodEvent.getReason() : "No Reason");
        socialSituationText.setText(moodEvent.getSocialSituation() != null ? moodEvent.getSocialSituation() : "No Social Situation");

        int color = moodEvent.getColorResource();
        background.setBackgroundColor(view.getResources().getColor(color, getContext().getTheme()));

        // Display photo
        if (moodEvent.getPhotoUri() != null) {
            Glide.with(getContext())
                    .load(moodEvent.getPhotoUri())
                    .placeholder(android.R.color.darker_gray)
                    .into(photo);
        } else {
            photo.setVisibility(View.GONE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        AlertDialog dialog = builder
                .setView(view)
                .setTitle("Mood Event Details")
                .setNegativeButton("Cancel", null)
                // Override this later
                .setPositiveButton("Edit", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                dialog.dismiss();
                listener.onMapMoodEventEdited(moodEvent);

            });
        });

        dialog.getWindow().setBackgroundDrawableResource(moodEvent.getColorResource());

        return dialog;

    }
}
