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
    private Map<LatLng,Section> sections = new HashMap<>();
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
        route.setCheckpoints((HashMap<LatLng, Section>) sections);
        return route;
    }

    private void setRoutePoints(List<LatLng> routePoints) {
        this.routePoints = new ArrayList<>(routePoints);
    }

    private void setCheckpoints(HashMap<LatLng,Section> sections) {
        this.sections = new HashMap<>(sections);
    }

    public Map<LatLng,Section> getCheckpoints() {
        return sections;
    }

    public void addSection(Marker startMarker,Marker middleMarker,Marker endMarker, String type, String difficulty, Context context, GoogleMap mMap) {
        //find closest route point
        LatLng closestRoutePointStart = null;
        LatLng closestRoutePointMiddle = null;
        LatLng closestRoutePointEnd = null;
        double minDistanceStart = Double.MAX_VALUE;
        double minDistanceMiddle = Double.MAX_VALUE;
        double minDistanceEnd = Double.MAX_VALUE;
        for (LatLng routePoint : routePoints.toArray(new LatLng[0])) {
            double distanceStart = Math.sqrt(Math.pow(routePoint.latitude - startMarker.getPosition().latitude, 2) + Math.pow(routePoint.longitude - startMarker.getPosition().longitude, 2));
            if (distanceStart < minDistanceStart) {
                minDistanceStart = distanceStart;
                closestRoutePointStart = routePoint;
            }

            double distanceMiddle = Math.sqrt(Math.pow(routePoint.latitude - middleMarker.getPosition().latitude, 2) + Math.pow(routePoint.longitude - middleMarker.getPosition().longitude, 2));
            if (distanceMiddle < minDistanceMiddle) {
                minDistanceMiddle = distanceMiddle;
                closestRoutePointMiddle = routePoint;
            }

            double distanceEnd = Math.sqrt(Math.pow(routePoint.latitude - endMarker.getPosition().latitude, 2) + Math.pow(routePoint.longitude - endMarker.getPosition().longitude, 2));
            if (distanceEnd < minDistanceEnd) {
                minDistanceEnd = distanceEnd;
                closestRoutePointEnd = routePoint;
            }
        }

        //mark closest route point as checkpoint
        startMarker.setPosition(closestRoutePointStart);
        endMarker.setPosition(closestRoutePointEnd);
        Section section = new Section(startMarker.getTitle(), startMarker.getPosition(), middleMarker.getPosition(), endMarker.getPosition(), type, difficulty);

        sections.put(closestRoutePointStart, section);
        rebuildRoute(mMap,context);
    }

    private void rebuildRoute(GoogleMap mMap, Context context) {

        //remove old route

        //add new route
        addRouteToMap(mMap,context);
    }
    private Section getNthSection(int n) {
        int i = 0;
        for (LatLng routePoint : routePoints) {
            if (sections.containsKey(routePoint)) {
                if (i == n) {
                    return sections.get(routePoint);
                }
                if(i>n)
                    return Section.DEFAULT;
                i++;
            }
        }
        return Section.DEFAULT;
    }
    public void addRouteToMap(GoogleMap mMap, Context context) {
        //add each route point to map to the polyline options
        PolylineOptions currentPolyline= new PolylineOptions().clickable(true);
        String colors = "";
        try {
            String color= "#808080"; //gray
            String type = "default";
            String name = "default";
            currentPolyline.color(Color.parseColor(color));

            for (int i=0; i<routePoints.size(); i++) {
                //if route point is a checkpoint, add a marker and change color
                LatLng routePoint = routePoints.get(i);
                if (sections.containsKey(routePoint)) {
                    //add current polyline segment to map
                    // the current polyline is not a special section
                    mMap.addPolyline(currentPolyline);
                    polylineSegments.add(currentPolyline);

                    //start new polyline segment
                    currentPolyline = new PolylineOptions().clickable(true);
                    Section currentSection = sections.get(routePoint);
                    color = currentSection.getColor();
                    name=currentSection.getTitle();
                    colors=colors+"; "+name+":"+color+","+type+","+currentSection.getDifficulty();
                    currentPolyline.color(Color.parseColor(color));
                    //add start marker, middle marker, and end marker
                    mMap.addMarker(new MarkerOptions().position(routePoint).title(currentSection.getTitle())); //TODO: add icon
                    mMap.addMarker(new MarkerOptions().position(currentSection.getMiddleLocation()).title(currentSection.getTitle())); //TODO: add icon
                    mMap.addMarker(new MarkerOptions().position(currentSection.getEndLocation()).title(currentSection.getTitle())); //TODO: add icon

                    //add route points until section finish
                    currentPolyline.add(routePoint);
                    while(routePoint != currentSection.getEndLocation()){
                        i++;
                        routePoint = routePoints.get(i);
                        currentPolyline.add(routePoint);
                    }
                    // increment i to skip the end location
                    i++;
                    //add polyline segment to map
                    mMap.addPolyline(currentPolyline);
                    polylineSegments.add(currentPolyline);

                    //start new polyline segment
                    currentPolyline = new PolylineOptions().clickable(true);
                    color = "#808080"; //gray
                    type = "default";
                    name = "default";
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


