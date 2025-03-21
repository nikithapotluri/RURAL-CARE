package com.example.ruralhealthcareconnect;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class PatientDashboard extends AppCompatActivity {
    private TextView tvUsername, tvDoctorSolutions;
    private EditText etNewIssue;
    private Button btnPostIssue;
    private DatabaseReference databaseReference;
    private String username;
    private LinearLayout layoutDoctorSolutions, layoutNewIssue;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_dashboard);

        // Initialize Views
        tvUsername = findViewById(R.id.tvUsername);
        tvDoctorSolutions = findViewById(R.id.tvDoctorSolutions);
        etNewIssue = findViewById(R.id.etNewIssue);
        btnPostIssue = findViewById(R.id.btnPostIssue);
        tabLayout = findViewById(R.id.tabLayout);

        // Layouts for each tab
        layoutDoctorSolutions = findViewById(R.id.layoutDoctorSolutions);
        layoutNewIssue = findViewById(R.id.layoutNewIssue);

        // Get username from intent
        username = getIntent().getStringExtra("USERNAME");
        tvUsername.setText("Welcome, " + username);

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("Issues");

        // Load doctor solutions
        loadDoctorSolutions();

        // Handle tab switching
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: // Doctor's Solutions
                        layoutDoctorSolutions.setVisibility(View.VISIBLE);
                        layoutNewIssue.setVisibility(View.GONE);
                        break;
                    case 1: // Post Issue
                        layoutDoctorSolutions.setVisibility(View.GONE);
                        layoutNewIssue.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Post a new issue
        btnPostIssue.setOnClickListener(v -> postNewIssue());
    }

    private void loadDoctorSolutions() {
        if (username == null) {
            Toast.makeText(this, "Error: Username not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> solutionsList = new ArrayList<>();

                if (!dataSnapshot.exists()) {
                    tvDoctorSolutions.setText("No solutions found.");
                    return;
                }

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String issueText = snapshot.child("issue").getValue(String.class);
                    Boolean resolved = snapshot.child("resolved").getValue(Boolean.class);
                    String solution = snapshot.child("solution").getValue(String.class);

                    if (issueText == null) continue;

                    // Add resolved or unresolved status
                    if (resolved != null && resolved) {
                        issueText += " (✅ Resolved)";
                    } else {
                        issueText += " (❌ Unresolved)";
                    }

                    // Prepare the display text for the solution
                    String issueWithSolution = "Issue: " + issueText;

                    // Check if a solution is available
                    if (solution != null && !solution.isEmpty()) {
                        issueWithSolution += "\nSolution: " + solution;
                    } else {
                        issueWithSolution += "\nSolution: No solution provided yet.";
                    }

                    // Add the issue and solution pair to the list
                    solutionsList.add(issueWithSolution);
                }

                // Display solutions
                if (solutionsList.isEmpty()) {
                    tvDoctorSolutions.setText("No solutions found.");
                } else {
                    tvDoctorSolutions.setText(String.join("\n\n", solutionsList));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(PatientDashboard.this, "Failed to load solutions: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void postNewIssue() {
        String newIssue = etNewIssue.getText().toString().trim();
        if (newIssue.isEmpty()) {
            Toast.makeText(this, "Please enter an issue", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username == null) {
            Toast.makeText(this, "Error: Username not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate unique issue ID
        String issueId = databaseReference.push().getKey();
        if (issueId == null) {
            Toast.makeText(this, "Error generating issue ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Store issue with username
        Issue issue = new Issue(username, newIssue);
        databaseReference.child(issueId).setValue(issue).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(PatientDashboard.this, "Issue posted successfully!", Toast.LENGTH_SHORT).show();
                etNewIssue.setText("");  // Clear input field
                loadDoctorSolutions();  // Refresh doctor solutions
                tabLayout.getTabAt(0).select(); // Switch back to "Doctor's Solutions" tab
            } else {
                Toast.makeText(PatientDashboard.this, "Failed to post issue", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
