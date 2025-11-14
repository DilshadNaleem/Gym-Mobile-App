package com.example.kolonnawabarbellgym;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kolonnawabarbellgym.DTO.User;
import com.example.kolonnawabarbellgym.Database.DatabaseHelperClass;
import com.example.kolonnawabarbellgym.DatabaseController.ProfileController;


import java.io.IOException;

public class ProfileActivity extends BaseActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView profileImage;
    private TextView textEmail;
    private EditText editFirstName, editLastName, editPhoneNumber, editNIC;
    private Button btnUpdate, btnChangeImage;

    private DatabaseHelperClass dbHelper;
    private ProfileController profileController;
    private User currentUser;
    private String userEmail;
    private Bitmap selectedImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        currentNavItemId = R.id.navigation_profile;
        setupBottomNavigation(R.id.navigation_profile);

        // Initialize database
        dbHelper = new DatabaseHelperClass(this);
        profileController = new ProfileController(dbHelper);

        // Get user email from intent or shared preferences
        userEmail = getIntent().getStringExtra("remail");
        if (userEmail == null) {
            // Try to get from shared preferences or previous session
            // You might want to implement shared preferences for persistent login
            Toast.makeText(this, "User session not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadUserData();
        setupClickListeners();
    }

    private void initializeViews() {
        profileImage = findViewById(R.id.profile_image);
        textEmail = findViewById(R.id.text_email);
        editFirstName = findViewById(R.id.edit_first_name);
        editLastName = findViewById(R.id.edit_last_name);
        editPhoneNumber = findViewById(R.id.edit_phone_number);
        editNIC = findViewById(R.id.edit_nic);
        btnUpdate = findViewById(R.id.btn_update);
        btnChangeImage = findViewById(R.id.btn_change_image);
    }

    private void loadUserData() {
        currentUser = profileController.getUserByEmail(userEmail);

        if (currentUser != null) {
            textEmail.setText(currentUser.getEmail());
            editFirstName.setText(currentUser.getFirstName());
            editLastName.setText(currentUser.getLastName());
            editPhoneNumber.setText(currentUser.getPhoneNumber());
            editNIC.setText(currentUser.getNic());

            // Load profile image if exists
            if (currentUser.getProfileImagge() != null) {
                Bitmap profileBitmap = profileController.byteArrayToBitmap(currentUser.getProfileImagge());
                profileImage.setImageBitmap(profileBitmap);
            } else {
                // Set default profile image
                profileImage.setImageResource(R.drawable.ic_profile_default);
            }
        } else {
            Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        btnChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImage.setImageBitmap(selectedImageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateProfile() {
        String firstName = editFirstName.getText().toString().trim();
        String lastName = editLastName.getText().toString().trim();
        String phoneNumber = editPhoneNumber.getText().toString().trim();
        String nic = editNIC.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(firstName)) {
            editFirstName.setError("First name is required");
            return;
        }

        if (TextUtils.isEmpty(lastName)) {
            editLastName.setError("Last name is required");
            return;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            editPhoneNumber.setError("Phone number is required");
            return;
        }

        // Update user object
        currentUser.setFirstName(firstName);
        currentUser.setLastName(lastName);
        currentUser.setPhoneNumber(phoneNumber);
        currentUser.setNic(nic);

        // Convert selected image to byte array if exists
        if (selectedImageBitmap != null) {
            byte[] imageBytes = profileController.bitmapToByteArray(selectedImageBitmap);
            currentUser.setProfileImagge(imageBytes);
        }

        // Update in database
        boolean success = profileController.updateUserProfile(currentUser);

        if (success) {
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            // Reload data to reflect changes
            loadUserData();
        } else {
            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}