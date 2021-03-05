package com.brightsky.medicab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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

public class OTPLoginActivity extends AppCompatActivity {
    private TextView nextButtonText;
    private ProgressBar progressBar;
    private FrameLayout nextButton;
    private ImageView backButton;
    private EditText phoneNumberEditText;
    private TextView googleLoginText;

    private String phoneNumber;
    private final String COUNTRY_CODE = "+91";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_login);

        phoneNumberEditText = findViewById(R.id.phone_number_edit_text);
        nextButtonText = findViewById(R.id.next_button_text);
        nextButtonText = findViewById(R.id.next_button_text);
        googleLoginText = findViewById(R.id.google);
        backButton = findViewById(R.id.back_button);
        nextButton = findViewById(R.id.next_button);
        progressBar = findViewById(R.id.progress_bar);

        nextButton.setClickable(true);
        nextButtonText.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        backButton.setOnClickListener(v -> finish());
        googleLoginText.setOnClickListener(v -> startActivity(new Intent(OTPLoginActivity.this,
                GoogleOAuthSignInActivity.class)));


        nextButton.setOnClickListener(v -> {
            phoneNumber = phoneNumberEditText.getText().toString();

            if (phoneNumber.isEmpty())
                Toast.makeText(OTPLoginActivity.this, "Please Enter Your Phone Number", Toast.LENGTH_SHORT).show();
            else {
                nextButtonText.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);

                nextButton.setClickable(false);

                PhoneAuthOptions options =
                        PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                                .setPhoneNumber(COUNTRY_CODE + phoneNumber)
                                .setTimeout(60L, TimeUnit.SECONDS)
                                .setActivity(OTPLoginActivity.this)
                                .setCallbacks(onVerificationStateChangedCallbacks)
                                .build();

                PhoneAuthProvider.verifyPhoneNumber(options);
            }
        });
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks onVerificationStateChangedCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            Intent loginIntent = new Intent(OTPLoginActivity.this, CustomerMapsActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            e.printStackTrace();
            Toast.makeText(OTPLoginActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();

            nextButton.setClickable(true);
            nextButtonText.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            Intent intent = new Intent(OTPLoginActivity.this, OTPInputActivity.class);

            intent.putExtra("verificationId", s);
            intent.putExtra("phoneNo", phoneNumber);

            startActivity(intent);
        }
    };
}