package com.example.kolonnawabarbellgym;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kolonnawabarbellgym.Adapter.TodaySalesAdapter;
import com.example.kolonnawabarbellgym.Database.DatabaseHelperClass;
import com.example.kolonnawabarbellgym.Model.Payment;
import com.example.kolonnawabarbellgym.Repository.PaymentRepository;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TodaySalesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvTotalTodaySales;
    private TodaySalesAdapter adapter;
    private PaymentRepository paymentRepository;
    private List<Payment> todayPayments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_sales);

        initializeViews();
        setupRepository();
        loadTodaySales();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewTodaySales);
        tvTotalTodaySales = findViewById(R.id.tvTotalTodaySales);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupRepository() {
        DatabaseHelperClass databaseHelper = new DatabaseHelperClass(this);
        paymentRepository = new PaymentRepository(databaseHelper);
    }

    private void loadTodaySales() {
        todayPayments = paymentRepository.getTodayPayments();
        updateUI();
    }

    private void updateUI() {
        // Calculate total today sales
        double totalTodaySales = 0;
        for (Payment payment : todayPayments) {
            totalTodaySales += payment.getPrice();
        }

        // Format and display total amount
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("si", "LK"));
        format.setMaximumFractionDigits(2);
        tvTotalTodaySales.setText(format.format(totalTodaySales));

        // Setup adapter - remove 'this' parameter
        adapter = new TodaySalesAdapter(this,todayPayments);
        recyclerView.setAdapter(adapter);
    }
}