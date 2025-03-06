package com.example.projectapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.Manifest;
import androidx.core.app.ActivityCompat;
public class GeoLocation {
    private Context context;
    public void Geolocation(Context context) {
        this.context = context;
    }

    @SuppressLint("MissingPermission")
    public Location getCurrentLocation() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted; proceed with accessing the location
        } else {
            // Permission is not granted; request it from the user
        }

        return null;
    }


}
