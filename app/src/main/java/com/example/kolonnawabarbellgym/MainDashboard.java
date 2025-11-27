package com.example.kolonnawabarbellgym;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
    private TextView tvTodayExpenses, tvTotalExpenses;
    private ImageButton btnAddExpense;

    // TextViews for displaying data
    private TextView tvTodaySales, tvTotalSales, tvTotalAmount, tvPendingAdmission, tvExistingMembers;
    private TextView tvTotalMembers, tvPaidMembers, tvPendingMembers, tvOtherAmountLabel, tvOtherAmounts;
    private TextView tvTodayProfit, tvTotalProfit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_dashboard);

        initializeViews();
        setupPresenter();
        setupClickListeners(); // Changed from setupButtonListeners()

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
        tvTotalSales = findViewById(R.id.tvTotalSales); // This now shows Total Admission Price
        tvTotalAmount = findViewById(R.id.tvTotalAmount); // New TextView for Total Amount Received
        tvPendingAdmission = findViewById(R.id.tvPendingAdmission);
        tvExistingMembers = findViewById(R.id.tvExistingMembers);
        tvOtherAmountLabel = findViewById(R.id.tvOtherAmountsLabel);
        tvOtherAmounts = findViewById(R.id.tvOtherAmounts);

        // Initialize member count TextViews
        tvTotalMembers = findViewById(R.id.tvTotalMembers);
        tvPaidMembers = findViewById(R.id.tvPaidMembers);
        tvPendingMembers = findViewById(R.id.tvPendingMembers);
        tvTodayExpenses = findViewById(R.id.tvTodayExpenses);
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses);
        btnAddExpense = findViewById(R.id.btnAddExpense);
        tvTodayProfit = findViewById(R.id.tvTodayProfit);
        tvTotalProfit = findViewById(R.id.tvTotalProfit);
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

    private void setupClickListeners() {
        // Button to navigate to DatabaseManagementActivity
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

        // Refresh button
        fabRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshData();
            }
        });

        tvTodayExpenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToTodayExpenses();
            }
        });

        // Total expenses click - navigate to all expenses
        tvTotalExpenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToAllExpenses();
            }
        });

        // Add expense button
        btnAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToAddExpense();
            }
        });

        // Profit click listeners
        setupProfitClickListeners();

        setupTodaySalesClick();
        // Existing Members click listener
        setupExistingMembersClick();
        setUpTotalAdmissionPrice();
        setupOtherAmountsClick();
    }

    private void setupProfitClickListeners() {
        tvTodayProfit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show today's profit breakdown in popup (existing functionality)
                if (presenter != null) {
                    DashboardPresenter.ProfitBreakdown breakdown = presenter.getTodayProfitBreakdown();
                    showProfitBreakdown(breakdown);
                }
            }
        });

        // Total profit click - NEW: Navigate to Profit Analysis Activity
        tvTotalProfit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToProfitAnalysis();
            }
        });
    }

    private void navigateToProfitAnalysis() {
        Intent intent = new Intent(MainDashboard.this, ProfitAnalyticsActivity.class);
        if (userEmail != null) {
            intent.putExtra("remail", userEmail);
        }
        startActivity(intent);
    }

    private void showProfitBreakdown(DashboardPresenter.ProfitBreakdown breakdown) {
        // Extract values from the breakdown object
        String title = breakdown.getTitle(); // or breakdown.title if it's public
        double income = breakdown.getIncome(); // or breakdown.income
        double expenses = breakdown.getExpenses(); // or breakdown.expenses
        double profit = breakdown.getProfit(); // or breakdown.profit

        String message = String.format(Locale.getDefault(),
                "%s\n\n" +
                        "💰 Income: %s\n" +
                        "💸 Expenses: %s\n" +
                        "📊 Profit: %s\n\n" +
                        "Formula: Income - Expenses = Profit",
                title,
                formatCurrency(income),
                formatCurrency(expenses),
                formatCurrency(profit));

        new AlertDialog.Builder(this)
                .setTitle("Profit Calculation")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private String formatCurrency(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("si", "LK"));
        format.setMaximumFractionDigits(2);
        return format.format(amount);
    }

    private void setupExistingMembersClick() {
        tvExistingMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToExistingMembers();
            }
        });
    }

    private void navigateToExistingMembers() {
        Intent intent = new Intent(MainDashboard.this, ExistingMemberActivity.class);
        if (userEmail != null) {
            intent.putExtra("remail", userEmail);
        }
        startActivity(intent);
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
                    tvTotalSales.setText(format.format(salesData.getTotalSales())); // Total Admission Price
                    tvTotalAmount.setText(format.format(salesData.getTotalAmountOfPrices())); // Total Amount Received
                    tvPendingAdmission.setText(format.format(salesData.getPendingAdmissionFees()));
                    tvExistingMembers.setText(format.format(salesData.getExistingMembersFees()));
                    tvOtherAmounts.setText(format.format(salesData.getOtherAmount()));

                    // Update expense data
                    tvTodayExpenses.setText(format.format(salesData.getTodayExpenses()));
                    tvTotalExpenses.setText(format.format(salesData.getTotalExpenses()));

                    // Update profit data - ADD THESE LINES
                    tvTodayProfit.setText(format.format(salesData.getTodayProfit()));
                    tvTotalProfit.setText(format.format(salesData.getTotalProfit()));

                    // Update member counts
                    tvTotalMembers.setText(String.valueOf(salesData.getTotalMembers()));
                    tvPaidMembers.setText(String.valueOf(salesData.getPaidMembers()));
                    tvPendingMembers.setText(String.valueOf(salesData.getPendingMembers()));

                    // Update visual indicators
                    updateVisualIndicators(salesData);
                    updateExpenseVisualIndicators(salesData);
                    updateProfitVisualIndicators(salesData); // ADD THIS LINE

                } catch (Exception e) {
                    Log.e("MainDashboard", "Error updating UI: " + e.getMessage());
                }
            }
        });
    }

    private void updateVisualIndicators(SalesData salesData) {
        // Reset backgrounds
        tvTodaySales.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        tvTotalAmount.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        tvPendingAdmission.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        tvExistingMembers.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        tvOtherAmounts.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        // Highlight if there are pending admissions
        if (salesData.getPendingAdmissionFees() > 0) {
            tvPendingAdmission.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
        }

        // Highlight if today's sales are good
        if (salesData.getTodaySales() > 0) {
            tvTodaySales.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        }

        // Highlight total amount received
        if (salesData.getTotalAmountOfPrices() > 0) {
            tvTotalAmount.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        }

        // Highlight if there are existing members with unpaid fees
        if (salesData.getExistingMembersFees() > 0) {
            tvExistingMembers.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        }

        if (salesData.getOtherAmount() > 0) {
            tvOtherAmounts.setBackgroundColor(getResources().getColor(android.R.color.holo_purple));
        }
    }

    private void updateExpenseVisualIndicators(SalesData salesData) {
        // Reset backgrounds for expense views
        tvTodayExpenses.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        tvTotalExpenses.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        // Highlight if there are expenses today
        if (salesData.getTodayExpenses() > 0) {
            tvTodayExpenses.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
        }

        // Highlight total expenses
        if (salesData.getTotalExpenses() > 0) {
            tvTotalExpenses.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        }
    }

    // ADD THIS METHOD FOR PROFIT VISUAL INDICATORS
    private void updateProfitVisualIndicators(SalesData salesData) {
        // Reset backgrounds for profit views
        tvTodayProfit.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        tvTotalProfit.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        // Reset text colors
        tvTodayProfit.setTextColor(getResources().getColor(android.R.color.black));
        tvTotalProfit.setTextColor(getResources().getColor(android.R.color.black));

        // Highlight today's profit - green for profit, red for loss
        if (salesData.getTodayProfit() > 0) {
            // Profit - green background
            tvTodayProfit.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            tvTodayProfit.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else if (salesData.getTodayProfit() < 0) {
            // Loss - red background
            tvTodayProfit.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            tvTodayProfit.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            // Break-even - yellow background
            tvTodayProfit.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
        }

        // Highlight total profit - green for profit, red for loss
        if (salesData.getTotalProfit() > 0) {
            // Profit - green background
            tvTotalProfit.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            tvTotalProfit.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else if (salesData.getTotalProfit() < 0) {
            // Loss - red background
            tvTotalProfit.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            tvTotalProfit.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            // Break-even - yellow background
            tvTotalProfit.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
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

    private void setupTodaySalesClick() {
        tvTodaySales.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToTodaySales();
            }
        });
    }

    private void navigateToTodaySales() {
        Intent intent = new Intent(MainDashboard.this, TodaySalesActivity.class);
        if (userEmail != null) {
            intent.putExtra("remail", userEmail);
        }
        startActivity(intent);
    }

    private void setUpTotalAdmissionPrice()
    {
        tvTotalSales.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainDashboard.this, TotalSalesActivity.class);
                {
                    if (userEmail != null) {
                        intent.putExtra("remail", userEmail);
                    }
                    startActivity(intent);
                }
            }
        });
    }

    private void setupOtherAmountsClick() {
        tvOtherAmounts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToOtherAmounts();
            }
        });
    }

    private void navigateToOtherAmounts() {
        Intent intent = new Intent(MainDashboard.this,OtherAmounts.class);
        startActivity(intent);
        Toast.makeText(this, "Other Amounts Details - Implement this screen", Toast.LENGTH_SHORT).show();
    }

    private void navigateToTodayExpenses() {
        Intent intent = new Intent(MainDashboard.this, TodayExpensesActivity.class);
        if (userEmail != null) {
            intent.putExtra("remail", userEmail);
        }
        startActivity(intent);
    }

    private void navigateToAllExpenses() {
        Intent intent = new Intent(MainDashboard.this, AllExpensesActivity.class);
        if (userEmail != null) {
            intent.putExtra("remail", userEmail);
        }
        startActivity(intent);
    }

    private void navigateToAddExpense() {
        Intent intent = new Intent(MainDashboard.this, AddExpenseActivity.class);
        if (userEmail != null) {
            intent.putExtra("remail", userEmail);
        }
        startActivity(intent);
    }
}