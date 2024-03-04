package com.example.myapplication;

import com.example.myapplication.Route;
import com.example.myapplication.Section;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SerializableRoute implements Serializable {
    private String name;
    private List<SerializableLatLng> routePoints = new ArrayList<>();
    private List<Long> timePoints = new ArrayList<>();
    private Map<String, SerializableSection> sections = new HashMap<>();
    private List<String> users = new ArrayList<>();
    private Float distance;
    private String time;
    private SerializableLatLng locationStart;
    private SerializableLatLng locationEnd;
    private boolean isPublic;

    // Required public, no-argument constructor
    public SerializableRoute() {
        // Default constructor required for serialization
    }

    // Constructor to convert a Route to SerializableRoute
    public SerializableRoute(Route route) {
        this.name = route.getName();
        this.routePoints = new ArrayList<>();
        for (LatLng latLng : route.getRoutePoints()) {
            this.routePoints.add(new SerializableLatLng(latLng));
        }

        this.timePoints = route.getTimePoints();
        this.sections = new HashMap<>();
        for (Map.Entry<LatLng, Section> entry : route.getSections().entrySet()) {
            this.sections.put(new SerializableLatLng(entry.getKey()).toString(), new SerializableSection(entry.getValue()));
        }
//        this.polylineSegments = route.getPolylineSegments();
        this.distance = route.getDistance();
        this.time = route.getTime();
        this.isPublic = route.getIsPublic();
        this.users = route.getUsers();
        this.locationStart = new SerializableLatLng(route.getStartMarker().getPosition());
        this.locationEnd = new SerializableLatLng(route.getEndMarker().getPosition());
    }

    public Route toRoute() {
        Route route = new Route(name, isPublic, users.get(0));
        ArrayList<LatLng> routePoints = new ArrayList<>();
        for (SerializableLatLng serializableLatLng : this.routePoints) {
            routePoints.add(serializableLatLng.toLatLng());
        }
        route.setRoutePoints(routePoints);
        route.setTimePoints(timePoints);
        Map<LatLng, Section> sections = new HashMap<>();
        for (Map.Entry<String, SerializableSection> entry : this.sections.entrySet()) {
            sections.put(SerializableLatLng.fromString(entry.getKey()).toLatLng(), entry.getValue().toSection());
        }
        route.setSections(sections);
        route.setIsPublic(isPublic);
        route.setUsers(users);
        route.setDistance(distance);
        route.setTime(time);
        return route;
    }

    // Getter methods for all fields
    public String getName() {
        return name;
    }

    public List<SerializableLatLng> getRoutePoints() {
        return routePoints;
    }

    public List<Long> getTimePoints() { return timePoints; }

    public Map<String, SerializableSection> getSections() {
        return sections;
    }

    public Float getDistance() {
        return distance;
    }

    public String getTime() {
        return time;
    }

    public SerializableLatLng getLocationStart() {
        return locationStart;
    }

    public SerializableLatLng getLocationEnd() {
        return locationEnd;
    }

    public boolean getIsPublic() { return isPublic; }

    public List<String> getUsers() { return users; }

    //Setter methods for all fields
    public void setName(String name) {
        this.name = name;
    }

    public void setRoutePoints(List<SerializableLatLng> routePoints) { this.routePoints = routePoints; }

    public void setTimePoints(List<Long> timePoints) {
        this.timePoints = timePoints;
    }

    public void setSections(Map<String, SerializableSection> sections) { this.sections = sections; }

    public void setDistance(Float distance) {
        this.distance = distance;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setLocationStart(SerializableLatLng locationStart) { this.locationStart = locationStart; }

    public void setLocationEnd(SerializableLatLng locationEnd) {
        this.locationEnd = locationEnd;
    }

    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }

    public void setUsers(List<String> users) { this.users = users; }

}
