package com.example.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Route {

    private String name;
    private List<LatLng> routePoints = new ArrayList<>();
    private Marker startMarker;
    private Map<LatLng,Checkpoint> checkpoints = new HashMap<>();
    private Marker endMarker;
    private List<PolylineOptions> polylineSegments = new ArrayList<>();

    public Route( String name) {
        this.name = name;
        PolylineOptions polylineOptions= new PolylineOptions().clickable(true);
        polylineOptions.color(Color.parseColor("#808080"));
        polylineSegments.add(polylineOptions);

    }

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
        route.setPolylineSegments(polylineSegments);
        route.setRoutePoints(routePoints);
        route.setCheckpoints((HashMap<LatLng, Checkpoint>) checkpoints);
        return route;
    }

    private void setRoutePoints(List<LatLng> routePoints) {
        this.routePoints = new ArrayList<>(routePoints);
    }

    private void setCheckpoints(HashMap<LatLng,Checkpoint> checkpoints) {
        this.checkpoints = new HashMap<>(checkpoints);
    }

    public Map<LatLng,Checkpoint> getCheckpoints() {
        return checkpoints;
    }

    public void addCheckpoint(Marker marker,String type,Context context, GoogleMap mMap) {
        //find closest route point
        LatLng closestRoutePoint = null;
        double minDistance = Double.MAX_VALUE;
        for (LatLng routePoint : routePoints.toArray(new LatLng[0])) {
            double distance = Math.sqrt(Math.pow(routePoint.latitude - marker.getPosition().latitude, 2) + Math.pow(routePoint.longitude - marker.getPosition().longitude, 2));
            if (distance < minDistance) {
                minDistance = distance;
                closestRoutePoint = routePoint;
            }
        }

        //mark closest route point as checkpoint
        marker.setPosition(closestRoutePoint);
        Checkpoint checkpoint = new Checkpoint(marker.getTitle(), marker.getPosition(), type);
        checkpoints.put(closestRoutePoint, checkpoint);
        rebuildRoute(mMap,context);
    }

    private void rebuildRoute(GoogleMap mMap, Context context) {

        //remove old route

        //add new route
        addRouteToMap(mMap,context);
    }
    private Checkpoint getNthCheckpoint(int n) {
        int i = 0;
        for (LatLng routePoint : routePoints) {
            if (checkpoints.containsKey(routePoint)) {
                if (i == n) {
                    return checkpoints.get(routePoint);
                }
                if(i>n)
                    return Checkpoint.DEFAULT;
                i++;
            }
        }
        return Checkpoint.DEFAULT;
    }
    public void addRouteToMap(GoogleMap mMap, Context context) {
        //add each route point to map to the polyline options
        PolylineOptions currentPolyline= new PolylineOptions().clickable(true);
        String colors = "";
        try {
            int i = 0;
            String color= "#";
            color = getNthCheckpoint(i).getColor();
            String name=getNthCheckpoint(i).getTitle();
            String type=getNthCheckpoint(i).getType();
            colors=name+":"+color+","+type;
            currentPolyline.color(Color.parseColor(color));
            for (LatLng routePoint : routePoints) {
                //if route point is a checkpoint, add a marker and change color
                if (checkpoints.containsKey(routePoint)) {
                    //add polyline segment to map
                    mMap.addPolyline(currentPolyline);
                    polylineSegments.add(currentPolyline);

                    //start new polyline segment
                    currentPolyline = new PolylineOptions().clickable(true);
                    i++;
                    color = getNthCheckpoint(i).getColor();
                    name=getNthCheckpoint(i).getTitle();
                    type=getNthCheckpoint(i).getType();
                    //random color
//                    color = String.format("#%06x", (int) (Math.random() * 0xffffff));
                    colors=colors+"; "+name+":"+color+","+type;
                    mMap.addMarker(new MarkerOptions().position(routePoint).title(checkpoints.get(routePoint).getTitle())); //TODO: add icon
                    currentPolyline.color(Color.parseColor(color));
                }
                currentPolyline.add(routePoint);
            }
            mMap.addPolyline(currentPolyline);
            mMap.addMarker(new MarkerOptions().position(this.getRoutePoints().get(0)).title("Start of " + name));
            mMap.addMarker(new MarkerOptions().position(this.getRoutePoints().get(this.getRoutePoints().size() - 1)).title("End of " + name));
            Toast.makeText(context, "Route added with colors: " + colors, Toast.LENGTH_SHORT).show();
        }
        catch (NullPointerException e) {
            // Handle the NullPointerException and show an error message using a Toast
            Toast.makeText(context, "An error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }

    public void setPolylineSegments(List<PolylineOptions> polylineOptions) {
        this.polylineSegments = new ArrayList<>(polylineOptions);
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
        polylineSegments.get(polylineSegments.size() - 1).add(latLng);
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

        //remove polyline segments
//        for (PolylineOptions polylineSegment : polylineSegments) {
//            polylineSegment.remove();
//        }


        PolylineOptions polylineOptions = new PolylineOptions().clickable(true);
        polylineOptions.color(Color.parseColor("#FF6750A3"));
        polylineSegments.add(polylineOptions);
    }

    public List<PolylineOptions> getPolylineSegments() {
        return polylineSegments;
    }

    public List<LatLng> getRoutePoints() {
        return routePoints;
    }

    public Marker getEndMarker() {
        return endMarker;
    }
}


