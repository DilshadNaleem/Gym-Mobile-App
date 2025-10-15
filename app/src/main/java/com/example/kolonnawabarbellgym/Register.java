package com.example.kolonnawabarbellgym;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kolonnawabarbellgym.DTO.User;
import com.example.kolonnawabarbellgym.Database.DatabaseHelperClass;
import com.example.kolonnawabarbellgym.DatabaseController.RegisterController;
import com.example.kolonnawabarbellgym.Mail.MailSender;
import com.example.kolonnawabarbellgym.Mail.OtpGenerator;

import java.util.regex.Pattern;

public class Register extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private TextView textSigning;
    private EditText etFirstName, etLastName, etEmail, etNIC, editTextPhone, etPassword, etConfirmPassword;
    private Button btnRegister;
    private DatabaseHelperClass dbHelper;
    private RegisterController dbController;

    // Sri Lankan NIC regex pattern (supports both old and new formats)
    private static final Pattern NIC_PATTERN = Pattern.compile("^([0-9]{9}[xXvV]|[0-9]{12})$");
    // Sri Lankan phone number pattern
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(?:0|94|\\+94)?(?:(11|21|23|24|25|26|27|31|32|33|34|35|36|37|38|41|45|47|51|52|54|55|57|63|65|66|67|81|91)(0|2|3|4|5|7|9)|7(0|1|2|4|5|6|7|8)\\d)\\d{6}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeViews();
        setupDatabase();
        setupClickListeners();
    }

    private void initializeViews() {
        textSigning = findViewById(R.id.txtSigning);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etNIC = findViewById(R.id.etNIC);
        editTextPhone = findViewById(R.id.editTextPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.editTextTextPassword2);
        btnRegister = findViewById(R.id.btnRegister);
    }

    private void setupDatabase() {
        dbHelper = new DatabaseHelperClass(this);
        dbController = new RegisterController(dbHelper);
    }

    private void setupClickListeners() {
        textSigning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Register.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String nic = etNIC.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (validateInputs(firstName, lastName, email, nic, phone, password, confirmPassword)) {
            User user = new User(firstName, lastName, email, phone, nic, password);

            if (dbController.registerUser(user)) {
                String otp = OtpGenerator.generateOtp(6);
                String subject = "Your OTP Code";
                String message = "Your OTP is: " + otp + "\n Valid for 5 minutes";

                MailSender mail = new MailSender(email, subject, message);
                mail.start();

                Toast.makeText(Register.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                clearForm();
                // Redirect to login or verification activity
                Intent intent = new Intent(Register.this, OTPVerification.class);
                intent.putExtra("otp", otp);
                intent.putExtra("email", email);
                Log.d(TAG,"OTP Session Stored " + otp);
                Log.d(TAG,"Email Session Stored " + email);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(Register.this, "Registration failed! Email may already exist.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validateInputs(String firstName, String lastName, String email,
                                   String nic, String phone, String password, String confirmPassword) {

        if (TextUtils.isEmpty(firstName)) {
            etFirstName.setError("First name is required");
            etFirstName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(lastName)) {
            etLastName.setError("Last name is required");
            etLastName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(nic)) {
            etNIC.setError("NIC is required");
            etNIC.requestFocus();
            return false;
        }

        if (!NIC_PATTERN.matcher(nic).matches()) {
            etNIC.setError("Please enter a valid Sri Lankan NIC");
            etNIC.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(phone)) {
            editTextPhone.setError("Phone number is required");
            editTextPhone.requestFocus();
            return false;
        }

        if (!PHONE_PATTERN.matcher(phone).matches()) {
            editTextPhone.setError("Please enter a valid Sri Lankan phone number");
            editTextPhone.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void clearForm() {
        etFirstName.setText("");
        etLastName.setText("");
        etEmail.setText("");
        etNIC.setText("");
        editTextPhone.setText("");
        etPassword.setText("");
        etConfirmPassword.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}