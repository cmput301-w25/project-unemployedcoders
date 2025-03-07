package com.example.projectapp;

import static android.text.TextUtils.isDigitsOnly;
import static android.text.TextUtils.isEmpty;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Movie;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class MoodEventDetailsAndEditingFragment extends DialogFragment {


    private Spinner editEmotionalStateSpinner;
    private Spinner editSocialSituationSpinner;
    private EditText editTrigger;

    public interface EditMoodEventListener {
        void onMoodEventEdited(MoodEvent moodEvent);
    }

    private EditMoodEventListener listener;

    public static MoodEventDetailsAndEditingFragment newInstance(MoodEvent moodEvent) {
        Bundle args = new Bundle();
        args.putSerializable("moodEvent", moodEvent);
        MoodEventDetailsAndEditingFragment fragment = new MoodEventDetailsAndEditingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof EditMoodEventListener) {
            listener = (EditMoodEventListener) context;
        } else {
            throw new RuntimeException(context + " must Implement EditMoodEventListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_mood_event_details_and_edit, null);
        editEmotionalStateSpinner = (Spinner) view.findViewById(R.id.details_fragment_edit_spinner_emotional_state);
        editSocialSituationSpinner = (Spinner) view.findViewById(R.id.details_fragment_edit_spinner_situation);
        editTrigger = view.findViewById(R.id.details_fragment_edit_trigger);
        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.emotional_states,
                android.R.layout.simple_spinner_item
        );
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editEmotionalStateSpinner.setAdapter(stateAdapter);
        ArrayAdapter<CharSequence> situationAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.social_situations,
                android.R.layout.simple_spinner_item
        );
        situationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editSocialSituationSpinner.setAdapter(situationAdapter);


        //get elements of mood event we're viewing/editing and set the fields to
        //current values
        MoodEvent moodEvent = (MoodEvent) requireArguments().getSerializable("moodEvent");
        if (moodEvent != null) {
            editEmotionalStateSpinner.setSelection(stateAdapter.getPosition(moodEvent.getEmotionalState()));
            if (moodEvent.getSocialSituation() != null && !moodEvent.getSocialSituation().isEmpty()) {
                editSocialSituationSpinner.setSelection(situationAdapter.getPosition(moodEvent.getSocialSituation()));
            }
            editTrigger.setText(moodEvent.getTrigger());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        AlertDialog dialog = builder
                .setView(view)
                .setTitle("Mood Event Details")
                .setNegativeButton("Cancel", null)
                // Override this later
                .setPositiveButton("Save", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                if (!validInput()){
                    return;
                }

                String newEmotionalState = editEmotionalStateSpinner.getSelectedItem().toString();
                String newSituation = editSocialSituationSpinner.getSelectedItem().toString();
                String newTrigger = editTrigger.getText().toString();
                if (moodEvent != null) {
                    //this might need to be changed since we're using MoodHistory now
                    moodEvent.setEmotionalState(newEmotionalState);
                    moodEvent.setSocialSituation(newSituation);
                    moodEvent.setTrigger(newTrigger);
                    if (listener != null) {
                        listener.onMoodEventEdited(moodEvent); // Notify the activity to update the UI
                    }
                }
                dialog.dismiss();
            });
        });

        return dialog;
    }

    private boolean validInput() {
        String trigger = editTrigger.getText().toString();

        if (!MoodEvent.validTrigger(trigger)) {
            editTrigger.setError("Invalid Trigger");
            return false;
        }
        return true;
    }
}
