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
    private Button buttonViewMap; // New button for viewing map

    private LatLng eventLocation; // Store selected event location

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
        imageView = findViewById(R.id.imageView); // Replace with your ImageView ID
        buttonAddLocation.setOnClickListener(view -> {
            // Launch a map picker or retrieve current location (dummy location used here)
            eventLocation = new LatLng(37.7749, -122.4194); // Example: San Francisco
            Toast.makeText(this, "Location Added!", Toast.LENGTH_SHORT).show();
        });
        // Setup button listener for viewing map
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
        buttonAddEvent.setOnClickListener(view -> {
            String emotionalStateString = spinnerEmotionalState.getSelectedItem().toString();
            String trigger = editTrigger.getText().toString().trim();
            String socialSituation = spinnerSocialSituation.getSelectedItem().toString();

            // Validate and create a MoodEvent
            if (!MoodEvent.validTrigger(trigger)) {
                Toast.makeText(this, "Trigger is invalid. (<=20 chars, <=3 words)", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                MoodEvent newEvent = new MoodEvent(emotionalStateString, trigger, socialSituation);
                Toast.makeText(this, "Mood Event Added!", Toast.LENGTH_SHORT).show();
                finish();
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

        // In your onCreate() method:
        String[] emotionalStates = getResources().getStringArray(R.array.emotional_states);
        MoodSpinnerAdapter adapter = new MoodSpinnerAdapter(this, R.layout.spinner_item, emotionalStates);
        spinnerEmotionalState.setAdapter(adapter);


        // Setup button listener for photo upload (optional)
        buttonUploadPhoto.setOnClickListener(view -> {
            showImagePickerDialog(); // Open the image picker dialog
        });

        // Setup button listener for adding location (optional)
        buttonAddLocation.setOnClickListener(view -> {
            // Code to get or pick a location
        });

        // Listener for the "Add Event" button
        buttonAddEvent.setOnClickListener(view -> {
            // 1. Retrieve values from UI
            String emotionalStateString = spinnerEmotionalState.getSelectedItem().toString();
            String briefExplanation = editBriefExplanation.getText().toString().trim();
            String trigger = editTrigger.getText().toString().trim();
            String socialSituation = spinnerSocialSituation.getSelectedItem().toString();

            // Convert the selected mood string to a MoodType enum
            MoodType moodType = MoodType.fromString(emotionalStateString);
            if (moodType == null) {
                Toast.makeText(this, "Selected mood is not recognized.", Toast.LENGTH_SHORT).show();
                return;
            }
            int moodColor = moodType.getColorCode();
            int emoticonResId = moodType.getEmoticonResId();

            // 2. Validate input (e.g., validate trigger length)
            if (!MoodEvent.validTrigger(trigger)) {
                Toast.makeText(this, "Trigger is invalid. (<=20 chars, <=3 words)", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3. Create and add MoodEvent
            try {
                // Create a new MoodEvent (ensure your MoodEvent class supports moodColor and emoticonResId)
                MoodEvent newEvent = new MoodEvent(emotionalStateString, trigger, socialSituation);
                // Optionally, store the brief explanation if your model supports it
                // newEvent.setBriefExplanation(briefExplanation);

                // 4. Save to MoodHistory or via ViewModel/Repository (not shown here)
                // e.g., moodHistory.addEvent(newEvent);

                // 5. Notify user and finish activity
                Toast.makeText(this, "Mood Event Added! Color: " + moodColor, Toast.LENGTH_SHORT).show();
                finish();  // or navigate to another screen
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, "Invalid input: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Setup Bottom Navigation Listener
        // Setup Bottom Navigation Listener
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent;

            if (id == R.id.nav_home) {
                // Navigate to MainActivity and select Home fragment
                intent = new Intent(MoodEventActivity.this, MainActivity.class);
                intent.putExtra("selected_fragment", "home");
                startActivity(intent);
                finish(); // Optional: finish MoodEventActivity
                return true;
            } else if (id == R.id.nav_map) {
                // Navigate to MainActivity and select Map fragment
                intent = new Intent(MoodEventActivity.this, MainActivity.class);
                intent.putExtra("selected_fragment", "map");
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_history) {
                // Navigate to MainActivity and select History fragment
                intent = new Intent(MoodEventActivity.this, MainActivity.class);
                intent.putExtra("selected_fragment", "history");
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_inbox) {
                // Navigate to MainActivity and select Inbox fragment
                intent = new Intent(MoodEventActivity.this, MainActivity.class);
                intent.putExtra("selected_fragment", "inbox");
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                // Navigate to MainActivity and select Profile fragment
                intent = new Intent(MoodEventActivity.this, MainActivity.class);
                intent.putExtra("selected_fragment", "profile");
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            // Permission already granted, open the camera directly
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
                        checkCameraPermission(); // This will call openCamera() if permitted
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
            File storageDir = getExternalFilesDir(null); // Use null for default external files directory
            if (storageDir != null && !storageDir.exists()) {
                storageDir.mkdirs(); // Create directory if it doesn’t exist
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
                imageView.setImageURI(imageUri); // Display the selected image
                Toast.makeText(this, "Image Selected!", Toast.LENGTH_SHORT).show();
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                imageView.setImageURI(imageUri); // Display the captured image
                Toast.makeText(this, "Photo Captured!", Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

}

