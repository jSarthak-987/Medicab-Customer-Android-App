package com.brightsky.medicab;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.razorpay.Checkout;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.Objects;

public class PaymentFragment extends Fragment {

    private CardView confirmButton;
    private Button cancelButton;
    private String priceString;
    private int price;
    private TextView payNowButtonText;
    private TextView payNowPrice;
    private TextView payLaterPrice;
    public static String TAG = "PriceDialogModelSheetFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_price_dialog_model_sheet_list_dialog, container, false);
        confirmButton = view.findViewById(R.id.onlinepaybtn);
        payNowButtonText = view.findViewById(R.id.pay_button_text);
        payNowPrice = view.findViewById(R.id.pay_now_price);
        payLaterPrice = view.findViewById(R.id.pay_later_price);
      //  cancelButton = view.findViewById(R.id.cancel_payment);


        if (getArguments() != null) {
            priceString = getArguments().getString("AmbulancePrice");
            price = Integer.parseInt(priceString);

            String payNowPriceString = payNowPrice.getText() + "" + (price * 5 /100);
            String payLaterPriceString = payLaterPrice.getText() + "" + (price - (price * 5 / 100));

            payNowPrice.setText(payNowPriceString);
            payLaterPrice.setText(payLaterPriceString);

            String payNowButtonTextString = payNowButtonText.getText().toString() + priceString;
            payNowButtonText.setText(payNowPriceString);
            Toast.makeText(getContext(), "Price: " + price, Toast.LENGTH_SHORT).show();
            Toast.makeText(getContext(), "PriceString: " + priceString, Toast.LENGTH_SHORT).show();
        }

        //cancelButton.setOnClickListener(v -> dismiss());

        confirmButton.setOnClickListener(v -> {
            if (priceString != null) {
                Checkout checkout = new Checkout();
                checkout.setKeyID("rzp_test_3PiOcZSjoaFdOc");

                try {
                    JSONObject orderRequest = new JSONObject();
                    orderRequest.put("amount", (price * 5));
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}