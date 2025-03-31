// -----------------------------------------------------------------------------
// File: GeoLocation.java
// -----------------------------------------------------------------------------
// This file defines the GeoLocation class, which encapsulates location retrieval
// functionality for the ProjectApp. It provides methods to get the last known location
// synchronously and to fetch a fresh location update asynchronously using the
// Fused Location Provider.
//
// Design Pattern: MVC (Model)
// Outstanding Issues:
//  N/A
// -----------------------------------------------------------------------------

package com.example.projectapp.models;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.Manifest;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

/* https://developer.android.com/develop/sensors-and-location/location/request-updates
https://notificare.com/blog/2024/01/26/android-location-permission-guide/
Also help from ChatGPT while building it.

 */
public class GeoLocation {
    private Context context;
    private FusedLocationProviderClient fusedLocationClient;

    /**
     * Callback interface for asynchronous location updates.
     */
    public interface OnLocationReceivedListener {
        void onLocationReceived(Location location);
        void onLocationFailure(String error);
    }

    /**
     * Constructs a GeoLocation object.
     *
     * @param context The application context.
     */
    public GeoLocation(Context context) {
        this.context = context;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * Retrieves the last known location using the system LocationManager.
     * Note: This method returns null if no cached location is available.
     *
     * @return The last known Location or null.
     */
    @SuppressLint("MissingPermission")
    public Location getCurrentLocation() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            return location;
        } else {
            return null;
        }
    }

    /**
     * Requests a fresh location update asynchronously using the Fused Location Provider.
     * The result is delivered via the provided OnLocationReceivedListener.
     *
     * @param listener The callback to receive the location update.
     */
    @SuppressLint("MissingPermission")
    public void fetchFreshLocation(final OnLocationReceivedListener listener) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);         // Desired interval: 10 seconds
        locationRequest.setFastestInterval(5000);     // Fastest interval: 5 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    listener.onLocationFailure("Unable to get location");
                    return;
                }
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    listener.onLocationReceived(location);
                } else {
                    listener.onLocationFailure("Location is null");
                }
                fusedLocationClient.removeLocationUpdates(this);
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }
}
