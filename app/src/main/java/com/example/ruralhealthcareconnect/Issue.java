package com.example.ruralhealthcareconnect;

public class Issue {
    public String username;
    public String issue;
    public boolean resolved;
    public String solution;  // New field for the doctor's solution

    public Issue() {
        // Default constructor required for Firebase
    }

    public Issue(String username, String issue) {
        this.username = username;
        this.issue = issue;
        this.resolved = false; // Initially, the issue is unresolved
        this.solution = null;  // No solution initially
    }

    // Getters and setters for all fields
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }
}

