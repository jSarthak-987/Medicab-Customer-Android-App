package com.brightsky.medicab;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.razorpay.OTP;

import java.util.Map;
import java.util.Objects;

public class DriversFirebaseMessagingService extends FirebaseMessagingService {

    public static final String INTENT_FILTER = "SARTHAKISTHEREALMVP";
    public static final String OTP_VERIFICATION_INTENT_FILTER = "OTP_VERIFICATION";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> payloadData = remoteMessage.getData();

            if(Objects.equals(payloadData.get("type"), "CONFIRMATION")) {

                String txid = payloadData.get("txid");
                String driverkey = payloadData.get("driverkey");

                Intent intent = new Intent(INTENT_FILTER);

                intent.putExtra("TransactionId", txid);
                intent.putExtra("DriverKey", driverkey);

                sendBroadcast(intent);

                Log.e("FirebaseMessaging", "txid: " + txid + "    driverkey: " + driverkey);
            } else if(Objects.equals(payloadData.get("type"), "OTP_VERIFICATION")){
                String otp = payloadData.get("otp");

                Intent intent = new Intent(OTP_VERIFICATION_INTENT_FILTER);
                intent.putExtra("OTP", otp);

                sendBroadcast(intent);
            }
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }
}