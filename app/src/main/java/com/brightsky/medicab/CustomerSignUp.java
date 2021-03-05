package com.brightsky.medicab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CustomerSignUp extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    String email, password;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        TextView loginPrompt = findViewById(R.id.login_prompt);
        Button mSignUpButton = findViewById(R.id.signup_button);

        mAuth = FirebaseAuth.getInstance();

        firebaseAuthStateListener = firebaseAuth -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if(user != null) {
                Intent homeIntent = new Intent(CustomerSignUp.this, MapsActivity.class);
                startActivity(homeIntent);
                finish();
            }
        };

        mSignUpButton.setOnClickListener(v -> {
             email = emailEditText.getText().toString();
             password = passwordEditText.getText().toString();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Enter your Email", Toast.LENGTH_SHORT).show();
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Enter a secure password", Toast.LENGTH_SHORT).show();
            }
            if (password.length() > 6) {
                login();

            } else {
                Toast.makeText(this, "Enter a password of minimum 6 characters", Toast.LENGTH_SHORT).show();
            }

        });




        }

    private void login() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating your account...");
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    Toast.makeText(CustomerSignUp.this, "Account Created Successfully ", Toast.LENGTH_SHORT).show();

                    if (mAuth.getCurrentUser() != null) {
                        String userId = mAuth.getCurrentUser().getUid();
                        DatabaseReference currentUserDBReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId);
                        currentUserDBReference.setValue(true);
                        progressDialog.dismiss();
                        Intent intent = new Intent(CustomerSignUp.this,MapsActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
                else {
                    progressDialog.dismiss();

                    Toast.makeText(CustomerSignUp.this, "Failed.....", Toast.LENGTH_SHORT).show();

                }


            }


            }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(CustomerSignUp.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });







    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthStateListener);
    }
}