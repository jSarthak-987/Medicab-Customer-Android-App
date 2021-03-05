package com.brightsky.medicab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class UserSettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting);

        TextView signOut = findViewById(R.id.sign_out);
        CircleImageView userImage = findViewById(R.id.user_image);
        TextView userName = findViewById(R.id.username);
        ImageView backButton = findViewById(R.id.back);

        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            startActivity(new Intent(UserSettingActivity.this, OTPLoginActivity.class));
            finish();
        }

        String mAuth = FirebaseAuth.getInstance().getCurrentUser().getUid();

        backButton.setOnClickListener(v -> {
            startActivity(new Intent(UserSettingActivity.this, MapsActivity.class));
            finish();
        });

        signOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            startActivity(new Intent(UserSettingActivity.this, GetStartedActivity.class));
            finish();
        });

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ClientsProfile");
        Query checkUser = reference.orderByChild(mAuth);
        checkUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){

                    String nameString = snapshot.child(mAuth).child("FirstName").getValue(String.class);
                    String profileImageString = snapshot.child(mAuth).child("ImageUrl").getValue(String.class);

                    Toast.makeText(UserSettingActivity.this, nameString, Toast.LENGTH_SHORT).show();

                    userName.setText(nameString);
                    if (profileImageString != null && !profileImageString.equals("true"))
                        Glide.with(UserSettingActivity.this).load(profileImageString).placeholder(R.mipmap.avatar_dummy).into(userImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}