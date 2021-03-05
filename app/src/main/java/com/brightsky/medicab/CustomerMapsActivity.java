package com.brightsky.medicab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brightsky.medicab.pickuppointmodel.Candidate;
import com.brightsky.medicab.pickuppointmodel.PlacesSearchResponsePojo;
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
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;
import com.mecofarid.squeezeloader.SqueezeLoader;
import com.razorpay.PaymentResultListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CustomerMapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, PaymentResultListener {

    private GoogleMap mMap;
    private Location currentLocation;
    private ConstraintLayout pickupLocationButton;
    private ConstraintLayout hospitalLocationButton;
    private ConstraintLayout pickupLocationField;
    private ConstraintLayout hospitalLocationField;
    private TextView pickupLocationText;
    private TextView hospitalLocationText;
    private FrameLayout locationPanel;
    private boolean userIsSelectingLocation;

    private boolean driverFound = false;
    private Button confirmButton;
    private ImageView currentLocationMarker;
    private TextView navigationProfileName;
    private TextView navigationProfilePhoneNumber;
    private ImageView navProfileImage;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private Polyline lastPlottedPolyline;

    private String hospitalLocationName;

    private String selectedAmbulanceType;

    private LatLng pickupLatLng;
    private LatLng hospitalLatLng;
    private LatLng onMapLatLng;

    private Marker pickupMarker;
    private Marker hospitalMarker;
    private int hospitalPickupDistance;
    private int alsPrice;
    private int blsPrice;
    private int intercityPrice;
    private int mortgagePrice;

    private ExecutorService executorService;
    private Handler mainHandler;

    private GpsReceiver gpsReceiver;

    private List<String> ambulanceKeys;
    private List<String> alsAmbulanceKeys;
    private List<String> blsAmbulanceKeys;
    private List<String> mortgageAmbulanceKeys;
    private List<String> intercityAmbulanceKeys;
//    private List<Marker> ambulanceMarkers;
    private HashMap<String, Marker> ambulanceMarkersHashMap;
    private BottomSelectionFragment bottomSelectionFragment;

    //    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar toolbar;

    private ImageView backButton;
    private ImageView pickupLocationIcon;
    private ImageView dropOffLocationIcon;
    private NetworkChangeListener networkChangeListener;
    private AlertDialog.Builder networkUnavailableAlerty;

    private PickupSearchResultRecyclerviewAdapter pickupSearchResultRecyclerviewAdapter = null;
    private RecyclerView pickupSearchResultRecyclerView;
    private SqueezeLoader pickupSqueezeLoader;
    private LinearLayout pickupSearchLayout;
    private LinearLayout hospitalSearchLayout;

    private List<PlaceSearchResultModel> pickupSearchResults;


    private RecyclerView hospitalSearchResultRecyclerView;
    private List<PlaceSearchResultModel> hospitalSearchResults;

    private EditText hospitalSearchEditText;
    private TextView recentHospitalTitle;

    private HospitalSearchResultRecyclerviewAdapter hospitalSearchResultRecyclerviewAdapter = null;
    private SqueezeLoader hospitalSqueezeLoader;

    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_maps);

        pickupLocationButton = findViewById(R.id.pickup_field_button);
        hospitalLocationButton = findViewById(R.id.dropoff_field_button);
        pickupLocationText = findViewById(R.id.pickup_location_name);
        hospitalLocationText = findViewById(R.id.dropoff_location_name);
        pickupLocationField = findViewById(R.id.pickup_field);
        hospitalLocationField = findViewById(R.id.dropoff_field);
        confirmButton = findViewById(R.id.confirm_button);
        currentLocationMarker = findViewById(R.id.location_marker);
        locationPanel = findViewById(R.id.location_panel);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar_maps);
        NavigationView navigationView = findViewById(R.id.navigation_view_maps);
        ImageView recenterButton = findViewById(R.id.recenter_button);
        backButton = findViewById(R.id.back_button);
        pickupLocationIcon = findViewById(R.id.pickup_location_icon);
        dropOffLocationIcon = findViewById(R.id.dropoff_location_icon);
        pickupSqueezeLoader = findViewById(R.id.pickup_loading_bar);
        EditText pickupSearchEditText = findViewById(R.id.pickup_search_edit_text);
        pickupSearchResultRecyclerView = findViewById(R.id.pickup_search_result_recycler_view);
        LinearLayout pickupLocateOnMapButton = findViewById(R.id.pickup_locate_on_map_button);
        pickupSearchLayout = findViewById(R.id.pickup_search_layout);
        ImageView pickupSearchBackButton = findViewById(R.id.pickup_back_button);

        hospitalSearchLayout = findViewById(R.id.hospital_search_layout);
        ImageView hospitalBackButton = findViewById(R.id.hospital_back_button);
        hospitalSqueezeLoader = findViewById(R.id.hospital_loading_bar);
        recentHospitalTitle = findViewById(R.id.recent_search_title);
        LinearLayout hospitalLocateOnMapButton = findViewById(R.id.hospital_locate_on_map_button);
        hospitalSearchEditText = findViewById(R.id.hospital_search_edit_text);
        hospitalSearchResultRecyclerView = findViewById(R.id.hospital_search_result_recycler_view);
        RecyclerView recentHospitalSearchRecyclerView = findViewById(R.id.recent_search_hospitals_recycler_view);


        ExecutorThreadService executorThreadService = ExecutorThreadService.getInstance();
        executorService = executorThreadService.getExecutor();
        mainHandler = executorThreadService.getHandler();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

        userIsSelectingLocation = false;
        pickupSearchResultRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        networkUnavailableAlerty = new AlertDialog.Builder(CustomerMapsActivity.this)
                .setTitle("Network Disconnected")
                .setMessage("Internet Disconnected! Please Reconnect with the Internet to be able to use Medicab")
                .setPositiveButton("Go To Settings", (dialog, which) -> {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setClassName("com.android.phone", "com.android.phone.NetworkSetting");
                    startActivity(intent);
                })
                .setCancelable(false);

        if(mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User Is Not Logged In", Toast.LENGTH_SHORT).show();
        }

        else {
            String uid = mAuth.getCurrentUser().getUid();
            DatabaseReference clientsProfileReference = mDatabase.getReference("ClientsProfile").child(uid);






            //Navigation Panel

            setSupportActionBar(toolbar);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.close, R.string.open);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
            toggle.getDrawerArrowDrawable().setColor(Color.parseColor("#212121"));

            View headerView = navigationView.getHeaderView(0);
            navigationProfileName = headerView.findViewById(R.id.nav_name_new);
            navigationProfilePhoneNumber = headerView.findViewById(R.id.nav_phone_number);
            navProfileImage = headerView.findViewById(R.id.nav_profile);

            clientsProfileReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.exists()) {
                        String nameValue = Objects.requireNonNull(snapshot.child("FirstName").getValue()).toString();
                        String imageUrlValue = Objects.requireNonNull(snapshot.child("ImageUrl").getValue()).toString();
                        String phoneNumberUrl = Objects.requireNonNull(snapshot.child("PhoneNo").getValue()).toString();

                        navigationProfileName.setText(nameValue);
                        navigationProfilePhoneNumber.setText(phoneNumberUrl);

                        Glide.with(CustomerMapsActivity.this)
                                .load(imageUrlValue)
                                .placeholder(R.mipmap.avatar_dummy)
                                .into(navProfileImage);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    error.toException().printStackTrace();
                    Toast.makeText(CustomerMapsActivity.this, "Error Fetching Profile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });






            //Maps and Places API

            Places.initialize(getApplicationContext(), "AIzaSyDTyvJynEqi6qChFNEgNaCXqJrLBN8BIFk");
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }






            pref = getApplicationContext().getSharedPreferences("MyPref", 0);

            List<RecentHospitalSearchResult> recentHospitalSearchResults = getRecentSearches();
            RecentSearchHospitalRecyclerViewAdapter recentSearchHospitalRecyclerViewAdapter = new RecentSearchHospitalRecyclerViewAdapter(recentHospitalSearchResults);


            hospitalSearchResultRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            recentHospitalSearchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            recentHospitalSearchRecyclerView.setAdapter(recentSearchHospitalRecyclerViewAdapter);





            //Click Listeners

            headerView.setOnClickListener(v -> {
                Intent intent = new Intent(CustomerMapsActivity.this, ProfileUpdateActivity.class);
                startActivity(intent);
            });

            recenterButton.setOnClickListener(v -> {
                if (currentLocation != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(currentLocation.getLatitude(),
                                    currentLocation.getLongitude()), 15));
                } else {
                    getCurrentFusedLocation();
                }
            });

            pickupSearchBackButton.setOnClickListener(v -> {
                pickupSearchLayout.setVisibility(View.GONE);
                pickupSearchEditText.setText("");
                pickupSearchEditText.setHint("Search Pickup Location");
                locationPanel.setVisibility(View.VISIBLE);
                toolbar.setVisibility(View.VISIBLE);
            });

            hospitalBackButton.setOnClickListener(v -> {
                hospitalSearchLayout.setVisibility(View.GONE);
                hospitalSearchEditText.setText("");
                hospitalSearchEditText.setHint("Search Pickup Location");
                locationPanel.setVisibility(View.VISIBLE);
                toolbar.setVisibility(View.VISIBLE);
            });

            pickupLocationButton.setOnClickListener(v -> {
                pickupSearchLayout.setVisibility(View.VISIBLE);
                locationPanel.setVisibility(View.GONE);
                toolbar.setVisibility(View.GONE);
            });

            hospitalLocationButton.setOnClickListener(v -> {
                locationPanel.setVisibility(View.GONE);
                hospitalSearchLayout.setVisibility(View.VISIBLE);
                toolbar.setVisibility(View.GONE);
            });

            backButton.setOnClickListener(v -> backPressed());

            pickupSearchEditText.addTextChangedListener(new TextWatcher() {
                private Timer timer = new Timer();

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    pickupSqueezeLoader.setVisibility(View.VISIBLE);
                    timer.cancel();
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            getPickupLists(s.toString());
                        }
                    }, 1000);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            hospitalSearchEditText.addTextChangedListener(new TextWatcher() {
                private Timer timer = new Timer();

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    hospitalSqueezeLoader.setVisibility(View.VISIBLE);

                    timer.cancel();
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            getHospitalLists(s.toString());
                        }
                    }, 3000);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            pickupLocateOnMapButton.setOnClickListener(view -> {
                pickupSearchLayout.setVisibility(View.GONE);
                locationPanel.setVisibility(View.VISIBLE);
                toolbar.setVisibility(View.VISIBLE);

                if(mMap != null) {
                    userIsSelectingLocation = true;

                    pickupLocationIcon.setImageDrawable(ContextCompat.getDrawable(CustomerMapsActivity.this, R.drawable.pickup_blue));

                    toolbar.setVisibility(View.GONE);
                    backButton.setVisibility(View.VISIBLE);

                    confirmButton.setVisibility(View.VISIBLE);
                    hospitalLocationField.setVisibility(View.GONE);
                    hospitalLocationButton.setClickable(false);
                    currentLocationMarker.setVisibility(View.VISIBLE);

                    mMap.setOnCameraIdleListener(() -> executorService.execute(() -> {
                        mainHandler.post(() -> {
                            onMapLatLng = mMap.getCameraPosition().target;
                            String currentLocationAddress = getCurrentLocationAddress(onMapLatLng);
                            pickupLocationText.setText(currentLocationAddress);
                        });
                    }));

                    confirmButton.setOnClickListener(v -> {
                        mMap.setOnCameraIdleListener(() -> {});

                        userIsSelectingLocation = false;

                        if(onMapLatLng == null) {
                            onMapLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            String currentLocationAddress = getCurrentLocationAddress(onMapLatLng);
                            pickupLocationText.setText(currentLocationAddress);
                        }

                        pickupLatLng = onMapLatLng;

                        confirmButton.setVisibility(View.GONE);
                        hospitalLocationField.setVisibility(View.VISIBLE);
                        hospitalLocationButton.setClickable(true);
                        currentLocationMarker.setVisibility(View.GONE);
                        toolbar.setVisibility(View.VISIBLE);
                        backButton.setVisibility(View.GONE);

                        if(pickupMarker != null)
                            pickupMarker.remove();

                        pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLatLng));

                        getNearbyCaptains(pickupLatLng);
                        moveCameraOnLocationFieldsUpdate();
                        onMapLatLng = null;
                    });
                }
            });

            hospitalLocateOnMapButton.setOnClickListener(view -> {
                hospitalSearchLayout.setVisibility(View.GONE);
                locationPanel.setVisibility(View.VISIBLE);
                toolbar.setVisibility(View.VISIBLE);

                if(mMap != null) {
                    userIsSelectingLocation = true;

                    dropOffLocationIcon.setImageDrawable(ContextCompat.getDrawable(CustomerMapsActivity.this, R.drawable.dropone_blue));

                    confirmButton.setVisibility(View.VISIBLE);
                    pickupLocationField.setVisibility(View.GONE);
                    pickupLocationButton.setClickable(false);
                    currentLocationMarker.setVisibility(View.VISIBLE);

                    toolbar.setVisibility(View.GONE);
                    backButton.setVisibility(View.VISIBLE);

                    mMap.setOnCameraIdleListener(() -> executorService.execute(() -> {
                        mainHandler.post(() -> {
                            onMapLatLng = mMap.getCameraPosition().target;
                            String currentLocationAddress = getCurrentLocationAddress(onMapLatLng);
                            hospitalLocationText.setText(currentLocationAddress);
                        });
                    }));

                    confirmButton.setOnClickListener(v -> {
                        mMap.setOnCameraIdleListener(() -> {});

                        userIsSelectingLocation = false;

                        if(onMapLatLng == null) {
                            onMapLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            String currentLocationAddress = getCurrentLocationAddress(onMapLatLng);
                            hospitalLocationText.setText(currentLocationAddress);
                        }

                        hospitalLatLng = onMapLatLng;

                        confirmButton.setVisibility(View.GONE);
                        pickupLocationField.setVisibility(View.VISIBLE);
                        pickupLocationButton.setClickable(true);
                        currentLocationMarker.setVisibility(View.GONE);
                        toolbar.setVisibility(View.VISIBLE);
                        backButton.setVisibility(View.GONE);

                        if(hospitalMarker != null)
                            hospitalMarker.remove();

                        hospitalMarker = mMap.addMarker(new MarkerOptions().position(hospitalLatLng));

                        moveCameraOnLocationFieldsUpdate();
                        onMapLatLng = null;
                    });
                }
            });
        }
    }

    private List<RecentHospitalSearchResult> getRecentSearches() {
        Set<String> hospitalNameSet = pref.getStringSet("HospitalName", null);
        Set<String> hospitalAddressSet = pref.getStringSet("HospitalAddress", null);
        Set<String> hospitalLatSet = pref.getStringSet("HospitalLat", null);
        Set<String> hospitalLongSet = pref.getStringSet("HospitalLong", null);

        List<RecentHospitalSearchResult> recentHospitalSearchResults = new ArrayList<>();

        if(hospitalNameSet != null && hospitalAddressSet != null && hospitalLatSet != null && hospitalLongSet != null) {
            List<String> hospitalNameList = new ArrayList<>(hospitalNameSet);
            List<String> hospitalAddressList = new ArrayList<>(hospitalAddressSet);
            List<String> hospitalLatList = new ArrayList<>(hospitalLatSet);
            List<String> hospitalLongList = new ArrayList<>(hospitalLongSet);

            if(hospitalNameList.size() > 0) {
                recentHospitalTitle.setVisibility(View.VISIBLE);
            }

            for (int i = 0; i < hospitalNameList.size() && i < 5; i++) {
                String hospitalName = hospitalNameList.get(i);
                String hospitalAddress = hospitalAddressList.get(i);
                String hospitalLat = hospitalLatList.get(i);
                String hospitalLong = hospitalLongList.get(i);

                RecentHospitalSearchResult recentHospitalSearchResult = new RecentHospitalSearchResult(hospitalName, hospitalAddress, hospitalLat, hospitalLong);
                recentHospitalSearchResults.add(recentHospitalSearchResult);
            }
        }

        return recentHospitalSearchResults;
    }

    private void getPickupLists(String text) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitCallbacks callbacks = retrofit.create(RetrofitCallbacks.class);

        Call<PlacesSearchResponsePojo> pickupSearchResultCallback = callbacks.getPlaceSearchResult(text, "textquery", "formatted_address,name,geometry", getString(R.string.google_maps_key));
        pickupSearchResultCallback.enqueue(new Callback<PlacesSearchResponsePojo>() {
            @Override
            public void onResponse(@NotNull Call<PlacesSearchResponsePojo> call, @NotNull Response<PlacesSearchResponsePojo> response) {
                PlacesSearchResponsePojo pickups = response.body();
                pickupSearchResults = new ArrayList<>();

                if (pickups != null) {
                    for(int i = 0; i < pickups.getCandidates().size(); i++) {
                        pickupSearchResults.add(new PlaceSearchResultModel(pickups.getCandidates().get(i), pickups.getCandidates().get(i).getFormattedAddress()));
                    }

                    if (pickupSearchResultRecyclerviewAdapter == null) {
                        pickupSearchResultRecyclerviewAdapter = new PickupSearchResultRecyclerviewAdapter(pickupSearchResults);
                        pickupSearchResultRecyclerView.setAdapter(pickupSearchResultRecyclerviewAdapter);
                    } else {
                        pickupSearchResultRecyclerviewAdapter.updateResults(pickupSearchResults);
                        pickupSearchResultRecyclerviewAdapter.notifyDataSetChanged();
                    }
                }

                else {
                    Toast.makeText(CustomerMapsActivity.this, "No Result Found", Toast.LENGTH_SHORT).show();
                }

                pickupSqueezeLoader.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(@NotNull Call<PlacesSearchResponsePojo> call, @NotNull Throwable t) {
                t.printStackTrace();
                Toast.makeText(CustomerMapsActivity.this, "Error Fetching Results", Toast.LENGTH_SHORT).show();

                pickupSqueezeLoader.setVisibility(View.INVISIBLE);
            }
        });
    }

    private boolean backPressed() {
        if(userIsSelectingLocation) {
            mMap.setOnCameraIdleListener(() -> {});
            currentLocationMarker.setVisibility(View.GONE);
            confirmButton.setVisibility(View.GONE);

            if(pickupLocationField.getVisibility() == View.VISIBLE) {
                pickupLocationText.setText("");
                pickupLocationText.setHint("Enter Your Pickup Address");

                hospitalLocationField.setVisibility(View.VISIBLE);
                hospitalLocationButton.setClickable(true);

                if(pickupMarker != null) {
                    pickupMarker.remove();
                }

                pickupLatLng = null;

                pickupLocationIcon.setImageDrawable(ContextCompat.getDrawable(CustomerMapsActivity.this, R.drawable.pickupone));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15));
                onMapLatLng = null;
            }

            if(hospitalLocationField.getVisibility() == View.VISIBLE) {
                hospitalLocationText.setText("");
                hospitalLocationText.setHint("Enter Your Pickup Address");

                if(pickupMarker != null) {
                    pickupMarker.remove();
                }

                hospitalLatLng = null;

                dropOffLocationIcon.setImageDrawable(ContextCompat.getDrawable(CustomerMapsActivity.this, R.drawable.dropone));
                pickupLocationField.setVisibility(View.VISIBLE);
                pickupLocationButton.setClickable(true);

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15));
                onMapLatLng = null;
            }
        }

        else {
            if (pickupLatLng != null && hospitalLatLng != null) {
                if (bottomSelectionFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .remove(bottomSelectionFragment)
                            .commit();
                }

                pickupLocationText.setText("");
                pickupLocationText.setHint("Enter Your Pickup Address");
                hospitalLocationText.setText("");
                hospitalLocationText.setHint("Enter Hospital Address");

                pickupLatLng = null;
                hospitalLatLng = null;

                pickupLocationIcon.setImageDrawable(ContextCompat.getDrawable(CustomerMapsActivity.this, R.drawable.pickupone));
                dropOffLocationIcon.setImageDrawable(ContextCompat.getDrawable(CustomerMapsActivity.this, R.drawable.dropone));

                lastPlottedPolyline.remove();
                pickupMarker.remove();
                hospitalMarker.remove();

                locationPanel.setVisibility(View.VISIBLE);
                mMap.setOnCameraIdleListener(() -> {});

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15));
                onMapLatLng = null;

            }

            else if (pickupLatLng != null) {
                pickupLatLng = null;
                pickupMarker.remove();
                pickupLocationText.setText("");
                pickupLocationText.setHint("Enter Your Pickup Address");

                pickupLocationField.setVisibility(View.VISIBLE);
                pickupLocationButton.setClickable(true);
                currentLocationMarker.setVisibility(View.GONE);
                confirmButton.setVisibility(View.GONE);

                mMap.setOnCameraIdleListener(() -> {});

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15));
                onMapLatLng = null;

            }

            else if (hospitalLatLng != null) {
                hospitalLocationText.setText("");
                hospitalLocationText.setHint("Enter Hospital Address");
                hospitalLatLng = null;

                hospitalLocationField.setVisibility(View.VISIBLE);
                hospitalLocationButton.setClickable(true);
                currentLocationMarker.setVisibility(View.GONE);
                confirmButton.setVisibility(View.GONE);

                hospitalMarker.remove();

                mMap.setOnCameraIdleListener(() -> {});

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15));
                onMapLatLng = null;

            }

            else {
                backButton.setVisibility(View.GONE);
                toolbar.setVisibility(View.VISIBLE);
                return false;
            }
        }

        backButton.setVisibility(View.GONE);
        toolbar.setVisibility(View.VISIBLE);
        return true;
    }

    @Override
    public void onBackPressed() {
        boolean backPress = backPressed();
        if(!backPress) {
            super.onBackPressed();
        }
        else {
            backButton.setVisibility(View.GONE);
            toolbar.setVisibility(View.VISIBLE);
        }
    }

//        private void backButtonListener() {
//        if (pickupLatLng != null && hospitalLatLng != null) {
//
//            getSupportFragmentManager().beginTransaction()
//                    .remove(ambulanceTypesFragment)
//                    .commit();
//
////            getSupportFragmentManager().beginTransaction()
////                    .replace(BOTTOM_FRAGMENT_ID, bottomLocationSelectionFragment)
////                    .commit();
//
//            pickupLatLng = null;
//            hospitalLatLng = null;
//            onMapLatLng = null;
//
//            pickupMarker.remove();
//            hospitalMarker.remove();
//
//            lastPlottedPolyline.remove();
//
//            ambulanceKeys = new ArrayList<>();
//            alsAmbulanceKeys = new ArrayList<>();
//            blsAmbulanceKeys = new ArrayList<>();
//            mortgageAmbulanceKeys = new ArrayList<>();
//            intercityAmbulanceKeys = new ArrayList<>();
//            ambulanceMarkers = new ArrayList<>();
//            ambulanceMarkersHashMap = new HashMap<>();
//
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15));
//            backButton.setVisibility(View.GONE);
//
//        } else if (pickupLatLng != null) {
//            onMapHospitalLocationName = "";
//            centerMapMarker.setVisibility(View.GONE);
////            bottomLocationSelectionFragment.CLICKABILITY = true;
////            bottomLocationSelectionFragment.getConfirmHospitalLocationButton().setVisibility(View.GONE);
//
//            mMap.setOnCameraIdleListener(() -> {
//            });
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pickupLatLng, 15));
////            bottomLocationSelectionFragment.updateHospitalAddress("Enter The Hospital Address");
////            bottomLocationSelectionFragment.getHospitalAddressTextView().setHintTextColor(Color.parseColor("#919191"));
//
//            if (hospitalMarker != null) {
//                hospitalMarker.remove();
//            }
//
//            backButton.setVisibility(View.GONE);
//        } else if (hospitalLatLng != null) {
//            onMapPickupLocationName = "";
//            centerMapMarker.setVisibility(View.GONE);
////            bottomLocationSelectionFragment.getConfirmPickupLocationButton().setVisibility(View.GONE);
////            bottomLocationSelectionFragment.CLICKABILITY = true;
//
//            mMap.setOnCameraIdleListener(() -> {
//            });
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(hospitalLatLng, 15));
////            bottomLocationSelectionFragment.updatePickupAddress("Enter The Pickup Address");
////            bottomLocationSelectionFragment.getPickupAddressTextView().setHintTextColor(Color.parseColor("#919191"));
//
//            if (pickupMarker != null) {
//                pickupMarker.remove();
//            }
//
//            backButton.setVisibility(View.GONE);
//        } else if (hospitalLatLng == null && pickupLatLng == null) {
//            onMapPickupLocationName = "";
//            onMapHospitalLocationName = "";
//            centerMapMarker.setVisibility(View.GONE);
////            bottomLocationSelectionFragment.CLICKABILITY = true;
//
////            bottomLocationSelectionFragment.getConfirmHospitalLocationButton().setVisibility(View.GONE);
////            bottomLocationSelectionFragment.getConfirmPickupLocationButton().setVisibility(View.GONE);
//
//            mMap.setOnCameraIdleListener(() -> {
//            });
//
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                    new LatLng(currentLocation.getLatitude(),
//                            currentLocation.getLongitude()), 15));
//
////            bottomLocationSelectionFragment.updatePickupAddress("Enter The Pickup Address");
////            bottomLocationSelectionFragment.updateHospitalAddress("Enter The Hospital Address");
////
////            bottomLocationSelectionFragment.getPickupAddressTextView().setHintTextColor(Color.parseColor("#919191"));
////            bottomLocationSelectionFragment.getHospitalAddressTextView().setHintTextColor(Color.parseColor("#919191"));
//
//            if (pickupMarker != null) {
//                pickupMarker.remove();
//            }
//
//            if (hospitalMarker != null) {
//                hospitalMarker.remove();
//            }
//
//            backButton.setVisibility(View.INVISIBLE);
//        }
//    }

    private void getCurrentFusedLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        currentLocation = task.getResult();

                        if(currentLocation != null) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(currentLocation.getLatitude(),
                                            currentLocation.getLongitude()), 15));
                        }
                    } else {
                        Log.e("TAG", "Error Fetching Current Location");
                        Toast.makeText(CustomerMapsActivity.this, "Current Location Couldn't be fetched", Toast.LENGTH_SHORT).show();
                    }
                });

//                LocationRequest locationRequest = LocationRequest.create();
//                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//                locationRequest.setInterval(1000);
//                LocationCallback locationCallback = new LocationCallback() {
//                    @Override
//                    public void onLocationResult(LocationResult locationResult) {
//                        super.onLocationResult(locationResult);
//
//                        if (locationResult != null) {
//                            for (Location location : locationResult.getLocations()) {
//                                currentLocation = location;
//
//                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                                        new LatLng(currentLocation.getLatitude(),
//                                                currentLocation.getLongitude()), 15));
//
//                            }
//                        }
//                    }
//                };
//
//                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            }
        } catch (Exception e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void getDeviceCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setCompassEnabled(false);
//        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(CustomerMapsActivity.this, R.raw.maps_style));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            if (checkIfLocationIsEnabled()) {
                getDeviceCurrentLocation();
            }
            getCurrentFusedLocation();
        } else {
            Toast.makeText(CustomerMapsActivity.this, "Location Permission Denied", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private boolean checkIfLocationIsEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean gpsEnabled = false;
        boolean networkEnabled = false;

        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(CustomerMapsActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

        if (!gpsEnabled && !networkEnabled)
            Toast.makeText(CustomerMapsActivity.this, "Can't Access Location! Make Sure the Location is Enabled", Toast.LENGTH_SHORT).show();

        return gpsEnabled && networkEnabled;
    }

    @Override
    protected void onStart() {
        super.onStart();

        gpsReceiver = new GpsReceiver(() -> {
            if (checkIfLocationIsEnabled()) {
                getDeviceCurrentLocation();
            }
        });
        networkChangeListener = new NetworkChangeListener(networkAvailable -> {
            if(!networkAvailable) {
                networkUnavailableAlerty.show();
            }
            else {
                networkUnavailableAlerty.create().dismiss();
            }
        });

        registerReceiver(gpsReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        registerReceiver(networkChangeListener, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(gpsReceiver);
        unregisterReceiver(networkChangeListener);
    }

    private String getCurrentLocationAddress(LatLng currentLocation) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(currentLocation.latitude, currentLocation.longitude, 1);
            if(addresses.size() > 0) {
                return addresses.get(0).getAddressLine(0);
            }
            else {
                return "";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if(currentLocation == null) {
            currentLocation = location;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15));
        }
        else {
            currentLocation = location;
        }
    }

//    private void mapReadyProcessingCallback(int requestCode, int resultCode, Intent data) {
//        if(!CustomerMapsActivity.MAPREADY || !BottomLocationSelectionFragment.FRAGMENTREADY) {
//            new Handler(Looper.getMainLooper()).postDelayed(() -> mapReadyProcessingCallback(requestCode, resultCode, data), 200);
//        }
//        else {
//            try {
//                if (data != null) {
//                    if (resultCode == PICKUP_LOCATION_LIST_SELECTION_RESULT_CODE) {
//
//                        pickupLocationName = data.getStringExtra("PickupName");
//                        pickupLatLng = data.getParcelableExtra("PickupLatLng");
//
//                        if (pickupMarker != null) pickupMarker.remove();
//
//
//                        pickupMarker = mMap.addMarker(new MarkerOptions()
//                                .title("Pickup Point")
//                                .position(pickupLatLng));
//
//                        bottomLocationSelectionFragment.updatePickupAddress(pickupLocationName);
//                        displayRoutes();
//                    } else if (resultCode == HOSPITAL_LOCATION_LIST_SELECTION_RESULT_CODE) {
//
//                        hospitalLocationName = data.getStringExtra("HospitalName");
//                        hospitalLatLng = data.getParcelableExtra("HospitalLatLng");
//
//
//                        if (hospitalMarker != null) hospitalMarker.remove();
//
//
//                        hospitalMarker = mMap.addMarker(new MarkerOptions()
//                                .title("Hospital Point")
//                                .position(hospitalLatLng));
//
//                        bottomLocationSelectionFragment.updateHospitalAddress(hospitalLocationName);
//                        displayRoutes();
//
//                    } else {
//                        Toast.makeText(CustomerMapsActivity.this, "Bad Response!", Toast.LENGTH_SHORT).show();
//                        Log.e("CustomerMapsActivity", "Data: " + data + "\nRequest Code: " + requestCode + "\nResult Code: " + resultCode);
//                    }
//
//                } else {
//                    if (resultCode == PICKUP_LOCATION_MAP_SELECTION_RESULT_CODE) {
//                        FrameLayout confirmPickupButton = bottomLocationSelectionFragment.getConfirmPickupLocationButton();
//                        bottomLocationSelectionFragment.CLICKABILITY = false;
//
//                        confirmPickupButton.setVisibility(View.VISIBLE);
//                        backButton.setVisibility(View.VISIBLE);
//                        centerMapMarker.setVisibility(View.VISIBLE);
//
//                        mMap.setOnCameraIdleListener(() -> {
//                            onMapLatLng = mMap.getCameraPosition().target;
//
//                            executorService.execute(() -> {
//                                onMapPickupLocationName = getCurrentLocationAddress(onMapLatLng);
//                                mainHandler.post(() -> bottomLocationSelectionFragment.updatePickupAddress(onMapPickupLocationName));
//                            });
//                        });
//
//                        confirmPickupButton.setOnClickListener(v -> {
//                            mMap.setOnCameraIdleListener(() -> {
//                            });
//
//                            if (onMapLatLng == null)
//                                onMapLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//
//                            pickupLatLng = onMapLatLng;
//
//                            if (onMapPickupLocationName == null)
//                                onMapPickupLocationName = getCurrentLocationAddress(pickupLatLng);
//
//                            pickupLocationName = onMapPickupLocationName;
//                            bottomLocationSelectionFragment.updatePickupAddress(pickupLocationName);
//
//                            if (pickupMarker != null) pickupMarker.remove();
//                            pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("Hospital Location"));
//
//                            confirmPickupButton.setVisibility(View.GONE);
//                            centerMapMarker.setVisibility(View.GONE);
//
//                            bottomLocationSelectionFragment.CLICKABILITY = true;
//
//                            getNearbyCaptains(pickupLatLng);
//                            displayRoutes();
//                        });
//                    } else if (resultCode == HOSPITAL_LOCATION_MAP_SELECTION_RESULT_CODE) {
//                        FrameLayout confirmHospitalButton = bottomLocationSelectionFragment.getConfirmHospitalLocationButton();
//                        bottomLocationSelectionFragment.CLICKABILITY = false;
//
//                        confirmHospitalButton.setVisibility(View.VISIBLE);
//                        backButton.setVisibility(View.VISIBLE);
//                        centerMapMarker.setVisibility(View.VISIBLE);
//
//                        mMap.setOnCameraIdleListener(() -> {
//                            onMapLatLng = mMap.getCameraPosition().target;
//
//                            executorService.execute(() -> {
//                                onMapHospitalLocationName = getCurrentLocationAddress(onMapLatLng);
//                                mainHandler.post(() -> bottomLocationSelectionFragment.updateHospitalAddress(onMapHospitalLocationName));
//                            });
//                        });
//
//                        confirmHospitalButton.setOnClickListener(v -> {
//                            mMap.setOnCameraIdleListener(() -> {
//                            });
//
//                            if (onMapLatLng == null)
//                                onMapLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//
//                            hospitalLatLng = onMapLatLng;
//
//                            if (onMapPickupLocationName == null)
//                                onMapPickupLocationName = getCurrentLocationAddress(onMapLatLng);
//
//                            hospitalLocationName = onMapHospitalLocationName;
//
//                            if (hospitalMarker != null) hospitalMarker.remove();
//                            hospitalMarker = mMap.addMarker(new MarkerOptions().position(hospitalLatLng).title("Hospital Location"));
//
//                            confirmHospitalButton.setVisibility(View.GONE);
//                            centerMapMarker.setVisibility(View.GONE);
//
//                            bottomLocationSelectionFragment.CLICKABILITY = true;
//                            displayRoutes();
//                        });
//                    } else {
//                        Toast.makeText(CustomerMapsActivity.this, "Bad Response!", Toast.LENGTH_SHORT).show();
//                        Log.e("CustomerMapsActivity", "Data: " + data + "\nRequest Code: " + requestCode + "\nResult Code: " + resultCode);
//                    }
//                }
//            }
//            catch(Exception e) {
//                e.printStackTrace();
//                Toast.makeText(CustomerMapsActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if(data != null) {
////            if(resultCode == PICKUP_LOCATION_LIST_SELECTION_RESULT_CODE) {
////
//
////            }
//        if(resultCode == HOSPITAL_LOCATION_LIST_SELECTION_RESULT_CODE) {
//
//            }
//        } else {
////            if(resultCode == PICKUP_LOCATION_MAP_SELECTION_RESULT_CODE) {
////
////            }
//
//            if(resultCode == HOSPITAL_LOCATION_MAP_SELECTION_RESULT_CODE) {
//
//            }
//        }
//    }

    private void displayBottomPanelFragment(ArrayList<String> pricesArrayList) {
        ArrayList<String> ambulanceTypesAvailable = ambulanceTypesAvailable();

        Bundle bundle = new Bundle();
        bundle.putStringArrayList("Prices", pricesArrayList);
        bundle.putStringArrayList("AmbulanceAvailable", ambulanceTypesAvailable);

        backButton.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.GONE);

        bottomSelectionFragment = new BottomSelectionFragment();
        bottomSelectionFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down)
                .replace(R.id.bottom_fragment, bottomSelectionFragment)
                .commit();

        bottomSelectionFragment.setSelectedAmbulanceType(ambulanceType -> selectedAmbulanceType = ambulanceType);
        locationPanel.setVisibility(View.GONE);
    }

    private void moveCameraOnLocationFieldsUpdate() {
        if(mMap != null) {
            if(pickupLatLng != null && hospitalLatLng != null) {
                displayRoutes();
            } else if(pickupLatLng != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pickupLatLng, 16));
            }
            else if(hospitalLatLng != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(hospitalLatLng, 16));
            }
        }
    }

    private void displayRoutes() {

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Finding Fastest Route for you..");
        progressDialog.show();
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
                pickupLatLng.latitude + "," + pickupLatLng.longitude,
                hospitalLatLng.latitude + "," + hospitalLatLng.longitude,
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

                    if(routesLatLng.size() > 0) {

                        String distance = String.valueOf(hospitalPickupDistance);
                        int distanceKm;

                        if(distance.length() > 3) {
                            distanceKm = Integer.parseInt(distance.substring(0, distance.length() - 3));

                            if (distanceKm > 5) distanceKm = distanceKm - 5;
                            else distanceKm = 0;
                        } else {
                            distanceKm = 0;
                        }

                        blsPrice = (distanceKm * 50) + 300;
                        alsPrice = (distanceKm * 50) + 500;
                        mortgagePrice = (distanceKm * 50) + 250;
                        intercityPrice = (distanceKm * 50);

                        ArrayList<String> ambulancePrices = new ArrayList<>();

                        ambulancePrices.add(String.valueOf(alsPrice));
                        ambulancePrices.add(String.valueOf(blsPrice));
                        ambulancePrices.add(String.valueOf(mortgagePrice));
                        ambulancePrices.add(String.valueOf(intercityPrice));

                        displayBottomPanelFragment(ambulancePrices);
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

                        mMap.animateCamera(cameraUpdate);
                    }
                    else {
                        Toast.makeText(CustomerMapsActivity.this, "No Routes Available", Toast.LENGTH_SHORT).show();
                    }

                    progressDialog.dismiss();
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

    private void getNearbyCaptains(LatLng pickupLocation) {
        ambulanceKeys = new ArrayList<>();
        alsAmbulanceKeys = new ArrayList<>();
        blsAmbulanceKeys = new ArrayList<>();
        mortgageAmbulanceKeys = new ArrayList<>();
        intercityAmbulanceKeys = new ArrayList<>();
        ambulanceMarkersHashMap = new HashMap<>();
//        ambulanceMarkers = new ArrayList<>();

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
//                ambulanceMarkers.add(marker);
            }

            @Override
            public void onKeyExited(String key) {
                ambulanceKeys.remove(key);
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
                    Toast.makeText(CustomerMapsActivity.this, "No Driver Available Nearby", Toast.LENGTH_SHORT).show();
                else getAmbulancesTypesAvailable(ambulanceKeys);
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
            }
        });
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

    private ArrayList<String> ambulanceTypesAvailable() {
        ArrayList<String> ambulanceTypesAvailable = new ArrayList<>();

        if(alsAmbulanceKeys.size() > 0) ambulanceTypesAvailable.add("ALS");
        if(blsAmbulanceKeys.size() > 0) ambulanceTypesAvailable.add("BLS");
        if(mortgageAmbulanceKeys.size() > 0) ambulanceTypesAvailable.add("Mortgage");
        if(intercityAmbulanceKeys.size() > 0) ambulanceTypesAvailable.add("Intercity");

        return ambulanceTypesAvailable;
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
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

    }

    private void getHospitalLists(String text) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitCallbacks callbacks = retrofit.create(RetrofitCallbacks.class);

        Call<PlacesSearchResponsePojo> pickupSearchResultCallback = callbacks.getPlaceSearchResult(text, "textquery", "formatted_address,name,geometry", getString(R.string.google_maps_key));
        pickupSearchResultCallback.enqueue(new Callback<PlacesSearchResponsePojo>() {
            @Override
            public void onResponse(@NotNull Call<PlacesSearchResponsePojo> call, @NotNull Response<PlacesSearchResponsePojo> response) {
                PlacesSearchResponsePojo pickups = response.body();
                hospitalSearchResults = new ArrayList<>();

                if (pickups != null) {
                    for(int i = 0; i < pickups.getCandidates().size(); i++) {
                        hospitalSearchResults.add(new PlaceSearchResultModel(pickups.getCandidates().get(i), pickups.getCandidates().get(i).getFormattedAddress()));
                    }

                    if (hospitalSearchResultRecyclerviewAdapter == null) {
                        hospitalSearchResultRecyclerviewAdapter = new HospitalSearchResultRecyclerviewAdapter(hospitalSearchResults, pref);
                        hospitalSearchResultRecyclerView.setAdapter(hospitalSearchResultRecyclerviewAdapter);
                    } else {
                        hospitalSearchResultRecyclerviewAdapter.updateResults(hospitalSearchResults);
                        hospitalSearchResultRecyclerviewAdapter.notifyDataSetChanged();
                    }
                }

                else {
                    Toast.makeText(CustomerMapsActivity.this, "No Result Found", Toast.LENGTH_SHORT).show();
                }

                hospitalSqueezeLoader.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(@NotNull Call<PlacesSearchResponsePojo> call, @NotNull Throwable t) {
                t.printStackTrace();
                Toast.makeText(CustomerMapsActivity.this, "Error Fetching Results", Toast.LENGTH_SHORT).show();

                hospitalSqueezeLoader.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (checkIfLocationIsEnabled()) {
                    getDeviceCurrentLocation();
                }
                getCurrentFusedLocation();
            } else {
                Toast.makeText(CustomerMapsActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPaymentSuccess(String s) {
        try {
            Intent ambulanceBookingIntent = new Intent(CustomerMapsActivity.this, BookingConfirmation.class);
            ambulanceBookingIntent.putExtra("PickupLatLng", pickupLatLng);

            ArrayList<String> selectedAmbulanceKeys = new ArrayList<>();

            switch (selectedAmbulanceType) {
                case "ALS":
                    Toast.makeText(CustomerMapsActivity.this, "ALS", Toast.LENGTH_SHORT).show();
                    selectedAmbulanceKeys = (ArrayList<String>) alsAmbulanceKeys;
                    break;
                case "BLS":
                    Toast.makeText(CustomerMapsActivity.this, "BLS", Toast.LENGTH_SHORT).show();
                    selectedAmbulanceKeys = (ArrayList<String>) blsAmbulanceKeys;
                    break;
                case "Mortgage":
                    Toast.makeText(CustomerMapsActivity.this, "Mortgage", Toast.LENGTH_SHORT).show();
                    selectedAmbulanceKeys = (ArrayList<String>) mortgageAmbulanceKeys;
                    break;
                case "Intercity":
                    Toast.makeText(CustomerMapsActivity.this, "Intercity", Toast.LENGTH_SHORT).show();
                    selectedAmbulanceKeys = (ArrayList<String>) intercityAmbulanceKeys;
                    break;
            }

            if(selectedAmbulanceKeys.size() > 0) {
                DriverBookingFragment driverBookingFragment = new DriverBookingFragment();

                Toast.makeText(CustomerMapsActivity.this, "Payment Successful! Inside Callback", Toast.LENGTH_SHORT).show();

                getSupportFragmentManager().beginTransaction()
                        .remove(bottomSelectionFragment)
                        .replace(R.id.bottom_fragment, driverBookingFragment)
                        .commit();
            } else {
                Toast.makeText(CustomerMapsActivity.this, "No Ambulance Available! Please Select Available Ambulance", Toast.LENGTH_SHORT).show();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPaymentError(int i, String s) {
        Log.i("CustomerMapsActivity", "RazorPay Error");
    }

    class PickupSearchResultRecyclerviewAdapter extends RecyclerView.Adapter<PickupSearchResultRecyclerviewAdapter.ViewHolder>{
        private List<PlaceSearchResultModel> pickupSearchResult;

        public PickupSearchResultRecyclerviewAdapter(List<PlaceSearchResultModel> hospitalSearchResult){
            this.pickupSearchResult = hospitalSearchResult;
        }

        public void updateResults(List<PlaceSearchResultModel> hospitalSearchResult){
            this.pickupSearchResult = hospitalSearchResult;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.hospital_search_result_layout, parent, false);
            return new ViewHolder(layout);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TextView hospitalNameTextView = holder.getPickupNameTextView();
            TextView hospitalAddressTextView = holder.getPickupAddressTextView();

            hospitalNameTextView.setText(pickupSearchResult.get(position).getCandidate().getName());
            hospitalAddressTextView.setText(pickupSearchResult.get(position).getAddress());
        }

        @Override
        public int getItemCount() {
            return pickupSearchResult.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final TextView pickupNameTextView;
            private final TextView pickupAddressTextView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                pickupNameTextView = itemView.findViewById(R.id.hospital_name);
                pickupAddressTextView = itemView.findViewById(R.id.hospital_address);
                itemView.setOnClickListener(this);
            }

            public TextView getPickupAddressTextView() { return pickupAddressTextView; }
            public TextView getPickupNameTextView() {
                return pickupNameTextView;
            }

            @Override
            public void onClick(View v) {
                pickupSearchLayout.setVisibility(View.GONE);
                locationPanel.setVisibility(View.VISIBLE);
                toolbar.setVisibility(View.VISIBLE);
                String pickupName = pickupNameTextView.getText().toString();
                pickupLatLng = new LatLng(pickupSearchResult.get(this.getLayoutPosition()).getCandidate().getGeometry().getLocation().getLat(), pickupSearchResult.get(this.getLayoutPosition()).getCandidate().getGeometry().getLocation().getLng());

                if(pickupMarker != null)
                    pickupMarker.remove();

                pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLatLng));
                pickupLocationText.setText(pickupName);

                getNearbyCaptains(pickupLatLng);
                moveCameraOnLocationFieldsUpdate();
            }
        }
    }

    class RecentSearchHospitalRecyclerViewAdapter extends RecyclerView.Adapter<RecentSearchHospitalRecyclerViewAdapter.ViewHolder> {

        List<RecentHospitalSearchResult> recentHospitalSearchResults;

        public RecentSearchHospitalRecyclerViewAdapter(List<RecentHospitalSearchResult> recentHospitalSearchResults) {
            this.recentHospitalSearchResults = recentHospitalSearchResults;
        }

        @NonNull
        @Override
        public RecentSearchHospitalRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.hospital_search_result_layout, parent, false);
            return new RecentSearchHospitalRecyclerViewAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecentSearchHospitalRecyclerViewAdapter.ViewHolder holder, int position) {
            TextView hospitalName = holder.getHospitalName();
            TextView hospitalAddress = holder.getHospitalAddress();

            hospitalAddress.setText(recentHospitalSearchResults.get(position).getHospitalAddress());
            hospitalName.setText(recentHospitalSearchResults.get(position).getHospitalName());
        }

        @Override
        public int getItemCount() {
            return recentHospitalSearchResults.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

            private final TextView hospitalName;
            private final TextView hospitalAddress;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                hospitalName = itemView.findViewById(R.id.hospital_name);
                hospitalAddress = itemView.findViewById(R.id.hospital_address);

                itemView.setOnClickListener(this);
            }

            public TextView getHospitalAddress() {
                return hospitalAddress;
            }

            public TextView getHospitalName() {
                return hospitalName;
            }

            @Override
            public void onClick(View v) {
                hospitalLatLng = new LatLng(
                        Double.parseDouble(recentHospitalSearchResults.get(getLayoutPosition()).getHospitalLat()),
                        Double.parseDouble(recentHospitalSearchResults.get(getLayoutPosition()).getHospitalLong()));

                hospitalLocationName = recentHospitalSearchResults.get(getLayoutPosition()).getHospitalName();

                if(hospitalMarker != null)
                    hospitalMarker.remove();

                hospitalSearchLayout.setVisibility(View.GONE);
                locationPanel.setVisibility(View.VISIBLE);
                toolbar.setVisibility(View.VISIBLE);

                hospitalMarker = mMap.addMarker(new MarkerOptions().position(hospitalLatLng));
                hospitalLocationText.setText(hospitalLocationName);

                moveCameraOnLocationFieldsUpdate();
            }
        }
    }

    class HospitalSearchResultRecyclerviewAdapter extends RecyclerView.Adapter<HospitalSearchResultRecyclerviewAdapter.ViewHolder>{
        private List<PlaceSearchResultModel> hospitalSearchResult;
        private final SharedPreferences preferences;

        public HospitalSearchResultRecyclerviewAdapter(List<PlaceSearchResultModel> hospitalSearchResult, SharedPreferences preferences){
            this.hospitalSearchResult = hospitalSearchResult;
            this.preferences = preferences;
        }

        public void updateResults(List<PlaceSearchResultModel> hospitalSearchResult){
            this.hospitalSearchResult = hospitalSearchResult;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.hospital_search_result_layout, parent, false);
            return new ViewHolder(layout);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TextView hospitalNameTextView = holder.getHospitalNameTextView();
            TextView hospitalAddressTextView = holder.getHospitalAddressTextView();

            hospitalNameTextView.setText(hospitalSearchResult.get(position).getCandidate().getName());
            hospitalAddressTextView.setText(hospitalSearchResult.get(position).getAddress());
        }

        @Override
        public int getItemCount() {
            return hospitalSearchResult.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final TextView hospitalNameTextView;
            private final TextView hospitalAddressTextView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                hospitalNameTextView = itemView.findViewById(R.id.hospital_name);
                hospitalAddressTextView = itemView.findViewById(R.id.hospital_address);
                itemView.setOnClickListener(this);
            }

            public TextView getHospitalAddressTextView() { return hospitalAddressTextView; }
            public TextView getHospitalNameTextView() { return hospitalNameTextView; }

            @Override
            public void onClick(View v) {
                String hospitalName = hospitalNameTextView.getText().toString();
                String hospitalAddress = hospitalAddressTextView.getText().toString();
                hospitalLatLng = new LatLng(hospitalSearchResult.get(this.getLayoutPosition()).getCandidate().getGeometry().getLocation().getLat(),
                        hospitalSearchResult.get(this.getLayoutPosition()).getCandidate().getGeometry().getLocation().getLng());

                Set<String> hospitalNameSet = preferences.getStringSet("HospitalName", null);
                Set<String> hospitalLatSet = preferences.getStringSet("HospitalLat", null);
                Set<String> hospitalLongSet = preferences.getStringSet("HospitalLong", null);
                Set<String> hospitalAddressSet = preferences.getStringSet("HospitalAddress", null);

                SharedPreferences.Editor editor = preferences.edit();

                if(hospitalAddressSet == null) hospitalAddressSet = new HashSet<>();
                if(hospitalLatSet == null) hospitalLatSet = new HashSet<>();
                if(hospitalLongSet == null) hospitalLongSet = new HashSet<>();
                if(hospitalNameSet == null) hospitalNameSet = new HashSet<>();

                hospitalAddressSet.add(hospitalAddress);
                hospitalNameSet.add(hospitalName);
                hospitalLatSet.add(String.valueOf(hospitalLatLng.latitude));
                hospitalLongSet.add(String.valueOf(hospitalLatLng.longitude));

                editor.putStringSet("HospitalName", hospitalNameSet);
                editor.putStringSet("HospitalLat", hospitalLatSet);
                editor.putStringSet("HospitalLong", hospitalLongSet);
                editor.putStringSet("HospitalAddress", hospitalAddressSet);

                editor.apply();

                hospitalSearchLayout.setVisibility(View.GONE);
                locationPanel.setVisibility(View.VISIBLE);
                toolbar.setVisibility(View.VISIBLE);

                hospitalLocationName = hospitalName;

                if(hospitalMarker != null)
                    hospitalMarker.remove();

                hospitalMarker = mMap.addMarker(new MarkerOptions().position(hospitalLatLng));
                hospitalLocationText.setText(hospitalName);

                moveCameraOnLocationFieldsUpdate();
            }
        }
    }
}

class GpsReceiver extends BroadcastReceiver {

    LocationChangeListener locationChangeListener;

    public GpsReceiver(LocationChangeListener locationChangeListener) {
        this.locationChangeListener = locationChangeListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.location.PROVIDERS_CHANGED")) {
            locationChangeListener.onLocationChanged();
        }
    }
}

interface LocationChangeListener {
    void onLocationChanged();
}

interface SelectedAmbulanceType {
    void ambulanceSelected(String ambulanceType);
}

class PlaceSearchResultModel {
    private final Candidate candidate;
    private final String address;

    public PlaceSearchResultModel(Candidate result, String address) {
        this.candidate = result;
        this.address = address;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public String getAddress() {
        return address;
    }
}

class RecentHospitalSearchResult {

    private final String hospitalName;
    private final String hospitalAddress;
    private final String hospitalLat;
    private final String hospitalLong;

    public RecentHospitalSearchResult(String hospitalName, String hospitalAddress, String hospitalLat, String hospitalLong) {
        this.hospitalAddress = hospitalAddress;
        this.hospitalLat = hospitalLat;
        this.hospitalLong = hospitalLong;
        this.hospitalName = hospitalName;
    }

    public String getHospitalAddress() {
        return hospitalAddress;
    }

    public String getHospitalLat() {
        return hospitalLat;
    }

    public String getHospitalLong() {
        return hospitalLong;
    }

    public String getHospitalName() {
        return hospitalName;
    }
}