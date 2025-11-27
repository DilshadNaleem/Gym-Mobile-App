package com.example.kolonnawabarbellgym;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.kolonnawabarbellgym.Database.DatabaseHelperClass;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProfitAnalyticsActivity extends AppCompatActivity {

    private DatabaseHelperClass databaseHelper;
    private LinearLayout profitContainer;
    private EditText etSearch;
    private Spinner spinnerYear, spinnerMonth;
    private TextView tvTotalProfit, tvTotalIncome, tvTotalExpenses, tvAnalysisSummary;

    private List<MonthlyProfit> monthlyProfits = new ArrayList<>();
    private NumberFormat currencyFormat;
    private String selectedYear = "All";
    private String selectedMonth = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profit_analytics);

        databaseHelper = new DatabaseHelperClass(this);

        initializeViews();
        setupCurrencyFormat();
        setupSpinners();
        setupSearch();

        // Initialize database helper


        loadProfitData();
    }

    private void initializeViews() {
        profitContainer = findViewById(R.id.profitContainer);
        etSearch = findViewById(R.id.etSearch);
        spinnerYear = findViewById(R.id.spinnerYear);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        tvTotalProfit = findViewById(R.id.tvTotalProfit);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses);
        tvAnalysisSummary = findViewById(R.id.tvAnalysisSummary);
    }

    private void setupCurrencyFormat() {
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("si", "LK"));
        currencyFormat.setMaximumFractionDigits(2);
    }

    private void setupSpinners() {
        // Setup Year Spinner
        List<String> years = getAvailableYears();
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        // Setup Month Spinner
        List<String> months = getAvailableMonths();
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        // Year selection listener
        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = parent.getItemAtPosition(position).toString();
                filterProfitData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Month selection listener
        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMonth = parent.getItemAtPosition(position).toString();
                filterProfitData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProfitData();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private List<String> getAvailableYears() {
        List<String> years = new ArrayList<>();
        years.add("All");

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT DISTINCT strftime('%Y', created_at) as year FROM payment " +
                        "UNION SELECT DISTINCT strftime('%Y', created_at) as year FROM expenses " +
                        "ORDER BY year DESC", null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String year = cursor.getString(0);
                if (year != null) {
                    years.add(year);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        return years;
    }

    private List<String> getAvailableMonths() {
        List<String> months = new ArrayList<>();
        months.add("All");
        months.add("January"); months.add("February"); months.add("March");
        months.add("April"); months.add("May"); months.add("June");
        months.add("July"); months.add("August"); months.add("September");
        months.add("October"); months.add("November"); months.add("December");
        return months;
    }

    private void loadProfitData() {
        monthlyProfits.clear();

        // Get all months with data
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Query to get all months that have either payments or expenses
        String query = "SELECT DISTINCT strftime('%Y-%m', created_at) as month_year " +
                "FROM (" +
                "  SELECT created_at FROM payment " +
                "  UNION ALL " +
                "  SELECT created_at FROM expenses" +
                ") ORDER BY month_year DESC";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String monthYear = cursor.getString(0);
                if (monthYear != null) {
                    MonthlyProfit monthlyProfit = calculateMonthlyProfit(monthYear);
                    if (monthlyProfit != null) {
                        monthlyProfits.add(monthlyProfit);
                    }
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        displayProfitData(monthlyProfits);
        updateSummary();
    }

    private MonthlyProfit calculateMonthlyProfit(String monthYear) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Calculate monthly income
        double monthlyIncome = 0;
        Cursor incomeCursor = db.rawQuery(
                "SELECT SUM(price) FROM payment WHERE strftime('%Y-%m', created_at) = ?",
                new String[]{monthYear}
        );
        if (incomeCursor != null && incomeCursor.moveToFirst()) {
            monthlyIncome = incomeCursor.getDouble(0);
            incomeCursor.close();
        }

        // Calculate monthly expenses
        double monthlyExpenses = 0;
        Cursor expenseCursor = db.rawQuery(
                "SELECT SUM(price) FROM expenses WHERE strftime('%Y-%m', created_at) = ?",
                new String[]{monthYear}
        );
        if (expenseCursor != null && expenseCursor.moveToFirst()) {
            monthlyExpenses = expenseCursor.getDouble(0);
            expenseCursor.close();
        }

        double monthlyProfit = monthlyIncome - monthlyExpenses;

        // Format month name
        String[] parts = monthYear.split("-");
        String year = parts[0];
        String monthNumber = parts[1];
        String monthName = getMonthName(Integer.parseInt(monthNumber));

        return new MonthlyProfit(monthYear, monthName, year, monthlyIncome, monthlyExpenses, monthlyProfit);
    }

    private String getMonthName(int monthNumber) {
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        return months[monthNumber - 1];
    }

    private void filterProfitData() {
        List<MonthlyProfit> filteredList = new ArrayList<>();
        String searchQuery = etSearch.getText().toString().toLowerCase();

        for (MonthlyProfit profit : monthlyProfits) {
            boolean yearMatch = selectedYear.equals("All") || profit.getYear().equals(selectedYear);
            boolean monthMatch = selectedMonth.equals("All") || profit.getMonthName().equals(selectedMonth);
            boolean searchMatch = searchQuery.isEmpty() ||
                    profit.getMonthName().toLowerCase().contains(searchQuery) ||
                    profit.getYear().contains(searchQuery);

            if (yearMatch && monthMatch && searchMatch) {
                filteredList.add(profit);
            }
        }

        displayProfitData(filteredList);
    }

    private void displayProfitData(List<MonthlyProfit> profits) {
        profitContainer.removeAllViews();

        if (profits.isEmpty()) {
            TextView noData = new TextView(this);
            noData.setText("No profit data found for the selected filters");
            noData.setTextSize(16);
            noData.setPadding(32, 32, 32, 32);
            noData.setGravity(View.TEXT_ALIGNMENT_CENTER);
            profitContainer.addView(noData);
            return;
        }

        double totalIncome = 0;
        double totalExpenses = 0;
        double totalProfit = 0;

        for (MonthlyProfit profit : profits) {
            CardView profitCard = createProfitCard(profit);
            profitContainer.addView(profitCard);

            totalIncome += profit.getIncome();
            totalExpenses += profit.getExpenses();
            totalProfit += profit.getProfit();
        }

        // Update totals
        tvTotalIncome.setText(currencyFormat.format(totalIncome));
        tvTotalExpenses.setText(currencyFormat.format(totalExpenses));
        tvTotalProfit.setText(currencyFormat.format(totalProfit));

        // Update profit color
        if (totalProfit > 0) {
            tvTotalProfit.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else if (totalProfit < 0) {
            tvTotalProfit.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            tvTotalProfit.setTextColor(getResources().getColor(android.R.color.black));
        }
    }

    private CardView createProfitCard(MonthlyProfit profit) {
        // Inflate the card layout
        CardView cardView = (CardView) getLayoutInflater().inflate(R.layout.item_profit_card, null);

        // Get references to card views
        TextView tvMonthYear = cardView.findViewById(R.id.tvMonthYear);
        TextView tvIncome = cardView.findViewById(R.id.tvIncome);
        TextView tvExpenses = cardView.findViewById(R.id.tvExpenses);
        TextView tvProfit = cardView.findViewById(R.id.tvProfit);
        TextView tvProfitPercentage = cardView.findViewById(R.id.tvProfitPercentage);
        View profitIndicator = cardView.findViewById(R.id.profitIndicator);

        // Set data
        tvMonthYear.setText(profit.getMonthName() + " " + profit.getYear());
        tvIncome.setText(currencyFormat.format(profit.getIncome()));
        tvExpenses.setText(currencyFormat.format(profit.getExpenses()));
        tvProfit.setText(currencyFormat.format(profit.getProfit()));

        // Calculate profit percentage
        double profitPercentage = 0;
        if (profit.getIncome() > 0) {
            profitPercentage = (profit.getProfit() / profit.getIncome()) * 100;
        }
        tvProfitPercentage.setText(String.format(Locale.getDefault(), "%.1f%%", profitPercentage));

        // Set colors based on profit
        if (profit.getProfit() > 0) {
            // Profit - green
            tvProfit.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            tvProfitPercentage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            profitIndicator.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
        } else if (profit.getProfit() < 0) {
            // Loss - red
            tvProfit.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            tvProfitPercentage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            profitIndicator.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            // Break-even - gray
            tvProfit.setTextColor(getResources().getColor(android.R.color.darker_gray));
            tvProfitPercentage.setTextColor(getResources().getColor(android.R.color.darker_gray));
            profitIndicator.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        }

        // Add click listener for more details
        cardView.setOnClickListener(v -> showMonthlyDetails(profit));

        return cardView;
    }

    private void showMonthlyDetails(MonthlyProfit profit) {
        Intent intent = new Intent(this, MonthlyProfitDetailsActivity.class);
        intent.putExtra("month_year", profit.getMonthYear());
        intent.putExtra("month_name", profit.getMonthName());
        intent.putExtra("year", profit.getYear());
        startActivity(intent);
    }

    private void updateSummary() {
        if (monthlyProfits.isEmpty()) {
            tvAnalysisSummary.setText("No profit data available for analysis");
            return;
        }

        // Calculate statistics
        int profitableMonths = 0;
        int lossMonths = 0;
        double bestProfit = Double.MIN_VALUE;
        double worstProfit = Double.MAX_VALUE;
        String bestMonth = "", worstMonth = "";

        for (MonthlyProfit profit : monthlyProfits) {
            if (profit.getProfit() > 0) {
                profitableMonths++;
            } else if (profit.getProfit() < 0) {
                lossMonths++;
            }

            if (profit.getProfit() > bestProfit) {
                bestProfit = profit.getProfit();
                bestMonth = profit.getMonthName() + " " + profit.getYear();
            }

            if (profit.getProfit() < worstProfit) {
                worstProfit = profit.getProfit();
                worstMonth = profit.getMonthName() + " " + profit.getYear();
            }
        }

        String summary = String.format(Locale.getDefault(),
                "📊 Analysis Summary:\n" +
                        "• Profitable Months: %d\n" +
                        "• Loss Months: %d\n" +
                        "• Best Performance: %s (%s)\n" +
                        "• Needs Attention: %s (%s)",
                profitableMonths, lossMonths,
                bestMonth, currencyFormat.format(bestProfit),
                worstMonth, currencyFormat.format(worstProfit));

        tvAnalysisSummary.setText(summary);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }

    // Monthly Profit data class
    private static class MonthlyProfit {
        private String monthYear;
        private String monthName;
        private String year;
        private double income;
        private double expenses;
        private double profit;

        public MonthlyProfit(String monthYear, String monthName, String year,
                             double income, double expenses, double profit) {
            this.monthYear = monthYear;
            this.monthName = monthName;
            this.year = year;
            this.income = income;
            this.expenses = expenses;
            this.profit = profit;
        }

        public String getMonthYear() { return monthYear; }
        public String getMonthName() { return monthName; }
        public String getYear() { return year; }
        public double getIncome() { return income; }
        public double getExpenses() { return expenses; }
        public double getProfit() { return profit; }
    }
}