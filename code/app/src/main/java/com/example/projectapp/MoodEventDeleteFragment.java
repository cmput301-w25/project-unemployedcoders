package com.example.projectapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

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
