package com.brightsky.medicab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brightsky.medicab.distance.DistanceMatrixResponsePojo;
import com.brightsky.medicab.distance.Element;
import com.brightsky.medicab.distance.Row;
import com.brightsky.medicab.routesmodel.Leg;
import com.brightsky.medicab.routesmodel.Route;
import com.brightsky.medicab.routesmodel.RoutesResponsePojo;
import com.brightsky.medicab.routesmodel.Step;
import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, PaymentResultListener, LocationListener {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;

    private static final int CURRENT_LOCATION_ZOOM = 17;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 2;

    private final int PICKUP_LOCATION_RESULT_CODE = 1;
    private final int HOSPITAL_LOCATION_RESULT_CODE = 2;

    private int hospitalPickupDistance = 0;
    private int alsPrice = 0;
    private int blsPrice = 0;
    private int mortgagePrice = 0;
    private int intercityPrice = 0;

    private LatLng pickupLocation;
    private LatLng hospitalLocation;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private ImageView profileImageView;
    private ImageView hamburger3;
    private ImageView hamburger4;

    private ConstraintLayout pickupPointConstraintLayout;
    private ConstraintLayout hospitalPointConstraintLayout;

    private TextView pickupAddressEditText;
    private ImageView recenterButton;
    private TextView profileName;
    private TextView profilePhoneNumber;
    private TextView alsPriceTextView;
    private TextView blsPriceTextView;
    private TextView mortgagePriceTextView;
    private TextView intercityPriceTextView;
    private TextView hospitalAddressEditText;

    private Toolbar toolbar;
    private FirebaseAuth firebaseAuth;

    private boolean driverFound = false;

    private String uid;
    private String pickupCity;
    private String hospitalCity;

    private Menu navigationItemsMenu;
    private SupportMapFragment mapFragment;

    //    private Polyline lastPlottedPolyline;
    private DatabaseReference databaseReference;
    private FirebaseDatabase firebaseDatabase;

    private List<String> ambulanceKeys;
    private List<Marker> ambulanceMarkers;
    private List<String> ambulanceTypesAvailable;
    private List<String> alsAmbulanceKeys;
    private List<String> blsAmbulanceKeys;
    private List<String> mortgageAmbulanceKeys;
    private List<String> intercityAmbulanceKeys;
    private HashMap<String, Marker> ambulanceMarkersHashMap;

    private FrameLayout bottomFragmentPosition;
    private ConstraintLayout locationSelectionLayout;
    private ConstraintLayout paymentBottomPanel;
    private Marker currMarker;

    private ConstraintLayout alsButton;
    private ConstraintLayout blsButton;
    private ConstraintLayout mortgageButton;
    private ConstraintLayout intercityButton;

    private Polyline lastPlottedPolyline;
    private Marker markerPickup;
    private Marker markerDrop;
    private RelativeLayout paymentButton;
    private GpsReceiver gpsReceiver;
    private NetworkChangeListener networkChangeListener;

    private ConstraintLayout selectedButton;
    private int selectedAmbulancePrice = 0;
    private int duration = 0;
    private AlertDialog.Builder networkUnavailableDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        verifyUserIsLoggedIn();

        pickupAddressEditText = findViewById(R.id.pickup_address);
        hospitalAddressEditText = findViewById(R.id.destination_address);
        hamburger3 = findViewById(R.id.hamburger3);
        hamburger4 = findViewById(R.id.hamburger4);
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
      //  navigationView = findViewById(R.id.naviagtionview);
        recenterButton = findViewById(R.id.recenter);
        locationSelectionLayout = findViewById(R.id.location_selection_bottom_panel);
        paymentBottomPanel = findViewById(R.id.payment_bottom_panel);
        hospitalPointConstraintLayout = findViewById(R.id.hospital_location_field);
        pickupPointConstraintLayout = findViewById(R.id.pickup_location_field);
        paymentButton = findViewById(R.id.payment_button);
        alsButton = findViewById(R.id.als_button);
        blsButton = findViewById(R.id.bls_button);
        mortgageButton = findViewById(R.id.mortgage_button);
        intercityButton = findViewById(R.id.intercity_button);
        alsPriceTextView = findViewById(R.id.als_price);
        blsPriceTextView = findViewById(R.id.bls_price);
        mortgagePriceTextView = findViewById(R.id.mortgage_price);
        intercityPriceTextView = findViewById(R.id.intercity_price);

        ambulanceMarkers = new ArrayList<>();
        ambulanceTypesAvailable = new ArrayList<>();
        ambulanceKeys = new ArrayList<>();
        ambulanceMarkersHashMap = new HashMap<>();
        alsAmbulanceKeys = new ArrayList<>();
        blsAmbulanceKeys = new ArrayList<>();
        mortgageAmbulanceKeys = new ArrayList<>();
        intercityAmbulanceKeys = new ArrayList<>();


        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.close, R.string.open);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationItemsMenu = navigationView.getMenu();
        MenuItem settingButton = navigationItemsMenu.findItem(R.id.settings);

        settingButton.setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(MapsActivity.this, UserSettingActivity.class);
            startActivity(intent);
            return true;
        });

        updateHeader();

        //Maps and Places API Initialization and Declaration
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        Places.initialize(getApplicationContext(), "AIzaSyDTyvJynEqi6qChFNEgNaCXqJrLBN8BIFk");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        recenterButton.setOnClickListener(v -> {
            if (lastKnownLocation != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(lastKnownLocation.getLatitude(),
                                lastKnownLocation.getLongitude()), CURRENT_LOCATION_ZOOM));
            } else {
                getCurrentDeviceLocation();
            }
        });

        hamburger4.setOnClickListener(v -> revertPaymentPage());

        hospitalPointConstraintLayout.setOnClickListener(v -> {
            if(lastKnownLocation != null) {
                Intent pickupPointClickIntent = new Intent(MapsActivity.this, HospitalSearch.class);
                pickupPointClickIntent.putExtra("PickupLatLng", lastKnownLocation);
                startActivityForResult(pickupPointClickIntent, HOSPITAL_LOCATION_RESULT_CODE);
            } else {
                Toast.makeText(MapsActivity.this, "Error Fetching Your Location! Please turn on the Location in your phone", Toast.LENGTH_SHORT).show();
            }
        });

        pickupPointConstraintLayout.setOnClickListener(v -> {
            if(lastKnownLocation != null) {
                Intent pickupPointClickIntent = new Intent(MapsActivity.this, PickupSearch.class);
                pickupPointClickIntent.putExtra("PickupLatLng", lastKnownLocation);
                startActivityForResult(pickupPointClickIntent, PICKUP_LOCATION_RESULT_CODE);
            } else {
                Toast.makeText(MapsActivity.this, "Error Fetching Your Location! Please turn on the Location in your phone", Toast.LENGTH_SHORT).show();
            }
        });

        paymentButton.setOnClickListener(v -> {
            if(selectedButton != null && selectedAmbulancePrice != 0) {
                Checkout checkout = new Checkout();
                checkout.setKeyID("rzp_test_3PiOcZSjoaFdOc");

                try {
                    JSONObject orderRequest = new JSONObject();
                    orderRequest.put("amount", selectedAmbulancePrice);
                    orderRequest.put("currency", "INR");
                    orderRequest.put("receipt", "order_rcptid_11");

                    checkout.open(this, orderRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(MapsActivity.this, "Please Select An Ambulance", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getNearbyCaptains(LatLng pickupLocation) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("DriverLocation");
        GeoFire geoFire = new GeoFire(reference);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), 200);

        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound) {
                    driverFound = true;
                }
                ambulanceKeys.add(key);

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(location.latitude, location.longitude))
                        .title("Driver Found Here")
                        .icon(getBitmapFromVector()));

                ambulanceMarkersHashMap.put(key, marker);
                ambulanceMarkers.add(marker);
            }

            @Override
            public void onKeyExited(String key) {
                ambulanceKeys.remove(key);
                ambulanceTypesAvailable = new ArrayList<>();

                Objects.requireNonNull(ambulanceMarkersHashMap.get(key)).remove();
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Objects.requireNonNull(ambulanceMarkersHashMap
                        .get(key)).setPosition(new LatLng(location.latitude, location.longitude));
            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound)
                    Toast.makeText(MapsActivity.this, "No Driver Available Nearby", Toast.LENGTH_SHORT).show();
                else getAmbulancesTypesAvailable(ambulanceKeys);
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
            }
        });
    }

    private void verifyUserIsLoggedIn() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            uid = firebaseAuth.getCurrentUser().getUid();
            databaseReference = firebaseDatabase.getReference("ClientsProfile");
        } else {
            Toast.makeText(MapsActivity.this, "Please Login Again", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MapsActivity.this, GetStartedActivity.class));
        }
    }

    private void updateHeader() {
        View headerView = navigationView.getHeaderView(0);
        profileName = headerView.findViewById(R.id.nav_name);
        profileImageView = headerView.findViewById(R.id.nav_profile_image);
        profilePhoneNumber = headerView.findViewById(R.id.nav_phone);

        headerView.setOnClickListener(v -> {
            Intent intent = new Intent(MapsActivity.this, ProfileUpdateActivity.class);
            startActivityForResult(intent, 4);
        });

        Query checkUser = databaseReference.orderByChild(uid);
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    String name = snapshot.child(uid).child("FirstName").getValue().toString();
                    String profileImageString = snapshot.child(uid).child("ImageUrl").getValue().toString();
                    String lastName = snapshot.child(uid).child("LastName").getValue().toString();
                    String phoneNumber = "+91" + snapshot.child(uid).child("PhoneNo").getValue().toString();

                    String profileNameString = name + " " + lastName;

                    profileName.setText(profileNameString);
                    profilePhoneNumber.setText(phoneNumber);

                    if (profileImageString != null && !profileImageString.equals("true"))
                        Glide.with(MapsActivity.this)
                                .load(profileImageString)
                                .placeholder(R.mipmap.avatar_dummy)
                                .into(profileImageView);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            getCurrentDeviceLocation();
        }
    }

    private void getCurrentDeviceLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        lastKnownLocation = task.getResult();
                        if (lastKnownLocation != null) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(lastKnownLocation.getLatitude(),
                                            lastKnownLocation.getLongitude()), CURRENT_LOCATION_ZOOM));


                            if(currMarker == null)
                                currMarker = mMap.addMarker(new MarkerOptions().title("Current Location")
                                    .icon(getLocationMarkerBitmap())
                                    .position(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude())));


                            locationSelectionLayout.setVisibility(View.VISIBLE);
                        } else {
                            Log.e(TAG, "Error Fetching Current Location");
                            Toast.makeText(MapsActivity.this, "Current Location Couldn't be fetched", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Error Fetching Current Location");
                        Toast.makeText(MapsActivity.this, "Current Location Couldn't be fetched", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(MapsActivity.this, "Location Permission Denied", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(gpsReceiver);
        unregisterReceiver(networkChangeListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION && requestCode == PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentDeviceLocation();
            }
        } else {
            Toast.makeText(MapsActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (paymentBottomPanel.getVisibility() == View.VISIBLE) {
            revertPaymentPage();
        } else if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        getLocationPermission();

        networkUnavailableDialog = alertNetworkUnavailableDialog();

        gpsReceiver = new GpsReceiver(() -> {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            try {
                boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (!gpsEnabled && !networkEnabled) {
                    Toast.makeText(MapsActivity.this, "Please Turn On The Location Access of The Phone", Toast.LENGTH_SHORT).show();
                    Log.e("MapsActivity", "Last Known Location : " + lastKnownLocation.getLatitude());
                } else {
                    Toast.makeText(MapsActivity.this, "Location is Turned On", Toast.LENGTH_SHORT).show();

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
                    }

                    Log.e("MapsActivity", "Last Known Location : " + lastKnownLocation.getLatitude());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        networkChangeListener = new NetworkChangeListener(networkAvailable -> {
            if(!networkAvailable) {
                Log.e("MapsActivity", "Internet Unavailable");
                networkUnavailableDialog.show();
            }
            else {
                networkUnavailableDialog.create().dismiss();
            }
        });

        registerReceiver(gpsReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        registerReceiver(networkChangeListener, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private AlertDialog.Builder alertNetworkUnavailableDialog() {
        return new AlertDialog.Builder(this)
                .setTitle("Network Unavailable")
                .setMessage("Can't connect with the Internet! Please turn on the wifi or data connection.")
                .setPositiveButton("Turn On", (dialog, which) -> {
                    Intent networkSettingIntent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                    startActivity(networkSettingIntent);
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            if (requestCode == HOSPITAL_LOCATION_RESULT_CODE) {
                String hospitalName = data.getStringExtra("HospitalName");
                hospitalLocation = data.getExtras().getParcelable("HospitalLatLng");
                hospitalCity = data.getStringExtra("HospitalCity");

                hospitalAddressEditText.setText(hospitalName);
                hospitalAddressEditText.setTextColor(Color.parseColor("#212121"));

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hospitalLocation, 1));
                displayRoutes();

                if(currMarker != null)
                    currMarker.remove();

                markerDrop = mMap.addMarker(new MarkerOptions().position(hospitalLocation));

            } else if (requestCode == PICKUP_LOCATION_RESULT_CODE) {
                String pickupName = data.getStringExtra("PickupName");
                pickupLocation = data.getExtras().getParcelable("PickupLatLng");
                pickupCity = data.getStringExtra("PickupCity");

                pickupAddressEditText.setText(pickupName);
                pickupAddressEditText.setTextColor(Color.parseColor("#212121"));

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickupLocation, 1));

                getNearbyCaptains(pickupLocation);
                displayRoutes();

                if(currMarker != null)
                    currMarker.remove();

                markerPickup =  mMap.addMarker(new MarkerOptions().position(pickupLocation));
            } else if(requestCode == 4) {
                updateHeader();
            } else {
                Toast.makeText(MapsActivity.this, "Error Displaying Selected Location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void revertPaymentPage() {
            hamburger3.setVisibility(View.GONE);
            hamburger4.setVisibility(View.VISIBLE);

            recenterButton.setVisibility(View.VISIBLE);

            paymentBottomPanel.setVisibility(View.GONE);
            locationSelectionLayout.setVisibility(View.VISIBLE);

            if (markerPickup !=null && markerDrop!=null){

                markerPickup.remove();
                markerDrop.remove();
            }

            if (lastPlottedPolyline!=null){
                lastPlottedPolyline.remove();

                hamburger4.setVisibility(View.GONE);
                hamburger3.setVisibility(View.VISIBLE);
                pickupAddressEditText.setHint("Enter your Pickup Location");
                pickupAddressEditText.setHintTextColor(Color.parseColor("#818181"));

                hospitalAddressEditText.setHint("Enter Hospital Address");
                hospitalAddressEditText.setHintTextColor(Color.parseColor("#818181"));

                pickupAddressEditText.setText(null);
                hospitalAddressEditText.setText(null);

                selectedButton = null;
                alsButton.setBackgroundColor(Color.parseColor("#fafafa"));
                blsButton.setBackgroundColor(Color.parseColor("#fafafa"));
                mortgageButton.setBackgroundColor(Color.parseColor("#fafafa"));
                intercityButton.setBackgroundColor(Color.parseColor("#fafafa"));
            }

            if(lastKnownLocation != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(lastKnownLocation.getLatitude(),
                                lastKnownLocation.getLongitude()), CURRENT_LOCATION_ZOOM));

                currMarker = mMap.addMarker(new MarkerOptions().title("Current Location")
                        .icon(getLocationMarkerBitmap())
                        .position(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude())));
            } else {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pickupLocation, CURRENT_LOCATION_ZOOM));

                currMarker = mMap.addMarker(new MarkerOptions().title("Current Location")
                        .icon(getLocationMarkerBitmap())
                        .position(pickupLocation));
            }
    }

    private void getAmbulancesTypesAvailable(List<String> ambulanceKeys) {
        for (String ambulanceKey : ambulanceKeys) {

                DatabaseReference ambulanceTypesDatabaseReference = FirebaseDatabase.getInstance().getReference().child("AmbulanceTypes").child(ambulanceKey);
                ambulanceTypesDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.getValue() != null) {

                            if(snapshot.getValue().toString().equals("ALS")) {
                                alsAmbulanceKeys.add(ambulanceKey);
                            }

                            if(snapshot.getValue().toString().equals("BLS")) {
                                blsAmbulanceKeys.add(ambulanceKey);
                            }

                            if(snapshot.getValue().toString().equals("Mortgage")) {
                                mortgageAmbulanceKeys.add(ambulanceKey);
                            }

                            if(snapshot.getValue().toString().equals("Intercity")) {
                                intercityAmbulanceKeys.add(ambulanceKey);
                            }

                            if (ambulanceTypesAvailable.size() < 4) {
                                if (!ambulanceTypesAvailable.contains(snapshot.getValue().toString())) {
                                    ambulanceTypesAvailable.add(snapshot.getValue().toString());
                                    Log.e(TAG, "Ambulance Type Found: " + snapshot.getValue().toString() + "!");
                                }
                            }
                        }
                        displayAmbulanceTypes();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }

    }

    private void setAmbulanceTypeSelectionListener(ConstraintLayout ambulanceType, int ambulancePrice, String ambulanceTypeString) {
         ambulanceType.setOnClickListener(v -> {

             selectedAmbulancePrice = ambulancePrice;
             
             if(ambulanceType != selectedButton) {

                 int startColor = Color.parseColor("#fafafa");
                 int endColor = Color.parseColor("#e0e0e0");

                 ValueAnimator valueAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
                 valueAnimator.setDuration(300);
                 valueAnimator.setInterpolator(new LinearInterpolator());

                 valueAnimator.addUpdateListener(animation -> {
//                     Log.e("SetAmbulanceType", "Color: " + animation.getAnimatedValue().toString());
                     ambulanceType.setBackgroundColor((int) animation.getAnimatedValue());
                 });

                 valueAnimator.start();

                 if (selectedButton != null) {
                     ValueAnimator revertAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), endColor, startColor);
                     revertAnimation.setDuration(300);
                     revertAnimation.setInterpolator(new LinearInterpolator());

                     revertAnimation.addUpdateListener(animation -> {
//                         Log.e("SetAmbulanceType", "Color: " + animation.getAnimatedValue().toString());
                         selectedButton.setBackgroundColor((int) animation.getAnimatedValue());
                     });

                     revertAnimation.start();
                 }


                 selectedButton = ambulanceType;
                 selectedAmbulancePrice = ambulancePrice;
                 Toast.makeText(MapsActivity.this, ambulanceTypeString + " Selected", Toast.LENGTH_SHORT).show();
             }
         });
    }

    private void displayAmbulanceTypes() {
        if(ambulanceTypesAvailable.contains("ALS")){
            alsButton.setVisibility(View.VISIBLE);
        }
        if(ambulanceTypesAvailable.contains("BLS")){
            blsButton.setVisibility(View.VISIBLE);
        }
        if(ambulanceTypesAvailable.contains("Mortgage")){
            mortgageButton.setVisibility(View.VISIBLE);
        }
        if(ambulanceTypesAvailable.contains("Intercity") && !pickupCity.equalsIgnoreCase(hospitalCity)){
            intercityButton.setVisibility(View.VISIBLE);
        }
    }

    private void displayRoutes() {
        if(pickupLocation != null && hospitalLocation != null && pickupAddressEditText.getText() != "" && hospitalAddressEditText.getText() != "") {

            hospitalPickupDistance = 0;
            alsPrice = 0;
            blsPrice = 0;
            intercityPrice = 0;
            mortgagePrice = 0;

            List<LatLng> routesLatLng = new ArrayList<>();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://maps.googleapis.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            RetrofitCallbacks callbacks = retrofit.create(RetrofitCallbacks.class);

            Call<RoutesResponsePojo> routesResultCallback = callbacks.getRoute(
                    pickupLocation.latitude + "," + pickupLocation.longitude,
                    hospitalLocation.latitude + "," + hospitalLocation.longitude,
                    getString(R.string.google_maps_key)
            );

            try {
                routesResultCallback.enqueue(new Callback<RoutesResponsePojo>() {
                    @Override
                    public void onResponse(@NotNull Call<RoutesResponsePojo> call, @NotNull Response<RoutesResponsePojo> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            for (Route route : response.body().getRoutes()) {
                                for (Leg leg : route.getLegs()) {
                                    for (Step step : leg.getSteps()) {
                                        List<LatLng> decodedPoints = PolyUtil.decode(step.getPolyline().getPoints());
                                        hospitalPickupDistance += step.getDistance().getValue();
                                        routesLatLng.addAll(decodedPoints);
                                    }
                                }
                            }
                        }

                        String distance = String.valueOf(hospitalPickupDistance);
                        int distanceKm;
                        if(distance.length() > 3)
                             distanceKm = Integer.parseInt(distance.substring(0, distance.length() - 2));
                        else
                            distanceKm = 0;

                        if(distanceKm > 5) distanceKm = distanceKm - 5;
                        else distanceKm = 0;

                        blsPrice = (distanceKm * 50) + 300;
                        alsPrice = (distanceKm * 50) + 500;
                        mortgagePrice = (distanceKm * 50) + 250;
                        intercityPrice = (distanceKm * 50);

                        setAmbulancePrice(alsPriceTextView, String.valueOf(alsPrice));
                        setAmbulancePrice(blsPriceTextView, String.valueOf(blsPrice));
                        setAmbulancePrice(mortgagePriceTextView, String.valueOf(mortgagePrice));
                        setAmbulancePrice(intercityPriceTextView, String.valueOf(intercityPrice));

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();

                        for (LatLng point : routesLatLng)
                            builder.include(point);

                        PolylineOptions polylineOptions = new PolylineOptions()
                                .width(5)
                                .color(Color.BLACK)
                                .geodesic(true);
                        polylineOptions.addAll(routesLatLng);

                        if (lastPlottedPolyline != null) {
                            lastPlottedPolyline.remove();
                        }

                        lastPlottedPolyline = mMap.addPolyline(polylineOptions);

                        LatLngBounds bounds = builder.build();
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 50);

                        locationSelectionLayout.setVisibility(View.GONE);
                        locationSelectionLayout.setVisibility(View.GONE);
                        paymentBottomPanel.setVisibility(View.VISIBLE);
                        mMap.animateCamera(cameraUpdate);

                        hamburger3.setVisibility(View.GONE);
                        hamburger4.setVisibility(View.VISIBLE);
						
						
						setAmbulanceTypeSelectionListener(intercityButton, intercityPrice, "Intercity");
						setAmbulanceTypeSelectionListener(alsButton, alsPrice, "ALS");
						setAmbulanceTypeSelectionListener(blsButton, blsPrice, "BLS");
						setAmbulanceTypeSelectionListener(mortgageButton, mortgagePrice, "Mortgage");
                    }

                    @Override
                    public void onFailure(@NotNull Call<RoutesResponsePojo> call, @NotNull Throwable t) {
                        t.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

//    private void animateBottomLocationBar() {
//        if (pickupLocation != null && hospitalLocation!=null) {
//
//            Animation slide_down = AnimationUtils.loadAnimation(getApplicationContext(),
//                    R.anim.slide_down);
//
//            slide_down.setAnimationListener(new Animation.AnimationListener() {
//                @Override
//                public void onAnimationStart(Animation animation) {
//                }
//
//                @Override
//                public void onAnimationEnd(Animation animation) {
//                    locationSelectionLayout.setVisibility(View.GONE);
//                    animatePaymentBottomPanel();
//                }
//
//                @Override
//                public void onAnimationRepeat(Animation animation) {
//                }
//            });
//
//            locationSelectionLayout.startAnimation(slide_down);
//        }
//    }

//    private void animatePaymentBottomPanel() {
//        if (pickupLocation != null && hospitalLocation!=null) {
//
//            Animation slide_up = AnimationUtils.loadAnimation(getApplicationContext(),
//                    R.anim.slide_up);
//
//            slide_up.setAnimationListener(new Animation.AnimationListener() {
//                @Override
//                public void onAnimationStart(Animation animation) {
//                    locationSelectionLayout.setVisibility(View.VISIBLE);
//                }
//
//                @Override
//                public void onAnimationEnd(Animation animation) {}
//
//                @Override
//                public void onAnimationRepeat(Animation animation) {}
//            });
//
//            paymentBottomPanel.startAnimation(slide_up);
//        }
//    }

    private void setAmbulancePrice(TextView ambulacePriceTextView, String price) {
        ambulacePriceTextView.setText(price);
    }
    
    private BitmapDescriptor getBitmapFromVector() {
        Drawable vectorDrawable = ContextCompat.getDrawable(this, R.drawable.ic_ambulance);
        Bitmap bitmap = null;
        if (vectorDrawable != null) {
            vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
            bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.draw(canvas);
        }

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private BitmapDescriptor getLocationMarkerBitmap() {
        Drawable vectorDrawable = ContextCompat.getDrawable(this, R.drawable.ic_location);
        Bitmap bitmap = null;
        if (vectorDrawable != null) {
            vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
            bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.draw(canvas);
        }

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onPaymentSuccess(String s) {
        Intent ambulanceBookingIntent = new Intent(MapsActivity.this, BookingConfirmation.class);
        ambulanceBookingIntent.putExtra("PickupLatLng", pickupLocation);

        ArrayList<String> selectedAmbulanceKeys = new ArrayList<>();

        if(selectedButton == alsButton)
            selectedAmbulanceKeys = (ArrayList<String>) alsAmbulanceKeys;

        else if(selectedButton == blsButton)
            selectedAmbulanceKeys = (ArrayList<String>) blsAmbulanceKeys;


        else if(selectedButton == mortgageButton)
            selectedAmbulanceKeys = (ArrayList<String>) mortgageAmbulanceKeys;

        else if(selectedButton == intercityButton)
            selectedAmbulanceKeys = (ArrayList<String>) intercityAmbulanceKeys;


        if(selectedAmbulanceKeys.size() > 0) {
            ambulanceBookingIntent.putStringArrayListExtra("AmbulanceKeys", selectedAmbulanceKeys);
            startActivity(ambulanceBookingIntent);
            finish();
        }
        else
            Toast.makeText(MapsActivity.this, "No Ambulance Available! Please Select Available Ambulance", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onPaymentError(int i, String s) {
        Log.i(TAG, "RazorPay Error");
    }

//    public void getJourneyTime() {
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("https://maps.googleapis.com/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        RetrofitCallbacks callbacks = retrofit.create(RetrofitCallbacks.class);
//
//        String pickupPointString = pickupLocation.latitude + "," + pickupLocation.longitude;
//        String destinationPointString = hospitalLocation.latitude + "," + hospitalLocation.longitude;
//
//        Call<DistanceMatrixResponsePojo> journeyTimeCallback = callbacks.getJourneyTime(
//                pickupPointString,
//                destinationPointString,
//                getString(R.string.google_maps_key)
//        );
//
//        try {
//            journeyTimeCallback.enqueue(new Callback<DistanceMatrixResponsePojo>() {
//                @Override
//                public void onResponse(@NotNull Call<DistanceMatrixResponsePojo> call, @NotNull Response<DistanceMatrixResponsePojo> response) {
//                    if (response.body() != null) {
//                        for (Row row : response.body().getRows()) {
//                            for (Element element : row.getElements()) {
//                                duration += element.getDuration().getValue();
//                            }
//                        }
//                    }
//                }
//
//                @Override
//                public void onFailure(@NotNull Call<DistanceMatrixResponsePojo> call, @NotNull Throwable t) {
//
//                }
//            });
//        } catch (Exception e) {
//
//        }
//    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if(lastKnownLocation == null)
            lastKnownLocation = location;

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(lastKnownLocation.getLatitude(),
                            lastKnownLocation.getLongitude()), CURRENT_LOCATION_ZOOM));


            locationSelectionLayout.setVisibility(View.VISIBLE);

            if(currMarker == null)
                currMarker = mMap.addMarker(new MarkerOptions().title("Current Location")
                    .icon(getLocationMarkerBitmap())
                    .position(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude())));
    }
}