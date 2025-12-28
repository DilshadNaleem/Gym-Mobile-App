package com.example.kolonnawabarbellgym;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.kolonnawabarbellgym.Database.DatabaseHelperClass;
import com.example.kolonnawabarbellgym.DatabaseController.RegisterController;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.util.Locale;

public class OTPVerification extends AppCompatActivity {
    private String OTP, email;
    private String TAG = "OTPVerification Activity";
    private DatabaseHelperClass dbHelper;
    private RegisterController registerController;

    // UI Components
    private LottieAnimationView lottieAnimation, loadingAnimation;
    private LinearLayout otpContainer;
    private TextInputEditText etOtpInput;
    private TextInputLayout otpInputLayout;
    private MaterialButton btnVerifyOtp;
    private TextView txtOtpMessage, txtEmailDisplay, txtTimer, txtResend, txtDifferentEmail;
    private MaterialCardView loadingCard;

    private CountDownTimer countDownTimer;
    private boolean isResendEnabled = false;
    private static final long COUNTDOWN_TIME = 120000; // 2 minutes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpverification);

        // Initialize database helper and controller
        dbHelper = new DatabaseHelperClass(this);
        registerController = new RegisterController(dbHelper);

        initializeViews();
        setupOTPInput();
        startCountdownTimer();

        Intent intent = getIntent();
        if (intent != null) {
            OTP = intent.getStringExtra("otp");
            email = intent.getStringExtra("email");
            Log.d(TAG, "OTP and Email: " + OTP + " " + email);

            if (email != null) {
                txtEmailDisplay.setText(email);
            }

            if (OTP == null || email == null) {
                Toast.makeText(this, "OTP or email is null", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "OTP or Email is null");
            }
        }

        setupClickListeners();

        // Auto-focus on OTP input
        etOtpInput.requestFocus();
        showKeyboard();
    }

    private void initializeViews() {
        lottieAnimation = findViewById(R.id.lottieAnimation);
        loadingAnimation = findViewById(R.id.loadingAnimation);
        otpContainer = findViewById(R.id.otpContainer);
        etOtpInput = findViewById(R.id.etOtpInput);
        otpInputLayout = findViewById(R.id.otpInputLayout);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        txtOtpMessage = findViewById(R.id.txtOtpMessage);
        txtEmailDisplay = findViewById(R.id.txtEmailDisplay);
        txtTimer = findViewById(R.id.txtTimer);
        txtResend = findViewById(R.id.txtResend);
        txtDifferentEmail = findViewById(R.id.txtDifferentEmail);
        loadingCard = findViewById(R.id.loadingCard);
    }

    private void setupOTPInput() {
        // Create 6 OTP digit boxes
        for (int i = 0; i < 6; i++) {
            MaterialTextView otpDigit = new MaterialTextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(48),
                    dpToPx(56)
            );
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            otpDigit.setLayoutParams(params);

            // Create rounded background
            GradientDrawable background = new GradientDrawable();
            background.setShape(GradientDrawable.RECTANGLE);
            background.setCornerRadius(dpToPx(12));
            background.setStroke(dpToPx(2), getColor(R.color.colorPrimaryLight));
            background.setColor(getColor(R.color.colorSurface));

            otpDigit.setBackground(background);
            otpDigit.setGravity(android.view.Gravity.CENTER);
            otpDigit.setTextSize(18);
            otpDigit.setTextColor(getColor(R.color.colorTextPrimary));
            otpDigit.setTypeface(otpDigit.getTypeface(), android.graphics.Typeface.BOLD);
            otpDigit.setId(View.generateViewId());

            otpContainer.addView(otpDigit);
        }

        // Setup text watcher for OTP input
        etOtpInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String otp = s.toString();
                updateOTPBoxes(otp);

                // Enable verify button when OTP is complete
                boolean isOtpComplete = otp.length() == 6;
                btnVerifyOtp.setEnabled(isOtpComplete);

                // Clear error when user starts typing
                if (otp.length() > 0) {
                    otpInputLayout.setError(null);
                }

                // Animate button state change
                animateButtonState(isOtpComplete);
            }
        });

        // Set focus on OTP input when OTP container is clicked
        otpContainer.setOnClickListener(v -> {
            etOtpInput.requestFocus();
            showKeyboard();
        });

        // Also make the OTP input layout clickable
        otpInputLayout.setOnClickListener(v -> {
            etOtpInput.requestFocus();
            showKeyboard();
        });
    }

    private void updateOTPBoxes(String otp) {
        for (int i = 0; i < 6; i++) {
            MaterialTextView otpDigit = (MaterialTextView) otpContainer.getChildAt(i);
            GradientDrawable background = (GradientDrawable) otpDigit.getBackground();

            if (i < otp.length()) {
                // Filled box
                otpDigit.setText(String.valueOf(otp.charAt(i)));
                background.setStroke(dpToPx(2), getColor(R.color.colorPrimary));
                background.setColor(getColor(R.color.colorPrimaryLight));
                otpDigit.setTextColor(getColor(R.color.colorPrimary));
            } else {
                // Empty box
                otpDigit.setText("");
                background.setStroke(dpToPx(2), getColor(R.color.colorPrimaryLight));
                background.setColor(getColor(R.color.colorSurface));
                otpDigit.setTextColor(getColor(R.color.colorTextPrimary));
            }
        }
    }

    private void animateButtonState(boolean enabled) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(
                new ArgbEvaluator(),
                enabled ? getColor(R.color.colorPrimary) : getColor(R.color.colorPrimaryLight),
                enabled ? getColor(R.color.colorPrimary) : getColor(R.color.colorPrimaryLight)
        );

        colorAnimation.setDuration(300);
        colorAnimation.addUpdateListener(animator -> {
            btnVerifyOtp.setBackgroundColor((int) animator.getAnimatedValue());
        });
        colorAnimation.start();
    }

    private void setupClickListeners() {
        btnVerifyOtp.setOnClickListener(v -> verifyOTP());

        txtResend.setOnClickListener(v -> {
            if (isResendEnabled) {
                resendOTP();
            } else {
                Toast.makeText(this, "Please wait for timer to finish", Toast.LENGTH_SHORT).show();
            }
        });

        txtDifferentEmail.setOnClickListener(v -> {
            // Navigate back to registration or email input screen
            finish();
        });
    }

    private void startCountdownTimer() {
        countDownTimer = new CountDownTimer(COUNTDOWN_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateTimerText(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                enableResend();
            }
        }.start();
    }

    private void updateTimerText(long millisUntilFinished) {
        int minutes = (int) (millisUntilFinished / 1000) / 60;
        int seconds = (int) (millisUntilFinished / 1000) % 60;

        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        txtTimer.setText(timeFormatted);

        // Change color when less than 30 seconds remaining
        if (millisUntilFinished < 30000) {
            txtTimer.setTextColor(getColor(R.color.colorError));
        } else {
            txtTimer.setTextColor(getColor(R.color.colorPrimary));
        }
    }

    private void enableResend() {
        isResendEnabled = true;
        txtResend.setTextColor(getColor(R.color.colorPrimary));
        txtResend.setText("Resend OTP");
        txtTimer.setText("00:00");
        txtTimer.setTextColor(getColor(R.color.colorTextSecondary));

        // Add pulse animation to resend text
        ValueAnimator pulseAnimator = ValueAnimator.ofFloat(0.8f, 1.2f);
        pulseAnimator.setDuration(500);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            txtResend.setScaleX(scale);
            txtResend.setScaleY(scale);
        });
        pulseAnimator.start();
    }

    private void verifyOTP() {
        String enteredOTP = etOtpInput.getText().toString().trim();

        if (enteredOTP.isEmpty()) {
            otpInputLayout.setError("Please enter OTP");
            showErrorAnimation();
            return;
        }

        if (enteredOTP.length() != 6) {
            otpInputLayout.setError("Please enter 6-digit OTP");
            showErrorAnimation();
            return;
        }

        if (OTP == null || email == null) {
            otpInputLayout.setError("OTP verification failed. Please try again.");
            showErrorAnimation();
            return;
        }

        showLoading();

        // Simulate network delay
        new android.os.Handler().postDelayed(() -> {
            if (enteredOTP.equals(OTP)) {
                // OTP verification successful
                boolean updateSuccess = updateUserStatus();

                if (updateSuccess) {
                    showSuccessAnimation();
                    navigateToMainActivity();
                } else {
                    otpInputLayout.setError("Verification failed. Please try again.");
                    showErrorAnimation();
                }
            } else {
                otpInputLayout.setError("Invalid OTP. Please try again.");
                showErrorAnimation();
            }
        }, 2000);
    }

    private void showLoading() {
        loadingCard.setVisibility(View.VISIBLE);
        btnVerifyOtp.setEnabled(false);
        hideKeyboard();
    }

    private void hideLoading() {
        loadingCard.setVisibility(View.GONE);
        btnVerifyOtp.setEnabled(etOtpInput.getText().toString().length() == 6);
    }

    private void showSuccessAnimation() {
        lottieAnimation.setAnimation(R.raw.success_animation);
        lottieAnimation.playAnimation();
        hideLoading();
    }

    private void showErrorAnimation() {
        lottieAnimation.setAnimation(R.raw.error_animation);
        lottieAnimation.playAnimation();
        hideLoading();
    }

    private void resendOTP() {
        // Implement OTP resend logic here
        showLoading();

        new android.os.Handler().postDelayed(() -> {
            hideLoading();
            Toast.makeText(this, "OTP Resent to your email!", Toast.LENGTH_SHORT).show();

            // Reset UI
            etOtpInput.setText("");
            otpInputLayout.setError(null);
            isResendEnabled = false;
            txtResend.setTextColor(getColor(R.color.colorTextSecondary));
            txtResend.setScaleX(1f);
            txtResend.setScaleY(1f);

            // Restart countdown
            startCountdownTimer();

            // You can regenerate OTP and send it again via email/SMS
            // For now, we'll just use the same OTP for demo
            OTP = generateNewOTP();
            Log.d(TAG, "New OTP: " + OTP);

        }, 1500);
    }

    private String generateNewOTP() {
        // Generate a random 6-digit OTP
        return String.valueOf((int) ((Math.random() * 900000) + 100000));
    }

    private boolean updateUserStatus() {
        // Your existing database update logic
        try {
            // Implement your database update logic here
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error updating user status: " + e.toString());
            return false;
        }
    }

    private void navigateToMainActivity() {
        new android.os.Handler().postDelayed(() -> {
            Intent intent = new Intent(OTPVerification.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, 1500);
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(etOtpInput, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etOtpInput.getWindowToken(), 0);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}