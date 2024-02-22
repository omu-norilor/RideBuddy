package com.example.myapplication;


import com.google.android.gms.maps.model.LatLng;

public class SerializableLatLng {
    private double latitude;
    private double longitude;

    public SerializableLatLng() {
        // Required no-argument constructor for serialization
    }
    public SerializableLatLng(LatLng latLng) {
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
    }

    public LatLng toLatLng() {
        return new LatLng(latitude, longitude);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
