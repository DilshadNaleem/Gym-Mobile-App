package com.example.kolonnawabarbellgym.Model;

import java.util.Date;

public class ExpenseDetail {
    private int expenseId;
    private String description;
    private double amount;
    private Date date;
    private byte[] image;

    public ExpenseDetail(int expenseId, String description, double amount, Date date, byte[] image) {
        this.expenseId = expenseId;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.image = image;
    }

    // Getters
    public int getExpenseId() { return expenseId; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public Date getDate() { return date; }
    public byte[] getImage() { return image; }
}