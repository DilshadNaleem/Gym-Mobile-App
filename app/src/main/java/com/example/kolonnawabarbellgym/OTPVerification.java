package com.example.kolonnawabarbellgym;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.kolonnawabarbellgym.Database.DatabaseHelperClass;
import com.example.kolonnawabarbellgym.DatabaseController.RegisterController;

public class OTPVerification extends AppCompatActivity {
    private String OTP, email;
    private String TAG = "OTPVerification Activity";
    private DatabaseHelperClass dbHelper;
    private RegisterController registerController;

    EditText verifyOtp;
    Button btnVerify, btnResendOTP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_otpverification);

        // Initialize database helper and controller
        dbHelper = new DatabaseHelperClass(this);
        registerController = new RegisterController(dbHelper);

        verifyOtp = findViewById(R.id.etVerifyOtp);
        btnVerify = findViewById(R.id.btnVerifyOtp);
        btnResendOTP = findViewById(R.id.btnResendOtp);

        Intent intent = getIntent();
        if (intent != null) {
            OTP = intent.getStringExtra("otp");
            email = intent.getStringExtra("email");
            Log.d(TAG, "OTP and Email: " + OTP + " " + email);

            if (OTP == null || email == null) {
                Toast.makeText(this, "OTP or email is null", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "OTP or Email is null");
            }
        }

        // Set up verify button click listener
        btnVerify.setOnClickListener(v -> verifyOTP());

        // Set up resend OTP button click listener (you can implement this later)
        btnResendOTP.setOnClickListener(v -> resendOTP());
    }

    private void verifyOTP() {
        String enteredOTP = verifyOtp.getText().toString().trim();

        if (enteredOTP.isEmpty()) {
            Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        if (OTP == null || email == null) {
            Toast.makeText(this, "OTP verification failed. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (enteredOTP.equals(OTP)) {
            // OTP verification successful - update user status and loggedIn
            boolean updateSuccess = updateUserStatus();

            if (updateSuccess) {
                Toast.makeText(this, "Verification Successful!", Toast.LENGTH_SHORT).show();

                // Navigate to main activity or login activity
                Intent intent = new Intent(OTPVerification.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Verification failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean updateUserStatus() {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("status", 1); // Update status to 1 (active/verified)
            values.put("loggedIn", "LoggedIn"); // Update loggedIn status

            // Update the user record with the matching email
            int rowsAffected = db.update(
                    "users",
                    values,
                    "email = ?",
                    new String[]{email}
            );

            db.close();

            Log.d(TAG, "Rows affected: " + rowsAffected);
            return rowsAffected > 0;

        } catch (Exception e) {
            Log.e(TAG, "Error updating user status: " + e.toString());
            return false;
        }
    }

    private void resendOTP() {
        // Implement OTP resend logic here
        Toast.makeText(this, "OTP Resent!", Toast.LENGTH_SHORT).show();
        // You can regenerate OTP and send it again via email/SMS
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}