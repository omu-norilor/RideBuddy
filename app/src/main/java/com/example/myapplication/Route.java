package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;

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
    private String personalTime;
    private String firebaseId;
    private boolean isPublic;
    private List<String> users = new ArrayList<>();


    public Route( String name, boolean isPublic, String email) {
        this.name = name;
        this.isPublic = isPublic;
        this.users.add(email);
        PolylineOptions polylineOptions= new PolylineOptions().clickable(true);
        polylineOptions.color(Color.parseColor("#808080"));
        polylineSegments.add(polylineOptions);

    }
    @Override
    public Route clone() {
        Route route = new Route(this.getName(), this.isPublic, this.users.get(0));
        route.setStartMarker(startMarker);
        route.setEndMarker(endMarker);
        route.setPolylineSegments(polylineSegments);
        route.setRoutePoints(routePoints);
        route.setCheckpoints((HashMap<LatLng, Section>) sections);
        route.setDistance(distance);
        route.setTimePoints(timePoints);
        return route;
    }
    ///-----------------------------------------------------///
    ///-----------------Getters and Setters-----------------///
    ///-----------------------------------------------------///
    public Map<LatLng, Section> getSections() {return sections;}
    public String getFirebaseId() {
        return firebaseId;
    }
    public String getName() {
        return name;
    }
    public boolean getIsPublic() { return isPublic; }
    public List<String> getUsers() { return users; }
    public Float getDistance() {
        return distance;
    }
    public String getTime() {
        return time;
    }
    public String getPersonalTime() { return personalTime; }
    public List<PolylineOptions> getPolylineSegments() {
        return polylineSegments;
    }
    public List<LatLng> getRoutePoints() {
        return routePoints;
    }
    public List<Long> getTimePoints() {return timePoints;}
    public Marker getEndMarker() {
        return endMarker;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setPolylineSegments(List<PolylineOptions> polylineOptions) { this.polylineSegments = new ArrayList<>(polylineOptions); }
    public Marker getStartMarker() {
        return startMarker;
    }
    public void setStartMarker(Marker marker) { startMarker = marker; }
    public void setEndMarker(Marker marker) {
        endMarker = marker;
    }
    public void addUser(String email) { users.add(email);}
    public void setSections(Map<LatLng, Section> sections) {
        this.sections =  sections;
    }
    public void setTimePoints(List<Long> timePoints) { this.timePoints = new ArrayList<>(timePoints); }
    public void setDistance(Float distance) {
        this.distance = distance;
    }
    public void updateTime(String time) { if (this.time == null || time.compareTo(this.time) < 0) this.time = time; }
    public void setTime(String time) { this.time = time; }
    public void setPersonalTime(String personalTime) { this.personalTime = personalTime; }
    public void setRoutePoints(List<LatLng> routePoints) { this.routePoints = new ArrayList<>(routePoints);}
    public void setCheckpoints(HashMap<LatLng,Section> sections) { this.sections = new HashMap<>(sections); }
    public void setFirebaseId(String firebaseId) {
        this.firebaseId=firebaseId;
    }
    public void setIsPublic(boolean isPublic) { this.isPublic = isPublic; }
    public void setUsers(List<String> users) { this.users = users; }

    ///-----------------------------------------------------///
    ///-----------------calcultate functions----------------///
    ///-----------------------------------------------------///
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

            if(routePoints.get(i).equals(section.getLocationStart())) status = true;
            if(routePoints.get(i).equals(section.getLocationEnd())) status = false;
        }
        section.setDistance(distance);
    }
    public void calculateSectionTime(Section section){
        long time = 0;
        Long startTime = Long.valueOf(0);
        Long endTime = Long.valueOf(0);
        // Get ends
        for (int i = 0; i < routePoints.size() - 1; i++) {
            if(routePoints.get(i).equals(section.getLocationStart())) startTime = timePoints.get(i);
            if(routePoints.get(i).equals(section.getLocationEnd())) endTime = timePoints.get(i);
        }
        // Calculate time
        time = endTime - startTime;
        section.setTime(time);
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


    ///-----------------------------------------------------///
    ///-----------------add remove functions----------------///
    ///-----------------------------------------------------///

    public void addSection(Marker startMarker,Marker middleMarker,Marker endMarker, String type, String difficulty, Context context, GoogleMap mMap) {
        //find closest route point
        LatLng closestRoutePointStart = null;
        LatLng closestRoutePointMiddle = null;
        LatLng closestRoutePointEnd = null;
        double minDistanceStart = Double.MAX_VALUE;
        double minDistanceMiddle = Double.MAX_VALUE;
        double minDistanceEnd = Double.MAX_VALUE;

        // For selected start and end markers and the calculated middleMarker, find the closest route points to associate with them
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

        // Mark closest route point as checkpoint
        startMarker.setPosition(closestRoutePointStart);
        endMarker.setPosition(closestRoutePointEnd);
        middleMarker.setPosition(closestRoutePointMiddle); // TODO this may be wrong, the program seemed to work without it
        Section section = new Section(startMarker.getTitle(), startMarker.getPosition(), middleMarker.getPosition(), endMarker.getPosition(), type, difficulty, (float) 0, (long) 0);
        calculateSectionDistance(section);
        calculateSectionTime(section);
        sections.put(closestRoutePointStart, section);
        rebuildRoute(mMap,context);
    }

    public void addRouteToMap(GoogleMap mMap, Context context) {
        // Add each route point to map to the polyline options
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
                // If route point is a checkpoint, add a marker and change color
                LatLng routePoint = routePoints.get(i);
                if (sections.containsKey(routePoint)) {
                    // Add current polyline segment to map
                    // The current polyline is not a special section
                    currentPolyline.add(routePoint);
                    mMap.addPolyline(currentPolyline);
                    polylineSegments.add(currentPolyline);

                    // Start new polyline segment
                    currentPolyline = new PolylineOptions().clickable(false);
                    Section currentSection = sections.get(routePoint);
                    color = currentSection.getColor();
                    name=currentSection.getTitle();
                    colors=colors+"; "+name+":"+color+","+type+","+currentSection.getDifficulty();
                    currentPolyline.color(Color.parseColor(color));

                    // Add middle marker
                    mMap.addMarker(new MarkerOptions().position(currentSection.getLocationMiddle()).title(currentSection.getTitle())).setIcon(setIcon(context,currentSection.getIcon(),120,160));

                    // Add route points until section finish
                    currentPolyline.add(routePoint);
                    while(distanceBetweenPoints(routePoint,currentSection.getLocationEnd()) != 0){
                        i++;
                        routePoint = routePoints.get(i);
                        currentPolyline.add(routePoint);
                    }
                    // Increment i to skip the end location
                    i++;
                    // Add polyline segment to map
                    mMap.addPolyline(currentPolyline);
                    polylineSegments.add(currentPolyline);

                    // Start new polyline segment
                    currentPolyline = new PolylineOptions().clickable(true);
                    color = "#808080"; // Gray
                    type = "default";
                    name = "default";
                    currentPolyline.color(Color.parseColor(color));
                }
                currentPolyline.add(routePoint);
            }

            // Add last polyline segment to map
            mMap.addPolyline(currentPolyline);

            // Add start and end markers
            this.startMarker = mMap.addMarker(new MarkerOptions().position(this.getRoutePoints().get(0)).title("Start of " + this.name));
            this.startMarker.setIcon(setIcon(context,R.drawable.start,120,120));
            this.endMarker = mMap.addMarker(new MarkerOptions().position(this.getRoutePoints().get(this.getRoutePoints().size() - 1)).title("End of " + this.name));
            this.endMarker.setIcon(setIcon(context,R.drawable.finish,120,120));
//            Toast.makeText(context, "Route added with colors: " + colors, Toast.LENGTH_SHORT).show();
            Log.d("Route", "Route added with colors: " + colors);
        }
        catch (NullPointerException e) {
            // Handle the NullPointerException and show an error message using a Toast
//            Toast.makeText(context, "An error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log .d("Route", "An error occurred: " + e.getMessage());

        }
    }
    public void addRoutePoint(LatLng latLng) {
        routePoints.add(latLng);
        timePoints.add(System.currentTimeMillis());
        polylineSegments.get(polylineSegments.size() - 1).add(latLng);
    }
    private void rebuildRoute(GoogleMap mMap, Context context) {
        //remove old route and add new route
        addRouteToMap(mMap,context);
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
        PolylineOptions polylineOptions = new PolylineOptions().clickable(true);
        polylineOptions.color(Color.parseColor("#FF6750A3"));
        polylineSegments.add(polylineOptions);
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

    public void setClickable(boolean b) {
        for (PolylineOptions polylineOptions : polylineSegments) {
            // if polyline is not a section, set it to clickable
            if (polylineOptions.getColor() == Color.parseColor("#808080")) {
                polylineOptions.clickable(b);
            }
        }
    }

    public void removeUser(String email) {
        users.remove(email);
    }
}


