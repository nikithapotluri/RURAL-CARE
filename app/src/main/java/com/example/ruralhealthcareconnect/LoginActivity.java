package com.example.ruralhealthcareconnect;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword, etName, etAge, etSpecialization;
    private RadioGroup userTypeGroup;
    private RadioButton rbPatient, rbDoctor;
    private Button btnRegister, btnLogin;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize Views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etSpecialization = findViewById(R.id.etSpecialization);
        userTypeGroup = findViewById(R.id.userTypeGroup);
        rbPatient = findViewById(R.id.rbPatient);
        rbDoctor = findViewById(R.id.rbDoctor);
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);

        // Toggle Age/Specialization fields based on user type selection
        rbPatient.setOnClickListener(v -> {
            etAge.setVisibility(View.VISIBLE);
            etSpecialization.setVisibility(View.GONE);
        });

        rbDoctor.setOnClickListener(v -> {
            etAge.setVisibility(View.GONE);
            etSpecialization.setVisibility(View.VISIBLE);
        });

        // Register Button Click
        btnRegister.setOnClickListener(v -> registerUser());

        // Login Button Click
        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String name = etName.getText().toString().trim();

        // Check if all required fields are filled
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_LONG).show();
            return;
        }

        final String role;
        final String ageOrSpecialization;

        // Check the user type and assign the role and other fields accordingly
        if (rbPatient.isChecked()) {
            role = "Patient";  // Set role to Patient
            // Make sure to parse the age correctly
            try {
                ageOrSpecialization = etAge.getText().toString().trim();
                if (TextUtils.isEmpty(ageOrSpecialization)) {
                    Toast.makeText(this, "Please enter your age", Toast.LENGTH_LONG).show();
                    return;
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error parsing age", Toast.LENGTH_LONG).show();
                return;
            }
        } else if (rbDoctor.isChecked()) {
            role = "Doctor";  // Set role to Doctor
            ageOrSpecialization = etSpecialization.getText().toString().trim();  // Set specialization for Doctor
            if (TextUtils.isEmpty(ageOrSpecialization)) {
                Toast.makeText(this, "Please enter your specialization", Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            // If no role is selected, show an error
            Toast.makeText(this, "Please select your role", Toast.LENGTH_LONG).show();
            return;
        }

        // Check if username already exists in the database
        databaseReference.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // If username already exists
                            Toast.makeText(LoginActivity.this, "Username already exists!", Toast.LENGTH_LONG).show();
                        } else {
                            // If username is unique, create a new user entry
                            String userId = databaseReference.push().getKey();

                            // Determine if the user is a Doctor or Patient and create the User object accordingly
                            User user;
                            if (role.equals("Doctor")) {
                                user = new User(username, password, role, name, ageOrSpecialization); // Doctor constructor
                            } else {
                                user = new User(username, password, role, name, Integer.parseInt(ageOrSpecialization)); // Patient constructor
                            }

                            // Save user to Firebase
                            databaseReference.child(userId).setValue(user)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            // Registration success
                                            Toast.makeText(LoginActivity.this, "Registration successful!", Toast.LENGTH_LONG).show();
                                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                            finish();
                                        } else {
                                            // Registration failed
                                            Toast.makeText(LoginActivity.this, "Registration failed!", Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(LoginActivity.this, "Error checking username!", Toast.LENGTH_LONG).show();
                    }
                });
    }




    private void loginUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                User user = snapshot.getValue(User.class);
                                if (user != null && user.password.equals(password)) {
                                    Intent intent = user.role.equals("Doctor") ?
                                            new Intent(LoginActivity.this, DoctorDashboard.class) :
                                            new Intent(LoginActivity.this, PatientDashboard.class);
                                    intent.putExtra("USERNAME", username);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Incorrect password!", Toast.LENGTH_SHORT).show();
                                }
                                return;
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "User not found!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(LoginActivity.this, "Error fetching data!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
