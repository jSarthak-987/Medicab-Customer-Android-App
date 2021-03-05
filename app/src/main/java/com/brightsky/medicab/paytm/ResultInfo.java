package com.brightsky.medicab.paytm;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResultInfo {

    @SerializedName("resultStatus")
    @Expose
    private String resultStatus;
    @SerializedName("resultCode")
    @Expose
    private String resultCode;
    @SerializedName("resultMsg")
    @Expose
    private String resultMsg;
    @SerializedName("bankRetry")
    @Expose
    private Object bankRetry;
    @SerializedName("retry")
    @Expose
    private Object retry;

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public Object getBankRetry() {
        return bankRetry;
    }

    public void setBankRetry(Object bankRetry) {
        this.bankRetry = bankRetry;
    }

    public Object getRetry() {
        return retry;
    }

    public void setRetry(Object retry) {
        this.retry = retry;
    }

}
