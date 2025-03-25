package com.example.projectapp;

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

import java.util.ArrayList;

public class FilterFragment extends DialogFragment {

    private Spinner peopleSpinner;
    private Spinner recencySpinner;
    private Spinner emotionalStateSpinner;
    private EditText reasonText;

    public interface FilterListener {
        void onFiltersEdited(ArrayList<String> filters);
    }

    private FilterListener listener;

    public static FilterFragment newInstance(ArrayList<String> filters) {
        Bundle args = new Bundle();
        args.putSerializable("filters", filters);
        FilterFragment fragment = new FilterFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof FilterListener) {
            listener = (FilterListener) context;
        } else {
            throw new RuntimeException(context + " must Implement FilterListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.filter_fragment, null);
        peopleSpinner = (Spinner) view.findViewById(R.id.people_filter_spinner);
        recencySpinner = (Spinner) view.findViewById(R.id.recency_filter_spinner);
        emotionalStateSpinner = (Spinner) view.findViewById(R.id.emotional_state_filter_spinner);
        reasonText = (EditText) view.findViewById(R.id.reason_filter_text);

        ArrayList<String> filters = (ArrayList<String>) requireArguments().getSerializable("filters");

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        AlertDialog dialog = builder
                .setView(view)
                .setTitle("Apply Filters")
                .setNegativeButton("Cancel", null)
                // Override this later
                .setPositiveButton("Save", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                filters.clear();
                if (!recencySpinner.getSelectedItem().toString().equals("No Filter")){
                   filters.add(recencySpinner.getSelectedItem().toString());
                }
                if (!emotionalStateSpinner.getSelectedItem().toString().equals("No Filter")){
                    filters.add("Emotional State:" + emotionalStateSpinner.getSelectedItem().toString());
                }
                if (!reasonText.getText().toString().trim().isEmpty()){
                    filters.add("Reason Contains:" + reasonText.getText().toString().trim());
                }
                filters.add(peopleSpinner.getSelectedItem().toString());


                listener.onFiltersEdited(filters);
                dialog.dismiss();
            });
        });

        return dialog;
    }
}
