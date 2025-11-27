package com.example.kolonnawabarbellgym;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.kolonnawabarbellgym.Database.DatabaseHelperClass;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AllExpensesActivity extends AppCompatActivity {

    private DatabaseHelperClass databaseHelper;
    private LinearLayout expensesContainer;
    private EditText etSearch;
    private TextView tvNoExpenses, tvTotalExpenses, tvExpensesCount;
    private String userEmail;

    private List<Expense> allExpensesList = new ArrayList<>();
    private NumberFormat currencyFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_expenses);

        initializeViews();
        setupCurrencyFormat();

        // Get user email from intent
        userEmail = getIntent().getStringExtra("remail");

        // Initialize database helper
        databaseHelper = new DatabaseHelperClass(this);

        loadAllExpenses();
        setupSearch();
    }

    private void initializeViews() {
        expensesContainer = findViewById(R.id.expensesContainer);
        etSearch = findViewById(R.id.etSearch);
        tvNoExpenses = findViewById(R.id.tvNoExpenses);
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses);
        tvExpensesCount = findViewById(R.id.tvExpensesCount);
    }

    private void setupCurrencyFormat() {
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("si", "LK"));
        currencyFormat.setMaximumFractionDigits(2);
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

    private void loadAllExpenses() {
        allExpensesList.clear();
        expensesContainer.removeAllViews();

        Cursor cursor = databaseHelper.getAllExpenses();

        if (cursor != null && cursor.moveToFirst()) {
            double totalAmount = 0;
            int expensesCount = 0;

            do {
                int expenseId = cursor.getInt(cursor.getColumnIndexOrThrow("expense_id"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
                String createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at"));
                byte[] imageBytes = cursor.getBlob(cursor.getColumnIndexOrThrow("image"));

                Expense expense = new Expense(expenseId, description, price, createdAt, imageBytes);
                allExpensesList.add(expense);
                totalAmount += price;
                expensesCount++;

            } while (cursor.moveToNext());

            cursor.close();

            // Display all expenses
            displayExpenses(allExpensesList);

            // Update totals
            tvTotalExpenses.setText(currencyFormat.format(totalAmount));
            tvExpensesCount.setText(String.format(Locale.getDefault(), "Total Expenses: %d", expensesCount));
            tvNoExpenses.setVisibility(View.GONE);
            expensesContainer.setVisibility(View.VISIBLE);

        } else {
            tvNoExpenses.setVisibility(View.VISIBLE);
            expensesContainer.setVisibility(View.GONE);
            tvTotalExpenses.setText(currencyFormat.format(0));
            tvExpensesCount.setText("Total Expenses: 0");
            Toast.makeText(this, "No expenses found", Toast.LENGTH_SHORT).show();
        }
    }

    private void filterExpenses(String query) {
        List<Expense> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList.addAll(allExpensesList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Expense expense : allExpensesList) {
                if (expense.getDescription().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(expense);
                }
            }
        }

        displayExpenses(filteredList);

        // Update filtered count
        if (!query.isEmpty()) {
            tvExpensesCount.setText(String.format(Locale.getDefault(), "Showing: %d expenses", filteredList.size()));
        } else {
            tvExpensesCount.setText(String.format(Locale.getDefault(), "Total Expenses: %d", allExpensesList.size()));
        }
    }

    private void displayExpenses(List<Expense> expenses) {
        expensesContainer.removeAllViews();

        if (expenses.isEmpty()) {
            tvNoExpenses.setVisibility(View.VISIBLE);
            expensesContainer.setVisibility(View.GONE);
            return;
        }

        tvNoExpenses.setVisibility(View.GONE);
        expensesContainer.setVisibility(View.VISIBLE);

        for (Expense expense : expenses) {
            CardView expenseCard = createExpenseCard(expense);
            expensesContainer.addView(expenseCard);
        }
    }

    private CardView createExpenseCard(Expense expense) {
        // Inflate the card layout
        CardView cardView = (CardView) getLayoutInflater().inflate(R.layout.item_expense_card, null);

        // Get references to card views
        TextView tvDescription = cardView.findViewById(R.id.tvExpenseDescription);
        TextView tvAmount = cardView.findViewById(R.id.tvExpenseAmount);
        TextView tvTime = cardView.findViewById(R.id.tvExpenseTime);
        TextView tvDate = cardView.findViewById(R.id.tvExpenseDate);
        ImageView ivExpenseImage = cardView.findViewById(R.id.ivExpenseImage);
        LinearLayout imageContainer = cardView.findViewById(R.id.imageContainer);
        LinearLayout dateContainer = cardView.findViewById(R.id.dateContainer);

        // Set expense data
        tvDescription.setText(expense.getDescription());
        tvAmount.setText(currencyFormat.format(expense.getPrice()));

        // Format date and time
        String[] dateTime = formatDateTime(expense.getCreatedAt());
        tvDate.setText(dateTime[0]); // Date
        tvTime.setText(dateTime[1]); // Time

        // Show date container for all expenses
        dateContainer.setVisibility(View.VISIBLE);

        // Handle image
        if (expense.getImageBytes() != null && expense.getImageBytes().length > 0) {
            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(expense.getImageBytes(), 0, expense.getImageBytes().length);
                ivExpenseImage.setImageBitmap(bitmap);
                imageContainer.setVisibility(View.VISIBLE);

                // Add click listener to view full image
                ivExpenseImage.setOnClickListener(v -> viewFullImage(expense.getImageBytes()));

            } catch (Exception e) {
                Log.e("AllExpenses", "Error loading expense image: " + e.getMessage());
                imageContainer.setVisibility(View.GONE);
            }
        } else {
            imageContainer.setVisibility(View.GONE);
        }

        // Add long click listener for potential future features (delete, edit, etc.)
        cardView.setOnLongClickListener(v -> {
            Toast.makeText(this, "Expense: " + expense.getDescription(), Toast.LENGTH_SHORT).show();
            return true;
        });

        return cardView;
    }

    private String[] formatDateTime(String createdAt) {
        String[] dateTime = new String[2];
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

            Date date = inputFormat.parse(createdAt);
            dateTime[0] = dateFormat.format(date);
            dateTime[1] = timeFormat.format(date);
        } catch (Exception e) {
            dateTime[0] = "Unknown Date";
            dateTime[1] = createdAt;
        }
        return dateTime;
    }

    private void viewFullImage(byte[] imageBytes) {
        Intent intent = new Intent(this, FullScreenImageActivity.class);
        intent.putExtra("image_bytes", imageBytes);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllExpenses(); // Refresh data when returning to activity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }

    // Expense model class
    private static class Expense {
        private int expenseId;
        private String description;
        private double price;
        private String createdAt;
        private byte[] imageBytes;

        public Expense(int expenseId, String description, double price, String createdAt, byte[] imageBytes) {
            this.expenseId = expenseId;
            this.description = description;
            this.price = price;
            this.createdAt = createdAt;
            this.imageBytes = imageBytes;
        }

        public int getExpenseId() { return expenseId; }
        public String getDescription() { return description; }
        public double getPrice() { return price; }
        public String getCreatedAt() { return createdAt; }
        public byte[] getImageBytes() { return imageBytes; }
    }
}