package com.brightsky.medicab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class GoogleOAuthResponseProfileActivity extends AppCompatActivity {

    private static final String COUNTRY_CODE = "+91";
    EditText nameEditText;
    EditText emailEditText;
    EditText contactNumberEditText;
    FrameLayout nextButton;
    private TextView nextButtonText;
    private ProgressBar progressBar;
    private String email;
    private String lastName;
    private String firstName;
    private ImageView profileImage;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_oauth_response_profile);

        nameEditText = findViewById(R.id.full_name);
        emailEditText = findViewById(R.id.email);
        contactNumberEditText = findViewById(R.id.phone_number);
        nextButton = findViewById(R.id.next_button);
        nextButtonText = findViewById(R.id.next_text);
        progressBar = findViewById(R.id.progress_bar);
        profileImage = findViewById(R.id.profile_image);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        lastName = intent.getStringExtra("lastName");
        firstName = intent.getStringExtra("firstName");
        email = intent.getStringExtra("email");
        String fullName = firstName + " " + lastName;

        nameEditText.setText(fullName);
        emailEditText.setText(email);

        profileImage.setOnClickListener(v -> pickImage());

        nextButton.setOnClickListener(v -> {
            nextButtonText.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            if (contactNumberEditText.getText().toString().length() == 0)
                Toast.makeText(GoogleOAuthResponseProfileActivity.this, "Enter your phone number", Toast.LENGTH_SHORT).show();
            else {
                PhoneAuthOptions options =
                        PhoneAuthOptions.newBuilder(mAuth)
                                .setPhoneNumber(COUNTRY_CODE + contactNumberEditText.getText().toString())
                                .setTimeout(60L, TimeUnit.SECONDS)
                                .setActivity(GoogleOAuthResponseProfileActivity.this)
                                .setCallbacks(onVerificationStateChangedCallbacks)
                                .build();

                PhoneAuthProvider.verifyPhoneNumber(options);
            }
        });
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks onVerificationStateChangedCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            startActivity(new Intent(GoogleOAuthResponseProfileActivity.this, CustomerMapsActivity.class));
            finish();
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            e.printStackTrace();
            Toast.makeText(GoogleOAuthResponseProfileActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            Log.e("PhoneOtpAuth", "OTP Sent");

            Intent intent = new Intent(GoogleOAuthResponseProfileActivity.this, OTPInputActivity.class);
            intent.putExtra("verificationId", s);
            intent.putExtra("phoneNo", contactNumberEditText.getText().toString());
            intent.putExtra("firstName", firstName);
            intent.putExtra("lastName", lastName);
            intent.putExtra("email", email);
            intent.putExtra("imageUri", imageUri);

            startActivity(intent);
            finish();
        }
    };

    private void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(intent,1);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data!=null){
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    };
}