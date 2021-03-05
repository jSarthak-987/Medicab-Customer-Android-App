package com.brightsky.medicab.paytm;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Head {

    @SerializedName("responseTimestamp")
    @Expose
    private String responseTimestamp;
    @SerializedName("version")
    @Expose
    private String version;
    @SerializedName("clientId")
    @Expose
    private String clientId;
    @SerializedName("signature")
    @Expose
    private String signature;

    public String getResponseTimestamp() {
        return responseTimestamp;
    }

    public void setResponseTimestamp(String responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

}
