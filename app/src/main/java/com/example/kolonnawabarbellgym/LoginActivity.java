package com.example.kolonnawabarbellgym;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.kolonnawabarbellgym.Database.DatabaseHelperClass;
import com.example.kolonnawabarbellgym.DatabaseController.LoginController;
import com.example.kolonnawabarbellgym.Mail.MailSender;
import com.example.kolonnawabarbellgym.Mail.OtpGenerator;

public class LoginActivity extends AppCompatActivity {

    private TextView textRegister, textResetPassword;
    private EditText emailLogin, loginPassword;
    private CheckBox rememberMeCheckbox;
    private Button btnLogin;
    private DatabaseHelperClass dbHelper;
    private LoginController loginController;
    private static final String TAG = "LoginActivity";

    // SharedPreferences keys
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER_TIME = "remember_time";

    // 60 days in milliseconds
    private static final long REMEMBER_DAYS = 60L * 24L * 60L * 60L * 1000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelperClass(this);
        loginController = new LoginController(dbHelper);

        emailLogin = findViewById(R.id.etEmailLogin);
        loginPassword = findViewById(R.id.etLoginPassword);
        rememberMeCheckbox = findViewById(R.id.cbRememberMe);
        btnLogin = findViewById(R.id.btnLogin);
        textRegister = findViewById(R.id.txtSignup);
        textResetPassword = findViewById(R.id.txtForgotPassword);

        // Check if we should auto-login
        if (shouldAutoLogin()) {
            autoLogin();
        } else {
            // Load saved email only (not password for security)
            loadSavedCredentials();
        }

        textRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, Register.class);
                startActivity(intent);
            }
        });

        textResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ForgotPassword.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performLogin();
            }
        });

        setupAnimations();
    }

    private void setupAnimations() {
        // Scale animation for login button
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                0.95f, 1.0f, 0.95f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(2000);
        scaleAnimation.setRepeatCount(Animation.INFINITE);
        scaleAnimation.setRepeatMode(Animation.REVERSE);

        findViewById(R.id.btnLogin).startAnimation(scaleAnimation);
    }

    private void performLogin() {
        String email = emailLogin.getText().toString().trim();
        String password = loginPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(LoginActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean loginSuccess = loginController.loginUser(email, password);

        if (loginSuccess) {
            // Save credentials if Remember Me is checked
            if (rememberMeCheckbox.isChecked()) {
                saveCredentials(email, password);
            } else {
                clearCredentials();
            }

            Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

            // Navigate to main activity or home screen
            Intent intent = new Intent(LoginActivity.this, MainDashboard.class);
            intent.putExtra("remail", email);
            Log.d(TAG, "Login Using : " + email);
            startActivity(intent);
            finish(); // Close login activity
        } else {
            Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}";
        return email.matches(emailPattern);
    }

    private void saveCredentials(String email, String password) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(KEY_REMEMBER_ME, true);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.putLong(KEY_REMEMBER_TIME, System.currentTimeMillis());

        editor.apply();
    }

    private void loadSavedCredentials() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (preferences.getBoolean(KEY_REMEMBER_ME, false)) {
            String savedEmail = preferences.getString(KEY_EMAIL, "");
            emailLogin.setText(savedEmail);
            // Don't auto-fill password for security
            rememberMeCheckbox.setChecked(true);
        }
    }

    private void clearCredentials() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.clear();
        editor.apply();
    }

    private boolean shouldAutoLogin() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        boolean rememberMe = preferences.getBoolean(KEY_REMEMBER_ME, false);
        if (!rememberMe) {
            return false;
        }

        long savedTime = preferences.getLong(KEY_REMEMBER_TIME, 0);
        long currentTime = System.currentTimeMillis();

        // Check if 60 days have passed
        if ((currentTime - savedTime) > REMEMBER_DAYS) {
            clearCredentials();
            return false;
        }

        return true;
    }

    private void autoLogin() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String email = preferences.getString(KEY_EMAIL, "");
        String password = preferences.getString(KEY_PASSWORD, "");

        if (email.isEmpty() || password.isEmpty()) {
            return;
        }

        boolean loginSuccess = loginController.loginUser(email, password);

        if (loginSuccess) {
            Toast.makeText(LoginActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(LoginActivity.this, MainDashboard.class);
            intent.putExtra("remail", email);
            Log.d(TAG, "Auto Login Using : " + email);
            startActivity(intent);
            finish();
        } else {
            // If auto-login fails, clear credentials
            clearCredentials();
            emailLogin.setText(email);
            Toast.makeText(LoginActivity.this, "Auto-login failed. Please login manually.", Toast.LENGTH_SHORT).show();
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