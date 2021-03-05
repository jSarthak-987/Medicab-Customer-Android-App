package com.brightsky.medicab;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;


public class BottomLocationSelectionFragment extends Fragment {

    private TextView pickupAddressTextView;
    private TextView hospitalAddressTextView;
    private FrameLayout confirmPickupLocationButton;
    private FrameLayout confirmHospitalLocationButton;

    public static boolean FRAGMENTREADY;
    public boolean CLICKABILITY = true;

    public void updatePickupAddress(String address) {
        pickupAddressTextView.setText(address);
    }

    public void updateHospitalAddress(String address) {
        hospitalAddressTextView.setText(address);
    }

    public FrameLayout getConfirmHospitalLocationButton() {
        return confirmHospitalLocationButton;
    }

    public FrameLayout getConfirmPickupLocationButton() {
        return confirmPickupLocationButton;
    }

    public TextView getHospitalAddressTextView() {
        return hospitalAddressTextView;
    }

    public TextView getPickupAddressTextView() {
        return pickupAddressTextView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BottomLocationSelectionFragment.FRAGMENTREADY = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_location_selection, container, false);

        pickupAddressTextView = view.findViewById(R.id.pickup_address);
        hospitalAddressTextView = view.findViewById(R.id.hospital_address);
        confirmPickupLocationButton = view.findViewById(R.id.confirm_pickup_location);
        confirmHospitalLocationButton = view.findViewById(R.id.confirm_hospital_location);

//        pickupAddressTextView.setOnClickListener(v -> {
//            if(CLICKABILITY) {
//                Intent pickupIntent = new Intent(getContext(), PickupSearch.class);
//                startActivityForResult(pickupIntent, CustomerMapsActivity.PICKUP_LOCATION_LIST_SELECTION_RESULT_CODE);
//            }
//        });
//
//        hospitalAddressTextView.setOnClickListener(v -> {
//            if(CLICKABILITY) {
//                Intent pickupIntent = new Intent(getContext(), HospitalSearch.class);
//                startActivityForResult(pickupIntent, CustomerMapsActivity.HOSPITAL_LOCATION_LIST_SELECTION_RESULT_CODE);
//            }
//        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BottomLocationSelectionFragment.FRAGMENTREADY = true;
    }
}