package com.example.projectapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MapActivity extends AppCompatActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        MoodEventDetailsAndEditingFragment.EditMoodEventListener,
        MoodEventDetailsMapFragment.EditMoodEventMapListener,
        ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean permissionDenied = false;
    private GoogleMap map;
    private FusedLocationProviderClient mFusedLocationClient;

    private MoodHistory moodHistory;
    private FirebaseAuth mAuth;

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
        map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Marker"));

        map.setOnMyLocationButtonClickListener(this);
        map.setOnMyLocationClickListener(this);

        enableMyLocation();
        centerOnUserLocation();
        setUpdateListener();
        placeMoodHistoryMarkers();
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
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
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
        FirebaseSync fb = FirebaseSync.getInstance();

        fb.listenForUpdates(new FirebaseSync.DataStatus() {
            @Override
            public void onDataUpdated() {
                fb.fetchUserProfileObject(new UserProfileCallback() {
                    @Override
                    public void onUserProfileLoaded(UserProfile userProfile) {
                       placeMoodHistoryMarkers();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(MapActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e("Update Error", error);
            }
        });
    }

    /**
     * Places the current user's mood events on the map
     */
    private void placeMoodHistoryMarkers(){
        FirebaseSync fb = FirebaseSync.getInstance();
        // Fetch mood history from Firebase
        fb.fetchUserProfileObject(new UserProfileCallback() {
            @Override
            public void onUserProfileLoaded(UserProfile userProfile) {
                moodHistory = userProfile.getHistory();

                map.clear();
                for (MoodEvent m: moodHistory.getEvents()){
                    placeMoodEventMarker(m);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MapActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show(); // 1 time
            }
        });
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
        MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(moodEvent.getLatitude(), moodEvent.getLongitude())).title(moodEvent.getEmotionalState());

        Bitmap iconRes = BitmapFactory.decodeResource(getResources(), moodEvent.getMarkerResource());  // raw img

        Bitmap scaledIcon = Bitmap.createScaledBitmap(iconRes, 120, 200, false);  // scaled version

        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(scaledIcon));  // set the icon

        Marker marker = map.addMarker(markerOptions);

        if (marker != null){
            marker.setTag(moodEvent);
        }

        map.setOnMarkerClickListener(this);

    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        MoodEvent moodEvent = (MoodEvent)marker.getTag();

        if (moodEvent.getUserId() != null && moodEvent.getUserId().equals(mAuth.getCurrentUser().getUid())){
            MoodEventDetailsMapFragment.newInstance(moodEvent)
                    .show(getSupportFragmentManager(), "Mood Event Details");
        }

        return false;
    }

    @Override
    public void onMoodEventEdited(MoodEvent moodEvent) {
        FirebaseSync fb = FirebaseSync.getInstance();
        fb.fetchUserProfileObject(new UserProfileCallback() {
            @Override
            public void onUserProfileLoaded(UserProfile userProfile) {
                userProfile.setHistory(moodHistory);
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
        MoodEventDetailsAndEditingFragment.newInstance(moodEvent)
                .show(getSupportFragmentManager(), "Mood Event Details");
    }
}