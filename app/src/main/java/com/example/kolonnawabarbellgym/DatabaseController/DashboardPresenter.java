package com.example.kolonnawabarbellgym.DatabaseController;

import com.example.kolonnawabarbellgym.Model.SalesData;
import com.example.kolonnawabarbellgym.Repository.DashboardRepo;

public class DashboardPresenter {
    private DashboardView view;
    private DashboardRepo repository;
    private SalesData currentSalesData;

    public DashboardPresenter(DashboardView view, DashboardRepo repository) {
        this.view = view;
        this.repository = repository;
    }

    public void loadDashboardData() {
        if (view != null) {
            view.showLoading();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SalesData salesData = repository.getSalesData();
                    currentSalesData = salesData; // Store the current data

                    if (view != null) {
                        view.displaySalesData(salesData);
                        view.hideLoading();
                    }
                } catch (Exception e) {
                    if (view != null) {
                        view.showError("Failed to load dashboard data: " + e.getMessage());
                        view.hideLoading();
                    }
                }
            }
        }).start();
    }

    // Profit calculation methods
    public double getTodaySales() {
        if (currentSalesData != null) {
            return currentSalesData.getTodaySales();
        }
        return 0.0;
    }

    public double getTotalSales() {
        if (currentSalesData != null) {
            return currentSalesData.getTotalSales();
        }
        return 0.0;
    }

    public double getTodayExpenses() {
        if (currentSalesData != null) {
            return currentSalesData.getTodayExpenses();
        }
        return 0.0;
    }

    public double getTotalExpenses() {
        if (currentSalesData != null) {
            return currentSalesData.getTotalExpenses();
        }
        return 0.0;
    }

    public double getTodayProfit() {
        if (currentSalesData != null) {
            return currentSalesData.getTodayProfit();
        }
        return 0.0;
    }

    public double getTotalProfit() {
        if (currentSalesData != null) {
            return currentSalesData.getTotalProfit();
        }
        return 0.0;
    }

    // Method to get profit breakdown data
    public ProfitBreakdown getTodayProfitBreakdown() {
        return new ProfitBreakdown(
                "Today's Profit Breakdown",
                getTodaySales(),
                getTodayExpenses(),
                getTodayProfit()
        );
    }

    public ProfitBreakdown getTotalProfitBreakdown() {
        return new ProfitBreakdown(
                "Total Profit Breakdown",
                getTotalSales(),
                getTotalExpenses(),
                getTotalProfit()
        );
    }

    public void detachView() {
        this.view = null;
    }

    public interface DashboardView {
        void displaySalesData(SalesData salesData);
        void showError(String message);
        void showLoading();
        void hideLoading();
    }

    // Profit breakdown data class
    public static class ProfitBreakdown {
        private String title;
        private double income;
        private double expenses;
        private double profit;

        public ProfitBreakdown(String title, double income, double expenses, double profit) {
            this.title = title;
            this.income = income;
            this.expenses = expenses;
            this.profit = profit;
        }

        public String getTitle() { return title; }
        public double getIncome() { return income; }
        public double getExpenses() { return expenses; }
        public double getProfit() { return profit; }
    }
}