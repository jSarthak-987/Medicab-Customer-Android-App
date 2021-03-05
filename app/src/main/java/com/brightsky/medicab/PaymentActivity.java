package com.brightsky.medicab;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.brightsky.medicab.paytm.PaytmTransactionIdResponseModel;
import com.google.android.gms.maps.model.LatLng;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PaymentActivity extends AppCompatActivity implements PaymentResultListener {

    private static final String TAG = "PaymentActivity";
    Checkout checkout;
    private final String GOOGLE_PAY_PACKAGE_NAME = "com.google.android.apps.nbu.paisa.user";
    private final String PHONEPE_PACKAGE_NAME = "com.phonepe.app";
    private final String PAYTM_PACKAGE_NAME = "net.one97.paytm";

    private final int GOOGLE_PAY_REQUEST_CODE = 123;
    private final int PHONEPE_REQUEST_CODE = 234;
    private final int PAYTM_REQUEST_CODE = 345;

    private LatLng pickupLatLng;
    private String amount;
    private String orderId;
    private String callbackurl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        pickupLatLng = getIntent().getExtras().getParcelable("PickupLatLng");
        amount = getIntent().getStringExtra("Amount");
        orderId = "ORDER_1234";
        callbackurl = "https://securegw-stage.paytm.in/theia/paytmCallback?ORDER_ID=$" + orderId;

        Checkout.preload(getApplicationContext());
        ConstraintLayout cardPaymentButton = findViewById(R.id.debit_credit_card_details);

        RecyclerView upiRecyclerView = findViewById(R.id.upi_items);
        Button bookAmbulanceButton = findViewById(R.id.book_ambulance_button);

        List<String> upiItemNames = getUpiItemNames();
        List<Integer> upiItemImages = getUpiItemImages();

        List<UPIItems> upiItems = new ArrayList<>();

        for(int i = 0; i < upiItemImages.size(); i++) {
            upiItems.add(new UPIItems(upiItemNames.get(i), upiItemImages.get(i)));
        }

        upiRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        UPIMethodsRecyclerViewAdapter upiMethodsRecyclerViewAdapter = new UPIMethodsRecyclerViewAdapter(upiItems);
        upiMethodsRecyclerViewAdapter.setUpiItemClickListener(new UPIItemClickListener() {

            @Override
            public void onGooglePayItemClickListener() {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(getUPIUri("sarthak987joshi@oksbi", "Sarthak Joshi", "1"));
                intent.setPackage(GOOGLE_PAY_PACKAGE_NAME);
                startActivityForResult(intent, GOOGLE_PAY_REQUEST_CODE);
            }

            @Override
            public void onPhonePeItemClickListener() {
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setData(getUPIUri("sarthak987joshi@oksbi", "Sarthak Joshi", "1"));
//                intent.setPackage(PHONEPE_PACKAGE_NAME);
//                startActivityForResult(intent, PHONEPE_REQUEST_CODE);
            }

            @Override
            public void onPayTMItemClickListener() {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(getUPIUri("8005923313@paytm", "Sarthak Joshi", "1"));
                intent.setPackage(PAYTM_PACKAGE_NAME);
                startActivityForResult(intent, PAYTM_REQUEST_CODE);
            }
        });

        upiRecyclerView.setAdapter(upiMethodsRecyclerViewAdapter);

        cardPaymentButton.setOnClickListener(v -> {
            CardDetailsBottomSheetFragment cardDetailsBottomSheetFragment = new CardDetailsBottomSheetFragment();

            cardDetailsBottomSheetFragment.setCardDetailsListener(cardDetails -> {

                checkout = new Checkout();
                checkout.setKeyID("rzp_test_3PiOcZSjoaFdOc");

                try {
                    JSONObject orderRequest = new JSONObject();
                    orderRequest.put("amount", 50000);
                    orderRequest.put("currency", "INR");
                    orderRequest.put("receipt", "order_rcptid_11");

                    checkout.open(this, orderRequest);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            });

            cardDetailsBottomSheetFragment.show(getSupportFragmentManager(), "CardPaymentSheet");
        });

        bookAmbulanceButton.setOnClickListener(v -> {
            Intent ambulanceBookingIntent = new Intent(PaymentActivity.this, BookingConfirmation.class);
            ambulanceBookingIntent.putExtra("PickupLatLng", pickupLatLng);
            startActivity(ambulanceBookingIntent);
            finish();
        });
    }

    private List<String> getUpiItemNames() {
        List<String> upiItemNames = new ArrayList<>();

        upiItemNames.add("Google Pay");
        upiItemNames.add("BHIM UPI");
        upiItemNames.add("PhonePa");
        upiItemNames.add("PayTM");

        return upiItemNames;
    }

    private List<Integer> getUpiItemImages() {
        List<Integer> upiItemImages = new ArrayList<>();

        upiItemImages.add(R.mipmap.google_pay);
        upiItemImages.add(R.mipmap.google_pay);
        upiItemImages.add(R.mipmap.google_pay);
        upiItemImages.add(R.mipmap.google_pay);

        return upiItemImages;
    }

    private Uri getUPIUri(String vpa, String merchantName, String price) {
        return new Uri.Builder()
                .scheme("upi")
                .authority("pay")
                .appendQueryParameter("pa", vpa)
                .appendQueryParameter("pn", merchantName)
                .appendQueryParameter("tn", "Demo Testing Google Pay UPI")
                .appendQueryParameter("am", price)
                .appendQueryParameter("cu", "INR")
                .build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GOOGLE_PAY_REQUEST_CODE || requestCode== PAYTM_REQUEST_CODE || requestCode == PHONEPE_REQUEST_CODE) {
            Intent ambulanceBookingIntent = new Intent(PaymentActivity.this, BookingConfirmation.class);
            ambulanceBookingIntent.putExtra("PickupLatLng", pickupLatLng);
            startActivity(ambulanceBookingIntent);
            finish();
        }

        else if(requestCode == 200 && data != null) {
            Toast.makeText(this, data.getStringExtra("nativeSdkForMerchantMessage") + data.getStringExtra("response"), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPaymentSuccess(String s) {
        Intent ambulanceBookingIntent = new Intent(PaymentActivity.this, BookingConfirmation.class);
        ambulanceBookingIntent.putExtra("PickupLatLng", pickupLatLng);
        startActivity(ambulanceBookingIntent);
        finish();
    }

    @Override
    public void onPaymentError(int i, String s) {
        Log.i(TAG, "RazorPay Error");
    }
}