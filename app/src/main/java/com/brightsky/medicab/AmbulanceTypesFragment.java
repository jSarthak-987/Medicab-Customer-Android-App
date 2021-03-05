package com.brightsky.medicab;

import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.razorpay.Checkout;

import org.json.JSONObject;

import java.util.ArrayList;

public class AmbulanceTypesFragment extends Fragment {

    public int selectedAmbulance = 0;
    private int selectedAmbulancePrice = 0;

    public ConstraintLayout alsButton;
    public ConstraintLayout blsButton;
    public ConstraintLayout mortgageButton;
    public ConstraintLayout intercityButton;
    public RelativeLayout paymentButton;

    private TextView alsPrice;
    private TextView blsPrice;
    private TextView mortgagePrice;
    private TextView intercityPrice;

    public static int ALS_TYPE = 1;
    public static int BLS_TYPE = 1;
    public static int MORTGAGE_TYPE = 1;
    public static int INTERCITY_TYPE = 1;


    public ConstraintLayout getAlsButton() {
        return alsButton;
    }

    public ConstraintLayout getBlsButton() {
        return blsButton;
    }

    public ConstraintLayout getIntercityButton() {
        return intercityButton;
    }

    public ConstraintLayout getMortgageButton() {
        return mortgageButton;
    }

    public TextView getAlsPrice() {
        return alsPrice;
    }

    public TextView getBlsPrice() {
        return blsPrice;
    }

    public TextView getMortgagePrice() {
        return mortgagePrice;
    }

    public TextView getIntercityPrice() {
        return intercityPrice;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ambulance_types, container, false);

        alsButton = view.findViewById(R.id.als_button);
        blsButton = view.findViewById(R.id.bls_button);
        mortgageButton = view.findViewById(R.id.mortgage_button);
        intercityButton = view.findViewById(R.id.intercity_button);
        paymentButton = view.findViewById(R.id.payment_button);

        alsPrice = view.findViewById(R.id.als_price);
        blsPrice = view.findViewById(R.id.bls_price);
        mortgagePrice = view.findViewById(R.id.mortgage_price);
        intercityPrice = view.findViewById(R.id.intercity_price);

        ArrayList<String> ambulanceAvailables = getArguments().getStringArrayList("AMBULANCE_TYPES");

        if(ambulanceAvailables.size() > 0) {

            if (ambulanceAvailables.contains("ALS")) {
                alsButton.setVisibility(View.VISIBLE);
                selectedAmbulancePrice = getArguments().getInt("AlsPrice");
                alsPrice.setText(String.valueOf(getArguments().getInt("AlsPrice")));
            }

            if (ambulanceAvailables.contains("BLS")) {
                blsButton.setVisibility(View.VISIBLE);
                selectedAmbulancePrice = getArguments().getInt("BlsPrice");
                blsPrice.setText(String.valueOf(getArguments().getInt("BlsPrice")));
            }

            if (ambulanceAvailables.contains("Mortgage")) {
                mortgageButton.setVisibility(View.VISIBLE);
                selectedAmbulancePrice = getArguments().getInt("MortgagePrice");
                mortgagePrice.setText(String.valueOf(getArguments().getInt("MortgagePrice")));
            }

            if (ambulanceAvailables.contains("Intercity")) {
                intercityButton.setVisibility(View.VISIBLE);
                selectedAmbulancePrice = getArguments().getInt("IntercityPrice");
                intercityPrice.setText(String.valueOf(getArguments().getInt("IntercityPrice")));
            }
        }


        alsButton.setOnClickListener(v -> {
            selectedAmbulance = 1;
            Toast.makeText(getContext(), String.valueOf(selectedAmbulancePrice), Toast.LENGTH_SHORT).show();
        });

        blsButton.setOnClickListener(v -> {
            selectedAmbulance = 2;
            Toast.makeText(getContext(), String.valueOf(selectedAmbulancePrice), Toast.LENGTH_SHORT).show();
        });

        mortgageButton.setOnClickListener(v -> {
            selectedAmbulance = 3;
            Toast.makeText(getContext(), String.valueOf(selectedAmbulancePrice), Toast.LENGTH_SHORT).show();
        });

        intercityButton.setOnClickListener(v -> {
            selectedAmbulance = 4;
            Toast.makeText(getContext(), String.valueOf(selectedAmbulancePrice), Toast.LENGTH_SHORT).show();
        });

        paymentButton.setOnClickListener(v -> {

            if(selectedAmbulance != 0 && selectedAmbulancePrice != 0) {
                Checkout checkout = new Checkout();
                checkout.setKeyID("rzp_test_3PiOcZSjoaFdOc");

                try {
                    JSONObject orderRequest = new JSONObject();
                    orderRequest.put("amount", selectedAmbulancePrice);
                    orderRequest.put("currency", "INR");
                    orderRequest.put("receipt", "order_rcptid_11");

                    checkout.open(getActivity(), orderRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getContext(), "Please Select An Ambulance", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}