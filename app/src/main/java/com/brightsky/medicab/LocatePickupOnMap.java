package com.brightsky.medicab;

import androidx.fragment.app.FragmentActivity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;

import java.util.List;
import java.util.Locale;

public class LocatePickupOnMap extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener {

    private GoogleMap mMap;
    private static final int CURRENT_LOCATION_ZOOM = 17;
    private TextView locationAddressTextView;
    private AddressData markedAddress;
    private LatLng markedLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate_pickup_on_map);

        FrameLayout confirmLocation = findViewById(R.id.confirm_location);
        locationAddressTextView = findViewById(R.id.location_on_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        Places.initialize(getApplicationContext(), "AIzaSyDTyvJynEqi6qChFNEgNaCXqJrLBN8BIFk");

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        int locationIntent = getIntent().getIntExtra("LocationIntent", 3);
        Location pickupLatLng = getIntent().getParcelableExtra("PickupLatLng");
        markedLatLng = new LatLng(pickupLatLng.getLatitude(), pickupLatLng.getLongitude());

        confirmLocation.setOnClickListener(v -> {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("MarkedLatLng", markedLatLng);
            returnIntent.putExtra("MarkedLocationName", markedAddress.getAddressName());
            returnIntent.putExtra("MarkedLocationCity", markedAddress.getAddressCity());
            setResult(locationIntent, returnIntent);
            finish();
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraIdleListener(this);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markedLatLng, CURRENT_LOCATION_ZOOM));
    }

    private AddressData getAddressFromLatLng(LatLng currLatLng) {
        AddressData addressData = new AddressData();

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(currLatLng.latitude, currLatLng.longitude, 1);

            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();

            addressData.setAddressName(address);
            addressData.setAddressCity(city);

            return addressData;
        } catch (Exception e) {
            e.printStackTrace();
            return addressData;
        }
    }

    @Override
    public void onCameraIdle() {
        markedLatLng = mMap.getCameraPosition().target;
        markedAddress = getAddressFromLatLng(markedLatLng);
        if(markedAddress.getAddressName() != null)
            locationAddressTextView.setText(markedAddress.getAddressName());
    }
}

class AddressData {

    String addressName;
    String addressCity;

    public String getAddressName() {
        return this.addressName;
    }

    public String getAddressCity() {
        return this.addressCity;
    }

    public void setAddressName(String addressName) {
        this.addressName = addressName;
    }

    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }
}