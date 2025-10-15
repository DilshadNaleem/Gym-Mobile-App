package com.example.kolonnawabarbellgym.DTO;

public class User
{
    private int userid;
    private String unique_id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String nic;
    private String password;
    private int status;
    private String loggedIn;

    // Constructors
    public User() {}

    public User(String firstName, String lastName, String email, String phoneNumber, String nic, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.nic = nic;
        this.password = password;
        this.status = 0; // Default status
        this.loggedIn = "unverify"; // Default loggedIn status
    }

    // Getters and Setters
    public int getUserid() { return userid; }
    public void setUserid(int userid) { this.userid = userid; }

    public String getUnique_id() { return unique_id; }
    public void setUnique_id(String unique_id) { this.unique_id = unique_id; }

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

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getLoggedIn() { return loggedIn; }
    public void setLoggedIn(String loggedIn) { this.loggedIn = loggedIn; }
}
