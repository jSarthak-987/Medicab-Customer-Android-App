package com.brightsky.medicab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.UUID;

public class ProfileSetupActivity extends AppCompatActivity {

    private ImageView profileImage;

    private EditText emailEditText;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText phoneNumberEditText;

    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private String profile;

    private FrameLayout nextButton;
    private Uri imageUri;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    private String uid;
    private static final int CODE = 1;

    private FrameLayout verifyOtpButton;

    private TextView nextButtonText;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        Intent intent = getIntent();

        String phoneNo = intent.getStringExtra("phoneNo");

        profileImage = findViewById(R.id.profile_image);
        emailEditText = findViewById(R.id.email);
        firstNameEditText = findViewById(R.id.first_name);
        lastNameEditText = findViewById(R.id.last_name);
        phoneNumberEditText = findViewById(R.id.phone_number);
        nextButton = findViewById(R.id.next_button);
        nextButtonText = findViewById(R.id.next_text);
        progressBar = findViewById(R.id.progress_bar);

        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(ProfileSetupActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();

            Intent loginIntent = new Intent(ProfileSetupActivity.this, OTPLoginActivity.class);
            startActivity(loginIntent);
            finish();
        }

        uid = firebaseAuth.getCurrentUser().getUid();

        firebaseDatabase = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        databaseReference = firebaseDatabase.getReference("ClientsProfile");
        storageReference = storage.getReference("UploadImages/");

        phoneNumberEditText.setText(phoneNo);

        profileImage.setOnClickListener(v -> pickImage());

        nextButton.setOnClickListener(v -> {
            email = emailEditText.getText().toString().trim();
            firstName = firstNameEditText.getText().toString().trim();
            lastName = lastNameEditText.getText().toString().trim();
            phone = phoneNumberEditText.getText().toString().trim();

            boolean allFieldsAreEntered = checkFields();

            nextButton.setClickable(false);

            nextButtonText.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            uploadProfile(firstName, lastName, phone, email, imageUri);
        });
    }

    private void uploadProfile(String firstName, String lastName, String phone, String email, Uri imageUri) {
        final String randomKey = UUID.randomUUID().toString();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference riversRef = storageReference.child("images/" + randomKey);

        if (imageUri != null) {
            riversRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> riversRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = String.valueOf(uri);

                        HashMap<String, String> hashMap = new HashMap<>();

                        hashMap.put("FirstName", firstName);
                        hashMap.put("LastName", lastName);
                        hashMap.put("Email", email);
                        hashMap.put("PhoneNo", phone);
                        hashMap.put("Block", "0");
                        hashMap.put("ImageUrl", downloadUrl);

                        databaseReference.child(uid).setValue(hashMap).addOnCompleteListener(task -> {

                            Toast.makeText(ProfileSetupActivity.this, "Setup Successful", Toast.LENGTH_SHORT).show();

                            Log.d("ProfileSetup", "UID:" + uid);

                            Intent intent = new Intent(ProfileSetupActivity.this, CustomerMapsActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();

                        }).addOnFailureListener(e -> {
                            Toast.makeText(ProfileSetupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                            nextButtonText.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            nextButton.setClickable(true);
                        });
                    })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ProfileSetupActivity.this,
                                        e.getMessage(), Toast.LENGTH_SHORT).show();

                                nextButtonText.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                                nextButton.setClickable(true);
                            }))

                    .addOnFailureListener(exception -> {
                        Toast.makeText(ProfileSetupActivity.this,
                                exception.getMessage(), Toast.LENGTH_SHORT).show();

                        nextButtonText.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        nextButton.setClickable(true);
                    });
        } else {
            HashMap<String, String> hashMap = new HashMap<>();

            hashMap.put("FirstName", firstName);
            hashMap.put("LastName", lastName);
            hashMap.put("Email", email);
            hashMap.put("PhoneNo", phone);
            hashMap.put("Block", "0");
            hashMap.put("ImageUrl", "true");

            FirebaseDatabase.getInstance().getReference().child("ClientsProfile").child(uid).setValue(hashMap).addOnCompleteListener(task -> {

                Toast.makeText(ProfileSetupActivity.this, "Setup Successful", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(ProfileSetupActivity.this, CustomerMapsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();

            }).addOnFailureListener(e -> {
                Toast.makeText(ProfileSetupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                nextButtonText.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                nextButton.setClickable(true);
            });
        }
    }

    private void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(intent,CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODE && resultCode == RESULT_OK && data!=null){
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }

    private boolean checkFields() {
        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Enter your Email First", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (TextUtils.isEmpty(firstName)){
            Toast.makeText(this, "Enter your First Name", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (TextUtils.isEmpty(lastName)){
            Toast.makeText(this, "Enter your Last Name", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (TextUtils.isEmpty(phone)){
            Toast.makeText(this, "Enter your phone Number", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}