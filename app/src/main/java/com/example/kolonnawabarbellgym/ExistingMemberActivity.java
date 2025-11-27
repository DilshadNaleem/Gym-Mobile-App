package com.example.kolonnawabarbellgym;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kolonnawabarbellgym.Adapter.ExistingMemberAdapter;
import com.example.kolonnawabarbellgym.Database.DatabaseHelperClass;
import com.example.kolonnawabarbellgym.Model.Member;
import com.example.kolonnawabarbellgym.Repository.DashboardRepositoryImpl;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ExistingMemberActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvTotalAmount;
    private ExistingMemberAdapter adapter;
    private DashboardRepositoryImpl repository;
    private List<Member> membersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_existing_member);

        initializeViews();
        setupRepository();
        loadExistingMembers();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewExistingMembers);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupRepository() {
        DatabaseHelperClass databaseHelper = new DatabaseHelperClass(this);
        repository = new DashboardRepositoryImpl(databaseHelper);
    }

    private void loadExistingMembers() {
        membersList = repository.getExistingMembersWithUnpaidFees();
        updateUI();
    }

    private void updateUI() {
        // Calculate total amount
        double totalAmount = 0;
        for (Member member : membersList) {
            totalAmount += member.getTotalDue();
        }

        // Format and display total amount
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("si", "LK"));
        format.setMaximumFractionDigits(2);
        tvTotalAmount.setText(format.format(totalAmount));

        // Setup adapter
        adapter = new ExistingMemberAdapter(this, membersList);
        recyclerView.setAdapter(adapter);
    }
}