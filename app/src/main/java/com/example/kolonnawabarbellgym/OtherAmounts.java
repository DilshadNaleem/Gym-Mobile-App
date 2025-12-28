package com.example.kolonnawabarbellgym;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kolonnawabarbellgym.Adapter.OtherAmountsAdapter;
import com.example.kolonnawabarbellgym.Database.DatabaseHelperClass;
import com.example.kolonnawabarbellgym.Model.OtherAmount;

import java.util.ArrayList;
import java.util.List;

public class OtherAmounts extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OtherAmountsAdapter adapter;
    private List<OtherAmount> otherAmountList;
    private List<OtherAmount> filteredList;
    private EditText searchEditText;
    private TextView tvResultsCount, emptyStateTitle;
    private LinearLayout tvEmptyState;
    private DatabaseHelperClass databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_other_amounts);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupRecyclerView();
        loadOtherAmounts();
        setupSearchFilter();
        updateUI();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewOtherAmounts);
        searchEditText = findViewById(R.id.searchEditText);
        tvResultsCount = findViewById(R.id.tvResultsCount);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        emptyStateTitle = findViewById(R.id.tvEmptyStateTitle);
        databaseHelper = new DatabaseHelperClass(this);

        otherAmountList = new ArrayList<>();
        filteredList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new OtherAmountsAdapter(this, filteredList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadOtherAmounts() {
        try {
            otherAmountList = databaseHelper.getAllOtherAmounts();
            filteredList.clear();
            filteredList.addAll(otherAmountList);
            adapter.notifyDataSetChanged();
            updateUI();

            if (otherAmountList.isEmpty()) {
                Toast.makeText(this, "No other amounts found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("OtherAmounts", "Error loading other amounts: " + e.getMessage());
            Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSearchFilter() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterData(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            filteredList.addAll(otherAmountList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (OtherAmount amount : otherAmountList) {
                // Check all possible searchable fields
                if ((amount.getFirstName() != null && amount.getFirstName().toLowerCase().contains(lowerCaseQuery)) ||
                        (amount.getFirstName() != null && amount.getFirstName().toLowerCase().contains(lowerCaseQuery)) ||
                        (amount.getLastName() != null && amount.getLastName().toLowerCase().contains(lowerCaseQuery)) ||
                        (amount.getDescription() != null && amount.getDescription().toLowerCase().contains(lowerCaseQuery)) ||
                        (amount.getMonth() != null && amount.getMonth().toLowerCase().contains(lowerCaseQuery)) ||
                        (amount.getHandoveredTo() != null && amount.getHandoveredTo().toLowerCase().contains(lowerCaseQuery)) ||
                        (amount.getFormattedDate() != null && amount.getFormattedDate().toLowerCase().contains(lowerCaseQuery))) {
                    filteredList.add(amount);
                }
            }
        }
        adapter.notifyDataSetChanged();
        updateUI();
    }

    private void updateUI() {
        // Update results count
        String resultsText = filteredList.size() + " result" + (filteredList.size() != 1 ? "s" : "") + " found";
        if (!searchEditText.getText().toString().isEmpty()) {
            resultsText += " for '" + searchEditText.getText().toString() + "'";
        }
        tvResultsCount.setText(resultsText);

        // Show/hide empty state
        if (filteredList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

            if (!searchEditText.getText().toString().isEmpty()) {
                emptyStateTitle.setText("No results found for '" + searchEditText.getText().toString() + "'");
            } else {
                emptyStateTitle.setText("No other amounts found");
            }
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOtherAmounts(); // Refresh data when returning to activity
    }
}