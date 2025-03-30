package com.example.projectapp.views.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.projectapp.R;
import com.example.projectapp.models.Comment;
import com.example.projectapp.models.MoodEvent;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Date;

public class CommentDialogFragment extends DialogFragment {

    public interface CommentDialogListener {
        void onCommentAdded(Comment comment, MoodEvent moodEvent);
    }

    private CommentDialogListener listener;
    private MoodEvent moodEvent;

    public static CommentDialogFragment newInstance(MoodEvent moodEvent) {
        CommentDialogFragment fragment = new CommentDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("moodEvent", moodEvent);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        if (context instanceof CommentDialogListener) {
            listener = (CommentDialogListener) context;
        } else {
            throw new RuntimeException(context + " must implement CommentDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_comments, null);
        final EditText editComment = view.findViewById(R.id.edit_comment);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(view)
                .setTitle("Add Comment")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
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
                // Create Comment using current user info
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                // You might retrieve the username from your ProfileProvider if needed.
                Comment comment = new Comment(uid, uid, commentText, new Date());
                if (listener != null && moodEvent != null) {
                    listener.onCommentAdded(comment, moodEvent);
                }
                dialog.dismiss();
            });
        });

        if (getArguments() != null) {
            moodEvent = (MoodEvent) getArguments().getSerializable("moodEvent");
        }
        return dialog;
    }
}
