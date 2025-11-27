package com.example.kolonnawabarbellgym;

import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kolonnawabarbellgym.Adapter.MemberSalesAdapter;
import com.example.kolonnawabarbellgym.Database.DatabaseHelperClass;
import com.example.kolonnawabarbellgym.Model.MemberSales;
import com.example.kolonnawabarbellgym.Repository.DashboardRepositoryImpl;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TotalSalesActivity extends AppCompatActivity {

    private RecyclerView rvMembers;
    private ProgressBar progressBar;
    private TextView tvTotalMembersCount, tvTotalAmount;
    private MemberSalesAdapter adapter;
    private DashboardRepositoryImpl repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_sales);

        initializeViews();
        setupRepository();
        loadTotalAdmissionSales();
    }

    private void initializeViews() {
        rvMembers = findViewById(R.id.rvMembers);
        progressBar = findViewById(R.id.progressBar);
        tvTotalMembersCount = findViewById(R.id.tvTotalMembersCount);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);

        // Setup RecyclerView
        adapter = new MemberSalesAdapter(new java.util.ArrayList<>());
        rvMembers.setLayoutManager(new LinearLayoutManager(this));
        rvMembers.setAdapter(adapter);
    }

    private void setupRepository() {
        try {
            DatabaseHelperClass databaseHelper = new DatabaseHelperClass(this);
            repository = new DashboardRepositoryImpl(databaseHelper);
        } catch (Exception e) {
            Log.e("TotalSalesActivity", "Error setting up repository: " + e.getMessage());
        }
    }

    private void loadTotalAdmissionSales() {
        showLoading();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<MemberSales> memberSalesList = repository.getTotalAdmissionSales();
                    final double totalAmount = calculateTotalAmount(memberSalesList);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideLoading();
                            updateUI(memberSalesList, totalAmount);
                        }
                    });

                } catch (Exception e) {
                    Log.e("TotalSalesActivity", "Error loading admission sales: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideLoading();
                        }
                    });
                }
            }
        }).start();
    }

    private double calculateTotalAmount(List<MemberSales> memberSalesList) {
        double total = 0;
        for (MemberSales member : memberSalesList) {
            total += member.getAdmissionFee();
        }
        return total;
    }

    private void updateUI(List<MemberSales> memberSalesList, double totalAmount) {
        // Update member count
        tvTotalMembersCount.setText(String.valueOf(memberSalesList.size()));

        // Update total amount
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("si", "LK"));
        format.setMaximumFractionDigits(2);
        tvTotalAmount.setText(format.format(totalAmount));

        // Update RecyclerView
        adapter.updateData(memberSalesList);

        Log.d("TotalSalesActivity", "UI updated with " + memberSalesList.size() + " members");
    }

    private void showLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(android.view.View.VISIBLE);
                rvMembers.setVisibility(android.view.View.GONE);
            }
        });
    }

    private void hideLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(android.view.View.GONE);
                rvMembers.setVisibility(android.view.View.VISIBLE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to activity
        if (repository != null) {
            loadTotalAdmissionSales();
        }
    }
}