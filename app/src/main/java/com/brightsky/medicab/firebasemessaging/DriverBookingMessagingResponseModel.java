package com.brightsky.medicab.firebasemessaging;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DriverBookingMessagingResponseModel {

    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("response")
    @Expose
    private String response;

    @SerializedName("error")
    @Expose
    private String error;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }
}

