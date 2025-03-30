// -----------------------------------------------------------------------------
// File: MapDialogFragment.java
// -----------------------------------------------------------------------------
// This file defines the MapDialogFragment class, which displays a Google Map
// in a dialog. It shows a marker at the user’s location, using latitude and
// longitude passed as arguments. An X button is provided to dismiss the dialog.
//
// Design Pattern: MVC (View) - acts as a view controller for the map dialog.
// Outstanding Issues:
//  N/A
// -----------------------------------------------------------------------------

package com.example.projectapp.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.projectapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapDialogFragment extends DialogFragment implements OnMapReadyCallback {

    private static final String ARG_LATITUDE = "latitude";
    private static final String ARG_LONGITUDE = "longitude";
    private double latitude;
    private double longitude;
    private GoogleMap mMap;

    /**
     * Create a new instance of MapDialogFragment with the specified latitude and longitude.
     *
     * @param latitude  the latitude of the location
     * @param longitude the longitude of the location
     * @return a new instance of MapDialogFragment
     */
    public static MapDialogFragment newInstance(double latitude, double longitude) {
        MapDialogFragment fragment = new MapDialogFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_LATITUDE, latitude);
        args.putDouble(ARG_LONGITUDE, longitude);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the style so that this fragment displays as a dialog.
        // Ensure you have defined R.style.AppTheme_Dialog in your styles.xml.
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_map, container, false);

        // Retrieve the coordinates from arguments.
        if (getArguments() != null) {
            latitude = getArguments().getDouble(ARG_LATITUDE);
            longitude = getArguments().getDouble(ARG_LONGITUDE);
        }

        // Set up the close (X) button to dismiss the dialog.
        ImageButton closeButton = view.findViewById(R.id.button_close);
        closeButton.setOnClickListener(v -> dismiss());

        // Set up the map fragment using the child fragment manager.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng userLocation = new LatLng(latitude, longitude);
        // Add a marker at the user's location.
        mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
        // Move and zoom the camera to the user's location.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12));
    }
}
