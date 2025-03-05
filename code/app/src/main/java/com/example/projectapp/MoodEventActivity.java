package com.example.projectapp;

import static android.app.Activity.RESULT_OK;
import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;
import static androidx.core.content.ContextCompat.startActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
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
import android.widget.Spinner;
import android.widget.EditText;
import android.widget.Button;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MoodEventActivity extends AppCompatActivity {

    private Spinner spinnerEmotionalState;
    private EditText editBriefExplanation;
    private EditText editTrigger;
    private Spinner spinnerSocialSituation;
    private Button buttonUploadPhoto;
    private Button buttonAddLocation;
    private Button buttonAddEvent;
    private Button buttonViewMap;
    private Button buttonBackHome;
    private LatLng eventLocation;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    private Uri imageUri;
    private ImageView imageView;
    private static final int REQUEST_CAMERA_PERMISSION = 1001;
    private static final int CAMERA_REQUEST_CODE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_event);

        // Bind UI elements
        spinnerEmotionalState = findViewById(R.id.spinner_emotional_state);
        editBriefExplanation = findViewById(R.id.edit_brief_explanation);
        editTrigger = findViewById(R.id.edit_trigger);
        spinnerSocialSituation = findViewById(R.id.spinner_social_situation);
        buttonUploadPhoto = findViewById(R.id.button_upload_photo);
        buttonAddLocation = findViewById(R.id.button_add_location);
        buttonAddEvent = findViewById(R.id.button_add_event);
        buttonViewMap = findViewById(R.id.button_view_map);
        buttonBackHome = findViewById(R.id.button_back_home);
        imageView = findViewById(R.id.imageView);

        // Setup Back button to go to home page
        buttonBackHome.setOnClickListener(view -> {
            Intent intent = new Intent(MoodEventActivity.this, MainActivity.class);
            intent.putExtra("selected_fragment", "home");
            startActivity(intent);
            finish();
        });

        // Setup Add Location button
        buttonAddLocation.setOnClickListener(view -> {
            eventLocation = new LatLng(37.7749, -122.4194); // Example: San Francisco
            Toast.makeText(this, "Location Added!", Toast.LENGTH_SHORT).show();
        });

        // Setup View Map button
        buttonViewMap.setOnClickListener(view -> {
            if (eventLocation == null) {
                Toast.makeText(this, "No location added!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(MoodEventActivity.this, MapViewActivity.class);
            intent.putExtra("latitude", eventLocation.latitude);
            intent.putExtra("longitude", eventLocation.longitude);
            startActivity(intent);
        });

        // Setup Add Event button (finishes activity, returns to previous screen)
        buttonAddEvent.setOnClickListener(view -> {
            String emotionalStateString = spinnerEmotionalState.getSelectedItem().toString();
            String briefExplanation = editBriefExplanation.getText().toString().trim();
            String trigger = editTrigger.getText().toString().trim();
            String socialSituation = spinnerSocialSituation.getSelectedItem().toString();

            if (!MoodEvent.validTrigger(trigger)) {
                Toast.makeText(this, "Trigger is invalid. (<=20 chars, <=3 words)", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                MoodEvent newEvent = new MoodEvent(emotionalStateString, trigger, socialSituation);
                Toast.makeText(this, "Mood Event Added!", Toast.LENGTH_SHORT).show();
                finish(); // Return to previous screen (e.g., last fragment in MainActivity)
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, "Invalid input: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        imageView.setImageURI(imageUri);
                        Toast.makeText(this, "Photo Captured!", Toast.LENGTH_SHORT).show();
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
                        imageView.setImageURI(selectedImageUri);
                        Toast.makeText(this, "Image Selected!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        String[] emotionalStates = getResources().getStringArray(R.array.emotional_states);
        MoodSpinnerAdapter adapter = new MoodSpinnerAdapter(this, R.layout.spinner_item, emotionalStates);
        spinnerEmotionalState.setAdapter(adapter);

        buttonUploadPhoto.setOnClickListener(view -> {
            showImagePickerDialog();
        });

        // Setup Bottom Navigation Listener
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            Intent intent = new Intent(MoodEventActivity.this, MainActivity.class);

            int id = item.getItemId();
            if (id == R.id.nav_home) {
                intent.putExtra("selected_fragment", "home");
            } else if (id == R.id.nav_map) {
                intent.putExtra("selected_fragment", "map");
            } else if (id == R.id.nav_history) {
                intent.putExtra("selected_fragment", "history");
            } else if (id == R.id.nav_inbox) {
                intent.putExtra("selected_fragment", "inbox");
            } else if (id == R.id.nav_profile) {
                intent.putExtra("selected_fragment", "profile");
            } else {
                return false; // Unknown item
            }

            finish(); // Close the MoodEventActivity before starting MainActivity
            startActivity(intent);
            return true;
        });

    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to take pictures", Toast.LENGTH_SHORT).show();
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
                imageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
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
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File storageDir = getExternalFilesDir(null);
            if (storageDir != null && !storageDir.exists()) {
                storageDir.mkdirs();
            }
            File imageFile = File.createTempFile("IMG_" + timeStamp, ".jpg", storageDir);
            return imageFile;
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
}