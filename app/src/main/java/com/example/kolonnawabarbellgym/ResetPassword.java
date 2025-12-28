package com.example.kolonnawabarbellgym;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.kolonnawabarbellgym.Database.DatabaseHelperClass;
import com.example.kolonnawabarbellgym.Mail.MailSender;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

public class ResetPassword extends AppCompatActivity {

    private static final String TAG = "ResetPassword";

    private TextInputEditText etResetPassword, etResetConfirmPassword;
    private MaterialButton btnResetPassword;
    private MaterialTextView txtBackToLogin;
    private LottieAnimationView resetPasswordAnimation;
    private ScaleAnimation buttonPulseAnimation;

    private DatabaseHelperClass dbHelper;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Activity starting");

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_password);
        Log.d(TAG, "onCreate: Layout inflated");

        initializeViews();
        setupDatabase();
        setupAnimations();
        setupClickListeners();
        logIntentExtras();

        Log.d(TAG, "onCreate: Activity setup completed");
    }

    @SuppressLint("WrongViewCast")
    private void initializeViews() {
        etResetPassword = findViewById(R.id.etResetPassword);
        etResetConfirmPassword = findViewById(R.id.etResetConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        txtBackToLogin = findViewById(R.id.txtBackToLogin);
        resetPasswordAnimation = findViewById(R.id.resetPasswordAnimation);
        Log.d(TAG, "initializeViews: Views initialized");
    }

    private void setupDatabase() {
        dbHelper = new DatabaseHelperClass(this);
        Log.d(TAG, "setupDatabase: DatabaseHelper initialized");
    }

    private void logIntentExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Log.d(TAG, "logIntentExtras: Intent has extras, keys: " + extras.keySet().toString());
            for (String key : extras.keySet()) {
                Log.d(TAG, "logIntentExtras: Extra - " + key + ": " + extras.get(key));
            }
        } else {
            Log.w(TAG, "logIntentExtras: No extras found in intent");
        }
    }

    private void setupClickListeners() {
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Reset password button clicked");
                animateButtonClick(view);
                handlePasswordReset();
            }
        });

        txtBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateTextClick(view);
                navigateToLogin();
            }
        });
    }

    // ANIMATION METHODS
    private void setupAnimations() {
        setupButtonPulseAnimation();
        setupLottieAnimation();
        setupFormEntranceAnimation();
    }

    private void setupButtonPulseAnimation() {
        buttonPulseAnimation = new ScaleAnimation(
                0.98f, 1.02f,
                0.98f, 1.02f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        buttonPulseAnimation.setDuration(1200);
        buttonPulseAnimation.setRepeatCount(Animation.INFINITE);
        buttonPulseAnimation.setRepeatMode(Animation.REVERSE);

        btnResetPassword.startAnimation(buttonPulseAnimation);
    }

    private void setupLottieAnimation() {
        if (resetPasswordAnimation != null) {
            resetPasswordAnimation.setRepeatCount(com.airbnb.lottie.LottieDrawable.INFINITE);
            resetPasswordAnimation.setSpeed(1.0f);
            resetPasswordAnimation.playAnimation();
        }
    }

    private void setupFormEntranceAnimation() {
        new Handler().postDelayed(() -> {
            animateViewEntrance(etResetPassword, 0);
            animateViewEntrance(etResetConfirmPassword, 100);
            animateViewEntrance(btnResetPassword, 200);
            animateViewEntrance(txtBackToLogin, 300);
        }, 300);
    }

    private void animateViewEntrance(View view, long delay) {
        view.setAlpha(0f);
        view.setTranslationY(50f);

        new Handler().postDelayed(() -> {
            view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .start();
        }, delay);
    }

    private void animateButtonClick(View view) {
        ScaleAnimation clickAnimation = new ScaleAnimation(
                1f, 0.95f, 1f, 0.95f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        clickAnimation.setDuration(100);
        clickAnimation.setFillAfter(true);

        view.startAnimation(clickAnimation);

        new Handler().postDelayed(() -> {
            ScaleAnimation resetAnimation = new ScaleAnimation(
                    0.95f, 1f, 0.95f, 1f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );
            resetAnimation.setDuration(100);
            resetAnimation.setFillAfter(true);
            view.startAnimation(resetAnimation);
        }, 100);
    }

    private void animateTextClick(View view) {
        AlphaAnimation fadeAnimation = new AlphaAnimation(1f, 0.5f);
        fadeAnimation.setDuration(150);
        fadeAnimation.setFillAfter(true);

        view.startAnimation(fadeAnimation);

        new Handler().postDelayed(() -> {
            AlphaAnimation resetAnimation = new AlphaAnimation(0.5f, 1f);
            resetAnimation.setDuration(150);
            resetAnimation.setFillAfter(true);
            view.startAnimation(resetAnimation);
        }, 150);
    }

    private void playSuccessAnimation() {
        if (resetPasswordAnimation != null) {
            resetPasswordAnimation.setRepeatCount(0);
            resetPasswordAnimation.playAnimation();
        }
    }

    private void playErrorAnimation() {
        TranslateAnimation shake = new TranslateAnimation(0, 10, 0, 0);
        shake.setDuration(50);
        shake.setRepeatCount(3);
        shake.setRepeatMode(Animation.REVERSE);
        etResetPassword.startAnimation(shake);
        etResetConfirmPassword.startAnimation(shake);
    }

    // PASSWORD RESET LOGIC
    private void handlePasswordReset() {
        Log.d(TAG, "handlePasswordReset: Method started");

        String password = etResetPassword.getText().toString().trim();
        String confirmPassword = etResetConfirmPassword.getText().toString().trim();

        Log.d(TAG, "handlePasswordReset: Password length: " + password.length());
        Log.d(TAG, "handlePasswordReset: Confirm password length: " + confirmPassword.length());

        // Password validation with logs
        if (password.isEmpty()) {
            Log.w(TAG, "handlePasswordReset: Password field is empty");
            etResetPassword.setError("Please enter new Password");
            etResetPassword.requestFocus();
            playErrorAnimation();
            return;
        }

        if (password.length() < 6) {
            Log.w(TAG, "handlePasswordReset: Password too short - " + password.length() + " characters");
            etResetPassword.setError("Password should be at least 6 characters");
            etResetPassword.requestFocus();
            playErrorAnimation();
            return;
        }

        if (confirmPassword.isEmpty()) {
            Log.w(TAG, "handlePasswordReset: Confirm password field is empty");
            etResetConfirmPassword.setError("Please confirm your password");
            etResetConfirmPassword.requestFocus();
            playErrorAnimation();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Log.w(TAG, "handlePasswordReset: Passwords don't match");
            Log.d(TAG, "handlePasswordReset: Password: " + password);
            Log.d(TAG, "handlePasswordReset: Confirm: " + confirmPassword);
            etResetConfirmPassword.setError("Passwords do not match");
            etResetConfirmPassword.requestFocus();
            playErrorAnimation();
            return;
        }

        Log.d(TAG, "handlePasswordReset: All validations passed");

        // Get email from intent
        userEmail = getIntent().getStringExtra("ForgotEmail");
        Log.d(TAG, "handlePasswordReset: Retrieved userEmail from intent: " + userEmail);

        if (userEmail == null || userEmail.isEmpty()) {
            Log.e(TAG, "handlePasswordReset: userEmail is null or empty!");
            Toast.makeText(ResetPassword.this, "Error: Email not found", Toast.LENGTH_SHORT).show();
            playErrorAnimation();
            return;
        }



        if (updatePassword(userEmail, password)) {
            Log.i(TAG, "handlePasswordReset: Password updated successfully for: " + userEmail);

            // Play success animation
            playSuccessAnimation();

            // Send notification email to user
            sendPasswordChangeNotification(userEmail);

            Toast.makeText(ResetPassword.this, "Password Reset Successfully", Toast.LENGTH_SHORT).show();

            // Navigate to login after success animation
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(ResetPassword.this, LoginActivity.class);
                Log.d(TAG, "handlePasswordReset: Starting LoginActivity");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                Log.d(TAG, "handlePasswordReset: Finishing ResetPassword activity");
                finish();
            }, 1500);
        } else {
            Log.e(TAG, "handlePasswordReset: Failed to update password for: " + userEmail);
            Toast.makeText(ResetPassword.this, "Failed to reset Password. Please Try Again", Toast.LENGTH_SHORT).show();
            playErrorAnimation();
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

    private void sendPasswordChangeNotification(String userEmail) {
        Log.d(TAG, "sendPasswordChangeNotification: Sending password change notification to: " + userEmail);

        try {
            String subject = "Password Changed Successfully - Kolonnawa Barbell Gym";
            String message = "Dear User,\n\n" +
                    "Your password has been successfully changed for your Kolonnawa Barbell Gym account.\n\n" +
                    "If you did not initiate this password change, please contact our administrator immediately at:\n" +
                    "Phone: 0725958832\n\n" +
                    "Thank you for choosing Kolonnawa Barbell Gym!\n\n" +
                    "Best regards,\n" +
                    "Kolonnawa Barbell Gym Team";

            // Create and start the email thread
            MailSender mailSender = new MailSender(userEmail, subject, message);
            mailSender.start();

            Log.i(TAG, "sendPasswordChangeNotification: Password change notification sent successfully to: " + userEmail);

        } catch (Exception e) {
            Log.e(TAG, "sendPasswordChangeNotification: Failed to send notification email: " + e.getMessage(), e);
            // Don't show error to user as password was still reset successfully
            // Just log the error for debugging
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(ResetPassword.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: Activity destroying");
        super.onDestroy();
        if (btnResetPassword != null) {
            btnResetPassword.clearAnimation();
        }
        if (resetPasswordAnimation != null) {
            resetPasswordAnimation.cancelAnimation();
        }
        if (dbHelper != null) {
            dbHelper.close();
            Log.d(TAG, "onDestroy: Database helper closed");
        }
        Log.d(TAG, "onDestroy: Activity destroyed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Activity paused");
        if (resetPasswordAnimation != null) {
            resetPasswordAnimation.pauseAnimation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Activity resumed");
        if (resetPasswordAnimation != null) {
            resetPasswordAnimation.resumeAnimation();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Activity started");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: Activity stopped");
    }
}