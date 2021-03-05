package com.brightsky.medicab;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.razorpay.Checkout;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class BottomSelectionFragment extends Fragment {

    private int price;
    private TextView advancedPayment;
    private TextView laterPayment;

    private ArrayList<String> ambulancesPrices;
    private ConstraintLayout alsButton;
    private ConstraintLayout blsButton;
    private ConstraintLayout mortgageButton;
    private ConstraintLayout intercityButton;

    private int lastClickedAmbulanceButton;
    private SelectedAmbulanceType selectedAmbulanceType;



    public void setSelectedAmbulanceType(SelectedAmbulanceType selectedAmbulanceType) {
        this.selectedAmbulanceType = selectedAmbulanceType;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_selctionpanel, container, false);

        FrameLayout paymentButton = view.findViewById(R.id.payment_button);
        alsButton = view.findViewById(R.id.als_button);
        blsButton = view.findViewById(R.id.bls_button);
        mortgageButton = view.findViewById(R.id.mortgage_button);
        intercityButton = view.findViewById(R.id.intecity_button);
        TextView alsPrice = view.findViewById(R.id.als_price);
        TextView blsPrice = view.findViewById(R.id.bls_price);
        TextView mortgagePrice = view.findViewById(R.id.mortgage_price);
        TextView intercityPrice = view.findViewById(R.id.intercity_price);
        FrameLayout noAmbulanceAvailableFrameLayout = view.findViewById(R.id.no_ambulance_available);
        ConstraintLayout promoCode = view.findViewById(R.id.promo_code);
        ConstraintLayout paymentInfo = view.findViewById(R.id.payment_info);
        ConstraintLayout buttons = view.findViewById(R.id.buttons);
        advancedPayment = view.findViewById(R.id.advanced_payment);
        laterPayment = view.findViewById(R.id.later_payment);

        price = 0;

        if (getArguments() != null) {
            try {
                ambulancesPrices = getArguments().getStringArrayList("Prices");
                ArrayList<String> ambulanceAvailables = getArguments().getStringArrayList("AmbulanceAvailable");

                if(ambulanceAvailables.size() > 0) {
                    promoCode.setVisibility(View.VISIBLE);
                    paymentInfo.setVisibility(View.VISIBLE);
                    buttons.setVisibility(View.VISIBLE);

                    if(ambulanceAvailables.contains("ALS")) alsButton.setVisibility(View.VISIBLE);
                    if(ambulanceAvailables.contains("BLS")) blsButton.setVisibility(View.VISIBLE);
                    if(ambulanceAvailables.contains("Mortgage")) mortgageButton.setVisibility(View.VISIBLE);
                    if(ambulanceAvailables.contains("Intercity")) intercityButton.setVisibility(View.VISIBLE);

                    price = Integer.parseInt(ambulancesPrices.get(0));

                    String alsPriceString = "₹" + ambulancesPrices.get(0);
                    String blsPriceString = "₹" + ambulancesPrices.get(1);
                    String mortgagePriceString = "₹" + ambulancesPrices.get(2);
                    String intercityPriceString = "₹" + ambulancesPrices.get(3);

                    alsPrice.setText(alsPriceString);
                    blsPrice.setText(blsPriceString);
                    mortgagePrice.setText(mortgagePriceString);
                    intercityPrice.setText(intercityPriceString);
                } else {
                    noAmbulanceAvailableFrameLayout.setVisibility(View.VISIBLE);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        alsButton.setOnClickListener(v -> {
            price = Integer.parseInt(ambulancesPrices.get(0));
            Toast.makeText(getContext(), "Ambulance Selected: " + 0, Toast.LENGTH_SHORT).show();
            ambulanceSelectionAnimation(0);

            lastClickedAmbulanceButton = 0;

            advancedPayment.setText(String.valueOf(price * 20 / 100));
            laterPayment.setText(String.valueOf(price - (price * 20 / 100)));

            selectedAmbulanceType.ambulanceSelected("ALS");
        });

        blsButton.setOnClickListener(v -> {
            price = Integer.parseInt(ambulancesPrices.get(1));
            ambulanceSelectionAnimation(1);

            lastClickedAmbulanceButton = 1;

            advancedPayment.setText(String.valueOf(price * 20 / 100));
            laterPayment.setText(String.valueOf(price - (price * 20 / 100)));

            selectedAmbulanceType.ambulanceSelected("BLS");
        });

        mortgageButton.setOnClickListener(v -> {
            price = Integer.parseInt(ambulancesPrices.get(2));
            ambulanceSelectionAnimation(2);

            lastClickedAmbulanceButton = 2;

            advancedPayment.setText(String.valueOf(price * 20 / 100));
            laterPayment.setText(String.valueOf(price - (price * 20 / 100)));

            selectedAmbulanceType.ambulanceSelected("Mortgage");
        });

        intercityButton.setOnClickListener(v -> {
            price = Integer.parseInt(ambulancesPrices.get(3));
            ambulanceSelectionAnimation(3);

            lastClickedAmbulanceButton = 3;

            advancedPayment.setText(String.valueOf(price * 20 / 100));
            laterPayment.setText(String.valueOf(price - (price * 20 / 100)));

            selectedAmbulanceType.ambulanceSelected("Intercity");
        });

        paymentButton.setOnClickListener(v -> {
            if (price != 0) {
                Checkout checkout = new Checkout();
                checkout.setKeyID("rzp_test_3PiOcZSjoaFdOc");

                try {
                    JSONObject orderRequest = new JSONObject();
                    orderRequest.put("amount", (price * 20));
                    orderRequest.put("currency", "INR");
                    orderRequest.put("receipt", "order_rcptid_11");

                    checkout.open(Objects.requireNonNull(getActivity()), orderRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getActivity(), "Please Select An Ambulance", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void ambulanceSelectionAnimation(int ambulanceSelection) {
        ValueAnimator backgroundAnimateAnimation = ValueAnimator.ofArgb(Color.parseColor("#fafafa"), Color.parseColor("#f5f5f5"));
        ValueAnimator backgroundResetAnimation = ValueAnimator.ofArgb(Color.parseColor("#f5f5f5"), Color.parseColor("#fafafa"));
        ValueAnimator strokeAnimateAnimation = ValueAnimator.ofArgb(Color.parseColor("#f5f5f5"), Color.parseColor("#bbdefb"));
        ValueAnimator strokeResetAnimation = ValueAnimator.ofArgb(Color.parseColor("#bbdefb"), Color.parseColor("#f5f5f5"));

        Toast.makeText(getContext(), "" + ambulanceSelection, Toast.LENGTH_SHORT).show();

        backgroundAnimateAnimation.setDuration(300);
        backgroundResetAnimation.setDuration(300);

        backgroundResetAnimation.addUpdateListener(animation -> {
            if(ambulanceSelection != lastClickedAmbulanceButton)
                switch (lastClickedAmbulanceButton) {
                    case 0:
                        GradientDrawable alsGradientDrawable = (GradientDrawable) alsButton.getBackground();
                        alsGradientDrawable.setColor((int) animation.getAnimatedValue());
                        alsButton.setBackground(alsGradientDrawable);
                        break;

                    case 1:
                        GradientDrawable blsGradientDrawable = (GradientDrawable) blsButton.getBackground();
                        blsGradientDrawable.setColor((int) animation.getAnimatedValue());
                        blsButton.setBackground(blsGradientDrawable);
                        break;

                    case 2:
                        GradientDrawable mortgageGradientDrawable = (GradientDrawable) mortgageButton.getBackground();
                        mortgageGradientDrawable.setColor((int) animation.getAnimatedValue());
                        mortgageButton.setBackground(mortgageGradientDrawable);
                        break;

                    case 3:
                        GradientDrawable intercityGradientDrawable = (GradientDrawable) intercityButton.getBackground();
                        intercityGradientDrawable.setColor((int) animation.getAnimatedValue());
                        intercityButton.setBackground(intercityGradientDrawable);
                        break;
                }
        });

        strokeResetAnimation.addUpdateListener(animation -> {
            if(ambulanceSelection != lastClickedAmbulanceButton)
                switch (lastClickedAmbulanceButton) {
                    case 0:
                        GradientDrawable alsGradientDrawable = (GradientDrawable) alsButton.getBackground();
                        alsGradientDrawable.setStroke(2, (int) animation.getAnimatedValue());
                        alsButton.setBackground(alsGradientDrawable);
                        break;

                    case 1:
                        GradientDrawable blsGradientDrawable = (GradientDrawable) blsButton.getBackground();
                        blsGradientDrawable.setStroke(2, (int) animation.getAnimatedValue());
                        blsButton.setBackground(blsGradientDrawable);
                        break;

                    case 2:
                        GradientDrawable mortgageGradientDrawable = (GradientDrawable) mortgageButton.getBackground();
                        mortgageGradientDrawable.setStroke(2, (int) animation.getAnimatedValue());
                        mortgageButton.setBackground(mortgageGradientDrawable);
                        break;

                    case 3:
                        GradientDrawable intercityGradientDrawable = (GradientDrawable) intercityButton.getBackground();
                        intercityGradientDrawable.setStroke(2, (int) animation.getAnimatedValue());
                        intercityButton.setBackground(intercityGradientDrawable);
                        break;
                }
        });

        backgroundAnimateAnimation.addUpdateListener(animation -> {
            switch (ambulanceSelection) {
                case 0:
                    GradientDrawable alsGradientDrawable = (GradientDrawable) alsButton.getBackground();
                    alsGradientDrawable.setColor((int) animation.getAnimatedValue());
                    alsButton.setBackground(alsGradientDrawable);
                    break;

                case 1:
                    GradientDrawable blsGradientDrawable = (GradientDrawable) blsButton.getBackground();
                    blsGradientDrawable.setColor((int) animation.getAnimatedValue());
                    blsButton.setBackground(blsGradientDrawable);
                    break;

                case 2:
                    GradientDrawable mortgageGradientDrawable = (GradientDrawable) mortgageButton.getBackground();
                    mortgageGradientDrawable.setColor((int) animation.getAnimatedValue());
                    mortgageButton.setBackground(mortgageGradientDrawable);
                    break;

                case 3:
                    GradientDrawable intercityGradientDrawable = (GradientDrawable) intercityButton.getBackground();
                    intercityGradientDrawable.setColor((int) animation.getAnimatedValue());
                    intercityButton.setBackground(intercityGradientDrawable);
                    break;
            }
        });

        strokeAnimateAnimation.addUpdateListener(animation -> {
            switch (ambulanceSelection) {
                case 0:
                    GradientDrawable alsGradientDrawable = (GradientDrawable) alsButton.getBackground();
                    alsGradientDrawable.setStroke(2, (int) animation.getAnimatedValue());
                    alsButton.setBackground(alsGradientDrawable);
                    break;

                case 1:
                    GradientDrawable blsGradientDrawable = (GradientDrawable) blsButton.getBackground();
                    blsGradientDrawable.setStroke(2, (int) animation.getAnimatedValue());
                    blsButton.setBackground(blsGradientDrawable);
                    break;

                case 2:
                    GradientDrawable mortgageGradientDrawable = (GradientDrawable) mortgageButton.getBackground();
                    mortgageGradientDrawable.setStroke(2, (int) animation.getAnimatedValue());
                    mortgageButton.setBackground(mortgageGradientDrawable);
                    break;

                case 3:
                    GradientDrawable intercityGradientDrawable = (GradientDrawable) intercityButton.getBackground();
                    intercityGradientDrawable.setStroke(2, (int) animation.getAnimatedValue());
                    intercityButton.setBackground(intercityGradientDrawable);
                    break;
            }
        });

        backgroundAnimateAnimation.start();
        backgroundResetAnimation.start();
        strokeAnimateAnimation.start();
        strokeResetAnimation.start();
    }
}