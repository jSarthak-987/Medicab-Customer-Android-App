package com.brightsky.medicab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ProfileUpdateActivity extends AppCompatActivity {

    private EditText email;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private ImageView userImage;
    private Button updateProfileButton;
    private TextView updateProfileText;
    private ImageView imageView;
    private TextView completeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_update);

        email = findViewById(R.id.email_update);
        firstNameEditText = findViewById(R.id.first_name_update);
        lastNameEditText = findViewById(R.id.last_name_update);
        updateProfileButton = findViewById(R.id.update_button);
        imageView = findViewById(R.id.image_profile);
        completeName = findViewById(R.id.username);

        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            Intent intent = new Intent(ProfileUpdateActivity.this, OTPLoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        String mAuth = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userImage = findViewById(R.id.user_image);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ClientsProfile");
        Query checkUser = reference.orderByChild(mAuth);
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String nameString = snapshot.child(mAuth).child("FirstName").getValue(String.class);
                    String profileImageString = snapshot.child(mAuth).child("ImageUrl").getValue(String.class);
                    String lastName = snapshot.child(mAuth).child("LastName").getValue(String.class);
                    String emailString = snapshot.child(mAuth).child("Email").getValue(String.class);
                    String phoneNo = snapshot.child(mAuth).child("PhoneNo").getValue(String.class);

                    String fullName = nameString + " " + lastName;

                    firstNameEditText.setText(nameString);
                    lastNameEditText.setText(lastName);
                    email.setText(emailString);
                    completeName.setText(fullName);

                    if (profileImageString != null && !profileImageString.equals("true"))
                        Glide
                                .with(ProfileUpdateActivity.this)
                                .load(profileImageString)
                                .placeholder(R.mipmap.avatar_dummy)
                                .into(imageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        updateProfileButton.setOnClickListener(v -> {
//            updateProfileText.setVisibility(View.GONE);

            DatabaseReference firebaseDatabase = FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child("ClientsProfile")
                    .child(mAuth);

            firebaseDatabase.child("FirstName").setValue(firstNameEditText.getText().toString());
            firebaseDatabase.child("LastName").setValue(lastNameEditText.getText().toString());
            firebaseDatabase.child("Email").setValue(email.getText().toString());

            Toast.makeText(ProfileUpdateActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}