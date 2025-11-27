package com.example.kolonnawabarbellgym.Model;

import java.util.Date;

public class Payment {
    private int id;
    private String uniqueId;
    private double price;
    private String month;
    private String paymentType;
    private Date createdAt;
    private String memberName;

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUniqueId() { return uniqueId; }
    public void setUniqueId(String uniqueId) { this.uniqueId = uniqueId; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
}