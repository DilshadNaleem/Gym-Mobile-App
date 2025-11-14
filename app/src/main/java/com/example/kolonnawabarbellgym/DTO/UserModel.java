package com.example.kolonnawabarbellgym.DTO;

public class UserModel
{
    private String uniqueId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String nic;
    private byte[] profileImage;
    private String monthlyFee;
    private String createdTime;

    public UserModel() {
    }

    public UserModel(String uniqueId, String firstName, String lastName, String email,
                     String phoneNumber, String nic, byte[] profileImage, String monthlyFee, String createdTime) {
        this.uniqueId = uniqueId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.nic = nic;
        this.profileImage = profileImage;
        this.monthlyFee = monthlyFee;
        this.createdTime = createdTime;
    }

    // Getters and Setters
    public String getUniqueId() { return uniqueId; }
    public void setUniqueId(String uniqueId) { this.uniqueId = uniqueId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }

    public byte[] getProfileImage() { return profileImage; }
    public void setProfileImage(byte[] profileImage) { this.profileImage = profileImage; }

    public String getMonthlyFee() { return monthlyFee; }
    public void setMonthlyFee(String monthlyFee) { this.monthlyFee = monthlyFee; }

    public String getCreatedTime() { return createdTime; }
    public void setCreatedTime(String createdTime) { this.createdTime = createdTime; }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
