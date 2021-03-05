package com.brightsky.medicab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.stfalcon.smsverifycatcher.OnSmsCatchListener;
import com.stfalcon.smsverifycatcher.SmsVerifyCatcher;

import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OTPInputActivity extends AppCompatActivity {

    private String phoneNumber;
    private EditText code1;
    private EditText code2;
    private EditText code3;
    private EditText code4;
    private EditText code5;
    private EditText code6;

    private String verificationId;
    private String email;
    private String firstName;
    private String lastName;
    private Uri imageUri;

    private TextView verifyOtpText;
    private ProgressBar progressBar;

    private FrameLayout verifyOtpButton;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_input);

        verifyOtpButton = findViewById(R.id.verify_otp_button);
        verifyOtpText = findViewById(R.id.verify_otp_text);
        progressBar = findViewById(R.id.progress_bar);
        code1 = findViewById(R.id.code_1);
        code2 = findViewById(R.id.code_2);
        code3 = findViewById(R.id.code_3);
        code4 = findViewById(R.id.code_4);
        code5 = findViewById(R.id.code_5);
        code6 = findViewById(R.id.code_6);
        ImageView backButton = findViewById(R.id.back_button);
//        resendOtpText = findViewById(R.id.resend_otp_button);

        Intent intent = getIntent();

        verificationId = intent.getStringExtra("verificationId");
        imageUri = intent.getParcelableExtra("imageUri");
        phoneNumber = intent.getStringExtra("phoneNo");
        firstName = intent.getStringExtra("firstName");
        lastName = intent.getStringExtra("lastName");
        email = intent.getStringExtra("email");

        backButton.setOnClickListener(v -> finish());

        setTextWatched(null, code1, code2);
        setTextWatched(code1, code2, code3);
        setTextWatched(code2, code3, code4);
        setTextWatched(code3, code4, code5);
        setTextWatched(code4, code5, code6);
        setTextWatched(code5, code6, null);

        verifyOtpButton.setOnClickListener(v -> {

            String code1String = code1.getText().toString();
            String code2String = code2.getText().toString();
            String code3String = code3.getText().toString();
            String code4String = code4.getText().toString();
            String code5String = code5.getText().toString();
            String code6String = code6.getText().toString();

            String code = code1String + code2String + code3String + code4String + code5String + code6String;

            if(code.length() != 6) {
                Toast.makeText(OTPInputActivity.this, "Please Enter The Complete OTP", Toast.LENGTH_SHORT).show();
            }
            else {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                signInUser(credential);
            }
        });
    }

    private void setTextWatched(EditText prevText, EditText currText, EditText nextText) {

        currText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int textChanged = count - before;
                if(textChanged == 1 && nextText != null) {
                    currText.clearFocus();
                    nextText.requestFocus();
                    nextText.setCursorVisible(true);
                }
                else if(textChanged == -1 && prevText != null) {
                    currText.clearFocus();
                    prevText.requestFocus();
                    prevText.setCursorVisible(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        currText.setOnKeyListener((v, keyCode, event) -> {
            if(currText.getText().toString().length() == 0 && keyCode == KeyEvent.KEYCODE_DEL && prevText != null) {
                currText.clearFocus();
                prevText.requestFocus();
                prevText.setCursorVisible(true);
            }
            return false;
        });
    }

    private void signInUser(PhoneAuthCredential credential) {
        verifyOtpButton.setClickable(false);

        verifyOtpText.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        if(firstName != null && lastName != null && email != null) {
            FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().getUser() == null) {
                                Toast.makeText(OTPInputActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();

                                verifyOtpText.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                                verifyOtpButton.setClickable(true);
                            } else {
                                uid = task.getResult().getUser().getUid();
                                Log.d("OTPInput", "UID:" + uid);
                                googleOAuthLoginUser(phoneNumber);
                            }
                        } else {
                            Log.d("PhoneOtpAuth", "onComplete:" + task.getException());
                            Toast.makeText(OTPInputActivity.this, "Verification Failed!", Toast.LENGTH_SHORT).show();

                            verifyOtpText.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            verifyOtpButton.setClickable(true);
                        }
                    });
        } else {
            FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            uid = task.getResult().getUser().getUid();

                            Log.d("OTPInput", "UID:" + uid);
                            Intent intent = new Intent(OTPInputActivity.this, ProfileSetupActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("phoneNo", phoneNumber);
                            startActivity(intent);
                            finish();
                        }else {
                            Log.d("PhoneOtpAuth", "onComplete:" + task.getException());
                            Toast.makeText(OTPInputActivity.this, "Verification Failed!", Toast.LENGTH_SHORT).show();

                            verifyOtpText.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            verifyOtpButton.setClickable(true);
                        }
                    });
        }
    }

    private void uploadProfile(String firstName, String lastName, String phone, String email) {
        final String randomKey = UUID.randomUUID().toString();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageReference.child("ClientProfileImages/" + randomKey);

        if(imageUri != null) {
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = String.valueOf(uri);

                        HashMap<String, String> hashMap = new HashMap<>();

                        hashMap.put("FirstName", firstName);
                        hashMap.put("LastName", lastName);
                        hashMap.put("Email", email);
                        hashMap.put("PhoneNo", phone);
                        hashMap.put("Block", "0");
                        hashMap.put("ImageUrl", downloadUrl);

                        FirebaseDatabase.getInstance()
                                .getReference()
                                .child("ClientsProfile")
                                .child(uid)
                                .setValue(hashMap)
                                .addOnCompleteListener(task -> {
                                    Toast.makeText(OTPInputActivity.this, "Setup Successful", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(OTPInputActivity.this, CustomerMapsActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(this::profileUploadFailedListener);
                    })
                            .addOnFailureListener(this::profileUploadFailedListener))
                    .addOnFailureListener(this::profileUploadFailedListener);
        } else {
            HashMap<String, String> hashMap = new HashMap<>();

            hashMap.put("FirstName", firstName);
            hashMap.put("LastName", lastName);
            hashMap.put("Email", email);
            hashMap.put("PhoneNo", phone);
            hashMap.put("Block", "0");
            hashMap.put("ImageUrl", "true");

            FirebaseDatabase.getInstance()
                    .getReference()
                    .child("ClientsProfile")
                    .child(uid)
                    .setValue(hashMap)
                    .addOnCompleteListener(task -> {

                        Toast.makeText(OTPInputActivity.this,
                                "Setup Successful", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(OTPInputActivity.this, CustomerMapsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();

                    })
                    .addOnFailureListener(this::profileUploadFailedListener);
        }
    }


    private void profileUploadFailedListener(Exception exception) {
        Toast.makeText(OTPInputActivity.this,
                exception.getMessage(), Toast.LENGTH_SHORT).show();

        verifyOtpText.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        verifyOtpButton.setClickable(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        smsVerifyCatcher.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        smsVerifyCatcher.onStop();
    }

    SmsVerifyCatcher smsVerifyCatcher = new SmsVerifyCatcher(this, new OnSmsCatchListener<String>() {
        @Override
        public void onSmsCatch(String message) {
            String code = parseCode(message);
            code1.setText(String.valueOf(code.charAt(0)));
            code2.setText(String.valueOf(code.charAt(1)));
            code3.setText(String.valueOf(code.charAt(2)));
            code4.setText(String.valueOf(code.charAt(3)));
            code5.setText(String.valueOf(code.charAt(4)));
            code6.setText(String.valueOf(code.charAt(5)));

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            signInUser(credential);
        }
    });

    public String parseCode(String message) {
        Pattern p = Pattern.compile("\\b\\d{6}\\b");
        Matcher m = p.matcher(message);
        String code = "";
        while (m.find()) {
            code = m.group(0);
        }
        return code;
    }

    private void googleOAuthLoginUser(String phoneNumber) {
        FirebaseDatabase.getInstance()
                .getReference()
                .child("ClientsProfile")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean profileFound = false;

                        for(DataSnapshot snapshot1: snapshot.getChildren()) {
                            if(snapshot1.getValue().equals(uid)) {
                                Intent intent = new Intent(OTPInputActivity.this, CustomerMapsActivity.class);
                                startActivity(intent);
                                finish();
                                profileFound = true;
                                break;
                            }
                        }

                        if(!profileFound) {
                            uploadProfile(firstName, lastName, phoneNumber, email);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        error.toException().printStackTrace();
                        Toast.makeText(OTPInputActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void phoneNumberLoginUser(String phoneNumber) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("ClientsProfile").child(uid);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot snapshot1: snapshot.getChildren()) {
                        if(snapshot1.getKey().equals("PhoneNo") && snapshot1.getValue().equals(phoneNumber)) {
                            Intent intent = new Intent(OTPInputActivity.this, CustomerMapsActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Intent intent = new Intent(OTPInputActivity.this, ProfileSetupActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("phoneNo", phoneNumber);
                            startActivity(intent);
                            finish();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                error.toException().printStackTrace();
            }
        });
    }
}