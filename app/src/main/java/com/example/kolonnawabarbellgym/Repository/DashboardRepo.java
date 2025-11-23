package com.example.kolonnawabarbellgym.Repository;

import com.example.kolonnawabarbellgym.Model.SalesData;

public interface DashboardRepo
{
    SalesData getSalesData();
    void refreshData();
}
