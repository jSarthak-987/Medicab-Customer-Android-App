package com.brightsky.medicab;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.brightsky.medicab.routesmodel.Leg;
import com.brightsky.medicab.routesmodel.Route;
import com.brightsky.medicab.routesmodel.RoutesResponsePojo;
import com.brightsky.medicab.routesmodel.Step;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RideConfirmationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;
    private ConstraintLayout alsButton;
    private ConstraintLayout blsButton;
    private ConstraintLayout mortgageButton;
    private ConstraintLayout intercityButton;

    private ExecutorThreadService executorThreadService;
    private ExecutorService executorService;
    private Handler mainHandler;
    private List<String> ambulanceKeys;

    private static final int CURRENT_LOCATION_ZOOM = 17;
    private static final int DEFAULT_ZOOM = 5;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private Polyline lastPlottedPolyline;
    private LatLng pickupPoint;
    private LatLng hospitalPoint;

    private boolean driverFound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_confirmation);

        alsButton = findViewById(R.id.als_button);
        blsButton = findViewById(R.id.bls_button);
        mortgageButton = findViewById(R.id.mortgage_button);
        intercityButton = findViewById(R.id.intercity_button);

        pickupPoint = getIntent().getParcelableExtra("PickupLatLng");
        hospitalPoint = getIntent().getParcelableExtra("HospitalLatLng");

        ImageView backButton = findViewById(R.id.back_button);

        ambulanceKeys = new ArrayList<>();
        backButton.setOnClickListener(v -> finish());

        executorThreadService = ExecutorThreadService.getInstance();
        executorService = executorThreadService.getExecutor();
        mainHandler = executorThreadService.getHandler();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        Places.initialize(getApplicationContext(), "AIzaSyDTyvJynEqi6qChFNEgNaCXqJrLBN8BIFk");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

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

    private List<String> getAmbulancesTypesAvailable(List<String> ambulanceKeys) {
        List<String> ambulanceTypes = new ArrayList<>();

        for(String ambulanceKey: ambulanceKeys) {
            try {
                DatabaseReference ambulanceTypesDatabaseReference = FirebaseDatabase.getInstance().getReference().child("AmbulanceTypes").child(ambulanceKey);
                DataSnapshot ambulanceTypesDataSnapshot = ambulanceTypesDatabaseReference.get().getResult();

                if (ambulanceTypesDataSnapshot.exists() && ambulanceTypesDataSnapshot.getValue() != null) {
                    if(!ambulanceTypes.contains(ambulanceTypesDataSnapshot.getValue().toString()))
                        ambulanceTypes.add(ambulanceTypesDataSnapshot.getValue().toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return ambulanceTypes;

//            ambulanceTypesDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    if(snapshot.exists()) {
//
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    error.toException().printStackTrace();
//                }
//            });
    }

    private List<LatLng> displayRoutes() {
        List<LatLng> routesLatLng = new ArrayList<>();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitCallbacks callbacks = retrofit.create(RetrofitCallbacks.class);

        Call<RoutesResponsePojo> routesResultCallback = callbacks.getRoute(
                pickupPoint.latitude + "," + pickupPoint.longitude,
                hospitalPoint.latitude + "," + hospitalPoint.longitude,
                getString(R.string.google_maps_key)
        );

        try {
            Response<RoutesResponsePojo> response = routesResultCallback.execute();

            if (response.isSuccessful() && response.body() != null) {
                for (Route route : response.body().getRoutes()) {
                    for (Leg leg : route.getLegs()) {
                        for (Step step : leg.getSteps()) {
                            List<LatLng> decodedPoints = PolyUtil.decode(step.getPolyline().getPoints());
                            routesLatLng.addAll(decodedPoints);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return routesLatLng;
    }

    private void updateMaps(List<LatLng> routeLatLng) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (LatLng point : routeLatLng)
            builder.include(point);

        PolylineOptions polylineOptions = new PolylineOptions()
                .width(5)
                .color(Color.BLACK)
                .geodesic(true);
        polylineOptions.addAll(routeLatLng);

        if (lastPlottedPolyline != null) {
            lastPlottedPolyline.remove();
        }

        mainHandler.post(() -> {
            lastPlottedPolyline = mMap.addPolyline(polylineOptions);

            LatLngBounds bounds = builder.build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 50);

            mMap.animateCamera(cameraUpdate);
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        executorService.execute(() -> {
            List<LatLng> routes = displayRoutes();
            updateMaps(routes);
//            getNearbyCaptains();
        });
    }
}