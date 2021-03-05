package com.brightsky.medicab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserProfileActivity extends AppCompatActivity {

    private TextView phoneNumber;
    private TextView email;
    private TextView userName;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        phoneNumber = findViewById(R.id.phoneNo);
        email = findViewById(R.id.email);
        userName = findViewById(R.id.username);
        backButton = findViewById(R.id.back);

        backButton.setOnClickListener(v -> {
            startActivity(new Intent(UserProfileActivity.this, MapsActivity.class));
            finish();
        });

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
    }
}