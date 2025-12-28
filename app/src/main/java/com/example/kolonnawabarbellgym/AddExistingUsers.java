package com.example.kolonnawabarbellgym;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.kolonnawabarbellgym.Database.DatabaseHelperClass;
import com.google.android.material.textfield.TextInputEditText;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddExistingUsers extends BaseActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int STORAGE_WRITE_PERMISSION_CODE = 102;

    private TextInputEditText etFirstName, etLastName, etEmail, etPhoneNumber, etNIC, etMonthlyFee;
    private RadioGroup statusRadioGroup;
    private ImageView profileImageView, imagePreview;
    private Button btnGallery, btnCamera, btnRetake, btnDelete, btnSubmit;
    private CardView imagePreviewLayout;

    private Bitmap selectedImageBitmap;
    private byte[] profileImageBytes;
    private boolean isImageSelected = false;
    private Uri cameraImageUri;

    private DatabaseHelperClass databaseHelper;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d("GalleryResult", "Gallery result received - ResultCode: " + result.getResultCode());
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    Log.d("GalleryResult", "Image URI selected: " + imageUri);
                    try {
                        selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        Log.d("GalleryResult", "Image loaded successfully - Dimensions: " +
                                selectedImageBitmap.getWidth() + "x" + selectedImageBitmap.getHeight());
                        showImagePreview(selectedImageBitmap);
                    } catch (IOException e) {
                        Log.e("GalleryResult", "Failed to load image: " + e.getMessage());
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("GalleryResult", "Gallery operation cancelled or failed");
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d("CameraResult", "Camera result received - ResultCode: " + result.getResultCode());
                if (result.getResultCode() == RESULT_OK) {
                    if (cameraImageUri != null) {
                        // Image was saved to the provided URI - NO NEED TO SAVE AGAIN
                        Log.d("CameraResult", "Camera image saved to URI: " + cameraImageUri);
                        try {
                            selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), cameraImageUri);
                            // Ensure portrait orientation
                            selectedImageBitmap = ensurePortraitOrientation(selectedImageBitmap);
                            Log.d("CameraResult", "Camera image loaded from URI - Dimensions: " +
                                    selectedImageBitmap.getWidth() + "x" + selectedImageBitmap.getHeight());
                            showImagePreview(selectedImageBitmap);

                            // REMOVED: saveImageToGalleryAlbum(selectedImageBitmap); - Image already saved!

                        } catch (IOException e) {
                            Log.e("CameraResult", "Failed to load camera image: " + e.getMessage());
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Fallback for apps that don't support URI - SAVE ONLY IN THIS CASE
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            selectedImageBitmap = (Bitmap) extras.get("data");
                            if (selectedImageBitmap != null) {
                                // Ensure portrait orientation
                                selectedImageBitmap = ensurePortraitOrientation(selectedImageBitmap);
                                Log.d("CameraResult", "Camera image from extras - Dimensions: " +
                                        selectedImageBitmap.getWidth() + "x" + selectedImageBitmap.getHeight());
                                showImagePreview(selectedImageBitmap);

                                // Save to gallery album ONLY for fallback case
                                saveImageToGalleryAlbum(selectedImageBitmap);
                            }
                        }
                    }
                } else {
                    Log.d("CameraResult", "Camera operation cancelled or failed");
                }
            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_existing_users);

        currentNavItemId = R.id.navigation_existinguser;
        setupBottomNavigation(R.id.navigation_existinguser);

        // Initialize database helper
        databaseHelper = new DatabaseHelperClass(this);

        initializeViews();
        setupClickListeners();

        Log.d("AddExistingUsers", "Activity created successfully");
    }

    private void initializeViews() {
        Log.d("InitializeViews", "Initializing views...");

        // Text inputs
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etNIC = findViewById(R.id.etNIC);
        etMonthlyFee = findViewById(R.id.etMonthlyFee);

        // Radio group
        statusRadioGroup = findViewById(R.id.statusRadioGroup);

        // Image views and buttons
        profileImageView = findViewById(R.id.profileImageView);
        imagePreview = findViewById(R.id.imagePreview);
        btnGallery = findViewById(R.id.btnGallery);
        btnCamera = findViewById(R.id.btnCamera);
        btnRetake = findViewById(R.id.btnRetake);
        btnDelete = findViewById(R.id.btnDelete);
        btnSubmit = findViewById(R.id.btnSubmit);
        imagePreviewLayout = findViewById(R.id.imagePreviewLayout);

        if (statusRadioGroup == null) {
            Log.e("InitializeViews", "statusRadioGroup is null - check layout ID");
        } else {
            Log.d("InitializeViews", "All views initialized successfully");
        }
    }

    private void setupClickListeners() {
        Log.d("SetupListeners", "Setting up click listeners...");

        btnGallery.setOnClickListener(v -> {
            Log.d("ButtonClick", "Gallery button clicked");
            openGallery();
        });
        btnCamera.setOnClickListener(v -> {
            Log.d("ButtonClick", "Camera button clicked");
            openCamera();
        });
        btnRetake.setOnClickListener(v -> {
            Log.d("ButtonClick", "Retake button clicked");
            retakeImage();
        });
        btnDelete.setOnClickListener(v -> {
            Log.d("ButtonClick", "Delete button clicked");
            deleteImage();
        });
        btnSubmit.setOnClickListener(v -> {
            Log.d("ButtonClick", "Submit button clicked");
            addUserToDatabase();
        });

        Log.d("SetupListeners", "Click listeners setup completed");
    }

    private void openGallery() {
        Log.d("GalleryAccess", "openGallery() called");
        Log.d("GalleryAccess", "Device Android Version: " + Build.VERSION.SDK_INT);

        boolean hasPermission = checkStoragePermission();
        Log.d("GalleryAccess", "Has storage permission: " + hasPermission);

        if (hasPermission) {
            Log.d("GalleryAccess", "Permission granted, opening gallery...");
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            // Ensure we only get images
            intent.setType("image/*");
            // Set explicit MIME type to avoid issues
            String[] mimeTypes = {"image/jpeg", "image/png", "image/jpg"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

            // Verify that the intent will resolve to an activity
            if (intent.resolveActivity(getPackageManager()) != null) {
                Log.d("GalleryAccess", "Gallery intent resolved successfully");
                galleryLauncher.launch(intent);
            } else {
                Log.e("GalleryAccess", "No gallery app found on device");
                Toast.makeText(this, "No gallery app found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("GalleryAccess", "Permission denied, requesting storage permission");
            requestStoragePermission();
        }
    }

    private void openCamera() {
        Log.d("CameraAccess", "openCamera() called");

        boolean hasCameraPermission = checkCameraPermission();
        boolean hasWritePermission = checkWriteStoragePermission();

        Log.d("CameraAccess", "Camera permission: " + hasCameraPermission);
        Log.d("CameraAccess", "Write storage permission: " + hasWritePermission);

        if (hasCameraPermission && hasWritePermission) {
            Log.d("CameraAccess", "All permissions granted, opening camera...");
            try {
                // Create a file to save the image
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String imageFileName = "Existing_User_" + timeStamp + ".jpg";

                // For Android 10+, use MediaStore
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    Log.d("CameraAccess", "Using MediaStore for Android 10+");
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Existing Users");

                    cameraImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    Log.d("CameraAccess", "Camera image URI created: " + cameraImageUri);

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);

                    // Force portrait orientation
                    intent.putExtra("android.intent.extras.CAMERA_FACING", 1); // Front camera
                    intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);

                    cameraLauncher.launch(intent);
                } else {
                    Log.d("CameraAccess", "Using FileProvider for older Android versions");
                    // For older versions, use FileProvider
                    File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Existing Users");
                    if (!storageDir.exists()) {
                        boolean created = storageDir.mkdirs();
                        Log.d("CameraAccess", "Storage directory created: " + created);
                    }

                    File imageFile = new File(storageDir, imageFileName);
                    cameraImageUri = FileProvider.getUriForFile(this,
                            getApplicationContext().getPackageName() + ".provider",
                            imageFile);

                    Log.d("CameraAccess", "Camera image URI created: " + cameraImageUri);

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    // Force portrait orientation
                    intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                    intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);

                    cameraLauncher.launch(intent);
                }
            } catch (Exception e) {
                Log.e("CameraAccess", "Error setting up camera: " + e.getMessage());
                e.printStackTrace();
                // Fallback to default camera intent without saving
                Log.d("CameraAccess", "Using fallback camera intent");
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraLauncher.launch(intent);
            }
        } else {
            Log.d("CameraAccess", "Missing permissions, requesting...");
            if (!hasCameraPermission) {
                Log.d("CameraAccess", "Requesting camera permission");
                requestCameraPermission();
            }
            if (!hasWritePermission) {
                Log.d("CameraAccess", "Requesting write storage permission");
                requestWriteStoragePermission();
            }
        }
    }

    private void saveImageToGalleryAlbum(Bitmap bitmap) {
        if (bitmap == null) {
            Log.d("SaveImage", "Bitmap is null, skipping save");
            return;
        }

        Log.d("SaveImage", "Saving image to gallery album...");
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // For Android 10+ - Use MediaStore
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String imageFileName = "Existing_User_" + timeStamp + ".jpg";

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Existing Users");
                values.put(MediaStore.Images.Media.IS_PENDING, 1);

                Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                Uri imageUri = getContentResolver().insert(collection, values);

                if (imageUri != null) {
                    try (OutputStream out = getContentResolver().openOutputStream(imageUri)) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    }

                    // Now that we're finished, update IS_PENDING to 0
                    values.clear();
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    getContentResolver().update(imageUri, values, null, null);

                    // Notify gallery
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imageUri));

                    Log.d("SaveImage", "Image saved to gallery album successfully");
                    Toast.makeText(this, "Image saved to Existing Users album", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("SaveImage", "Failed to create image URI for saving");
                }
            } else {
                // For older Android versions
                String albumName = "Existing Users";
                File albumDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);

                if (!albumDir.exists()) {
                    if (!albumDir.mkdirs()) {
                        Log.e("SaveImage", "Failed to create album directory");
                        Toast.makeText(this, "Failed to create album directory", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String imageFileName = "Existing_User_" + timeStamp + ".jpg";
                File imageFile = new File(albumDir, imageFileName);

                try (FileOutputStream out = new FileOutputStream(imageFile)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);

                    // Notify gallery
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    mediaScanIntent.setData(Uri.fromFile(imageFile));
                    sendBroadcast(mediaScanIntent);

                    Log.d("SaveImage", "Image saved to gallery album successfully");
                    Toast.makeText(this, "Image saved to Existing Users album", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Log.e("SaveImage", "Failed to save image: " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e("SaveImage", "Exception saving image to gallery: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image to gallery", Toast.LENGTH_SHORT).show();
        }
    }

    private void showImagePreview(Bitmap bitmap) {
        Log.d("ImagePreview", "Showing image preview");
        imagePreview.setImageBitmap(bitmap);
        imagePreviewLayout.setVisibility(View.VISIBLE);
        isImageSelected = true;

        // Convert bitmap to byte array for database storage
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        profileImageBytes = stream.toByteArray();
        Log.d("ImagePreview", "Image converted to byte array - Size: " + profileImageBytes.length + " bytes");
    }

    private void retakeImage() {
        Log.d("ImageAction", "Retaking image");
        imagePreviewLayout.setVisibility(View.GONE);
        isImageSelected = false;
        profileImageBytes = null;
        cameraImageUri = null;
        openCamera();
    }

    private void deleteImage() {
        Log.d("ImageAction", "Deleting image");
        imagePreviewLayout.setVisibility(View.GONE);
        isImageSelected = false;
        profileImageBytes = null;
        selectedImageBitmap = null;
        cameraImageUri = null;
        profileImageView.setImageResource(R.drawable.ic_person);
    }

    private boolean checkCameraPermission() {
        boolean hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        Log.d("PermissionCheck", "CAMERA permission: " + hasPermission);
        return hasPermission;
    }

    private boolean checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ requires READ_MEDIA_IMAGES
            boolean hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
            Log.d("PermissionCheck", "Android 13+ - READ_MEDIA_IMAGES permission: " + hasPermission);
            return hasPermission;
        } else {
            // Android 10-12 requires READ_EXTERNAL_STORAGE
            boolean hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
            Log.d("PermissionCheck", "Android 10-12 - READ_EXTERNAL_STORAGE permission: " + hasPermission);
            return hasPermission;
        }
    }

    private boolean checkWriteStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // For Android 10+, we don't need WRITE_EXTERNAL_STORAGE for MediaStore
            Log.d("PermissionCheck", "Android 10+ - WRITE_EXTERNAL_STORAGE not required for MediaStore");
            return true;
        }
        boolean hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        Log.d("PermissionCheck", "Android <10 - WRITE_EXTERNAL_STORAGE permission: " + hasPermission);
        return hasPermission;
    }

    private void requestCameraPermission() {
        Log.d("PermissionRequest", "Requesting CAMERA permission");
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_CODE);
    }

    private void requestStoragePermission() {
        Log.d("PermissionRequest", "Requesting storage permission for Android SDK: " + android.os.Build.VERSION.SDK_INT);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            Log.d("PermissionRequest", "Requesting READ_MEDIA_IMAGES permission");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    STORAGE_PERMISSION_CODE);
        } else {
            // Android 10-12
            Log.d("PermissionRequest", "Requesting READ_EXTERNAL_STORAGE permission");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        }
    }

    private void requestWriteStoragePermission() {
        Log.d("PermissionRequest", "Requesting WRITE_EXTERNAL_STORAGE permission");
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                STORAGE_WRITE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d("PermissionResult", "onRequestPermissionsResult - RequestCode: " + requestCode);

        if (permissions != null && permissions.length > 0) {
            for (int i = 0; i < permissions.length; i++) {
                Log.d("PermissionResult", "Permission: " + permissions[i] + " - Result: " +
                        (grantResults[i] == PackageManager.PERMISSION_GRANTED ? "GRANTED" : "DENIED"));
            }
        } else {
            Log.d("PermissionResult", "No permissions in request");
        }

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("PermissionResult", "Storage permission GRANTED, opening gallery");
                openGallery();
            } else {
                Log.d("PermissionResult", "Storage permission DENIED");
                Toast.makeText(this, "Storage permission denied for gallery access", Toast.LENGTH_SHORT).show();
                // Show explanation why permission is needed
                showPermissionExplanation();
            }
        } else if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("PermissionResult", "Camera permission GRANTED, opening camera");
                openCamera();
            } else {
                Log.d("PermissionResult", "Camera permission DENIED");
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == STORAGE_WRITE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("PermissionResult", "Write storage permission GRANTED");
                openCamera();
            } else {
                Log.d("PermissionResult", "Write storage permission DENIED");
                Toast.makeText(this, "Storage permission denied for saving images", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showPermissionExplanation() {
        Log.d("Permission", "Showing permission explanation dialog");
        new AlertDialog.Builder(this)
                .setTitle("Gallery Access Needed")
                .setMessage("This permission is required to select profile pictures from your gallery. Without this permission, you won't be able to choose images for member profiles.")
                .setPositiveButton("Grant Permission", (dialog, which) -> {
                    Log.d("Permission", "User clicked Grant Permission");
                    requestStoragePermission();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Log.d("Permission", "User canceled permission request");
                    Toast.makeText(this, "Gallery access is required for profile images", Toast.LENGTH_LONG).show();
                })
                .show();
    }

    private void addUserToDatabase() {
        Log.d("Database", "Adding user to database...");

        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String nic = etNIC.getText().toString().trim();
        String monthlyFee = etMonthlyFee.getText().toString().trim();

        Log.d("Database", "Form data - First: " + firstName + ", Last: " + lastName +
                ", Email: " + email + ", Phone: " + phoneNumber);

        // Validate required fields
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phoneNumber.isEmpty()) {
            Log.d("Database", "Validation failed - missing required fields");
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // For existing users, status is always 2 (Old Member)
        int status = 2;

        // Insert into database and get the actual member ID that was inserted
        String insertedMemberId = insertUserIntoDatabase(firstName, lastName, email, phoneNumber, nic, profileImageBytes, monthlyFee, status);
        try {
            if (insertedMemberId != null) {
                Log.d("Database", "User inserted successfully with ID: " + insertedMemberId);
                // Generate PDF with the actual inserted member ID
                generateUserPDF(insertedMemberId, firstName, lastName, email, phoneNumber, nic, monthlyFee, status);
                clearForm();

                Toast.makeText(this, "✓ Existing member added successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("Database", "Failed to insert user into database");
                Toast.makeText(this, "Failed to add existing user to database", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("Database", "Exception adding user: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to add existing user " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public String insertUserIntoDatabase(String firstName, String lastName,
                                         String email, String phoneNumber, String nic,
                                         byte[] profileImage, String monthlyFee, int status) {
        Log.d("Database", "Inserting user into database...");
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        // Get the next member ID
        String nextMemberId = getNextMemberId(db);
        if (nextMemberId == null) {
            Log.e("Database", "Failed to get next member ID");
            db.close();
            return null;
        }

        Log.d("Database", "Next member ID: " + nextMemberId);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(calendar.getTime());

        ContentValues values = new ContentValues();
        values.put("unique_id", nextMemberId);
        values.put("firstName", firstName);
        values.put("lastName", lastName);
        values.put("email", email);
        values.put("phoneNumber", phoneNumber);
        values.put("nic", nic);
        values.put("profileImage", profileImage);
        values.put("monthlyFee", monthlyFee);
        values.put("status", status);
        values.put("created_time", currentTime);

        long result = db.insert("new_users", null, values);
        db.close();

        if (result != -1) {
            Log.d("Database", "User inserted successfully with row ID: " + result);
            return nextMemberId; // Return the actual member ID that was inserted
        } else {
            Log.e("Database", "Failed to insert user into database");
            return null;
        }
    }

    private String getNextMemberId(SQLiteDatabase db) {
        String nextMemberId = null;
        Cursor cursor = null;

        try {
            // Query to get the highest existing member ID from new_users table
            cursor = db.rawQuery("SELECT unique_id FROM new_users ORDER BY userid DESC LIMIT 1", null);

            if (cursor != null && cursor.moveToFirst()) {
                String lastUniqueId = cursor.getString(cursor.getColumnIndexOrThrow("unique_id"));
                Log.d("Database", "Last unique ID found: " + lastUniqueId);

                if (lastUniqueId != null && lastUniqueId.startsWith("mem_")) {
                    try {
                        // Extract the number part and increment it
                        String numberPart = lastUniqueId.substring(4); // Remove "mem_" prefix
                        int nextNumber = Integer.parseInt(numberPart) + 1;
                        nextMemberId = "mem_" + String.format("%02d", nextNumber);
                        Log.d("Database", "Incremented member ID: " + nextMemberId);
                    } catch (NumberFormatException e) {
                        Log.e("Database", "Number format exception: " + e.getMessage());
                        e.printStackTrace();
                        // If parsing fails, start from mem_01
                        nextMemberId = "mem_01";
                    }
                } else {
                    // If no proper format found, start from mem_01
                    Log.d("Database", "No proper format found, starting from mem_01");
                    nextMemberId = "mem_01";
                }
            } else {
                // If no records exist, start from mem_01
                Log.d("Database", "No records found, starting from mem_01");
                nextMemberId = "mem_01";
            }
        } catch (Exception e) {
            Log.e("Database", "Exception getting next member ID: " + e.getMessage());
            e.printStackTrace();
            nextMemberId = "mem_01";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        Log.d("Database", "Final next member ID: " + nextMemberId);
        return nextMemberId;
    }

    private void generateUserPDF(String memberId, String firstName, String lastName, String email,
                                 String phoneNumber, String nic, String monthlyFee, int status) {
        Log.d("PDF", "Generating PDF for member: " + memberId);
        try {
            // Create PDF filename with proper naming convention
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = firstName + "_" + lastName + "_Existing_User_" + timeStamp + ".pdf";

            File pdfFile;

            // For Android 10+, use MediaStore to save to public Documents folder
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                Log.d("PDF", "Using MediaStore for Android 10+ to save PDF");

                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/Existing Users");

                Uri collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                Uri pdfUri = getContentResolver().insert(collection, values);

                if (pdfUri != null) {
                    try (OutputStream out = getContentResolver().openOutputStream(pdfUri)) {
                        generatePDFContent(out, memberId, firstName, lastName, email, phoneNumber, nic, monthlyFee, status);
                    }
                    Log.d("PDF", "PDF saved to public Documents/Existing Users folder: " + fileName);
                    showPDFSuccessDialog(pdfUri, fileName);
                } else {
                    throw new IOException("Failed to create PDF file in Documents folder");
                }

            } else {
                // For older Android versions, use traditional file system
                Log.d("PDF", "Using traditional file system for PDF storage");

                File documentsDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Existing Users");
                if (!documentsDir.exists()) {
                    boolean created = documentsDir.mkdirs();
                    Log.d("PDF", "Documents directory created: " + created);
                }

                pdfFile = new File(documentsDir, fileName);

                try (FileOutputStream out = new FileOutputStream(pdfFile)) {
                    generatePDFContent(out, memberId, firstName, lastName, email, phoneNumber, nic, monthlyFee, status);
                }

                Log.d("PDF", "PDF saved to: " + pdfFile.getAbsolutePath());

                // Notify media scanner
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(Uri.fromFile(pdfFile));
                sendBroadcast(mediaScanIntent);

                showPDFSuccessDialog(Uri.fromFile(pdfFile), fileName);
            }

        } catch (Exception e) {
            Log.e("PDF", "Failed to generate PDF: " + e.getMessage(), e);
            e.printStackTrace();
            Toast.makeText(this, "Failed to generate PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // New method to handle PDF content generation separately
    private void generatePDFContent(OutputStream outputStream, String memberId, String firstName, String lastName,
                                    String email, String phoneNumber, String nic, String monthlyFee, int status) {
        try {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument, PageSize.A4);

            // Add header with logo and title
            addHeader(document);

            // Add member details section with profile image
            addMemberDetailsSection(document, memberId, firstName, lastName, email, phoneNumber, nic, monthlyFee, status);

            // Add footer
            addFooter(document);

            // Add logo watermark
            addLogoWatermark(document);

            document.close();

            Log.d("PDF", "PDF content generated successfully");

        } catch (Exception e) {
            Log.e("PDF", "Error generating PDF content: " + e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF content", e);
        }
    }

    private void addHeader(Document document) {
        try {
            // Create a table for header with logo and text side by side
            Table headerTable = new Table(2);
            headerTable.setWidth(PageSize.A4.getWidth() - 72); // Account for margins
            headerTable.setHorizontalAlignment(HorizontalAlignment.CENTER);
            headerTable.setMarginTop(20);
            headerTable.setMarginBottom(20);

            // Add logo (bigger size)
            Bitmap logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mainlogo);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            logoBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] logoBytes = stream.toByteArray();

            ImageData imageData = ImageDataFactory.create(logoBytes);
            Image logo = new Image(imageData);
            logo.setWidth(120); // Bigger logo
            logo.setHeight(120);
            logo.setHorizontalAlignment(HorizontalAlignment.LEFT);

            // Add gym name and title with different color for existing member
            Paragraph gymName = new Paragraph("KOLONNAWA BARBELL GYM")
                    .setBold()
                    .setFontSize(18)
                    .setFontColor(new DeviceRgb(139, 0, 0)) // Dark red color for existing members
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(5);

            Paragraph title = new Paragraph("Existing Member Registration")
                    .setBold()
                    .setFontSize(14)
                    .setFontColor(new DeviceRgb(139, 0, 0)) // Dark red color
                    .setTextAlignment(TextAlignment.LEFT);

            // Create cell for text content
            Cell textCell = new Cell();
            textCell.setBorder(Border.NO_BORDER);
            textCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            textCell.add(gymName);
            textCell.add(title);

            // Create cell for logo
            Cell logoCell = new Cell();
            logoCell.setBorder(Border.NO_BORDER);
            logoCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            logoCell.add(logo);

            headerTable.addCell(logoCell);
            headerTable.addCell(textCell);

            document.add(headerTable);

            // Add separator line with dark red color
            Paragraph separator = new Paragraph("")
                    .setHeight(2)
                    .setBackgroundColor(new DeviceRgb(139, 0, 0)) // Dark red line
                    .setMarginTop(10)
                    .setMarginBottom(20);
            document.add(separator);

        } catch (Exception e) {
            Log.e("PDF", "Error adding header: " + e.getMessage());
            e.printStackTrace();
            // Fallback header without logo
            Paragraph gymName = new Paragraph("KOLONNAWA BARBELL GYM")
                    .setBold()
                    .setFontSize(20)
                    .setFontColor(new DeviceRgb(139, 0, 0))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20)
                    .setMarginBottom(5);

            Paragraph title = new Paragraph("Existing Member Registration")
                    .setBold()
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);

            document.add(gymName);
            document.add(title);
        }
    }

    private void addMemberDetailsSection(Document document, String memberId, String firstName, String lastName,
                                         String email, String phoneNumber, String nic,
                                         String monthlyFee, int status) {

        // Create main container table with border
        Table containerTable = new Table(1);
        containerTable.setWidth(PageSize.A4.getWidth() - 72);
        containerTable.setHorizontalAlignment(HorizontalAlignment.CENTER);
        containerTable.setMarginBottom(30);
        containerTable.setBorder(new SolidBorder(new DeviceRgb(139, 0, 0), 2)); // Dark red border

        // Add section title
        Paragraph sectionTitle = new Paragraph("Existing Member Information")
                .setBold()
                .setFontSize(16)
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(new DeviceRgb(139, 0, 0)) // Dark red background
                .setPadding(10);

        Cell titleCell = new Cell();
        titleCell.setBorder(Border.NO_BORDER);
        titleCell.add(sectionTitle);
        containerTable.addCell(titleCell);

        // Create a 2-column layout for details and profile image
        Table contentTable = new Table(2);
        contentTable.setWidth(PageSize.A4.getWidth() - 100);
        contentTable.setHorizontalAlignment(HorizontalAlignment.CENTER);
        contentTable.setMarginTop(10);
        contentTable.setMarginBottom(10);

        // Left column - Member details
        Cell detailsCell = new Cell();
        detailsCell.setBorder(Border.NO_BORDER);
        detailsCell.setPadding(10);

        // Create details table
        Table detailsTable = new Table(2);
        detailsTable.setWidth(350);

        // Define colors - using warmer colors for existing members
        Color labelColor = new DeviceRgb(255, 240, 240); // Light red tint for labels
        Color valueColor = ColorConstants.WHITE; // White for values

        // Add member details with better styling - USE THE PASSED memberId
        addStyledTableRow(detailsTable, "Member ID:", memberId, labelColor, valueColor);
        addStyledTableRow(detailsTable, "First Name:", firstName, labelColor, valueColor);
        addStyledTableRow(detailsTable, "Last Name:", lastName, labelColor, valueColor);
        addStyledTableRow(detailsTable, "Email:", email, labelColor, valueColor);
        addStyledTableRow(detailsTable, "Phone Number:", phoneNumber, labelColor, valueColor);
        addStyledTableRow(detailsTable, "NIC:", nic.isEmpty() ? "N/A" : nic, labelColor, valueColor);
        addStyledTableRow(detailsTable, "Monthly Fee:", monthlyFee.isEmpty() ? "N/A" : "Rs. " + monthlyFee, labelColor, valueColor);

        // For existing members, show "Returning Member" status
        String memberStatus = "Returning Member";
        addStyledTableRow(detailsTable, "Member Type:", memberStatus, labelColor, valueColor);

        addStyledTableRow(detailsTable, "Re-registration Date:",
                new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()), labelColor, valueColor);

        detailsCell.add(detailsTable);

        // Right column - Profile image
        Cell imageCell = new Cell();
        imageCell.setBorder(Border.NO_BORDER);
        imageCell.setPadding(10);
        imageCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        imageCell.setHorizontalAlignment(HorizontalAlignment.CENTER);

        if (selectedImageBitmap != null) {
            try {
                // Add profile image title
                Paragraph imageTitle = new Paragraph("Profile Photo")
                        .setBold()
                        .setFontSize(12)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(5);
                imageCell.add(imageTitle);

                // Convert bitmap to PDF image
                ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
                selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, imageStream);
                byte[] imageBytes = imageStream.toByteArray();

                ImageData profileImageData = ImageDataFactory.create(imageBytes);
                Image profileImage = new Image(profileImageData);
                profileImage.setWidth(100);
                profileImage.setHeight(120);
                profileImage.setHorizontalAlignment(HorizontalAlignment.CENTER);
                profileImage.setBorder(new SolidBorder(new DeviceRgb(139, 0, 0), 1)); // Dark red border

                imageCell.add(profileImage);
            } catch (Exception e) {
                Log.e("PDF", "Error adding profile image to PDF: " + e.getMessage());
                e.printStackTrace();
                // If image fails to load, show placeholder
                Paragraph noImage = new Paragraph("No Profile\nImage")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontSize(10)
                        .setFontColor(ColorConstants.GRAY);
                imageCell.add(noImage);
            }
        } else {
            // No image selected
            Paragraph noImage = new Paragraph("No Profile\nImage Available")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY);
            imageCell.add(noImage);
        }

        // Add cells to content table
        contentTable.addCell(detailsCell);
        contentTable.addCell(imageCell);

        // Add content table to container
        Cell contentCell = new Cell();
        contentCell.setBorder(Border.NO_BORDER);
        contentCell.setPadding(5);
        contentCell.add(contentTable);
        containerTable.addCell(contentCell);

        document.add(containerTable);
    }

    private void addStyledTableRow(Table table, String label, String value, Color labelColor, Color valueColor) {
        // Label cell with styling
        Cell labelCell = new Cell();
        labelCell.add(new Paragraph(label).setBold());
        labelCell.setPadding(8);
        labelCell.setBackgroundColor(labelColor);
        labelCell.setBorder(new SolidBorder(ColorConstants.GRAY, 0.5f));

        // Value cell with styling
        Cell valueCell = new Cell();
        valueCell.add(new Paragraph(value));
        valueCell.setPadding(8);
        valueCell.setBackgroundColor(valueColor);
        valueCell.setBorder(new SolidBorder(ColorConstants.GRAY, 0.5f));

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addFooter(Document document) {
        Paragraph footer = new Paragraph("\nThis certificate confirms that the above mentioned person is a registered returning member of Kolonnawa Barbell Gym.\n")
                .setFontSize(10)
                .setFontColor(new DeviceRgb(139, 0, 0)) // Dark red color
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic();

        Paragraph signature = new Paragraph("Authorized Signature\n\nKolonnawa Barbell Gym Management")
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(20)
                .setMarginRight(50);

        document.add(footer);
        document.add(signature);
    }

    private void addLogoWatermark(Document document) {
        try {
            // Load logo for watermark
            Bitmap logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mainlogo);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            logoBitmap.compress(Bitmap.CompressFormat.PNG, 30, stream); // Lower quality for watermark
            byte[] logoBytes = stream.toByteArray();

            ImageData logoImageData = ImageDataFactory.create(logoBytes);

            // Create multiple logo watermarks for better coverage
            for (int i = 0; i < 6; i++) {
                int x = 50 + (i % 2) * 250;
                int y = 150 + (i / 2) * 200;

                Image logoWatermark = new Image(logoImageData);
                logoWatermark.setWidth(80);
                logoWatermark.setHeight(80);
                logoWatermark.setOpacity(0.1f); // Very transparent
                logoWatermark.setRotationAngle(Math.toRadians(45));
                logoWatermark.setFixedPosition(x, y);

                document.add(logoWatermark);
            }

            // Add some text watermarks as well for variety
            for (int i = 0; i < 4; i++) {
                int x = 100 + (i % 2) * 200;
                int y = 100 + (i / 2) * 250;

                Paragraph textWatermark = new Paragraph("EXISTING MEMBER")
                        .setFontSize(20)
                        .setFontColor(new DeviceRgb(255, 200, 200), 0.08f) // Light red tint
                        .setTextAlignment(TextAlignment.CENTER)
                        .setRotationAngle(Math.toRadians(45))
                        .setFixedPosition(x, y, 150);

                document.add(textWatermark);
            }

        } catch (Exception e) {
            Log.e("PDF", "Error adding watermark: " + e.getMessage());
            e.printStackTrace();
            // Fallback to text watermark if logo fails
            try {
                for (int i = 0; i < 9; i++) {
                    int x = 50 + (i % 3) * 180;
                    int y = 150 + (i / 3) * 150;

                    Paragraph watermark = new Paragraph("EXISTING MEMBER")
                            .setFontSize(24)
                            .setFontColor(new DeviceRgb(255, 200, 200), 0.1f)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setRotationAngle(Math.toRadians(45))
                            .setFixedPosition(x, y, 200);

                    document.add(watermark);
                }
            } catch (Exception ex) {
                Log.e("PDF", "Error adding fallback watermark: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void showPDFSuccessDialog(Uri pdfUri, String fileName) {
        Log.d("PDF", "Showing PDF success dialog");
        new AlertDialog.Builder(this)
                .setTitle("Success!")
                .setMessage("✓ Existing member registered successfully!\n✓ PDF certificate saved to: Documents/Existing Users/" + fileName)
                .setPositiveButton("View & Share PDF", (dialog, which) -> {
                    Log.d("PDF", "User selected to view/share PDF");
                    viewAndSharePDF(pdfUri);
                })
                .setNegativeButton("OK", (dialog, which) -> {
                    Log.d("PDF", "User dismissed PDF dialog");
                    Toast.makeText(this, "Existing member registration completed!", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void viewAndSharePDF(Uri pdfUri) {
        Log.d("PDF", "Viewing and sharing PDF: " + pdfUri.toString());
        try {
            // Create intent to view PDF
            Intent viewIntent = new Intent(Intent.ACTION_VIEW);
            viewIntent.setDataAndType(pdfUri, "application/pdf");
            viewIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Create intent to share PDF
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Kolonnawa Barbell Gym - Existing Member Registration");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Existing member registration certificate from Kolonnawa Barbell Gym");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Create chooser with both options
            Intent chooserIntent = Intent.createChooser(viewIntent, "Existing Member Certificate");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { shareIntent });

            startActivity(chooserIntent);

        } catch (Exception e) {
            Log.e("PDF", "Error viewing/sharing PDF: " + e.getMessage());
            Toast.makeText(this, "No PDF viewer app found", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap ensurePortraitOrientation(Bitmap bitmap) {
        if (bitmap.getWidth() > bitmap.getHeight()) {
            // Image is landscape, rotate to portrait
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle(); // Recycle the original bitmap
            return rotatedBitmap;
        }
        return bitmap;
    }

    private void clearForm() {
        Log.d("Form", "Clearing form data");
        etFirstName.setText("");
        etLastName.setText("");
        etEmail.setText("");
        etPhoneNumber.setText("");
        etNIC.setText("");
        etMonthlyFee.setText("");

        if (statusRadioGroup != null) {
            statusRadioGroup.clearCheck();
            Log.d("Form", "Radio group cleared");
        } else {
            Log.e("Form", "statusRadioGroup is null - cannot clear check");
        }

        deleteImage(); // This will reset the image
    }

    @Override
    protected void onDestroy() {
        Log.d("AddExistingUsers", "Activity destroyed");
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
            Log.d("Database", "Database helper closed");
        }
    }
}