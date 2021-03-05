package com.brightsky.medicab;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.brightsky.medicab.routesmodel.Leg;
import com.brightsky.medicab.routesmodel.Route;
import com.brightsky.medicab.routesmodel.RoutesResponsePojo;
import com.brightsky.medicab.routesmodel.Step;
import com.bumptech.glide.Glide;
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
import com.google.android.libraries.places.api.Places;
import com.google.maps.android.PolyUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DriverFoundActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Location lastKnownLocation;
    private final boolean locationPermissionGranted = false;
    private static final int CURRENT_LOCATION_ZOOM = 17;
    private static final int DEFAULT_ZOOM = 5;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private final LatLng defaultLocation = new LatLng(28.613939, 77.209021);

    private LatLng driverLocation;
    private String driverKey;
    private String driverLastName;
    private String driverFirstName;
    private String ambulanceType;
    private String driverProfileUrl;

    private LatLng pickupLocation;
    private Polyline lastPlottedPolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_found);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        Places.initialize(getApplicationContext(), "AIzaSyDTyvJynEqi6qChFNEgNaCXqJrLBN8BIFk");
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        driverLocation = getIntent().getExtras().getParcelable("DriversLatLng");
        driverKey = getIntent().getExtras().getString("DriverKey");
        pickupLocation = getIntent().getExtras().getParcelable("PickupLatLng");
        driverFirstName = getIntent().getStringExtra("FirstName");
        driverLastName = getIntent().getStringExtra("Lastname");
        ambulanceType = getIntent().getStringExtra("AmbulanceType");
        driverProfileUrl = getIntent().getStringExtra("ProfileURL");


//        Glide.with(DriverFoundActivity.this).load(driverProfileUrl).placeholder(R.mipmap.avatar_dummy).into(driverimage);
        Toast.makeText(DriverFoundActivity.this, driverFirstName, Toast.LENGTH_SHORT).show();
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

    private void displayRoute(LatLng driverLocation) {
        if(driverLocation != null && pickupLocation != null) {

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://maps.googleapis.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            RetrofitCallbacks callbacks = retrofit.create(RetrofitCallbacks.class);

            Call<RoutesResponsePojo> routesResultCallback = callbacks.getRoute(
                    pickupLocation.latitude + "," + pickupLocation.longitude,
                    driverLocation.latitude + "," + driverLocation.longitude,
                    getString(R.string.google_maps_key)
            );

            routesResultCallback.enqueue(new Callback<RoutesResponsePojo>() {
                @Override
                public void onResponse(@NotNull Call<RoutesResponsePojo> call, @NotNull Response<RoutesResponsePojo> response) {
                    List<LatLng> routeLatLng = new ArrayList<>();
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();

                    if (response.isSuccessful() && response.body() != null) {
                        for(Route route: response.body().getRoutes()){
                            for(Leg leg: route.getLegs()) {
                                for (Step step : leg.getSteps()) {
                                    List<LatLng> decodedPoints = PolyUtil.decode(step.getPolyline().getPoints());
                                    routeLatLng.addAll(decodedPoints);
                                }
                            }
                        }
                    }

                    for(LatLng point: routeLatLng)
                        builder.include(point);

                    PolylineOptions polylineOptions = new PolylineOptions()
                            .width(5)
                            .color(Color.BLACK)
                            .geodesic(true);
                    polylineOptions.addAll(routeLatLng);

                    if (lastPlottedPolyline != null) {
                        lastPlottedPolyline.remove();
                    }
                    lastPlottedPolyline = mMap.addPolyline(polylineOptions);

                    LatLngBounds bounds = builder.build();
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 50);
                    mMap.animateCamera(cameraUpdate);
                }

                @Override
                public void onFailure(@NotNull Call<RoutesResponsePojo> call, @NotNull Throwable t) {
                    t.printStackTrace();
                    call.cancel();
                }
            });
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        showDriversOnMap();
        showPickupPointOnMap();
        displayRoute(driverLocation);
    }

    private void showDriversOnMap() {
        mMap.addMarker(new MarkerOptions()
                .position(driverLocation)
                .icon(getBitmapFromVector())
                .title("Driver Location"));
    }

    private void showPickupPointOnMap() {
        mMap.addMarker(new MarkerOptions()
                .position(pickupLocation)
                .title("Pickup Location"));
    }
}