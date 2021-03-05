package com.brightsky.medicab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.ExecutorService;

public class GoogleOAuthSignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_oauth_signin);

        ExecutorThreadService executorThreadService = ExecutorThreadService.getInstance();
        Handler mainHandler = executorThreadService.getHandler();
        ExecutorService executorService = executorThreadService.getExecutor();

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        executorService.execute(() -> {
            GoogleSignInClient mGoogleSignInClient = createRequest();
            mainHandler.post(() -> signIn(mGoogleSignInClient));
        });
    }

    private GoogleSignInClient createRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        return GoogleSignIn.getClient(this, gso);
    }

    private void signIn(GoogleSignInClient mGoogleSignInClient) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            getUserDetails(account);
        } catch (ApiException e) {
            Toast.makeText(this, "Sign In Failed! Please Try Again", Toast.LENGTH_SHORT).show();
            Log.w("GoogleOAuthSignIn", "Google Sign In Failed", e);
            finish();
        }
    }

    private void getUserDetails(GoogleSignInAccount account) {
        String firstName = account.getGivenName();
        String lastName = account.getFamilyName();;
        String email = account.getEmail();

        FirebaseDatabase.getInstance()
                .getReference()
                .child("ClientsProfile")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot snapshot1: snapshot.getChildren()) {
                            if(snapshot1.getKey().equals(uid)) {
                                Intent intent = new Intent(GoogleOAuthSignInActivity.this, CustomerMapsActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            } else {
                                Intent intent = new Intent(GoogleOAuthSignInActivity.this, GoogleOAuthResponseProfileActivity.class);
                                intent.putExtra("firstName", firstName);
                                intent.putExtra("lastName", lastName);
                                intent.putExtra("email", email);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        error.toException().printStackTrace();
                        Toast.makeText(GoogleOAuthSignInActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}