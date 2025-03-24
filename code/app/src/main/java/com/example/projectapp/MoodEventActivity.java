// -----------------------------------------------------------------------------
// File: MoodEventActivity.java
// -----------------------------------------------------------------------------
// This file defines the MoodEventActivity class, which likely handles the display
// or management of mood-related events within the ProjectApp. It may serve as a
// secondary activity for tracking or viewing mood data.
//
// Design Pattern: MVC (View)
// Outstanding Issues:
//  N/A

// -----------------------------------------------------------------------------

package com.example.projectapp;

import android.content.res.Resources;
import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This activity handles creating a new MoodEvent and appending it to Firestore.
 */
public class MoodEventActivity extends AppCompatActivity {

    private Spinner spinnerEmotionalState, spinnerSocialSituation;
    private EditText editReason;
    private static final long MAX_PHOTO_SIZE = 65536;
    private Button buttonUploadPhoto, buttonAddLocation, buttonAddEvent,
            buttonViewMap, buttonBackHome, buttonVisibility;
    private ImageView imageView;
    private Uri imageUri;
    private static final int REQUEST_CAMERA_PERMISSION = 1001;
    private static final int REQUEST_LOCATION_PERMISSION = 2001;
    private static final int CAMERA_REQUEST_CODE = 1002;
    private LatLng eventLocation;
    private boolean isPublic = false; // toggled by buttonVisibility
    private FirebaseAuth mAuth;
    private ActivityResultLauncher<Intent> cameraLauncher, galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_event);

        mAuth = FirebaseAuth.getInstance();

        bindUIElements();
        configureVisibilityToggle();
        configureLocationButton();
        configureViewMapButton();
        configureAddEventButton();
        configurePhotoLaunchers();
        configureSpinnerAdapters();
        configureUploadPhotoButton();
        configureBottomNav();  // <-- call bottom nav setup here
    }

    private void bindUIElements() {
        spinnerEmotionalState = findViewById(R.id.spinner_emotional_state);
        editReason = findViewById(R.id.edit_reason);
        spinnerSocialSituation = findViewById(R.id.spinner_social_situation);
        buttonUploadPhoto = findViewById(R.id.button_upload_photo);
        buttonAddLocation = findViewById(R.id.button_add_location);
        buttonVisibility = findViewById(R.id.button_visibility);
        buttonAddEvent = findViewById(R.id.button_add_event);
        buttonViewMap = findViewById(R.id.button_view_map);
        buttonBackHome = findViewById(R.id.button_back_home);
        imageView = findViewById(R.id.imageView);
    }

    private void configureVisibilityToggle() {
        buttonVisibility.setText("Make Public");
        buttonVisibility.setOnClickListener(view -> {
            isPublic = !isPublic;
            buttonVisibility.setText(isPublic ? "Make Private" : "Make Public");
            Toast.makeText(this,
                    "Event is now " + (isPublic ? "Public" : "Private"),
                    Toast.LENGTH_SHORT).show();
        });
        // Back button → main page
        buttonBackHome.setOnClickListener(view -> {
            Intent intent = new Intent(MoodEventActivity.this, MainActivity.class);
            intent.putExtra("selected_fragment", "home");
            startActivity(intent);
            finish();
        });
    }

    private void configureLocationButton() {
        buttonAddLocation.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
            } else {
                GeoLocation geoLocation = new GeoLocation(this);
                geoLocation.fetchFreshLocation(new GeoLocation.OnLocationReceivedListener() {
                    @Override
                    public void onLocationReceived(Location location) {
                        eventLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        Toast.makeText(MoodEventActivity.this,
                                "Location Added: " + eventLocation.latitude + ", " + eventLocation.longitude,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onLocationFailure(String error) {
                        Toast.makeText(MoodEventActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void configureViewMapButton() {
        buttonViewMap.setOnClickListener(view -> {
            if (eventLocation == null) {
                Toast.makeText(this, "No location added!", Toast.LENGTH_SHORT).show();
                return;
            }
            MapDialogFragment dialogFragment = MapDialogFragment
                    .newInstance(eventLocation.latitude, eventLocation.longitude);
            dialogFragment.show(getSupportFragmentManager(), "MapDialog");
        });
    }

    /**
     * The crucial logic that fetches the user profile, appends a new event,
     * and writes to Firestore.
     */
    private void configureAddEventButton() {
        buttonAddEvent.setOnClickListener(view -> {
            String emotionalStateString = spinnerEmotionalState.getSelectedItem().toString();
            String reason = editReason.getText().toString().trim();
            String socialSituation = spinnerSocialSituation.getSelectedItem().toString().trim();

            if ((reason.isEmpty() && imageUri == null)
                    || (!reason.isEmpty() && !MoodEvent.validReason(reason))) {
                Toast.makeText(this,
                        "Provide a valid reason (≤200 chars) or a photo",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (socialSituation.equals("Choose not to answer")) {
                socialSituation = null;
            }

            String userId = (mAuth.getCurrentUser() != null)
                    ? mAuth.getCurrentUser().getUid() : null;
            if (userId == null) {
                Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            MoodEvent newEvent = new MoodEvent(
                    emotionalStateString,
                    reason.isEmpty() ? null : reason,
                    socialSituation,
                    imageUri,
                    userId
            );
            newEvent.setPublic(isPublic);


            if (eventLocation != null) {
                newEvent.setLatitude(eventLocation.latitude);
                newEvent.setLongitude(eventLocation.longitude);
            }


            FirebaseSync fb = FirebaseSync.getInstance();
            fb.fetchUserProfileObject(new UserProfileCallback() {
                @Override
                public void onUserProfileLoaded(UserProfile userProfile) {
                    fb.addEventToProfile(userProfile, newEvent);
                    Toast.makeText(MoodEventActivity.this,
                            "Mood Event Added!",
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(MoodEventActivity.this,
                            "Failed to add event: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });
    }





    private void configureSpinnerAdapters() {
        String[] emotionalStates = getResources().getStringArray(R.array.emotional_states);
        MoodSpinnerAdapter adapter = new MoodSpinnerAdapter(
                this,
                R.layout.spinner_item,
                emotionalStates
        );
        spinnerEmotionalState.setAdapter(adapter);
    }

    private void configureUploadPhotoButton() {
        buttonUploadPhoto.setOnClickListener(view -> {
            showImagePickerDialog();
        });
    }


    private void configureBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            Intent intent = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                intent = new Intent(this, HomeActivity.class);
            } else if (itemId == R.id.nav_map) {
                intent = new Intent(this, MapActivity.class);
            } else if (itemId == R.id.nav_history) {
                intent = new Intent(this, HistoryActivity.class);
            } else if (itemId == R.id.nav_inbox) {
                intent = new Intent(this, InboxActivity.class);
            } else if (itemId == R.id.nav_profile) {
                intent = new Intent(this, ProfileActivity.class);
            } else {
                // Unknown item
                return false;
            }

            // If we found a matching intent, start that Activity
            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }

            // Returning true indicates we handled this menu item
            return true;
        });
    }


    //========== Camera and Gallery Handling Below ==========//

    private void configurePhotoLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Uri compressedUri = compressAndValidatePhoto(imageUri);
                        if (compressedUri != null) {
                            imageUri = compressedUri; // Update imageUri with compressed version
                            imageView.setImageURI(imageUri);
                            Toast.makeText(this, "Photo Captured!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Camera cancelled or failed", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        Uri compressedUri = compressAndValidatePhoto(selectedImageUri);
                        if (compressedUri != null) {
                            imageUri = compressedUri;
                            imageView.setImageURI(imageUri);
                            Toast.makeText(this, "Image Selected!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this,
                        "Camera permission is required to take pictures",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source")
                .setItems(new String[]{"Take a Photo", "Choose from Gallery"}, (dialog, which) -> {
                    if (which == 0) {
                        checkCameraPermission();
                    } else {
                        openGallery();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(
                        this, getPackageName() + ".fileprovider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                cameraLauncher.launch(cameraIntent);
            } else {
                Toast.makeText(this, "Unable to create image file", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );
        galleryLauncher.launch(galleryIntent);
    }

    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(new Date());
            File storageDir = getExternalFilesDir(null);
            if (storageDir != null && !storageDir.exists()) {
                storageDir.mkdirs();
            }
            return File.createTempFile(
                    "IMG_" + timeStamp,
                    ".jpg",
                    storageDir
            );
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ImagePicker.REQUEST_CODE) {
                Uri imageUri = data.getData();
                imageView.setImageURI(imageUri);
                Toast.makeText(this, "Image Selected!", Toast.LENGTH_SHORT).show();
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                imageView.setImageURI(imageUri);
                Toast.makeText(this, "Photo Captured!", Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri compressAndValidatePhoto(Uri inputUri) {
        try {
            // Get the file from URI
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File photoFile = new File(getCacheDir(), "temp_photo_"+timeStamp+".jpg");
            Bitmap bitmap = BitmapFactory.decodeStream(
                    getContentResolver().openInputStream(inputUri)
            );

            // Compress until size is under 65,536 bytes
            int quality = 100;
            FileOutputStream fos = new FileOutputStream(photoFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            fos.close();

            while (photoFile.length() >= MAX_PHOTO_SIZE && quality > 0) {
                quality -= 5;
                fos = new FileOutputStream(photoFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
                fos.close();
            }

            if (photoFile.length() >= MAX_PHOTO_SIZE) {
                throw new IOException("Cannot compress photo below 64 KB");
            }
            return Uri.fromFile(photoFile);
        } catch (Exception e) {
            Toast.makeText(this, "Error processing photo: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}
