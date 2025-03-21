package com.example.ruralhealthcareconnect;

public class User {
    public String username;
    public String password;
    public String role;
    public String name;
    public String specialization; // Only for Doctors
    public int age; // Only for Patients

    // Default Constructor (Required for Firebase)
    public User() {
    }

    // Constructor for Doctors
    public User(String username, String password, String role, String name, String specialization) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.name = name;
        this.specialization = specialization;
        this.age = -1; // Not applicable for doctors
    }

    // Constructor for Patients
    public User(String username, String password, String role, String name, int age) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.name = name;
        this.age = age;
        this.specialization = null; // Not applicable for patients
    }
}