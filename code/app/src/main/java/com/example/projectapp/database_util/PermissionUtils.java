package com.example.projectapp.database_util;

import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest.permission;

/**
 * Utility class for access to runtime permissions.
 */
public abstract class PermissionUtils {

     /*
    The following code is from Google Maps Platform "Location Data Tutorial"
    Written by: Google
    Taken by Luke Yaremko on 2025-03-20
    https://developers.google.com/maps/documentation/android-sdk/location#:~:text=If%20your%20app%20needs%20to,location%20returned%20by%20the%20API.
     */

    /**
     * Requests the fine and coarse location permissions. If a rationale with an additional
     * explanation should be shown to the user, displays a dialog that triggers the request.
     */
    public static void requestLocationPermissions(AppCompatActivity activity, int requestId,
                                                  boolean finishActivity) {
        if (ActivityCompat
                .shouldShowRequestPermissionRationale(activity, permission.ACCESS_FINE_LOCATION) ||
                ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        permission.ACCESS_COARSE_LOCATION)) {
            // Display a dialog with rationale.
            //PermissionUtils.RationaleDialog.newInstance(requestId, finishActivity)
            //.show(activity.getSupportFragmentManager(), "dialog");
        } else {
            // Location permission has not been granted yet, request it.
            ActivityCompat.requestPermissions(activity,
                    new String[]{permission.ACCESS_FINE_LOCATION, permission.ACCESS_COARSE_LOCATION},
                    requestId);
        }
    }

    /**
     * Checks if the result contains a {@link PackageManager#PERMISSION_GRANTED} result for a
     * permission from a runtime permissions request.
     *
     * @see androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
     */
    public static boolean isPermissionGranted(String[] grantPermissions, int[] grantResults,
                                              String permission) {
        for (int i = 0; i < grantPermissions.length; i++) {
            if (permission.equals(grantPermissions[i])) {
                return grantResults[i] == PackageManager.PERMISSION_GRANTED;
            }
        }
        return false;
    }
}