package com.example.kolonnawabarbellgym;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.kolonnawabarbellgym.Database.DatabaseHelperClass;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AddExpenseActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;

    private EditText etExpenseDescription, etExpenseAmount;
    private Button btnSaveExpense, btnSelectImage, btnTakePhoto;
    private ImageView ivExpenseImage;

    private DatabaseHelperClass databaseHelper;
    private byte[] expenseImageBytes;
    private String userEmail;
    private Bitmap selectedImageBitmap;

    // Launchers for gallery and camera
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        showImagePreview(selectedImageBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        selectedImageBitmap = (Bitmap) extras.get("data");
                        showImagePreview(selectedImageBitmap);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        initializeViews();
        setupClickListeners();

        // Get user email from intent
        userEmail = getIntent().getStringExtra("remail");

        // Initialize database helper
        databaseHelper = new DatabaseHelperClass(this);
    }

    private void initializeViews() {
        etExpenseDescription = findViewById(R.id.etExpenseDescription);
        etExpenseAmount = findViewById(R.id.etExpenseAmount);
        btnSaveExpense = findViewById(R.id.btnSaveExpense);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        ivExpenseImage = findViewById(R.id.ivExpenseImage);
    }

    private void setupClickListeners() {
        btnSaveExpense.setOnClickListener(v -> saveExpense());
        btnSelectImage.setOnClickListener(v -> openGallery());
        btnTakePhoto.setOnClickListener(v -> openCamera());

        // Clear image when clicked
        ivExpenseImage.setOnClickListener(v -> {
            if (expenseImageBytes != null) {
                expenseImageBytes = null;
                selectedImageBitmap = null;
                ivExpenseImage.setImageResource(android.R.drawable.ic_menu_gallery);
                Toast.makeText(this, "Image cleared", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openGallery() {
        if (checkStoragePermission()) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        } else {
            requestStoragePermission();
        }
    }

    private void openCamera() {
        if (checkCameraPermission()) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(intent);
        } else {
            requestCameraPermission();
        }
    }

    private void showImagePreview(Bitmap bitmap) {
        ivExpenseImage.setImageBitmap(bitmap);

        // Convert bitmap to byte array for database storage
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        expenseImageBytes = stream.toByteArray();

        Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show();
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Storage permission denied for gallery access", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private byte[] getBytesFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        inputStream.close();
        return byteBuffer.toByteArray();
    }

    private void saveExpense() {
        String description = etExpenseDescription.getText().toString().trim();
        String amountStr = etExpenseAmount.getText().toString().trim();

        // Validation
        if (description.isEmpty()) {
            etExpenseDescription.setError("Please enter expense description");
            etExpenseDescription.requestFocus();
            return;
        }

        if (amountStr.isEmpty()) {
            etExpenseAmount.setError("Please enter expense amount");
            etExpenseAmount.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                etExpenseAmount.setError("Amount must be greater than 0");
                etExpenseAmount.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etExpenseAmount.setError("Please enter a valid amount");
            etExpenseAmount.requestFocus();
            return;
        }

        // Save expense to database
        boolean isSuccess = databaseHelper.addExpense(description, amount, expenseImageBytes);

        if (isSuccess) {
            Toast.makeText(this, "Expense saved successfully", Toast.LENGTH_SHORT).show();

            // Clear form
            etExpenseDescription.setText("");
            etExpenseAmount.setText("");
            expenseImageBytes = null;
            selectedImageBitmap = null;
            ivExpenseImage.setImageResource(android.R.drawable.ic_menu_gallery);

            // Set result to refresh dashboard
            setResult(RESULT_OK);

            // Finish the activity to go back
            finish();

        } else {
            Toast.makeText(this, "Failed to save expense", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}