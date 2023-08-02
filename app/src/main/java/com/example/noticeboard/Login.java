package com.example.noticeboard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Login extends AppCompatActivity {
    private TextInputLayout passwordLayout;
    private Button login;
    private FirebaseAuth firebaseAuth;
    private EditText email, password;
    private TextView forgot_password, register;
    private ProgressDialog loginDialog;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

//        Initializing FirebaseAuth
        firebaseAuth=FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance().getReference("users");
//        Getting Current User
        currentUser=firebaseAuth.getCurrentUser();

        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        passwordLayout=findViewById(R.id.passwordLayout);
        login=findViewById(R.id.login);
        register=findViewById(R.id.account);
        forgot_password=findViewById(R.id.forgot_password);

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String Password=charSequence.toString();
                if(Password.length() >= 6){
                    Pattern passwordPattern=Pattern.compile("[^a-zA-Z0-9]");
                    Matcher matcher=passwordPattern.matcher(Password);
                    boolean isPasswordContainsCharacters=matcher.find();
                    if(isPasswordContainsCharacters){
                        passwordLayout.setHelperText("Strong Password");
                        passwordLayout.setError("");
                    }else{
                        passwordLayout.setHelperText("");
                        passwordLayout.setError("Weak Password, Enter a Special Character");
                        password.requestFocus();
                        return;
                    }
                }else{
                    passwordLayout.setHelperText("Enter Minimum 6 Characters");
                    passwordLayout.setError("");
                    password.requestFocus();
                    return;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        loginDialog = new ProgressDialog(this);
        loginDialog.setCancelable(false);
        loginDialog.setMessage("Logging In...\nPlease wait");

//        Forgot Password TextView
        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, ForgotPassword.class));
                finish();
            }
        });

//        Login Button
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Email = email.getText().toString().trim();
                String Password = password.getText().toString().trim();

                if (Email.isEmpty()) {
                    email.setError("Email is required");
                    email.requestFocus();
                    return;
                } else if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
                    email.setError("Enter a valid email address");
                    email.requestFocus();
                    return;
                } else if (Password.isEmpty()) {
                    password.setError("Password is required");
                    password.requestFocus();
                    return;
                } else if (Password.length() < 6) {
                    password.setError("Password should be at least 6 characters long");
                    password.requestFocus();
                    return;
                }

                loginDialog.show();

                // Authenticate user with Firebase Authentication
                firebaseAuth.signInWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            currentUser = firebaseAuth.getCurrentUser();
                            String userId = currentUser.getUid();

                            // Retrieve user data from Realtime Database
                            databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String role = dataSnapshot.child("role").getValue(String.class);

                                        if (role != null && role.equals("user")) {
                                            // Update the user's email in Firebase Authentication
                                            currentUser.updateEmail(Email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        // Update the user's email in Realtime Database
                                                        databaseReference.child(userId).child("email").setValue(Email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                                                    startActivity(new Intent(Login.this, UserDashboard.class));
                                                                    finish();
                                                                } else {
                                                                    Toast.makeText(Login.this, "Failed to update email", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    } else {
                                                        Toast.makeText(Login.this, "Failed to update email", Toast.LENGTH_SHORT).show();
                                                    }
                                                    loginDialog.dismiss();
                                                }
                                            });
                                        }else if (role != null && role.equals("admin")) {
                                            // Update the user's email in Firebase Authentication
                                            currentUser.updateEmail(Email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        // Update the user's email in Realtime Database
                                                        databaseReference.child(userId).child("email").setValue(Email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                                                    startActivity(new Intent(Login.this, AdminDashboard.class));
                                                                    finish();
                                                                } else {
                                                                    Toast.makeText(Login.this, "Failed to update email", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    } else {
                                                        Toast.makeText(Login.this, "Failed to update email", Toast.LENGTH_SHORT).show();
                                                    }
                                                    loginDialog.dismiss();
                                                }
                                            });
                                        }else if (role != null && role.equals("official")) {
                                            // Update the user's email in Firebase Authentication
                                            currentUser.updateEmail(Email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        String email=dataSnapshot.child("email").getValue(String.class);
                                                        Log.d("Email", email);
                                                        // Update the user's email in Realtime Database
                                                        databaseReference.child(userId).child("email").setValue(Email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                                                    startActivity(new Intent(Login.this, OfficialDashboard.class));
                                                                    finish();
                                                                } else {
                                                                    Toast.makeText(Login.this, "Failed to update email", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    } else {
                                                        Toast.makeText(Login.this, "Failed to update email", Toast.LENGTH_SHORT).show();
                                                    }
                                                    loginDialog.dismiss();
                                                }
                                            });

                                        } else {
                                            Toast.makeText(Login.this, "Login Failed. Please try again", Toast.LENGTH_SHORT).show();
                                            loginDialog.dismiss();
                                        }
                                    } else {
                                        Toast.makeText(Login.this, "User not found", Toast.LENGTH_SHORT).show();
                                        loginDialog.dismiss();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(Login.this, "Login Failed. Please try again", Toast.LENGTH_SHORT).show();
                                    loginDialog.dismiss();
                                }
                            });
                        } else {
                            Toast.makeText(Login.this, "Login Failed. Please try again", Toast.LENGTH_SHORT).show();
                            loginDialog.dismiss();
                        }
                    }
                });
            }
        });
//        Register Button
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, Register.class));
                finish();
            }
        });
    }
}