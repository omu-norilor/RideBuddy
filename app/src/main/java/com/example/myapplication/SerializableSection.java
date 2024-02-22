package com.example.myapplication;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class SerializableSection implements Serializable {

    private int icon;
    private String title;
    private String color;
    private String type;
    private String difficulty;
    private Float distance;
    private Long time;
    private SerializableLatLng locationStart;
    private SerializableLatLng locationMiddle;
    private SerializableLatLng locationEnd;

    // Required public, no-argument constructor
    public SerializableSection() {
        // Default constructor required for serialization
    }

    // Constructor to convert a Section to SerializableSection
    public SerializableSection(Section section) {
        this.icon = section.getIcon();
        this.title = section.getTitle();
        this.color = section.getColor();
        this.type = section.getType();
        this.difficulty = section.getDifficulty();
        this.distance = section.getDistance();
        this.time = section.getTime();
        this.locationStart = new SerializableLatLng(section.getLocationStart());
        this.locationMiddle = new SerializableLatLng(section.getLocationMiddle());
        this.locationEnd = new SerializableLatLng(section.getLocationEnd());
    }

    public Section toSection() {
        return new Section(this.title, this.locationStart.toLatLng(), this.locationMiddle.toLatLng(), this.locationEnd.toLatLng(), this.type, this.difficulty, this.distance, this.time);
    }

    // Getter methods for all fields

    public int getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }

    public String getColor() {
        return color;
    }

    public String getType() {
        return type;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public Float getDistance() {
        return distance;
    }

    public Long getTime() {
        return time;
    }

    public SerializableLatLng getLocationStart() {
        return locationStart;
    }

    public SerializableLatLng getLocationMiddle() {
        return locationMiddle;
    }

    public SerializableLatLng getLocationEnd() {
        return locationEnd;
    }

    // Setter methods if needed...

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public void setLocationStart(SerializableLatLng locationStart) {
        this.locationStart = locationStart;
    }

    public void setLocationMiddle(SerializableLatLng locationMiddle) {
        this.locationMiddle = locationMiddle;
    }

    public void setLocationEnd(SerializableLatLng locationEnd) {
        this.locationEnd = locationEnd;
    }

}
