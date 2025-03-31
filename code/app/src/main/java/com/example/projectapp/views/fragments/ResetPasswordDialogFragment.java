// -----------------------------------------------------------------------------
// File: ResetPasswordDialogFragment.java
// -----------------------------------------------------------------------------
// This file defines the ResetPasswordDialogFragment class, which provides a
// custom dialog for resetting the user's password. When the dialog is shown,
// the user can enter their email address and tap the "Reset" button to request
// a password reset email via Firebase Authentication. The dialog also provides
// a "Cancel" button to dismiss the dialog.
//
// Design Pattern: MVC (Controller/View) - The dialog acts as a view that
// handles user input and communicates with Firebase via a controller-like logic.
// Outstanding Issues:
//  N/A
// -----------------------------------------------------------------------------

package com.example.projectapp.views.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.projectapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Inflate the custom layout
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_reset_password, null);

        final EditText editEmail = view.findViewById(R.id.edit_reset_email);

        // Build the AlertDialog WITHOUT default positive/negative buttons
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view)
                .setTitle("Reset Password");

        // Create the dialog
        AlertDialog dialog = builder.create();

        // Once the dialog is shown, set click listeners for custom layout buttons
        dialog.setOnShowListener(dialogInterface -> {
            // Find custom layout's buttons
            Button resetBtn = view.findViewById(R.id.button_dialog_reset);
            Button cancelBtn = view.findViewById(R.id.button_dialog_cancel);

            // Handle "Reset" button
            resetBtn.setOnClickListener(v -> {
                String email = editEmail.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    editEmail.setError("Please enter your email");
                    return;
                }
                // Use Firebase to send password reset email
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), "Password reset email sent!", Toast.LENGTH_SHORT).show();
                            } else {
                                String error = (task.getException() != null)
                                        ? task.getException().getMessage()
                                        : "Unknown error";
                                Toast.makeText(getActivity(), "Error: " + error, Toast.LENGTH_SHORT).show();
                            }
                            dialog.dismiss();
                        });
            });

            // Handle "Cancel" button
            cancelBtn.setOnClickListener(v -> dialog.dismiss());
        });

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Remove default background so your custom layout can fill the dialog more
        // and set the width to match parent, so it's not too narrow.
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
