package com.brightsky.medicab;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocationSelectionFragment extends Fragment {

    private final int PICKUP_LOCATION_RESULT_CODE = 1;
    private final int HOSPITAL_LOCATION_RESULT_CODE = 2;
    private final int PAYTM_TRANSACTION_RESULT_CODE = 3;

    private TextView hospitalAddressEditText;
    private TextView pickupAddressEditText;

    private ConstraintLayout pickupPoint;
    private ConstraintLayout hospitalPoint;

    private String pickupName;
    private String hospitalName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_location_selection, container, false);

        hospitalPoint = view.findViewById(R.id.hospital_location_field);
        pickupPoint = view.findViewById(R.id.pickup_location_field);
        pickupAddressEditText = view.findViewById(R.id.pickup_address);
        hospitalAddressEditText = view.findViewById(R.id.destination_address);

        LatLng currentLocation = getArguments().getParcelable("CurrentLatLng");

        hospitalPoint.setOnClickListener(v -> {
            Intent pickupPointClickIntent = new Intent(getContext(), HospitalSearch.class);
            pickupPointClickIntent.putExtra("CurrentLatLng", currentLocation);
            startActivity(pickupPointClickIntent);
//            startActivityForResult(pickupPointClickIntent, HOSPITAL_LOCATION_RESULT_CODE);
        });

        pickupPoint.setOnClickListener(v -> {
            Intent pickupPointClickIntent = new Intent(getContext(), PickupSearch.class);
            pickupPointClickIntent.putExtra("PickupLatLng", currentLocation);
            startActivity(pickupPointClickIntent);
//            startActivityForResult(pickupPointClickIntent, PICKUP_LOCATION_RESULT_CODE);
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null) {
            if (requestCode == PICKUP_LOCATION_RESULT_CODE) {
                pickupName = data.getStringExtra("PickupName");

//                SharedPreferences sharedPreferences = getContext().getSharedPreferences("PickupSearchResult", Context.MODE_PRIVATE);
//                String pickupName = sharedPreferences.getString("PickupName", "Enter The Pickup Address");
//                sharedPreferences.getString("PickupLat", "Enter The Pickup Address");
//                sharedPreferences.getString("PickupLng", "Enter The Pickup Address");

                pickupAddressEditText.setText(pickupName);

//                sharedPreferences.edit().clear().apply();
                Log.e("LocationSelection", "PickupName" + pickupName);

                pickupAddressEditText.setTextColor(Color.parseColor("#212121"));
            }

            if (requestCode == HOSPITAL_LOCATION_RESULT_CODE) {
                hospitalName = data.getStringExtra("HospitalName");

                hospitalAddressEditText.setText(hospitalName);

                Log.e("LocationSelection", "HospitalName" + hospitalName);
                hospitalAddressEditText.setTextColor(Color.parseColor("#212121"));
            }

            else if(requestCode == PAYTM_TRANSACTION_RESULT_CODE) {
                Toast.makeText(getContext(),
                        data.getStringExtra("nativeSdkForMerchantMessage") + data.getStringExtra("response"),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Error Displaying Selected Location", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();

        if(pickupName != null) {
            Log.e("LocationSelection", "PickupName" + pickupName);
            pickupAddressEditText.setText(pickupName);
        }

        if(hospitalName != null) {
            Log.e("LocationSelection", "PickupName" + pickupName);
            hospitalAddressEditText.setText(hospitalName);
        }
    }
}