// -----------------------------------------------------------------------------
// File: MoodSpinnerAdapter.java
// -----------------------------------------------------------------------------
// This file defines the MoodSpinnerAdapter class, a custom ArrayAdapter for
// displaying mood types in a Spinner within the ProjectApp. It binds mood type
// data to spinner item views for user selection.
//
// Design Pattern: Adapter
// Outstanding Issues:

// -----------------------------------------------------------------------------
package com.example.projectapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ImageView; // Not needed if not displaying an image.
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MoodSpinnerAdapter extends ArrayAdapter<String> {

    private final LayoutInflater inflater;
    private final int resource;

    public MoodSpinnerAdapter(Context context, int resource, String[] items) {
        super(context, resource, items);
        this.inflater = LayoutInflater.from(context);
        this.resource = resource;
    }

    private View createItemView(int position, View convertView, ViewGroup parent) {
        final View view = (convertView == null)
                ? inflater.inflate(resource, parent, false)
                : convertView;

        // Get the root layout and TextView from spinner_item.xml
        LinearLayout rootLayout = view.findViewById(R.id.spinner_item_layout);
        TextView textView = view.findViewById(R.id.spinner_item_text);

        String moodName = getItem(position);
        // Retrieve the MoodType using your enum:
        MoodType mood = MoodType.fromString(moodName);
        if (mood != null) {
            // Set the background color for the spinner item using the mood's color
            rootLayout.setBackgroundColor(getContext().getResources().getColor(mood.getColorCode()));
            // Retrieve the emoji string from resources
            String emoji = getContext().getString(mood.getEmoticonResId());
            // Set the text to include the emoji and the mood name
            textView.setText(emoji + " " + moodName);
        } else {
            rootLayout.setBackgroundColor(Color.TRANSPARENT);
            textView.setText(moodName);
        }

        return view;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }
}
