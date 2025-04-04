// -----------------------------------------------------------------------------
// File: CommentDialogFragment.java
// -----------------------------------------------------------------------------
// This DialogFragment is used to allow the user to add a comment to a MoodEvent.
// It also displays the current list of comments (if any) in a ListView.
// The layout dialog_comments.xml contains both the ListView (for existing comments)
// and an input area for a new comment.
// -----------------------------------------------------------------------------
package com.example.projectapp.views.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.projectapp.R;
import com.example.projectapp.database_util.ProfileProvider;
import com.example.projectapp.models.Comment;
import com.example.projectapp.models.MoodEvent;
import com.example.projectapp.models.UserProfile;
import com.example.projectapp.views.adapters.CommentAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class CommentDialogFragment extends DialogFragment {

    public interface CommentDialogListener {
        void onCommentAdded(Comment comment, MoodEvent moodEvent);
    }

    private CommentDialogListener listener;
    private MoodEvent moodEvent;

    public static CommentDialogFragment newInstance(MoodEvent moodEvent) {
        Bundle args = new Bundle();
        args.putSerializable("moodEvent", moodEvent);
        CommentDialogFragment fragment = new CommentDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof CommentDialogListener) {
            listener = (CommentDialogListener) parentFragment;
        } else if (context instanceof CommentDialogListener) {
            listener = (CommentDialogListener) context;
        } else {
            throw new RuntimeException("Parent fragment or activity must implement CommentDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Inflate the dialog_comments.xml layout
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_comments, null);
        // Retrieve the EditText for new comment input
        final EditText editComment = view.findViewById(R.id.edit_comment);
        // Retrieve the ListView that will display existing comments
        ListView commentsList = view.findViewById(R.id.comments_list);

        // Retrieve the MoodEvent from the arguments and set the adapter on the ListView
        if (getArguments() != null) {
            moodEvent = (MoodEvent) getArguments().getSerializable("moodEvent");
            if (moodEvent != null && moodEvent.getComments() != null) {
                CommentAdapter adapter = new CommentAdapter(getContext(), moodEvent.getComments());
                commentsList.setAdapter(adapter);
            }
        }

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(view)
                .setTitle("Add Comment")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                // We override the positive button below
                .setPositiveButton("Post", null);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button postButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            postButton.setOnClickListener(v -> {
                String commentText = editComment.getText().toString().trim();
                if (TextUtils.isEmpty(commentText)) {
                    editComment.setError("Comment cannot be empty");
                    return;
                }
                // Create a Comment using the current user's info
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                UserProfile curr = ProfileProvider.getInstance(FirebaseFirestore.getInstance()).getProfileByUID(uid);
                Comment comment = new Comment(uid, curr == null? "Username" : curr.getUsername(), commentText, new Date());
                if (listener != null && moodEvent != null) {
                    listener.onCommentAdded(comment, moodEvent);
                }
                dialog.dismiss();
            });
        });
        return dialog;
    }
}
