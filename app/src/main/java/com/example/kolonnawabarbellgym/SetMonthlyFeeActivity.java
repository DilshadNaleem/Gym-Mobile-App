package com.example.kolonnawabarbellgym;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kolonnawabarbellgym.Database.DatabaseHelperClass;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetMonthlyFeeActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmail;
    private EditText etMonthlyFee, etOtherMonth;
    private Button btnSaveFee;
    private LinearLayout monthContainer;
    private TextInputLayout tilOtherMonth;
    private CheckBox cbOtherMonth;
    private DatabaseHelperClass databaseHelper;
    private String userUniqueId, userName, userEmail;

    private Map<String, CheckBox> monthCheckboxes;
    private List<String> paidMonths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set_monthly_fee);

        initViews();
        getIntentData();
        setupMonthCheckboxes();
        setupClickListeners();
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        etMonthlyFee = findViewById(R.id.etMonthlyFee);
        etOtherMonth = findViewById(R.id.etOtherMonth);
        btnSaveFee = findViewById(R.id.btnSaveFee);
        monthContainer = findViewById(R.id.monthContainer);
        tilOtherMonth = findViewById(R.id.tilOtherMonth);
        cbOtherMonth = findViewById(R.id.cbOtherMonth);
        databaseHelper = new DatabaseHelperClass(this);

        monthCheckboxes = new HashMap<>();
        paidMonths = new ArrayList<>();
    }

    private void getIntentData() {
        userUniqueId = getIntent().getStringExtra("user_unique_id");
        userName = getIntent().getStringExtra("user_name");
        userEmail = getIntent().getStringExtra("user_email");
        String currentFee = getIntent().getStringExtra("current_fee");

        tvUserName.setText("User: " + userName);
        tvUserEmail.setText("Email: " + userEmail);

        if (currentFee != null && !currentFee.isEmpty()) {
            etMonthlyFee.setText(currentFee);
        }

        // Load already paid months
        loadPaidMonths();
    }

    private void loadPaidMonths() {
        paidMonths = databaseHelper.getPaidMonthsForUser(userUniqueId);
    }

    private void setupMonthCheckboxes() {
        String[] months = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        for (String month : months) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(month + " " + currentYear);
            checkBox.setTag(month);

            // Check if this month is already paid
            String monthYearKey = month + "_" + currentYear;
            if (paidMonths.contains(monthYearKey)) {
                checkBox.setChecked(true);
                checkBox.setEnabled(false);
                checkBox.setText(month + " " + currentYear + " (Paid)");
            }

            monthCheckboxes.put(month, checkBox);
            monthContainer.addView(checkBox);
        }
    }

    private void setupClickListeners() {
        btnSaveFee.setOnClickListener(v -> saveMonthlyFee());

        // Other month checkbox listener
        cbOtherMonth.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tilOtherMonth.setVisibility(View.VISIBLE);
            } else {
                tilOtherMonth.setVisibility(View.GONE);
                etOtherMonth.setText("");
            }
        });
    }

    private void saveMonthlyFee() {
        String monthlyFee = etMonthlyFee.getText().toString().trim();

        if (monthlyFee.isEmpty()) {
            Toast.makeText(this, "Please enter monthly fee", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected months and other month text
        List<String> selectedMonths = getSelectedMonths();
        String otherMonthText = etOtherMonth.getText().toString().trim();

        // Check if "Other" is selected but no text entered
        if (cbOtherMonth.isChecked() && otherMonthText.isEmpty()) {
            Toast.makeText(this, "Please enter custom month description", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add other month if selected and has text
        if (cbOtherMonth.isChecked() && !otherMonthText.isEmpty()) {
            selectedMonths.add(otherMonthText);
        }

        if (selectedMonths.isEmpty()) {
            Toast.makeText(this, "Please select at least one month or enter custom month", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update monthly fee
        boolean isFeeUpdated = databaseHelper.updateUserMonthlyFee(userUniqueId, monthlyFee);

        // Save payment records for selected months
        boolean isPaymentSaved = savePaymentRecords(selectedMonths, monthlyFee);

        if (isFeeUpdated && isPaymentSaved) {
            Toast.makeText(this, "Monthly fee and payments updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to update monthly fee and payments", Toast.LENGTH_SHORT).show();
        }
    }

    private List<String> getSelectedMonths() {
        List<String> selectedMonths = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        for (Map.Entry<String, CheckBox> entry : monthCheckboxes.entrySet()) {
            CheckBox checkBox = entry.getValue();
            if (checkBox.isEnabled() && checkBox.isChecked()) {
                String monthYearKey = entry.getKey() + "_" + currentYear;
                selectedMonths.add(monthYearKey);
            }
        }
        return selectedMonths;
    }

    private boolean savePaymentRecords(List<String> selectedMonths, String amount) {
        boolean allSuccess = true;
        String sessionEmail = getIntent().getStringExtra("remail");

        if (sessionEmail == null || sessionEmail.isEmpty()) {
            sessionEmail = "admin@kolonnawa.com";
        }

        for (String monthYear : selectedMonths) {
            boolean success;

            // Check if this is an "Other" entry (not in standard month_year format)
            if (isOtherMonthEntry(monthYear)) {
                // Save as other payment
                success = databaseHelper.saveOtherPaymentRecord(
                        userUniqueId,
                        userName,
                        monthYear, // This will be the custom description
                        amount,
                        sessionEmail
                );
            } else {
                // Save as regular monthly payment
                success = databaseHelper.savePaymentRecord(
                        userUniqueId,
                        userName,
                        monthYear,
                        amount,
                        sessionEmail
                );
            }

            if (!success) {
                allSuccess = false;
            }
        }

        return allSuccess;
    }

    private boolean isOtherMonthEntry(String monthYear) {
        // If it doesn't match the standard "Month_Year" pattern, it's an "Other" entry
        return !monthYear.matches("^(January|February|March|April|May|June|July|August|September|October|November|December)_\\d{4}$");
    }
}