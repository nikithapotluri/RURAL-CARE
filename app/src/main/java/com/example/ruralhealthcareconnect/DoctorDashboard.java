package com.example.ruralhealthcareconnect;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DoctorDashboard extends AppCompatActivity {

    private ListView lvUnresolvedIssues;
    private EditText etSolution;
    private Button btnResolveIssue;
    private String selectedUsername = null; // To keep track of selected username
    private String selectedIssue = null; // To keep track of selected issue
    private ArrayList<Issue> unresolvedIssues;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Issues");
    private TextView tvPatientPastIssues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dashboard);

        lvUnresolvedIssues = findViewById(R.id.lvUnresolvedIssues);
        etSolution = findViewById(R.id.etSolution);
        btnResolveIssue = findViewById(R.id.btnResolveIssue);
        tvPatientPastIssues = findViewById(R.id.tvPatientPastIssues);

        // Fetch unresolved issues from Firebase asynchronously
        getUnresolvedIssues(new IssueCallback() {
            @Override
            public void onIssuesFetched(ArrayList<Issue> issues) {
                unresolvedIssues = issues;
                // Use a custom adapter to display issues
                IssueAdapter adapter = new IssueAdapter(DoctorDashboard.this, unresolvedIssues);
                lvUnresolvedIssues.setAdapter(adapter);
            }
        });

        // Set up item click listener
        lvUnresolvedIssues.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Issue selectedIssueObj = unresolvedIssues.get(position);
                selectedUsername = selectedIssueObj.username;  // Store the username
                selectedIssue = selectedIssueObj.issue;        // Store the actual issue

                // Fetch the patient's past issues based on username
                getPastIssues(selectedUsername);
            }
        });

        // Handle resolve issue button click
        btnResolveIssue.setOnClickListener(v -> {
            String solution = etSolution.getText().toString().trim();
            if (selectedUsername != null && selectedIssue != null && !solution.isEmpty()) {
                // Update the issue with the solution and mark it as resolved
                resolveIssue(selectedUsername, selectedIssue, solution);
                Toast.makeText(DoctorDashboard.this, "Issue resolved!", Toast.LENGTH_SHORT).show();
                etSolution.setText(""); // Clear the solution text
                selectedUsername = null; // Reset the selected username
                selectedIssue = null; // Reset the selected issue
            } else {
                Toast.makeText(DoctorDashboard.this, "Please select an issue and enter a solution", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Fetch unresolved issues from Firebase
    public void getUnresolvedIssues(final IssueCallback callback) {
        databaseReference.orderByChild("resolved").equalTo(false).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Issue> unresolvedIssues = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Issue issue = snapshot.getValue(Issue.class);
                    unresolvedIssues.add(issue);
                }
                callback.onIssuesFetched(unresolvedIssues);  // Callback to return issues
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

    // Fetch past issues of the patient based on the username
    public void getPastIssues(String username) {
        DatabaseReference patientRef = FirebaseDatabase.getInstance().getReference("Issues");
        patientRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                StringBuilder pastIssues = new StringBuilder();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Issue issue = snapshot.getValue(Issue.class);
                    // Display past issues and their solutions
                    pastIssues.append("Issue: ").append(issue.issue).append("\nSolution: ").append(issue.solution).append("\n\n");
                }
                if (pastIssues.length() == 0) {
                    tvPatientPastIssues.setText("No past issues");
                } else {
                    tvPatientPastIssues.setText(pastIssues.toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

    // Mark the issue as resolved and store the solution
    public void resolveIssue(String username, String issue, String solution) {
        DatabaseReference issueRef = databaseReference;
        issueRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Issue issueData = snapshot.getValue(Issue.class);
                    if (issueData != null && issueData.issue.equals(issue)) {
                        String issueId = snapshot.getKey(); // Get the unique ID for the issue
                        // Update the issue with the solution and mark it as resolved
                        issueRef.child(issueId).child("resolved").setValue(true);
                        issueRef.child(issueId).child("solution").setValue(solution);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

    // Callback interface for fetching issues asynchronously
    public interface IssueCallback {
        void onIssuesFetched(ArrayList<Issue> unresolvedIssues);
    }

    // Custom adapter to display the issues
    public class IssueAdapter extends ArrayAdapter<Issue> {
        private Context context;
        private ArrayList<Issue> issues;

        public IssueAdapter(Context context, ArrayList<Issue> issues) {
            super(context, 0, issues);
            this.context = context;
            this.issues = issues;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
            }

            Issue currentIssue = issues.get(position);
            TextView textView = convertView.findViewById(android.R.id.text1);
            textView.setText("Patient: " + currentIssue.username + "\nIssue: " + currentIssue.issue);  // Display patient and issue text

            return convertView;
        }
    }
}
