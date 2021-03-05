package com.brightsky.medicab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.brightsky.medicab.firebasemessaging.DriverBookingMessagingRequestModel;
import com.brightsky.medicab.firebasemessaging.DriverBookingMessagingResponseModel;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BookingConfirmation extends AppCompatActivity {

    ExecutorThreadService execturorThreadService = ExecutorThreadService.getInstance();
    ExecutorService executorService = execturorThreadService.getExecutor();
    Handler mainThreadHandler = execturorThreadService.getHandler();
    private FetchNearbyDriverLocations fetchNearbyDriverLocations;
    private String clientFCMToken;
    private LatLng pickupLocation;
    private AmbulanceDriverProfile ambulanceDriverProfile;
    private ArrayList<String> driverKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmation);

        registerReceiver(broadcastReceiver, new IntentFilter(DriversFirebaseMessagingService.INTENT_FILTER));
        registerReceiver(otpBroadCastReceiver, new IntentFilter(DriversFirebaseMessagingService.OTP_VERIFICATION_INTENT_FILTER));

        pickupLocation = getIntent().getExtras().getParcelable("PickupLatLng");
        driverKeys = getIntent().getStringArrayListExtra("AmbulanceKeys");

        FirebaseMessaging.getInstance()
                .getToken()
                .addOnCompleteListener(task -> {
                    clientFCMToken = task.getResult();

//                    fetchNearbyDriverLocations = new FetchNearbyDriverLocations(executorService, mainThreadHandler);
                    fetchNearbyDriverLocations = new FetchNearbyDriverLocations();
                    fetchNearbyDriverLocations.fetchDrivers(driverKeys, clientFCMToken);
                });
    }

    static class AmbulanceDriverProfile {
        private String firstName;
        private String lastName;
        private String profileurl;
        private String Ambulancetype;

        public String getFirstName() {
            return firstName;
        }

        public String getAmbulancetype() {
            return Ambulancetype;
        }

        public String getLastName() {
            return lastName;
        }

        public String getProfileurl() {
            return profileurl;
        }

        public void setAmbulancetype(String ambulancetype) {
            Ambulancetype = ambulancetype;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public void setProfileurl(String profileurl) {
            this.profileurl = profileurl;
        }
    }

    DriverFoundCallback driverFoundCallback = new DriverFoundCallback() {
        @Override
        public void OnDriversFound(String driverKey) {

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("DriverLocation");
            GeoFire geoFire = new GeoFire(reference);
            geoFire.getLocation(driverKey, new LocationCallback() {
                @Override
                public void onLocationResult(String key, GeoLocation location) {
                    Intent driverFoundIntent = new Intent(BookingConfirmation.this, DriverFoundActivity.class);
                    driverFoundIntent.putExtra("PickupLatLng", pickupLocation);
                    driverFoundIntent.putExtra("DriverKey", key);
                    driverFoundIntent.putExtra("DriversLatLng", new LatLng(location.latitude, location.longitude));

                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("DriversProfile").child(key);
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot dataSnapshot: snapshot.getChildren()) {
                                if(dataSnapshot.getValue() != null) {
                                    if (Objects.equals(dataSnapshot.getKey(), "Firstname")) {
                                        driverFoundIntent.putExtra("FirstName", dataSnapshot.getValue().toString());
                                    }
                                    if (Objects.equals(dataSnapshot.getKey(), "Lastname")) {
                                        driverFoundIntent.putExtra("Lastname", dataSnapshot.getValue().toString());
                                    }
                                    if (Objects.equals(dataSnapshot.getKey(), "profileurl")) {
                                        driverFoundIntent.putExtra("ProfileURL", dataSnapshot.getValue().toString());
                                    }
                                    if (Objects.equals(dataSnapshot.getKey(), "Ambulancetype")) {
                                        driverFoundIntent.putExtra("AmbulanceType", dataSnapshot.getValue().toString());
                                    }
                                }
                            }

                            startActivity(driverFoundIntent);
                            finish();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            error.toException().printStackTrace();
                        }
                    });

                    Toast.makeText(BookingConfirmation.this, "Found Driver: " + driverKey, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(BookingConfirmation.this, "Driver Couldn't Find", Toast.LENGTH_SHORT).show();
                    Log.e("BookingConfirmation", "Driver Couldn't Find: " + databaseError.getDetails());
                }
            });
        }

        @Override
        public void OnNoDriverFound() {
            Toast.makeText(BookingConfirmation.this, "No Driver Found Nearby", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void OnError(String errorMessage) {
            Toast.makeText(BookingConfirmation.this, "Error Occurred Fetching Drivers", Toast.LENGTH_SHORT).show();
        }
    };

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(DriversFirebaseMessagingService.INTENT_FILTER)) {
                String driverKey = intent.getExtras().getString("DriverKey");
//                String transactionId = intent.getExtras().getString("TransactionId");

                fetchNearbyDriverLocations.completeTransaction(driverFoundCallback, driverKey);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(otpBroadCastReceiver);
    }

    interface DriverFoundCallback {
        void OnDriversFound(String driverKey);
        void OnNoDriverFound();
        void OnError(String errorMessage);
    }

    class FetchNearbyDriverLocations{

        private boolean driverFound = false;
        private ArrayList<String> driverKeys;

        private void fetchDrivers(ArrayList<String> driverKeys, String clientFCMToken) {
            this.driverKeys = driverKeys;
            getDriverFCMTokens(driverKeys, clientFCMToken);
        }

        private void getDriverFCMTokens(List<String> driverKeys, String clientFCMToken) {
            List<String> driverFCMTokens = new ArrayList<>();

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers");
            Query fcmTokenQuery = reference.orderByKey();

            fcmTokenQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        for (DataSnapshot drivers : snapshot.getChildren()) {
                            if (driverKeys.contains(drivers.getKey())) {
                                for (DataSnapshot driverItems : drivers.getChildren()) {
                                    String driverRegistrationToken = driverItems.getValue().toString();
                                    driverFCMTokens.add(driverRegistrationToken);
                                }
                            }
                        }
                    }
                    if(driverFCMTokens.size() > 0)
                        sendPickupNotificationToDrivers(driverFCMTokens, clientFCMToken);
                    else {
                        Toast.makeText(BookingConfirmation.this, "No Driver Found Available", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        private void sendPickupNotificationToDrivers(List<String> driverFCMTokens, String clientFCMToken) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://us-central1-acquired-jet-298514.cloudfunctions.net")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            RetrofitCallbacks callbacks = retrofit.create(RetrofitCallbacks.class);

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DriverBookingMessagingRequestModel driverBookingMessage = new DriverBookingMessagingRequestModel();
            driverBookingMessage.setDriverToken(driverFCMTokens);
            driverBookingMessage.setClientKey(userId);
            driverBookingMessage.setClientToken(clientFCMToken);
            driverBookingMessage.setTxid("#TX1234");

            Call<DriverBookingMessagingResponseModel> sendDemoNotificationToDriver = callbacks.sendNotificationDemo(driverBookingMessage);
            sendDemoNotificationToDriver.enqueue(new Callback<DriverBookingMessagingResponseModel>() {
                @Override
                public void onResponse(@NotNull Call<DriverBookingMessagingResponseModel> call, @NotNull Response<DriverBookingMessagingResponseModel> response) {
                    if(response.body() != null) {
                    }
                    else {
                        Log.e("BookingConfirmation", "Response: " + response);
                    }
                }

                @Override
                public void onFailure(@NotNull Call<DriverBookingMessagingResponseModel> call, @NotNull Throwable t) {
                    t.printStackTrace();
                }
            });
        }

        private void completeTransaction(DriverFoundCallback callback, String driverKey) {
            callback.OnDriversFound(driverKey);
        };

        private void updateUiMap(ArrayList<String> keys, HashMap<String, LatLng> driverLocations, DriverFoundCallback callback, String errorMessage, String clientFCMToken) {
            if(errorMessage != null) {
                callback.OnError(errorMessage);
            } else {
                if (driverLocations != null) {
                    getDriverFCMTokens(keys, clientFCMToken);
                } else {
                    callback.OnNoDriverFound();
                }
            }
        }
    }

    BroadcastReceiver otpBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(DriversFirebaseMessagingService.OTP_VERIFICATION_INTENT_FILTER)) {
                String otp = intent.getStringExtra("OTP");

                Toast.makeText(BookingConfirmation.this, "OTP is " + otp, Toast.LENGTH_SHORT).show();
            }
        }
    };
}