package com.example.kolonnawabarbellgym;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.logging.Logger;

public class OTPVerification extends AppCompatActivity {
    private String OTP, email;
    private String TAG = "OTPVerification Activity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_otpverification);

        Intent intent = getIntent();
        if (intent != null)
        {
            OTP = intent.getStringExtra("otp");
            email = intent.getStringExtra("email");
            Log.d(TAG, "OTP and Email: " + OTP + " "+ email);
            try {
                if (OTP != null && email != null)
                {
                    Toast.makeText(this, "OTP or email is null", Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception e)
            {
                Log.d(TAG, "OTP or Email is null " + e.toString());
            }

        }
    }
}