package com.example.projectapp;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * ViewHolder for a single FollowRequest item with Accept/Decline buttons.
 */
public class FollowViewHolder extends RecyclerView.ViewHolder {

    TextView text1, text2;
    Button acceptButton, declineButton;

    public FollowViewHolder(@NonNull View itemView) {
        super(itemView);
        text1 = itemView.findViewById(R.id.text1);
        text2 = itemView.findViewById(R.id.text2);
        acceptButton = itemView.findViewById(R.id.button_accept);
        declineButton = itemView.findViewById(R.id.button_decline);
    }

    public void bind(FollowRequest request) {
        // Use ProfileProvider to get the profile by UID and display the current username.
        UserProfile profile = ProfileProvider.getInstance(FirebaseFirestore.getInstance())
                .getProfileByUID(request.getFromUid());
        if (profile != null && profile.getUsername() != null) {
            text1.setText("From: @" + profile.getUsername());
        } else {
            text1.setText("From: " + request.getFromUid());
        }
        text2.setText("Status: " + request.getStatus());

        acceptButton.setOnClickListener(v -> {
            ((InboxActivity) itemView.getContext()).onAcceptClicked(request);
        });

        declineButton.setOnClickListener(v -> {
            ((InboxActivity) itemView.getContext()).onDeclineClicked(request);
        });
    }
}
