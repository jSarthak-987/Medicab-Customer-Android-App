package com.brightsky.medicab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.ExecutorService;

public class ActivitySplashScreen extends AppCompatActivity {

    private ImageView medicabLogo;
    private Handler mainThreadHandler;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        medicabLogo = findViewById(R.id.medicab_icon);

        ExecutorThreadService executorThreadService = ExecutorThreadService.getInstance();
        ExecutorService executorService = executorThreadService.getExecutor();
        mainThreadHandler = executorThreadService.getHandler();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        animateMedicabLogo();

        executorService.execute(() -> {
            if (firebaseUser != null) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("ClientsProfile").child(firebaseUser.getUid());
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists() && snapshot.hasChildren()) {
                            mainThreadHandler.post(() -> {
                                Intent intent = new Intent(ActivitySplashScreen.this, CustomerMapsActivity.class);
                                startActivity(intent);
                                finish();
                            });
                        } else {
                            mainThreadHandler.post(() -> {
                                Intent intent = new Intent(ActivitySplashScreen.this, GetStartedActivity.class);
                                startActivity(intent);
                                finish();
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        mainThreadHandler.post(() -> {
                            Intent intent = new Intent(ActivitySplashScreen.this, GetStartedActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    }
                });
            } else {
                mainThreadHandler.post(() -> {
                    Intent intent = new Intent(ActivitySplashScreen.this, GetStartedActivity.class);
                    startActivity(intent);
                    finish();
                });
            }
        });
    }

    private void animateMedicabLogo(){
        Animation fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        fadeOutAnimation.setStartOffset(1000);
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeInAnimation.setStartOffset(500);

        fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                medicabLogo.startAnimation(fadeOutAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                medicabLogo.startAnimation(fadeInAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        medicabLogo.startAnimation(fadeOutAnimation);
    }
}