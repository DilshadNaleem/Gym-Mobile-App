package com.example.kolonnawabarbellgym.Model;

public class Member
{
    private String uniqueId;
    private String name;
    private byte[] profileImage;
    private double monthlyFee;
    private String lastPaymentMonth;
    private int unpaidMonths;
    private double totalDue;

    public Member() {}

    public Member(String uniqueId, String name, byte[] profileImage, double monthlyFee,
                  String lastPaymentMonth, int unpaidMonths, double totalDue) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.profileImage = profileImage;
        this.monthlyFee = monthlyFee;
        this.lastPaymentMonth = lastPaymentMonth;
        this.unpaidMonths = unpaidMonths;
        this.totalDue = totalDue;
    }

    // Getters and Setters
    public String getUniqueId() { return uniqueId; }
    public void setUniqueId(String uniqueId) { this.uniqueId = uniqueId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public byte[] getProfileImage() { return profileImage; }
    public void setProfileImage(byte[] profileImage) { this.profileImage = profileImage; }

    public double getMonthlyFee() { return monthlyFee; }
    public void setMonthlyFee(double monthlyFee) { this.monthlyFee = monthlyFee; }

    public String getLastPaymentMonth() { return lastPaymentMonth; }
    public void setLastPaymentMonth(String lastPaymentMonth) { this.lastPaymentMonth = lastPaymentMonth; }

    public int getUnpaidMonths() { return unpaidMonths; }
    public void setUnpaidMonths(int unpaidMonths) { this.unpaidMonths = unpaidMonths; }

    public double getTotalDue() { return totalDue; }
    public void setTotalDue(double totalDue) { this.totalDue = totalDue; }
}
