package com.example.kolonnawabarbellgym.DatabaseController;

import com.example.kolonnawabarbellgym.Model.SalesData;
import com.example.kolonnawabarbellgym.Repository.DashboardRepo;

public class DashboardPresenter {
    private DashboardView view;
    private DashboardRepo repository;

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
                    if (view != null) {
                        view.hideLoading();
                        view.displaySalesData(salesData);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (view != null) {
                        view.hideLoading();
                        view.showError("Failed to load dashboard data: " + e.getMessage());
                    }
                }
            }
        }).start();
    }

    public void refreshData() {
        repository.refreshData();
        loadDashboardData();
    }

    public void setView(DashboardView view) {
        this.view = view;
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
}