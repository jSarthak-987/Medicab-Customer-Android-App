package com.brightsky.medicab.paytm;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Body {

    @SerializedName("resultInfo")
    @Expose
    private ResultInfo resultInfo;
    @SerializedName("extraParamsMap")
    @Expose
    private Object extraParamsMap;
    @SerializedName("txnToken")
    @Expose
    private String txnToken;
    @SerializedName("isPromoCodeValid")
    @Expose
    private Boolean isPromoCodeValid;
    @SerializedName("authenticated")
    @Expose
    private Boolean authenticated;

    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    public void setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }

    public Object getExtraParamsMap() {
        return extraParamsMap;
    }

    public void setExtraParamsMap(Object extraParamsMap) {
        this.extraParamsMap = extraParamsMap;
    }

    public String getTxnToken() {
        return txnToken;
    }

    public void setTxnToken(String txnToken) {
        this.txnToken = txnToken;
    }

    public Boolean getIsPromoCodeValid() {
        return isPromoCodeValid;
    }

    public void setIsPromoCodeValid(Boolean isPromoCodeValid) {
        this.isPromoCodeValid = isPromoCodeValid;
    }

    public Boolean getAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(Boolean authenticated) {
        this.authenticated = authenticated;
    }

}