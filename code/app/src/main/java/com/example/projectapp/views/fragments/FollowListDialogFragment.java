package com.example.projectapp.views.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.example.projectapp.R;
import com.example.projectapp.models.UserProfile;
import com.example.projectapp.views.activities.ProfileActivity;
import com.example.projectapp.views.adapters.UserAdapter;

import java.util.ArrayList;
import java.util.List;

public class FollowListDialogFragment extends DialogFragment {

    public static final String ARG_FOLLOW_TYPE = "followType";
    public static final String ARG_PROFILES_LIST = "profilesList";

    private List<UserProfile> profiles;
    private String followType;
    private RecyclerView recyclerView;
    private UserAdapter adapter;

    public static FollowListDialogFragment newInstance(String followType, ArrayList<UserProfile> profiles) {
        FollowListDialogFragment fragment = new FollowListDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FOLLOW_TYPE, followType);
        args.putSerializable(ARG_PROFILES_LIST, profiles);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_follow_list, null);
        recyclerView = view.findViewById(R.id.recycler_follow_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (getArguments() != null) {
            followType = getArguments().getString(ARG_FOLLOW_TYPE);
            profiles = (ArrayList<UserProfile>) getArguments().getSerializable(ARG_PROFILES_LIST);
        }
        // Provide an inline listener that launches the ProfileActivity when a user is clicked.
        adapter = new UserAdapter(getContext(), profiles, new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(UserProfile user) {
                Intent intent = new Intent(getContext(), ProfileActivity.class);
                intent.putExtra("uid", user.getUID());
                startActivity(intent);
                dismiss(); // close the dialog after clicking
            }
        });
        recyclerView.setAdapter(adapter);
        builder.setView(view)
                .setTitle(followType.equals("followers") ? "Followers" : "Following")
                .setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        return builder.create();
    }
}
