package com.brightsky.medicab;

public class UPIItems {

    private final String upiItemName;
    private final int upiItemImage;

    public UPIItems(String upiItemName, int upiItemImage) {
        this.upiItemName = upiItemName;
        this.upiItemImage = upiItemImage;
    }

    public String getUpiItemName() {
        return upiItemName;
    }

    public int getUpiItemImage() {
        return upiItemImage;
    }
}
