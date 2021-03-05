package com.brightsky.medicab;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import static android.content.Context.CONNECTIVITY_SERVICE;


public class NetworkChangeListener extends BroadcastReceiver {

    NetworkChangeInterface networkChangeInterface;

    public NetworkChangeListener(NetworkChangeInterface networkChangeInterface) {
        this.networkChangeInterface = networkChangeInterface;
    }

    private boolean checkIfInternetIsAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            boolean networkAvailable = checkIfInternetIsAvailable(context);
            networkChangeInterface.networkChanged(networkAvailable);
        }
    }
}


interface NetworkChangeInterface {
    void networkChanged(boolean networkAvailable);
}