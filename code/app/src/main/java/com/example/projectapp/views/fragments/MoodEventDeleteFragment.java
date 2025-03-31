// -----------------------------------------------------------------------------
// File: MoodEventDeleteFragment.java
// -----------------------------------------------------------------------------
// This file defines the MoodEventDeleteFragment class, a DialogFragment used
// in the ProjectApp to confirm the deletion of a MoodEvent. It displays a dialog
// with a confirmation message and notifies a listener when the user confirms
// deletion. The fragment follows the Listener pattern to communicate with its
// host activity or fragment.
//
// Design Pattern: Listener (for callback communication)
// Outstanding Issues:
// N/A
// -----------------------------------------------------------------------------
package com.example.projectapp.views.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.projectapp.models.MoodEvent;

public class MoodEventDeleteFragment extends DialogFragment {
    public interface DeleteMoodEventDialogListener {
        void onMoodEventDeleted(MoodEvent moodEvent);
    }
    private DeleteMoodEventDialogListener listener;

    public static MoodEventDeleteFragment newInstance(MoodEvent moodEvent) {
        Bundle args = new Bundle();
        args.putSerializable("moodEvent", moodEvent);
        MoodEventDeleteFragment fragment = new MoodEventDeleteFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof DeleteMoodEventDialogListener) {
            listener = (DeleteMoodEventDialogListener) context;
        } else {
            throw new RuntimeException(context + " Must implement listener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        MoodEvent moodEvent;
        if (bundle != null) {
            moodEvent = (MoodEvent) bundle.getSerializable("moodEvent");
        } else {
            throw new RuntimeException("Bundle was not present!");
        }
        if (moodEvent == null) {
            throw new RuntimeException("MoodEvent was not in bundle!");
        }

        return new AlertDialog.Builder(requireContext())
                .setMessage("Are you sure you want to delete the Mood Event " + moodEvent.getEmotionalState())
                .setPositiveButton("Delete", (dialog, which) -> {
                    listener.onMoodEventDeleted(moodEvent);
                })
                .create();
    }
}
