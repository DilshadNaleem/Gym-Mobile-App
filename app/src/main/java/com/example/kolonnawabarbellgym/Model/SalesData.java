package com.example.kolonnawabarbellgym.Model;

public class SalesData {
    private double todaySales;
    private double totalSales; // This will now represent Total Admission Price
    private double totalAmountOfPrices; // New field for total payments
    private double pendingAdmissionFees;
    private double existingMembersFees;
    private int totalMembers;
    private int paidMembers;
    private int pendingMembers;
    private double otherAmount;
    private double todayExpenses;
    private double totalExpenses;
    private double todayProfit;
    private double totalProfit;


    public double getTodayProfit() {
        return todayProfit;
    }

    public void setTodayProfit(double todayProfit) {
        this.todayProfit = todayProfit;
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(double totalProfit) {
        this.totalProfit = totalProfit;
    }

    public double getTodayExpenses() {
        return todayExpenses;
    }

    public void setTodayExpenses(double todayExpenses) {
        this.todayExpenses = todayExpenses;
    }

    public double getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(double totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public double getOtherAmount() {
        return otherAmount;
    }

    public void setOtherAmount(double otherAmount) {
        this.otherAmount = otherAmount;
    }

    // Getters and Setters
    public double getTodaySales() {
        return todaySales;
    }

    public void setTodaySales(double todaySales) {
        this.todaySales = todaySales;
    }

    public double getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(double totalSales) {
        this.totalSales = totalSales;
    }

    public double getTotalAmountOfPrices() {
        return totalAmountOfPrices;
    }

    public void setTotalAmountOfPrices(double totalAmountOfPrices) {
        this.totalAmountOfPrices = totalAmountOfPrices;
    }

    public double getPendingAdmissionFees() {
        return pendingAdmissionFees;
    }

    public void setPendingAdmissionFees(double pendingAdmissionFees) {
        this.pendingAdmissionFees = pendingAdmissionFees;
    }

    public double getExistingMembersFees() {
        return existingMembersFees;
    }

    public void setExistingMembersFees(double existingMembersFees) {
        this.existingMembersFees = existingMembersFees;
    }

    public int getTotalMembers() {
        return totalMembers;
    }

    public void setTotalMembers(int totalMembers) {
        this.totalMembers = totalMembers;
    }

    public int getPaidMembers() {
        return paidMembers;
    }

    public void setPaidMembers(int paidMembers) {
        this.paidMembers = paidMembers;
    }

    public int getPendingMembers() {
        return pendingMembers;
    }

    public void setPendingMembers(int pendingMembers) {
        this.pendingMembers = pendingMembers;
    }
}