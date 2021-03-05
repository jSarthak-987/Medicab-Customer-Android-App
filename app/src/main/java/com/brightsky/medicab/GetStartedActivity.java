package com.brightsky.medicab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class GetStartedActivity extends AppCompatActivity {

    FrameLayout getStartedCFrameLayout;
    private TextView getStartedText;
    private ProgressBar progressBar;

    private final int PERMISSIONS_PROVIDED = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);

        getStartedCFrameLayout = findViewById(R.id.get_started_button);
        getStartedText = findViewById(R.id.get_started_text);
        progressBar = findViewById(R.id.progress_bar);

        checkPermissions();

//        try {

            getStartedCFrameLayout.setOnClickListener(v -> {
                getStartedText.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);

                Intent intent = new Intent(GetStartedActivity.this, OTPLoginActivity.class);
                startActivity(intent);

                getStartedText.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            });
        }
//        catch(Exception e) {
//            e.printStackTrace();
//            Toast.makeText(GetStartedActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//        }

    private void checkPermissions() {
//        try {
            List<String> permissions = new ArrayList<>();

            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_SMS);
            }

            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.RECEIVE_SMS);
            }

            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }

            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }

            if (permissions.size() > 0) {
                String[] permissionsArray = new String[permissions.size()];

                for (int i = 0; i < permissions.size(); i++)
                    permissionsArray[i] = permissions.get(i);

                ActivityCompat.requestPermissions(this, permissionsArray, PERMISSIONS_PROVIDED);
            }
        }
//        catch(Exception e) {
//            e.printStackTrace();
//            Toast.makeText(GetStartedActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//        }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_PROVIDED) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(GetStartedActivity.this, "Permission Given", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(GetStartedActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}