package com.example.kolonnawabarbellgym;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Patterns;
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
import com.example.kolonnawabarbellgym.Mail.MailSender;

public class ForgotPassword extends AppCompatActivity {

    private TextView txtForgotPasswordEmail;
    private Button btnForgotPasswordButton;
    private EditText etForgotPasswordEmail;
    private DatabaseHelperClass databaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        databaseHelper = new DatabaseHelperClass(this);

        etForgotPasswordEmail = findViewById(R.id.txtForgotPasswordEmail);
        txtForgotPasswordEmail = findViewById(R.id.txtForgotPasswordNotice);
        btnForgotPasswordButton = findViewById(R.id.btnForgotPassword);

        btnForgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleForgotPassword();
            }
        });

        txtForgotPasswordEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ForgotPassword.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void handleForgotPassword()
    {
        String email = etForgotPasswordEmail.getText().toString().trim();

        if (email.isEmpty())
        {
            etForgotPasswordEmail.setError("Please Enter your Email");
            etForgotPasswordEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            etForgotPasswordEmail.setError("Please enter a Valid email");
            etForgotPasswordEmail.requestFocus();
            return;
        }

        if (isValidUser(email))
        {
            sendResetPasswordEmail(email);

            Intent intent = new Intent(ForgotPassword.this, ResetPassword.class);
            intent.putExtra("ForgotEmail", email);

            startActivity(intent);

            Toast.makeText(ForgotPassword.this,"Reset Password Email sent Successfully!", Toast.LENGTH_SHORT
            ).show();
        }
        else
        {
            Toast.makeText(ForgotPassword.this, "Email not found or account not verified", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidUser(String email)
    {
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

    private void sendResetPasswordEmail(String email)
    {
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
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}