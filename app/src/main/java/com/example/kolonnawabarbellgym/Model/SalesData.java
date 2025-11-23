package com.example.kolonnawabarbellgym.Model;

public class SalesData {
    private double todaySales;
    private double totalSales;
    private double pendingAdmissionFees;
    private double existingMembersFees;
    private int totalMembers;
    private int paidMembers;
    private int pendingMembers;

    public SalesData() {}

    // Getters and Setters
    public double getTodaySales() { return todaySales; }
    public void setTodaySales(double todaySales) { this.todaySales = todaySales; }

    public double getTotalSales() { return totalSales; }
    public void setTotalSales(double totalSales) { this.totalSales = totalSales; }

    public double getPendingAdmissionFees() { return pendingAdmissionFees; }
    public void setPendingAdmissionFees(double pendingAdmissionFees) { this.pendingAdmissionFees = pendingAdmissionFees; }

    public double getExistingMembersFees() { return existingMembersFees; }
    public void setExistingMembersFees(double existingMembersFees) { this.existingMembersFees = existingMembersFees; }

    public int getTotalMembers() { return totalMembers; }
    public void setTotalMembers(int totalMembers) { this.totalMembers = totalMembers; }

    public int getPaidMembers() { return paidMembers; }
    public void setPaidMembers(int paidMembers) { this.paidMembers = paidMembers; }

    public int getPendingMembers() { return pendingMembers; }
    public void setPendingMembers(int pendingMembers) { this.pendingMembers = pendingMembers; }
}