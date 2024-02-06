package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationUpdaterThread extends Thread {

    private static final long UPDATE_INTERVAL = 2000; // Update interval in milliseconds
    private static final long FASTEST_INTERVAL = 1000; // Fastest update interval in milliseconds

    private Context context;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;
    private Handler handler;

    private OnLocationChangeListener locationChangeListener;

    public interface OnLocationChangeListener {
        void onLocationChange(Location location);
    }

    public void setOnLocationChangeListener(OnLocationChangeListener listener) {
        this.locationChangeListener = listener;
    }


    public LocationUpdaterThread(Context context) {
        this.context = context;
        handler = new Handler(Looper.getMainLooper());
        buildLocationRequest();
        buildLocationCallback();
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location latestLocation = locationResult.getLastLocation();
                    updateCurrentLocation(latestLocation);
                }
            }
        };
    }

//    private void updateCurrentLocation(Location location) {
//        handler.post(() -> {
//            // Update your public variable here
//            currentLocation = location;
//            // You can perform any additional actions with the updated location if needed
//        });
//    }
    private void updateCurrentLocation(Location location) {
        handler.post(() -> {
            currentLocation = location;
            if (locationChangeListener != null) {
                locationChangeListener.onLocationChange(location);
            }
        });
    }
    @Override
    public void run() {
        // This method will be executed when the thread starts
        startLocationUpdates();
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdates() {
        LocationServices.getFusedLocationProviderClient(context)
                .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    public void stopLocationUpdates() {
        LocationServices.getFusedLocationProviderClient(context).removeLocationUpdates(locationCallback);
    }
}