package com.brightsky.medicab;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BookedDriverDetailsFragment extends Fragment {

    private TextView distance;
    private TextView distanceUnit;
    private TextView price;
    private TextView priceCurrency;
    private TextView ambulanceNumber;
    private Button callDriver;
    private TextView driverName;
    private TextView driverRating;
    private String driverContactNumber;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booked_driver_details, container, false);

        distance = view.findViewById(R.id.distance);
        price = view.findViewById(R.id.price);
        priceCurrency = view.findViewById(R.id.price_currency);
        distanceUnit = view.findViewById(R.id.distance_unit);
        ambulanceNumber = view.findViewById(R.id.ambulance_number);
        callDriver = view.findViewById(R.id.call_driver_button);
        driverName = view.findViewById(R.id.driver_name);
        driverRating = view.findViewById(R.id.rating);

        driverContactNumber = "+918005923313";

        if (getArguments() != null) {
            String distanceValue = getArguments().getString("Distance");
            String distanceUnitValue = getArguments().getString("DistanceUnit");
            String priceValue = getArguments().getString("Price");
            String priceCurrencyValue = getArguments().getString("PriceCurrency");

            priceCurrency.setText(priceValue);
            distance.setText(distanceValue);
            distanceUnit.setText(distanceUnitValue);
            priceCurrency.setText(priceCurrencyValue);

            callDriver.setOnClickListener(v -> {
                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "+918005923313"));
                startActivity(callIntent);
            });
        }

        else {
            Toast.makeText(getContext(), "Error Getting Driver Details",Toast.LENGTH_SHORT).show();
        }

        return view;
    }
}