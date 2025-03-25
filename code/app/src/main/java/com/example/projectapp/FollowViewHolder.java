package com.example.projectapp;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        text1.setText("From: @" + request.getFromUsername());
        text2.setText("Status: " + request.getStatus());

        acceptButton.setOnClickListener(v -> {
            // Cast the context to InboxActivity and call onAcceptClicked()
            ((InboxActivity) itemView.getContext()).onAcceptClicked(request);
        });

        declineButton.setOnClickListener(v -> {
            ((InboxActivity) itemView.getContext()).onDeclineClicked(request);
        });
    }
}
