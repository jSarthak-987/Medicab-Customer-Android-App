package com.brightsky.medicab.firebasemessaging;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DriverBookingMessagingRequestModel {
    @SerializedName("driverToken")
    @Expose
    private List<String> driverToken;

    @SerializedName("txid")
    @Expose
    private String txid;

    @SerializedName("clientToken")
    @Expose
    private String clientToken;

    @SerializedName("clientKey")
    @Expose
    private String clientKey;

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    public List<String> getDriverToken() {
        return driverToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    public void setDriverToken(List<String> driverToken) {
        this.driverToken = driverToken;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public String getClientKey() {
        return clientKey;
    }

    public String getClientToken() {
        return clientToken;
    }

    public String getTxid() {
        return txid;
    }
}
