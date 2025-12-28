package com.example.kolonnawabarbellgym;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kolonnawabarbellgym.Adapter.ExpenseDetailAdapter;
import com.example.kolonnawabarbellgym.Database.DatabaseHelperClass;
import com.example.kolonnawabarbellgym.Model.ExpenseDetail;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MonthlyProfitDetailsActivity extends AppCompatActivity {

    private DatabaseHelperClass databaseHelper;
    private ExpenseDetailAdapter adapter;
    private List<ExpenseDetail> allExpenses;
    private List<ExpenseDetail> filteredExpenses;

    private TextView tvHeader, tvMonthIncome, tvMonthExpenses, tvMonthProfit, tvExpensesTitle;
    private EditText etSearch;
    private RecyclerView rvExpenses;

    private String monthYear;
    private String monthName;
    private String year;

    private NumberFormat currencyFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_profit_details);

        // Get data from intent
        monthYear = getIntent().getStringExtra("month_year");
        monthName = getIntent().getStringExtra("month_name");
        year = getIntent().getStringExtra("year");

        databaseHelper = new DatabaseHelperClass(this);
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("si", "LK"));
        currencyFormat.setMaximumFractionDigits(2);

        initializeViews();
        setupRecyclerView();
        loadMonthlyData();
        setupSearch();
    }

    private void initializeViews() {
        tvHeader = findViewById(R.id.tvHeader);
        tvMonthIncome = findViewById(R.id.tvMonthIncome);
        tvMonthExpenses = findViewById(R.id.tvMonthExpenses);
        tvMonthProfit = findViewById(R.id.tvMonthProfit);
        tvExpensesTitle = findViewById(R.id.tvExpensesTitle);
        etSearch = findViewById(R.id.etSearch);
        rvExpenses = findViewById(R.id.rvExpenses);

        // Set header
        tvHeader.setText(monthName + " " + year + " Details");
        tvExpensesTitle.setText("Expenses for " + monthName + " " + year);
    }

    private void setupRecyclerView() {
        adapter = new ExpenseDetailAdapter();
        rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        rvExpenses.setAdapter(adapter);
    }

    private void loadMonthlyData() {
        allExpenses = new ArrayList<>();
        filteredExpenses = new ArrayList<>();

        // Load expenses for the selected month
        loadExpenses();

        // Calculate monthly totals
        calculateMonthlyTotals();

        // Update adapter
        adapter.updateList(filteredExpenses);
    }

    private void loadExpenses() {
        Cursor cursor = databaseHelper.getAllExpenses();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int expenseId = cursor.getInt(cursor.getColumnIndexOrThrow("expense_id"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
                String dateString = cursor.getString(cursor.getColumnIndexOrThrow("created_at"));
                byte[] image = cursor.getBlob(cursor.getColumnIndexOrThrow("image"));

                // Parse date
                Date date = parseDate(dateString);

                // Check if expense belongs to selected month
                if (isDateInSelectedMonth(date)) {
                    ExpenseDetail expense = new ExpenseDetail(expenseId, description, amount, date, image);
                    allExpenses.add(expense);
                }

            } while (cursor.moveToNext());
            cursor.close();
        }

        filteredExpenses.addAll(allExpenses);
    }

    private Date parseDate(String dateString) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return format.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }
    }

    private boolean isDateInSelectedMonth(Date date) {
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        String dateMonthYear = monthFormat.format(date);
        return dateMonthYear.equals(monthYear);
    }

    private void calculateMonthlyTotals() {
        double monthlyIncome = calculateMonthlyIncome();
        double monthlyExpenses = calculateMonthlyExpenses();
        double monthlyProfit = monthlyIncome - monthlyExpenses;

        // Update UI
        tvMonthIncome.setText(currencyFormat.format(monthlyIncome));
        tvMonthExpenses.setText(currencyFormat.format(monthlyExpenses));
        tvMonthProfit.setText(currencyFormat.format(monthlyProfit));

        // Set profit color
        if (monthlyProfit > 0) {
            tvMonthProfit.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else if (monthlyProfit < 0) {
            tvMonthProfit.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private double calculateMonthlyIncome() {
        double income = 0;
        Cursor cursor = databaseHelper.getReadableDatabase().rawQuery(
                "SELECT SUM(price) FROM payment WHERE strftime('%Y-%m', created_at) = ?",
                new String[]{monthYear}
        );

        if (cursor != null && cursor.moveToFirst()) {
            income = cursor.getDouble(0);
            cursor.close();
        }
        return income;
    }

    private double calculateMonthlyExpenses() {
        double expenses = 0;
        for (ExpenseDetail expense : allExpenses) {
            expenses += expense.getAmount();
        }
        return expenses;
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterExpenses(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterExpenses(String query) {
        filteredExpenses.clear();

        if (query.isEmpty()) {
            filteredExpenses.addAll(allExpenses);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (ExpenseDetail expense : allExpenses) {
                if (expense.getDescription().toLowerCase().contains(lowerCaseQuery) ||
                        String.valueOf(expense.getAmount()).contains(query)) {
                    filteredExpenses.add(expense);
                }
            }
        }

        adapter.updateList(filteredExpenses);

        if (filteredExpenses.isEmpty()) {
            Toast.makeText(this, "No expenses found matching your search", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}