package com.example.kolonnawabarbellgym;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.kolonnawabarbellgym.Database.DatabaseHelperClass;

public class ResetPassword extends AppCompatActivity {

    private static final String TAG = "ResetPassword"; // Consistent log tag

    private EditText etPassword, etConfirmPassword;
    private Button btnResetPassword;
    private DatabaseHelperClass dbHelper;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Activity starting");

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_password);
        Log.d(TAG, "onCreate: Layout inflated");

        // Initialize views
        etPassword = findViewById(R.id.etResetPassword);
        etConfirmPassword = findViewById(R.id.etResetConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        Log.d(TAG, "onCreate: Views initialized");

        dbHelper = new DatabaseHelperClass(this);
        Log.d(TAG, "onCreate: DatabaseHelper initialized");

        // Log intent extras for debugging
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Log.d(TAG, "onCreate: Intent has extras, keys: " + extras.keySet().toString());
            for (String key : extras.keySet()) {
                Log.d(TAG, "onCreate: Extra - " + key + ": " + extras.get(key));
            }
        } else {
            Log.w(TAG, "onCreate: No extras found in intent");
        }

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Reset password button clicked");
                resetPassword();
            }
        });

        Log.d(TAG, "onCreate: Activity setup completed");
    }

    private void resetPassword() {
        Log.d(TAG, "resetPassword: Method started");

        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim(); // Fixed the .toString().toString()

        Log.d(TAG, "resetPassword: Password length: " + password.length());
        Log.d(TAG, "resetPassword: Confirm password length: " + confirmPassword.length());

        // Password validation with logs
        if (password.isEmpty()) {
            Log.w(TAG, "resetPassword: Password field is empty");
            etPassword.setError("Please enter new Password");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            Log.w(TAG, "resetPassword: Password too short - " + password.length() + " characters");
            etPassword.setError("Password should be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            Log.w(TAG, "resetPassword: Confirm password field is empty");
            etConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Log.w(TAG, "resetPassword: Passwords don't match");
            Log.d(TAG, "resetPassword: Password: " + password);
            Log.d(TAG, "resetPassword: Confirm: " + confirmPassword);
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        Log.d(TAG, "resetPassword: All validations passed");

        // Get email from intent
        userEmail = getIntent().getStringExtra("ForgotEmail");
        Log.d(TAG, "resetPassword: Retrieved userEmail from intent: " + userEmail);

        if (userEmail == null || userEmail.isEmpty()) {
            Log.e(TAG, "resetPassword: userEmail is null or empty!");
            Toast.makeText(ResetPassword.this, "Error: Email not found", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "resetPassword: Attempting to update password for: " + userEmail);

        if (updatePassword(userEmail, password)) {
            Log.i(TAG, "resetPassword: Password updated successfully for: " + userEmail);
            Toast.makeText(ResetPassword.this, "Password Reset Successfully", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(ResetPassword.this, LoginActivity.class);
            Log.d(TAG, "resetPassword: Starting LoginActivity");

            startActivity(intent);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

            Log.d(TAG, "resetPassword: Finishing ResetPassword activity");
            finish();
        } else {
            Log.e(TAG, "resetPassword: Failed to update password for: " + userEmail);
            Toast.makeText(ResetPassword.this, "Failed to reset Password. Please Try Again", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean updatePassword(String email, String newPassword) {
        Log.d(TAG, "updatePassword: Starting database update");
        Log.d(TAG, "updatePassword: Email: " + email);
        Log.d(TAG, "updatePassword: New password length: " + newPassword.length());

        SQLiteDatabase db = dbHelper.openDB();
        Log.d(TAG, "updatePassword: Database opened");

        try {
            String sql = "UPDATE users SET password = ? WHERE email = ?";
            Log.d(TAG, "updatePassword: Executing SQL: " + sql);
            Log.d(TAG, "updatePassword: Parameters - password: [HIDDEN], email: " + email);

            db.execSQL(sql, new Object[]{newPassword, email});
            Log.i(TAG, "updatePassword: SQL executed successfully");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "updatePassword: Database error: " + e.getMessage(), e);
            e.printStackTrace();
            return false;
        } finally {
            db.close();
            Log.d(TAG, "updatePassword: Database closed");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Activity started");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Activity resumed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Activity paused");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: Activity stopped");
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: Activity destroying");
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
            Log.d(TAG, "onDestroy: Database helper closed");
        }
        Log.d(TAG, "onDestroy: Activity destroyed");
    }
}