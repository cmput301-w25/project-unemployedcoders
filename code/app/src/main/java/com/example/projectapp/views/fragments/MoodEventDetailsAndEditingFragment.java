// -----------------------------------------------------------------------------
// File: MoodEventDetailsAndEditingFragment.java
// -----------------------------------------------------------------------------
// This file defines the MoodEventDetailsAndEditingFragment class, a DialogFragment
// used in the ProjectApp to display and edit details of a MoodEvent. It provides
// a dialog with spinners for emotional state and social situation, an EditText for
// the trigger, and options to save or cancel changes. The fragment follows the
// Listener pattern to notify the host activity of edits.
//
// Design Pattern: Listener (for callback communication)
// Outstanding Issues:
//  N/A
// -----------------------------------------------------------------------------
package com.example.projectapp.views.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.projectapp.R;
import com.example.projectapp.models.MoodEvent;

public class MoodEventDetailsAndEditingFragment extends DialogFragment {

    private Spinner editEmotionalStateSpinner;
    private Spinner editSocialSituationSpinner;
    private EditText editReason;
    // New: Button to toggle the public/private status
    private Button togglePublicButton;

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
        editReason = view.findViewById(R.id.details_fragment_edit_reason);
        // Bind the new toggle button for public/private status
        togglePublicButton = view.findViewById(R.id.button_toggle_public);

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
            editReason.setText(moodEvent.getReason());
            // Set initial text for the toggle button based on current public status
            togglePublicButton.setText(moodEvent.isPublic() ? "Make Private" : "Make Public");
            // Toggle public status when the button is clicked
            togglePublicButton.setOnClickListener(v -> {
                moodEvent.setPublic(!moodEvent.isPublic());
                togglePublicButton.setText(moodEvent.isPublic() ? "Make Private" : "Make Public");
            });
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
                String newReason = editReason.getText().toString().trim();

                if (moodEvent != null) {
                    //this might need to be changed since we're using MoodHistory now
                    moodEvent.setEmotionalState(newEmotionalState);
                    moodEvent.setSocialSituation(newSituation);
                    moodEvent.setReason(newReason.trim());
                    // The isPublic status is already updated via the toggle button
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
        String reason = editReason.getText().toString().trim();
        if (!MoodEvent.validReason(reason)) {
            editReason.setError("Invalid Reason");
            return false;
        }
        return true;
    }
}
