package com.example.kolonnawabarbellgym;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AlphaAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.kolonnawabarbellgym.Database.DatabaseHelperClass;
import com.example.kolonnawabarbellgym.Mail.MailSender;

public class ForgotPassword extends AppCompatActivity {

    private TextView txtForgotPasswordNotice;
    private Button btnForgotPasswordButton;
    private EditText etForgotPasswordEmail;
    private DatabaseHelperClass databaseHelper;
    private LottieAnimationView forgotPasswordAnimation;

    // Animation variables
    private ScaleAnimation buttonPulseAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        databaseHelper = new DatabaseHelperClass(this);

        etForgotPasswordEmail = findViewById(R.id.txtForgotPasswordEmail);
        txtForgotPasswordNotice = findViewById(R.id.txtForgotPasswordNotice);
        btnForgotPasswordButton = findViewById(R.id.btnForgotPassword);

        // Initialize Lottie Animation
        forgotPasswordAnimation = findViewById(R.id.forgotPasswordAnimation);

        // Setup all animations
        setupAnimations();

        btnForgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Add button click animation
                animateButtonClick(view);
                handleForgotPassword();
            }
        });

        txtForgotPasswordNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Add text click animation
                animateTextClick(view);
                Intent intent = new Intent(ForgotPassword.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    // STYLING AND ANIMATION METHODS ONLY
    private void setupAnimations() {
        setupButtonPulseAnimation();
        setupLottieAnimation();
        setupFormEntranceAnimation();
    }

    private void setupButtonPulseAnimation() {
        // Create pulsing animation for reset button
        buttonPulseAnimation = new ScaleAnimation(
                0.98f, 1.02f,
                0.98f, 1.02f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        buttonPulseAnimation.setDuration(1200);
        buttonPulseAnimation.setRepeatCount(Animation.INFINITE);
        buttonPulseAnimation.setRepeatMode(Animation.REVERSE);

        btnForgotPasswordButton.startAnimation(buttonPulseAnimation);
    }

    private void setupLottieAnimation() {
        if (forgotPasswordAnimation != null) {
            // Configure Lottie animation
            forgotPasswordAnimation.setRepeatCount(com.airbnb.lottie.LottieDrawable.INFINITE);
            forgotPasswordAnimation.setSpeed(1.2f);
            forgotPasswordAnimation.playAnimation();
        }
    }

    private void setupFormEntranceAnimation() {
        // Staggered entrance animation for form elements
        new Handler().postDelayed(() -> {
            animateViewEntrance(etForgotPasswordEmail, 0);
            animateViewEntrance(btnForgotPasswordButton, 100);
            animateViewEntrance(txtForgotPasswordNotice, 200);
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

        // Reset animation after click
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
        if (forgotPasswordAnimation != null) {
            // Play success animation once
            forgotPasswordAnimation.setRepeatCount(0);
            forgotPasswordAnimation.playAnimation();
        }
    }

    private void playErrorAnimation() {
        // Shake animation for error state
        TranslateAnimation shake = new TranslateAnimation(0, 10, 0, 0);
        shake.setDuration(50);
        shake.setRepeatCount(3);
        shake.setRepeatMode(Animation.REVERSE);
        etForgotPasswordEmail.startAnimation(shake);
    }

    // YOUR EXISTING BUSINESS LOGIC METHODS (UNCHANGED)
    private void handleForgotPassword() {
        String email = etForgotPasswordEmail.getText().toString().trim();

        if (email.isEmpty()) {
            etForgotPasswordEmail.setError("Please Enter your Email");
            etForgotPasswordEmail.requestFocus();
            playErrorAnimation();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etForgotPasswordEmail.setError("Please enter a Valid email");
            etForgotPasswordEmail.requestFocus();
            playErrorAnimation();
            return;
        }

        if (isValidUser(email)) {
            sendResetPasswordEmail(email);

            // Play success animation when email is sent
            playSuccessAnimation();

            Intent intent = new Intent(ForgotPassword.this, ResetPassword.class);
            intent.putExtra("ForgotEmail", email);

            startActivity(intent);

            Toast.makeText(ForgotPassword.this,"Reset Password Email sent Successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ForgotPassword.this, "Email not found or account not verified", Toast.LENGTH_SHORT).show();
            playErrorAnimation();
        }
    }

    private boolean isValidUser(String email) {
        SQLiteDatabase db = databaseHelper.openDB();
        String[] columns = {"userid"};
        String selection = "email = ? AND status = 1 AND loggedIn = ?";
        String[] selectionArgs = {email, "1"};

        Cursor cursor = db.query("users", columns, selection, selectionArgs, null, null, null);

        boolean exists = cursor.getCount() > 0;

        cursor.close();
        db.close();

        return exists;
    }

    private void sendResetPasswordEmail(String email) {
        String subject = "Password Reset Request - Kolonnawa Barbell Gym";
        String messageBody = "Dear Member,\n\n" +
                "You have requested to reset your password for Kolonnawa Barbell Gym.\n\n" +
                "If you did not request this, please take care of this email.\n\n" +
                "Best regards,\n" +
                "Kolonnawa Barbell Gym Team";

        MailSender mailSender = new MailSender(email, subject, messageBody);
        mailSender.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up animations
        if (btnForgotPasswordButton != null) {
            btnForgotPasswordButton.clearAnimation();
        }
        if (forgotPasswordAnimation != null) {
            forgotPasswordAnimation.cancelAnimation();
        }
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause animation when activity is not visible
        if (forgotPasswordAnimation != null) {
            forgotPasswordAnimation.pauseAnimation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume animation when activity becomes visible again
        if (forgotPasswordAnimation != null) {
            forgotPasswordAnimation.resumeAnimation();
        }
    }
}