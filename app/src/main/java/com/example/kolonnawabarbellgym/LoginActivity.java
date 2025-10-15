package com.example.kolonnawabarbellgym;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.kolonnawabarbellgym.Mail.MailSender;
import com.example.kolonnawabarbellgym.Mail.OtpGenerator;

public class LoginActivity extends AppCompatActivity {

    private TextView textRegister, textResetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        textRegister = findViewById(R.id.txtSignup);
        textResetPassword = findViewById(R.id.txtForgotPassword);

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

//        String otp = OtpGenerator.generateOtp(6);
//        String userEmail = "dilshadnaleem13@gmail.com";
//        String subject = "Your OTP Code";
//        String message = "Your OTP is: " + otp + "\nValid for 5 minutes.";
//
//        MailSender mail = new MailSender(userEmail, subject, message);
//        mail.start();
    }
}