package com.example.kolonnawabarbellgym.Model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OtherAmount {
    private int paymentId;
    private String uniqueId;
    private String firstName;
    private String lastName;
    private String month;
    private double price;
    private String handoveredTo;
    private String sessionedEmail;
    private String createdAt;
    private String description;
    private double otherAmount;
    private byte[] profileImage;

    public OtherAmount() {}

    public OtherAmount(int paymentId, String uniqueId, String firstName,
                       String month, double price, String handoveredTo, String sessionedEmail,
                       String createdAt, String description, double otherAmount, byte[] profileImage) {
        this.paymentId = paymentId;
        this.uniqueId = uniqueId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.month = month;
        this.price = price;
        this.handoveredTo = handoveredTo;
        this.sessionedEmail = sessionedEmail;
        this.createdAt = createdAt;
        this.description = description;
        this.otherAmount = otherAmount;
        this.profileImage = profileImage;
    }

    // Getters and Setters
    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    public String getUniqueId() { return uniqueId; }
    public void setUniqueId(String uniqueId) { this.uniqueId = uniqueId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getHandoveredTo() { return handoveredTo; }
    public void setHandoveredTo(String handoveredTo) { this.handoveredTo = handoveredTo; }

    public String getSessionedEmail() { return sessionedEmail; }
    public void setSessionedEmail(String sessionedEmail) { this.sessionedEmail = sessionedEmail; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getOtherAmount() { return otherAmount; }
    public void setOtherAmount(double otherAmount) { this.otherAmount = otherAmount; }

    public byte[] getProfileImage() { return profileImage; }
    public void setProfileImage(byte[] profileImage) { this.profileImage = profileImage; }

    public String getFormattedDate() {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            Date date = inputFormat.parse(createdAt);
            return outputFormat.format(date);
        } catch (Exception e) {
            return createdAt;
        }
    }


}