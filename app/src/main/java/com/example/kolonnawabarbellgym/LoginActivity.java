package com.example.kolonnawabarbellgym;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
    private EditText emailLogin, loginPasword;
    private Button btnLogin;
    private DatabaseHelperClass dbHelper;
    private LoginController loginController;
    private static final String TAG = "LoginActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelperClass(this);
        loginController = new LoginController(dbHelper);

        emailLogin = findViewById(R.id.etEmailLogin);
        loginPasword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
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

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performLogin();
            }
        });
    }

    private void performLogin()
    {
        String email = emailLogin.getText().toString().trim();
        String password = loginPasword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty())
        {
            Toast.makeText(LoginActivity.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(LoginActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean loginSuccess = loginController.loginUser(email, password);

        if (loginSuccess) {
            Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

            // Navigate to main activity or home screen
            Intent intent = new Intent(LoginActivity.this, MainDashboard.class);
            intent.putExtra("remail", email);
            Log.d(TAG,"Login Using : " + email);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}