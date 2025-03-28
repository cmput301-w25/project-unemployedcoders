package com.example.projectapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        MoodEventDetailsAndEditingFragment.EditMoodEventListener,
        MoodEventDetailsMapFragment.EditMoodEventMapListener,
        FilterFragment.FilterListener,
        ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean permissionDenied = false;
    private GoogleMap map;
    private FusedLocationProviderClient mFusedLocationClient;

    private MoodHistory currentUserMoodHistory;
    private MoodHistory displayHistory;
    private FirebaseAuth mAuth;

    private ProfileProvider provider;

    private ArrayList<String> filters;

    /*
    Much of the following code is from Google Maps Platform "Location Data Tutorial"
    Written by: Google
    Taken by Luke Yaremko on 2025-03-20
    https://developers.google.com/maps/documentation/android-sdk/location#:~:text=If%20your%20app%20needs%20to,location%20returned%20by%20the%20API.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_map);

        mAuth = FirebaseAuth.getInstance();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        filters = new ArrayList<>();
        filters.add("Both");

        FloatingActionButton filterButton = findViewById(R.id.map_filter_button);

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterFragment.newInstance(filters).show(getSupportFragmentManager(), "SelectFilters");
            }
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_map); // Highlight current screen
        bottomNav.setOnItemSelectedListener(item -> {
            Intent intent = null;
            if (item.getItemId() == R.id.nav_map) {
                return true; // Already here
            } else if (item.getItemId() == R.id.nav_home) {
                intent = new Intent(this, HomeActivity.class);
            } else if (item.getItemId() == R.id.nav_history) {
                intent = new Intent(this, HistoryActivity.class);
            } else if (item.getItemId() == R.id.nav_inbox) {
                intent = new Intent(this, InboxActivity.class);
            } else if (item.getItemId() == R.id.nav_profile) {
                intent = new Intent(this, ProfileActivity.class);
            }

            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
            return true;
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        map.setOnMyLocationButtonClickListener(this);
        map.setOnMarkerClickListener(this);

        enableMyLocation();
        centerOnUserLocation();
        setUpdateListener();

    }

    private void enableMyLocation() {
        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            return;
        }

        // 2. Otherwise, request location permissions from the user.
        PermissionUtils.requestLocationPermissions(this, LOCATION_PERMISSION_REQUEST_CODE, true);

    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION) || PermissionUtils
                .isPermissionGranted(permissions, grantResults,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Permission was denied. Display an error message
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            //showMissingPermissionError();
            permissionDenied = false;
        }
    }


    /**
     * Centers the camera on the current user's current location
     */
    private void centerOnUserLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude()); // user's location
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                }
            });

        }

    }

    /**
     * Sets the update listener to update the mood event markers on a change from the database
     */
    private void setUpdateListener(){
        provider = ProfileProvider.getInstance(FirebaseFirestore.getInstance());

        provider.listenForUpdates(new ProfileProvider.DataStatus() {
            @Override
            public void onDataUpdated() {
                if (mAuth.getCurrentUser() != null) {
                    currentUserMoodHistory = provider.getProfileByUID(mAuth.getCurrentUser().getUid()).getHistory();
                    placeMoodHistoryMarkers(provider.getProfileByUID(mAuth.getCurrentUser().getUid()));
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MapActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void placeMoodHistoryMarkers(UserProfile currentUser){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    map.clear();

                    if (filters.contains("Just Me") || filters.contains("Both")){
                        displayHistory = currentUser.getHistory().getFilteredVersion(filters);
                    } else {
                        displayHistory = new MoodHistory();
                    }


                    if (filters.contains("Just People I'm Following") || filters.contains("Both")) {
                        ArrayList<UserProfile> profiles = provider.getProfiles();
                        for (UserProfile other : profiles) {
                            if (!currentUser.getUID().equals(other.getUID()) && currentUser.getFollowing().contains(other.getUID())) {  // other is followed by current
                                ArrayList<MoodEvent> otherHistory = other.getHistory().getFilteredVersion(filters).getEvents();
                                for (MoodEvent event : otherHistory) {
                                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude()); // user's location
                                    if (event.isPublic() && withinFiveKM(event, currentLocation)) {
                                        displayHistory.addEvent(event);
                                    }
                                }
                            }
                        }
                    }
                    for (MoodEvent event: displayHistory.getEvents()){
                        placeMoodEventMarker(event);
                    }
                }
            });

        }
    }

    /**
     * Places a single mood event on the map
     * @param moodEvent
     *      The mood event to place
     */
    private void placeMoodEventMarker(MoodEvent moodEvent){
        if (moodEvent.getLatitude() == 0 && moodEvent.getLongitude() == 0){
            return;
        }

        // TODO: put users's username on the tag too, unless it's the current user's
        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(moodEvent.getLatitude(), moodEvent.getLongitude())).title("@" + provider.getProfileByUID(moodEvent.getUserId()).getUsername());

        Bitmap iconRes = BitmapFactory.decodeResource(getResources(), moodEvent.getMarkerResource());  // raw img

        Bitmap scaledIcon = Bitmap.createScaledBitmap(iconRes, 120, 200, false);  // scaled version

        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(scaledIcon));  // set the icon

        Marker marker = map.addMarker(markerOptions);

        if (marker != null){
            marker.setTag(moodEvent);
        }


    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        MoodEvent moodEvent = (MoodEvent)marker.getTag();


        MoodEventDetailsMapFragment.newInstance(displayHistory.getEvents().get(displayHistory.getEvents().indexOf(moodEvent)))
                .show(getSupportFragmentManager(), "Mood Event Details");

        return false;  // to continue the default behavior of the map
    }

    @Override
    public void onMoodEventEdited(MoodEvent moodEvent) {
        FirebaseSync fb = FirebaseSync.getInstance();
        fb.fetchUserProfileObject(new UserProfileCallback() {
            @Override
            public void onUserProfileLoaded(UserProfile userProfile) {
                userProfile.setHistory(currentUserMoodHistory); // TODO: This needs to change once filters are shown.
                //moodEventAdapter.notifyDataSetChanged();
                fb.storeUserData(userProfile);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MapActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapMoodEventEdited(MoodEvent moodEvent) {
        if (moodEvent.getUserId() != null && moodEvent.getUserId().equals(mAuth.getCurrentUser().getUid())){
            MoodEventDetailsAndEditingFragment.newInstance(moodEvent)
                    .show(getSupportFragmentManager(), "Mood Event Details");
        }
    }

    @Override
    public void onFiltersEdited(ArrayList<String> filters) {
        this.filters = filters;
        map.clear();
        if (mAuth.getCurrentUser() != null){
            placeMoodHistoryMarkers(provider.getProfileByUID(mAuth.getCurrentUser().getUid()));
        }
    }

    private boolean withinFiveKM(MoodEvent event, LatLng currentLoc){
        return SphericalUtil.computeDistanceBetween(new LatLng(event.getLatitude(), event.getLongitude()), currentLoc) <= 5000;
    }

    private void updateDisplay(){
        FirebaseUser authUser = FirebaseAuth.getInstance().getCurrentUser();

        if (authUser != null){
            UserProfile currentUser = provider.getProfileByUID(FirebaseAuth.getInstance().getCurrentUser().getUid());
            if (currentUser != null) {
                placeMoodHistoryMarkers(currentUser);


            }
        }
    }
}