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
import android.media.ExifInterface;
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
import java.util.Date;
import java.util.Locale;

public class AddUser extends BaseActivity {

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
    private String currentPhotoPath;

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
                    if (currentPhotoPath != null) {
                        // Image was saved to the file path
                        Log.d("CameraResult", "Camera image saved to path: " + currentPhotoPath);
                        try {
                            // Load and rotate the image properly
                            selectedImageBitmap = handleImageRotation(currentPhotoPath);
                            if (selectedImageBitmap != null) {
                                Log.d("CameraResult", "Camera image loaded from path - Dimensions: " +
                                        selectedImageBitmap.getWidth() + "x" + selectedImageBitmap.getHeight());
                                showImagePreview(selectedImageBitmap);

                                // Save to gallery album with member name (only save, don't show duplicate toast)
                                saveImageToGalleryAlbum(selectedImageBitmap);
                            }
                        } catch (IOException e) {
                            Log.e("CameraResult", "Failed to load camera image: " + e.getMessage());
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Fallback for apps that don't support file saving
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            selectedImageBitmap = (Bitmap) extras.get("data");
                            Log.d("CameraResult", "Camera image from extras - Dimensions: " +
                                    (selectedImageBitmap != null ? selectedImageBitmap.getWidth() + "x" + selectedImageBitmap.getHeight() : "null"));
                            showImagePreview(selectedImageBitmap);

                            // Save to gallery album with member name
                            saveImageToGalleryAlbum(selectedImageBitmap);
                        }
                    }
                } else {
                    Log.d("CameraResult", "Camera operation cancelled or failed");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        currentNavItemId = R.id.navigation_newuser;
        setupBottomNavigation(R.id.navigation_newuser);

        // Initialize database helper
        databaseHelper = new DatabaseHelperClass(this);

        initializeViews();
        setupClickListeners();
        checkStoragePermission();

        Log.d("AddUser", "Activity created successfully");
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

        Log.d("InitializeViews", "All views initialized successfully");
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
                // Create a temporary file to store the image
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "_";

                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File imageFile = File.createTempFile(
                        imageFileName,  /* prefix */
                        ".jpg",         /* suffix */
                        storageDir      /* directory */
                );

                // Save the file path for later use
                currentPhotoPath = imageFile.getAbsolutePath();
                Log.d("CameraAccess", "Camera image path: " + currentPhotoPath);

                Uri photoURI = FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".provider",
                        imageFile);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                cameraLauncher.launch(intent);

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

    private Bitmap handleImageRotation(String imagePath) throws IOException {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        if (bitmap == null) return null;

        ExifInterface exif = new ExifInterface(imagePath);
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            default:
                return bitmap; // No rotation needed
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void saveImageToGalleryAlbum(Bitmap bitmap) {
        if (bitmap == null) {
            Log.d("SaveImage", "Bitmap is null, skipping save");
            return;
        }

        Log.d("SaveImage", "Saving image to gallery album...");
        try {
            // Get member name for file naming
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String memberName = "New_User";

            if (!firstName.isEmpty() && !lastName.isEmpty()) {
                memberName = firstName + "_" + lastName;
            } else if (!firstName.isEmpty()) {
                memberName = firstName;
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = memberName + "_" + timeStamp + ".jpg";

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // For Android 10+ - Use MediaStore
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Kolonnawa Gym/New Users");
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

                    Log.d("SaveImage", "Image saved to gallery successfully: " + imageFileName);
                    // Don't show toast here to avoid duplicate notifications
                } else {
                    Log.e("SaveImage", "Failed to create image URI for saving");
                }
            } else {
                // For older Android versions
                String albumName = "Kolonnawa Gym/New Users";
                File albumDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);

                if (!albumDir.exists()) {
                    if (!albumDir.mkdirs()) {
                        Log.e("SaveImage", "Failed to create album directory");
                        return;
                    }
                }

                File imageFile = new File(albumDir, imageFileName);

                try (FileOutputStream out = new FileOutputStream(imageFile)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);

                    // Notify gallery
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    mediaScanIntent.setData(Uri.fromFile(imageFile));
                    sendBroadcast(mediaScanIntent);

                    Log.d("SaveImage", "Image saved to gallery successfully: " + imageFileName);
                    // Don't show toast here to avoid duplicate notifications
                } catch (IOException e) {
                    Log.e("SaveImage", "Failed to save image: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            Log.e("SaveImage", "Exception saving image to gallery: " + e.getMessage());
            e.printStackTrace();
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
        selectedImageBitmap = null;
        currentPhotoPath = null;
        openCamera();
    }

    private void deleteImage() {
        Log.d("ImageAction", "Deleting image");
        imagePreviewLayout.setVisibility(View.GONE);
        isImageSelected = false;
        profileImageBytes = null;
        selectedImageBitmap = null;
        currentPhotoPath = null;
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

        // Get status from radio buttons
        int status = 0; // Default to 0 (No)
        int selectedId = statusRadioGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.radioYes) {
            status = 1;
        }
        Log.d("Database", "Admission status: " + (status == 1 ? "Paid" : "Unpaid"));

        // Insert into database and get the actual member ID that was inserted
        String insertedMemberId = insertUserIntoDatabase(firstName, lastName, email, phoneNumber, nic, profileImageBytes, monthlyFee, status);
        try {
            if (insertedMemberId != null) {
                Log.d("Database", "User inserted successfully with ID: " + insertedMemberId);
                // Generate PDF with the actual inserted member ID
                generateUserPDF(insertedMemberId, firstName, lastName, email, phoneNumber, nic, monthlyFee, status);
                clearForm();

                Toast.makeText(this, "✓ Member registered successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("Database", "Failed to insert user into database");
                Toast.makeText(this, "Failed to add user to database", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("Database", "Exception adding user: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to add user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            // Create PDF file with proper naming
            String fileName = firstName + "_" + lastName + "_Registration.pdf";

            // Try multiple directory options to ensure PDF is saved
            File pdfFile = null;
            String savedPath = "";

            // Option 1: Try app-specific external storage first (most reliable)
            File appSpecificDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (appSpecificDir != null) {
                File kolonnawaDir = new File(appSpecificDir, "Kolonnawa Gym");
                if (!kolonnawaDir.exists()) {
                    kolonnawaDir.mkdirs();
                }
                pdfFile = new File(kolonnawaDir, fileName);
                savedPath = "App Documents/Kolonnawa Gym/";
            }

            // Option 2: If app-specific fails, try Downloads directory
            if (pdfFile == null) {
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File kolonnawaDir = new File(downloadsDir, "Kolonnawa Gym");
                if (!kolonnawaDir.exists()) {
                    kolonnawaDir.mkdirs();
                }
                pdfFile = new File(kolonnawaDir, fileName);
                savedPath = "Downloads/Kolonnawa Gym/";
            }

            // Option 3: Fallback to internal storage
            if (pdfFile == null) {
                File internalDir = getFilesDir();
                File kolonnawaDir = new File(internalDir, "Kolonnawa Gym");
                if (!kolonnawaDir.exists()) {
                    kolonnawaDir.mkdirs();
                }
                pdfFile = new File(kolonnawaDir, fileName);
                savedPath = "Internal Storage/Kolonnawa Gym/";
            }

            Log.d("PDF", "PDF will be saved to: " + pdfFile.getAbsolutePath());

            PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument, PageSize.A4);

            // Add header with logo and title
            addHeader(document, firstName, lastName);

            // Add member details section with profile image
            addMemberDetailsSection(document, memberId, firstName, lastName, email, phoneNumber, nic, monthlyFee, status);

            // Add footer
            addFooter(document);

            // Add logo watermark
            addLogoWatermark(document);

            document.close();

            Log.d("PDF", "PDF generated successfully: " + fileName);

            // Show success message with actual file location
            showPDFSuccessDialog(pdfFile, fileName, savedPath);

        } catch (Exception e) {
            Log.e("PDF", "Failed to generate PDF: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to generate PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showPDFSuccessDialog(File pdfFile, String fileName, String savedPath) {
        String message = "✓ Member registered successfully!\n" +
                "✓ PDF saved as: " + fileName + "\n" +
                "✓ Location: " + savedPath + fileName;

        new AlertDialog.Builder(this)
                .setTitle("Registration Successful!")
                .setMessage(message)
                .setPositiveButton("View PDF", (dialog, which) -> viewAndSharePDF(pdfFile))
                .setNegativeButton("OK", (dialog, which) -> {
                    Toast.makeText(this, "Registration completed! PDF saved to " + savedPath, Toast.LENGTH_LONG).show();
                })
                .show();
    }



    private void addHeader(Document document, String firstName, String lastName) {
        try {
            // Create a table for header with logo and text side by side
            Table headerTable = new Table(2);
            headerTable.setWidth(PageSize.A4.getWidth() - 72);
            headerTable.setHorizontalAlignment(HorizontalAlignment.CENTER);
            headerTable.setMarginTop(20);
            headerTable.setMarginBottom(20);

            // Add logo
            Bitmap logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mainlogo);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            logoBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] logoBytes = stream.toByteArray();

            ImageData imageData = ImageDataFactory.create(logoBytes);
            Image logo = new Image(imageData);
            logo.setWidth(80);
            logo.setHeight(80);
            logo.setHorizontalAlignment(HorizontalAlignment.LEFT);

            // Add gym name and member-specific title
            Paragraph gymName = new Paragraph("KOLONNAWA BARBELL GYM")
                    .setBold()
                    .setFontSize(16)
                    .setFontColor(new DeviceRgb(0, 51, 102))
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(3);

            Paragraph title = new Paragraph(firstName + " " + lastName + " - Registration")
                    .setBold()
                    .setFontSize(12)
                    .setFontColor(new DeviceRgb(0, 0, 0))
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

            // Add separator line
            Paragraph separator = new Paragraph("")
                    .setHeight(1)
                    .setBackgroundColor(new DeviceRgb(0, 51, 102))
                    .setMarginTop(5)
                    .setMarginBottom(15);
            document.add(separator);

        } catch (Exception e) {
            Log.e("PDF", "Error adding header: " + e.getMessage());
            e.printStackTrace();
            // Fallback header without logo
            Paragraph gymName = new Paragraph("KOLONNAWA BARBELL GYM")
                    .setBold()
                    .setFontSize(18)
                    .setFontColor(new DeviceRgb(0, 51, 102))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20)
                    .setMarginBottom(5);

            document.add(gymName);
        }
    }

    private void addMemberDetailsSection(Document document, String memberId, String firstName, String lastName,
                                         String email, String phoneNumber, String nic,
                                         String monthlyFee, int status) {

        // Main container
        Table containerTable = new Table(1);
        containerTable.setWidth(PageSize.A4.getWidth() - 72);
        containerTable.setHorizontalAlignment(HorizontalAlignment.CENTER);
        containerTable.setMarginBottom(20);

        // Section title
        Paragraph sectionTitle = new Paragraph("Member Registration Details")
                .setBold()
                .setFontSize(14)
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(new DeviceRgb(0, 51, 102))
                .setPadding(8);

        Cell titleCell = new Cell();
        titleCell.setBorder(Border.NO_BORDER);
        titleCell.add(sectionTitle);
        containerTable.addCell(titleCell);

        // Content table with details and image
        Table contentTable = new Table(2);
        contentTable.setWidth(PageSize.A4.getWidth() - 80);
        contentTable.setMarginTop(10);
        contentTable.setMarginBottom(10);

        // Left column - Member details
        Cell detailsCell = new Cell();
        detailsCell.setBorder(Border.NO_BORDER);
        detailsCell.setPadding(8);

        Table detailsTable = new Table(2);
        detailsTable.setWidth(300);

        Color labelColor = new DeviceRgb(240, 240, 240);
        Color valueColor = ColorConstants.WHITE;

        addStyledTableRow(detailsTable, "Member ID:", memberId, labelColor, valueColor);
        addStyledTableRow(detailsTable, "Name:", firstName + " " + lastName, labelColor, valueColor);
        addStyledTableRow(detailsTable, "Email:", email, labelColor, valueColor);
        addStyledTableRow(detailsTable, "Phone:", phoneNumber, labelColor, valueColor);
        addStyledTableRow(detailsTable, "NIC:", nic.isEmpty() ? "N/A" : nic, labelColor, valueColor);
        addStyledTableRow(detailsTable, "Advance Fee:", monthlyFee.isEmpty() ? "N/A" : "Rs. " + monthlyFee, labelColor, valueColor);

        String admissionStatus = status == 1 ? "Paid" : "Pending";
        addStyledTableRow(detailsTable, "Admission:", admissionStatus, labelColor, valueColor);

        addStyledTableRow(detailsTable, "Date:",
                new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()), labelColor, valueColor);

        detailsCell.add(detailsTable);

        // Right column - Profile image
        Cell imageCell = new Cell();
        imageCell.setBorder(Border.NO_BORDER);
        imageCell.setPadding(8);
        imageCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        imageCell.setHorizontalAlignment(HorizontalAlignment.CENTER);

        if (selectedImageBitmap != null) {
            try {
                Paragraph imageTitle = new Paragraph("Profile Photo")
                        .setBold()
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(3);
                imageCell.add(imageTitle);

                ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
                selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, imageStream);
                byte[] imageBytes = imageStream.toByteArray();

                ImageData profileImageData = ImageDataFactory.create(imageBytes);
                Image profileImage = new Image(profileImageData);
                profileImage.setWidth(80);
                profileImage.setHeight(100);
                profileImage.setHorizontalAlignment(HorizontalAlignment.CENTER);
                profileImage.setBorder(new SolidBorder(ColorConstants.BLACK, 1));

                imageCell.add(profileImage);
            } catch (Exception e) {
                Log.e("PDF", "Error adding profile image: " + e.getMessage());
                Paragraph noImage = new Paragraph("No Photo")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontSize(9)
                        .setFontColor(ColorConstants.GRAY);
                imageCell.add(noImage);
            }
        } else {
            Paragraph noImage = new Paragraph("No Photo Available")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(9)
                    .setFontColor(ColorConstants.GRAY);
            imageCell.add(noImage);
        }

        contentTable.addCell(detailsCell);
        contentTable.addCell(imageCell);

        Cell contentCell = new Cell();
        contentCell.setBorder(Border.NO_BORDER);
        contentCell.setPadding(5);
        contentCell.add(contentTable);
        containerTable.addCell(contentCell);

        document.add(containerTable);
    }

    private void addStyledTableRow(Table table, String label, String value, Color labelColor, Color valueColor) {
        Cell labelCell = new Cell();
        labelCell.add(new Paragraph(label).setBold().setFontSize(9));
        labelCell.setPadding(5);
        labelCell.setBackgroundColor(labelColor);
        labelCell.setBorder(new SolidBorder(ColorConstants.GRAY, 0.5f));

        Cell valueCell = new Cell();
        valueCell.add(new Paragraph(value).setFontSize(9));
        valueCell.setPadding(5);
        valueCell.setBackgroundColor(valueColor);
        valueCell.setBorder(new SolidBorder(ColorConstants.GRAY, 0.5f));

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addFooter(Document document) {
        Paragraph footer = new Paragraph("\nThis document confirms the registration of the above member with Kolonnawa Barbell Gym.\n")
                .setFontSize(8)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic();

        Paragraph signature = new Paragraph("Authorized Signature\n\nKolonnawa Barbell Gym Management")
                .setFontSize(8)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(15)
                .setMarginRight(30);

        document.add(footer);
        document.add(signature);
    }

    private void addLogoWatermark(Document document) {
        try {
            Bitmap logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mainlogo);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            logoBitmap.compress(Bitmap.CompressFormat.PNG, 20, stream);
            byte[] logoBytes = stream.toByteArray();

            ImageData logoImageData = ImageDataFactory.create(logoBytes);

            // Add subtle watermark
            for (int i = 0; i < 4; i++) {
                int x = 100 + (i % 2) * 200;
                int y = 200 + (i / 2) * 150;

                Image logoWatermark = new Image(logoImageData);
                logoWatermark.setWidth(60);
                logoWatermark.setHeight(60);
                logoWatermark.setOpacity(0.05f);
                logoWatermark.setFixedPosition(x, y);

                document.add(logoWatermark);
            }
        } catch (Exception e) {
            Log.e("PDF", "Error adding watermark: " + e.getMessage());
        }
    }

    private void showPDFSuccessDialog(File pdfFile, String fileName) {
        new AlertDialog.Builder(this)
                .setTitle("Registration Successful!")
                .setMessage("✓ Member registered successfully!\n✓ PDF saved as: " + fileName)
                .setPositiveButton("View PDF", (dialog, which) -> viewAndSharePDF(pdfFile))
                .setNegativeButton("OK", (dialog, which) -> {
                    Toast.makeText(this, "Registration completed!", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void viewAndSharePDF(File pdfFile) {
        try {
            Uri pdfUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".provider",
                    pdfFile);

            // Create intent to view PDF
            Intent viewIntent = new Intent(Intent.ACTION_VIEW);
            viewIntent.setDataAndType(pdfUri, "application/pdf");
            viewIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Create intent to share PDF
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Kolonnawa Barbell Gym - " + etFirstName.getText().toString() + " " + etLastName.getText().toString() + " Registration");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Member registration document from Kolonnawa Barbell Gym");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Create chooser with both options
            Intent chooserIntent = Intent.createChooser(viewIntent, "Open Member Registration");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { shareIntent });

            startActivity(chooserIntent);

        } catch (Exception e) {
            Log.e("PDF", "Error viewing/sharing PDF: " + e.getMessage());

            // Show where the PDF was saved
            String filePath = pdfFile.getAbsolutePath();
            new AlertDialog.Builder(this)
                    .setTitle("PDF Saved Successfully")
                    .setMessage("PDF was saved to:\n" + filePath + "\n\nYou can find it in your file manager.")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private void clearForm() {
        Log.d("Form", "Clearing form data");
        etFirstName.setText("");
        etLastName.setText("");
        etEmail.setText("");
        etPhoneNumber.setText("");
        etNIC.setText("");
        etMonthlyFee.setText("");
        statusRadioGroup.check(R.id.radioNo);
        deleteImage();
    }

    @Override
    protected void onDestroy() {
        Log.d("AddUser", "Activity destroyed");
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}