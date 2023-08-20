package com.example.myapplication;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class Route {

    private String name;
    private List<LatLng> routePoints = new ArrayList<>();
    private Marker startMarker;
    private Marker endMarker;
    private PolylineOptions polylineOptions;

    public Route( String name) {
        this.name = name;
        polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.parseColor("#FF6750A3"));

    }

//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//        sb.append(name).append(";");
//
//        for (LatLng point : routePoints) {
//            sb.append(point.latitude).append(",").append(point.longitude).append(";");
//        }
//
//        if (startMarker != null) {
//            sb.append(startMarker.getPosition().latitude).append(",").append(startMarker.getPosition().longitude).append(";");
//        } else {
//            sb.append("null;");
//        }
//
//        if (endMarker != null) {
//            sb.append(endMarker.getPosition().latitude).append(",").append(endMarker.getPosition().longitude).append(";");
//        } else {
//            sb.append("null;");
//        }
//
//        // Append other fields as needed
//
//        return sb.toString();
//    }
//
//    public static Route parseFromString(String input) {
//        String[] parts = input.split(";");
//        Route route = new Route("meh");
//
//        if (parts.length < 5) {
//            throw new IllegalArgumentException("Invalid input format");
//        }
//
//        route.name = parts[0];
//
//        for (int i = 1; i < parts.length - 4; i++) {
//            String[] latLng = parts[i].split(",");
//            double latitude = Double.parseDouble(latLng[0]);
//            double longitude = Double.parseDouble(latLng[1]);
//            route.routePoints.add(new LatLng(latitude, longitude));
//        }
//
//        if (!parts[parts.length - 4].equals("null")) {
//            // Get lat and long
//            String[] latLng = parts[parts.length - 4].split(",");
//            double latitude = Double.parseDouble(latLng[0]);
//            double longitude = Double.parseDouble(latLng[1]);
//            LatLng position = new LatLng(latitude, longitude);
//            MarkerOptions markerOptions = new MarkerOptions().position(position).title("Start of"+route.name);
//            route.startMarker = new Marker(markerOptions);
//        }
//
//        if (!parts[parts.length - 3].equals("null")) {
//            route.endMarker = Marker.parseFromString(parts[parts.length - 3]);
//            String[] latLng = parts[parts.length - 4].split(",");
//            double latitude = Double.parseDouble(latLng[0]);
//            double longitude = Double.parseDouble(latLng[1]);
//            LatLng position = new LatLng(latitude, longitude);
//            MarkerOptions markerOptions = new MarkerOptions().position(position).title("Start of"+route.name);
//        }
//
//        // Parse other fields as needed
//
//        return route;
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Route clone() {
        Route route = new Route(this.getName());
        route.setStartMarker(startMarker);
        route.updateEndMarker(endMarker);
        route.setPolylineOptions(polylineOptions);
        route.setRoutePoints(routePoints);
        return route;
    }

    private void setRoutePoints(List<LatLng> routePoints) {
        this.routePoints = new ArrayList<>(routePoints);
    }

    private void setPolylineOptions(PolylineOptions polylineOptions) {
        this.polylineOptions = new PolylineOptions();
        this.polylineOptions.color(Color.parseColor("#FF6750A3"));
        this.polylineOptions.addAll(polylineOptions.getPoints());
    }

    public Marker getStartMarker() {
        return startMarker;
    }
    public void setStartMarker(Marker marker) {
        startMarker = marker;
    }

    public void updateEndMarker(Marker marker) {
        endMarker = marker;
    }
    public void addRoutePoint(LatLng latLng) {
        routePoints.add(latLng);
        polylineOptions.add(latLng);
    }

    public void removeRoute() {
        //remove markers
        if (startMarker != null) {
            startMarker.remove();
        }
        if (endMarker != null) {
            endMarker.remove();
        }
        routePoints.clear();
        polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.parseColor("#FF6750A3"));
    }

    public PolylineOptions getPolylineOptions() {
        return polylineOptions;
    }

    public List<LatLng> getRoutePoints() {
        return routePoints;
    }

    public Marker getEndMarker() {
        return endMarker;
    }
}
