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

public class CustomerLogin extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    String email, password;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        TextView signUpPrompt = findViewById(R.id.signup_prompt);
        Button mLoginButton = findViewById(R.id.login_button);

        mAuth = FirebaseAuth.getInstance();

        firebaseAuthStateListener = firebaseAuth -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if(user != null) {
                Intent homeIntent = new Intent(CustomerLogin.this, MapsActivity.class);
                startActivity(homeIntent);
                finish();
            }
        };

        mLoginButton.setOnClickListener(v -> {
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

        signUpPrompt.setOnClickListener(v -> {
            Intent signUpIntent = new Intent(CustomerLogin.this, CustomerSignUp.class);
            startActivity(signUpIntent);
            finish();
        });
    }

    private void login() {

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Login to your account....");
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    progressDialog.dismiss();

                    Toast.makeText(CustomerLogin.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CustomerLogin.this,MapsActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    Toast.makeText(CustomerLogin.this, "Login Failed....", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();

                Toast.makeText(CustomerLogin.this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthStateListener);
    }
}