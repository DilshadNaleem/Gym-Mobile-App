package com.example.kolonnawabarbellgym;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import com.example.kolonnawabarbellgym.Database.DatabaseHelperClass;
import com.example.kolonnawabarbellgym.Database.DatabaseManagementActivity;
import com.example.kolonnawabarbellgym.Model.SalesData;
import com.example.kolonnawabarbellgym.DatabaseController.DashboardPresenter;
import com.example.kolonnawabarbellgym.Repository.DashboardRepo;
import com.example.kolonnawabarbellgym.Repository.DashboardRepositoryImpl;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.util.Locale;

public class MainDashboard extends BaseActivity implements DashboardPresenter.DashboardView {

    private String userEmail;
    private Button btn;
    private FloatingActionButton fabRefresh;
    private DashboardPresenter presenter;
    private ProgressBar progressBar;

    // TextViews for displaying data
    private TextView tvTodaySales, tvTotalSales, tvPendingAdmission, tvExistingMembers;
    private TextView tvTotalMembers, tvPaidMembers, tvPendingMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_dashboard);

        initializeViews();
        setupPresenter();
        setupButtonListeners();

        // Get user email from intent
        userEmail = getIntent().getStringExtra("remail");
        Log.d("MainDashboard", "User email: " + userEmail);

        currentNavItemId = R.id.navigation_dashboard;
        setupBottomNavigation(R.id.navigation_dashboard);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Load dashboard data after the view is fully set up
        if (presenter != null) {
            presenter.loadDashboardData();
        }
    }

    private void initializeViews() {
        btn = findViewById(R.id.btnintent);
        fabRefresh = findViewById(R.id.fabRefresh);
        progressBar = findViewById(R.id.progressBar);

        // Initialize sales TextViews
        tvTodaySales = findViewById(R.id.tvTodaySales);
        tvTotalSales = findViewById(R.id.tvTotalSales);
        tvPendingAdmission = findViewById(R.id.tvPendingAdmission);
        tvExistingMembers = findViewById(R.id.tvExistingMembers);

        // Initialize member count TextViews
        tvTotalMembers = findViewById(R.id.tvTotalMembers);
        tvPaidMembers = findViewById(R.id.tvPaidMembers);
        tvPendingMembers = findViewById(R.id.tvPendingMembers);
    }

    private void setupPresenter() {
        try {
            DatabaseHelperClass databaseHelper = new DatabaseHelperClass(this);
            DashboardRepo repository = new DashboardRepositoryImpl(databaseHelper);
            presenter = new DashboardPresenter(this, repository);
        } catch (Exception e) {
            Log.e("MainDashboard", "Error setting up presenter: " + e.getMessage());
            Toast.makeText(this, "Error initializing dashboard", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupButtonListeners() {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to DatabaseManagementActivity
                Intent intent = new Intent(MainDashboard.this, DatabaseManagementActivity.class);
                if (userEmail != null) {
                    intent.putExtra("remail", userEmail);
                }
                startActivity(intent);
            }
        });

        fabRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshData();
            }
        });
    }

    private void refreshData() {
        if (presenter != null) {
            presenter.loadDashboardData();
            Toast.makeText(this, "Refreshing data...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void displaySalesData(SalesData salesData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Format currency
                    NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("si", "LK"));
                    format.setMaximumFractionDigits(2);

                    // Update sales data
                    tvTodaySales.setText(format.format(salesData.getTodaySales()));
                    tvTotalSales.setText(format.format(salesData.getTotalSales()));
                    tvPendingAdmission.setText(format.format(salesData.getPendingAdmissionFees()));
                    tvExistingMembers.setText(format.format(salesData.getExistingMembersFees()));

                    // Update member counts
                    tvTotalMembers.setText(String.valueOf(salesData.getTotalMembers()));
                    tvPaidMembers.setText(String.valueOf(salesData.getPaidMembers()));
                    tvPendingMembers.setText(String.valueOf(salesData.getPendingMembers()));

                    // Update visual indicators
                    updateVisualIndicators(salesData);

                } catch (Exception e) {
                    Log.e("MainDashboard", "Error updating UI: " + e.getMessage());
                }
            }
        });
    }

    private void updateVisualIndicators(SalesData salesData) {
        // Reset backgrounds
        tvTodaySales.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        tvPendingAdmission.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        // Highlight if there are pending admissions
        if (salesData.getPendingAdmissionFees() > 0) {
            tvPendingAdmission.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
        }

        // Highlight if today's sales are good
        if (salesData.getTodaySales() > 0) {
            tvTodaySales.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        }
    }

    @Override
    public void showError(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Toast.makeText(MainDashboard.this, message, Toast.LENGTH_SHORT).show();
                    Log.e("MainDashboard", message);
                } catch (Exception e) {
                    Log.e("MainDashboard", "Error showing error message: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void showLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void hideLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to the dashboard
        if (presenter != null) {
            presenter.loadDashboardData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up presenter to prevent memory leaks
        if (presenter != null) {
            presenter.detachView();
        }
    }
}