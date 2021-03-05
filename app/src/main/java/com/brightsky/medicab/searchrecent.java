package com.brightsky.medicab;

class searchrecent {

    private final String hospitalName;
    private final String hospitalAddress;
    private final String hospitalLat;
    private final String hospitalLong;

    public searchrecent(String hospitalName, String hospitalAddress, String hospitalLat, String hospitalLong) {
        this.hospitalAddress = hospitalAddress;
        this.hospitalLat = hospitalLat;
        this.hospitalLong = hospitalLong;
        this.hospitalName = hospitalName;
    }

    public String getHospitalAddress() {
        return hospitalAddress;
    }

    public String getHospitalLat() {
        return hospitalLat;
    }

    public String getHospitalLong() {
        return hospitalLong;
    }

    public String getHospitalName() {
        return hospitalName;
    }
}