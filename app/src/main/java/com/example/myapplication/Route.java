package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
    private List<Long> timePoints = new ArrayList<>();
    private Marker startMarker;
    private Map<LatLng,Section> sections = new HashMap<>();
    private Marker endMarker;
    private List<PolylineOptions> polylineSegments = new ArrayList<>();
    private Float distance;
    private String time;


    public Route( String name) {
        this.name = name;
        PolylineOptions polylineOptions= new PolylineOptions().clickable(true);
        polylineOptions.color(Color.parseColor("#808080"));
        polylineSegments.add(polylineOptions);

    }

    public Map<LatLng, Section> getSections() {
        return sections;
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
        route.setDistance(distance);
        route.setTimePoints(timePoints);
        return route;
    }

    private void setTimePoints(List<Long> timePoints) {
        this.timePoints = new ArrayList<>(timePoints);
    }

    private void setDistance(Float distance) {
        this.distance = distance;
    }

    public Float getDistance() {
        return distance;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        if (this.time == null)
            this.time = time;
        else if (time.compareTo(this.time) < 0)
            this.time = time;
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

    private void setRoutePoints(List<LatLng> routePoints) {
        this.routePoints = new ArrayList<>(routePoints);
    }

    private void setCheckpoints(HashMap<LatLng,Section> sections) {
        this.sections = new HashMap<>(sections);
    }

    public Map<LatLng,Section> getCheckpoints() {
        return sections;
    }

    public void calculateDistance() {
        float distance = 0;
        for (int i = 0; i < routePoints.size() - 1; i++) {
            distance += distanceBetweenPoints(routePoints.get(i), routePoints.get(i + 1));
            Log.d("distance", String.valueOf(distance));
        }
        // convert to km
        distance = distance / 1000;
        this.distance = distance;
    }

    public void calculateTime(){
        long time = 0;

        //get ends
        Long startTime = timePoints.get(0);
        Long endTime = timePoints.get(timePoints.size()-1);

        //calculate time
        time = endTime - startTime;
    }

    public void calculateSectionDistance(Section section){
        float distance = 0;
        boolean status = false;
        for (int i = 0; i < routePoints.size() - 1; i++) {

            if(status == true){
                double dist = distanceBetweenPoints(routePoints.get(i), routePoints.get(i + 1));
                //convert to km, with 2 decimal places
                distance = distance + (float) dist/1000 ;
            }

            if(routePoints.get(i).equals(section.getStartLocation())) status = true;
            if(routePoints.get(i).equals(section.getEndLocation())) status = false;
        }
        section.setDistance(distance);
    }

    public void calculateSectionTime(Section section){
        long time = 0;
        Long startTime = Long.valueOf(0);
        Long endTime = Long.valueOf(0);
        //get ends
        for (int i = 0; i < routePoints.size() - 1; i++) {
            if(routePoints.get(i).equals(section.getStartLocation())) startTime = timePoints.get(i);
            if(routePoints.get(i).equals(section.getEndLocation())) endTime = timePoints.get(i);
        }

        //calculate time
        time = endTime - startTime;
        section.setTime(time);
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
        middleMarker.setPosition(closestRoutePointMiddle); // TODO this may be wrong, the program seemed to work without it
        Section section = new Section(startMarker.getTitle(), startMarker.getPosition(), middleMarker.getPosition(), endMarker.getPosition(), type, difficulty, (float) 0, (long) 0);
        calculateSectionDistance(section);
        calculateSectionTime(section);
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
        calculateDistance();
        calculateTime();
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
                    currentPolyline.add(routePoint);
                    mMap.addPolyline(currentPolyline);
                    polylineSegments.add(currentPolyline);

                    //start new polyline segment
                    currentPolyline = new PolylineOptions().clickable(false);
                    Section currentSection = sections.get(routePoint);
                    color = currentSection.getColor();
                    name=currentSection.getTitle();
                    colors=colors+"; "+name+":"+color+","+type+","+currentSection.getDifficulty();
                    currentPolyline.color(Color.parseColor(color));
                    //add start marker, middle marker, and end marker
//                    mMap.addMarker(new MarkerOptions().position(routePoint).title(currentSection.getTitle())); //TODO: add icon
                    mMap.addMarker(new MarkerOptions().position(currentSection.getMiddleLocation()).title(currentSection.getTitle())).setIcon(setIcon(context,currentSection.getIcon(),120,160)); //TODO: add icon
//                    mMap.addMarker(new MarkerOptions().position(currentSection.getEndLocation()).title(currentSection.getTitle())); //TODO: add icon

                    //add route points until section finish
                    currentPolyline.add(routePoint);
                    while(distanceBetweenPoints(routePoint,currentSection.getEndLocation()) != 0){
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
            mMap.addMarker(new MarkerOptions().position(this.getRoutePoints().get(0)).title("Start of " + this.name)).setIcon(setIcon(context,R.drawable.start,120,120));
            mMap.addMarker(new MarkerOptions().position(this.getRoutePoints().get(this.getRoutePoints().size() - 1)).title("End of " + this.name)).setIcon(setIcon(context,R.drawable.finish,120,120));
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
        timePoints.add(System.currentTimeMillis());

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
        timePoints.clear();

        //remove polyline segments
//        for (PolylineOptions polylineSegment : polylineSegments) {
//            polylineSegment.remove();
//        }


        PolylineOptions polylineOptions = new PolylineOptions().clickable(true);
        polylineOptions.color(Color.parseColor("#FF6750A3"));
        polylineSegments.add(polylineOptions);
    }


    public BitmapDescriptor setIcon(Context context, int iconId, int width, int height) {
        Drawable vectorDrawable = context.getDrawable(iconId);

        if (vectorDrawable == null) {
            // Handle the case where the vector drawable resource is not found.
            return null;
        }

        vectorDrawable.setBounds(0, 0, width, height); // Set the desired width and height

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private double badDistanceBetweenPoints(LatLng p1, LatLng p2) {
        double dx = p1.latitude - p2.latitude;
        double dy = p1.longitude - p2.longitude;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static double distanceBetweenPoints(LatLng p1, LatLng p2){
        final double R = 6371.0;

        // Convert latitude and longitude from degrees to radians
        double lat1Rad = Math.toRadians(p1.latitude);
        double lon1Rad = Math.toRadians(p1.longitude);
        double lat2Rad = Math.toRadians(p2.latitude);
        double lon2Rad = Math.toRadians(p2.longitude);

        // Calculate the differences in coordinates
        double dlat = lat2Rad - lat1Rad;
        double dlon = lon2Rad - lon1Rad;

        // Haversine formula
        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.sin(dlon / 2) * Math.sin(dlon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Calculate and return the distance
        return R * c * 1000;
    }
}


