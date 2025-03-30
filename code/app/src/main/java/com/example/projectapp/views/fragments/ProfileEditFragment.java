package com.example.projectapp.views.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.projectapp.database_util.ProfileProvider;
import com.example.projectapp.R;
import com.example.projectapp.models.UserProfile;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileEditFragment  extends DialogFragment {

    public interface EditProfileListener {
        void onProfileEdited(UserProfile profile);
    }

    private EditProfileListener listener;

    public static ProfileEditFragment newInstance(UserProfile profile) {
        Bundle args = new Bundle();
        args.putSerializable("profile", profile);
        ProfileEditFragment fragment = new ProfileEditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof EditProfileListener) {
            listener = (EditProfileListener) context;
        } else {
            throw new RuntimeException(context + " must Implement EditProfileListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.edit_profile_fragment, null);
        EditText editUsername = view.findViewById(R.id.edit_username_text);
        EditText editName = view.findViewById(R.id.edit_name_text);

        UserProfile profile = (UserProfile) requireArguments().getSerializable("profile");

        if (profile != null){
            editUsername.setText(profile.getUsername());
            editName.setText(profile.getName());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        AlertDialog dialog = builder
                .setView(view)
                .setTitle("Edit Profile")
                .setNegativeButton("Cancel", null)
                // Override this later
                .setPositiveButton("Save", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                ProfileProvider provider = ProfileProvider.getInstance(FirebaseFirestore.getInstance());
                provider.listenForUpdates(new ProfileProvider.DataStatus() {
                    @Override
                    public void onDataUpdated() {
                        String newUsername = editUsername.getText().toString().trim();
                        String newName = editName.getText().toString().trim();

                        if (!provider.usernameAvailable(newUsername)){
                            editUsername.setError("Username taken. Try another one.");
                            return;
                        }

                        if (profile != null) {
                            //this might need to be changed since we're using MoodHistory now
                            profile.setUsername(newUsername);
                            profile.setName(newName);
                            if (listener != null) {
                                listener.onProfileEdited(profile); // Notify the activity to update the UI
                            }
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(String error) {

                    }
                });


            });
        });

        return dialog;
    }


}


