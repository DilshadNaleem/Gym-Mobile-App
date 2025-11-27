package com.example.kolonnawabarbellgym.Model;

import android.graphics.Bitmap;

public class MemberSales {
    private String uniqueId;
    private String name;
    private byte[] profileImage;
    private double admissionFee;
    private String joinDate;
    private String status;

    public MemberSales(String uniqueId, String name, byte[] profileImage, double admissionFee, String joinDate, String status) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.profileImage = profileImage;
        this.admissionFee = admissionFee;
        this.joinDate = joinDate;
        this.status = status;
    }

    // Getters and Setters
    public String getUniqueId() { return uniqueId; }
    public void setUniqueId(String uniqueId) { this.uniqueId = uniqueId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public byte[] getProfileImage() { return profileImage; }
    public void setProfileImage(byte[] profileImage) { this.profileImage = profileImage; }

    public double getAdmissionFee() { return admissionFee; }
    public void setAdmissionFee(double admissionFee) { this.admissionFee = admissionFee; }

    public String getJoinDate() { return joinDate; }
    public void setJoinDate(String joinDate) { this.joinDate = joinDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}