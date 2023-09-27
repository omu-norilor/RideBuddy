package com.example.myapplication;

import com.google.android.gms.maps.model.LatLng;

public class Section {
    public static final Section DEFAULT = new Section("Default", new LatLng(0,0), new LatLng(0,0), new LatLng(0,0),"other","default");
    String title;
    String color;
    String type;

    String difficulty;

    //difficulties
    //Easy = green
    //Medium = blue
    //Hard = red
    //Expert = black


    LatLng locationStart;
    LatLng locationMiddle;
    LatLng locationEnd;

    public Section(String title, LatLng locationStart, LatLng locationMiddle, LatLng locationEnd, String type, String difficulty){
        this.title = title;
        this.locationStart = locationStart;
        this.locationMiddle = locationMiddle;
        this.locationEnd = locationEnd;
        this.type = type;
        this.difficulty = difficulty;
        if(this.difficulty.equals("Easy")){
            this.color = "#00FF00";
        }
        else if(this.difficulty.equals("Medium")){
            this.color = "#0000FF";
        }
        else if(this.difficulty.equals("Hard")){
            this.color = "#FF0000";
        }
        else if(this.difficulty.equals("Expert")){
            this.color = "#000000";
        }
        else{
            //light gray
            this.color = "#808080";
        }


    }

    public String getTitle(){
        return title;
    }

    public LatLng getStartLocation(){
        return locationStart;
    }

    public LatLng getMiddleLocation(){ return locationMiddle; }

    public LatLng getEndLocation(){ return locationEnd; }

    public String getColor(){
        return color;
    }

    public String getType(){ return type; }

    public void setTitle(String title){
        this.title = title;
    }

    public void setLocationStart(LatLng locationStart){
        this.locationStart = locationStart;
    }

    public void setLocationMiddle(LatLng locationMiddle){ this.locationMiddle = locationMiddle; }

    public void setLocationEnd(LatLng locationEnd){ this.locationEnd = locationEnd; }

    public void setColor(String color){
        this.color = color;
    }

    public void setType(String type){ this.type = type; }
    public Section clone() {
        return new Section(this.title, this.locationStart, this.locationMiddle, this.locationEnd, this.type, this.difficulty);
    }

    public String getDifficulty() { return difficulty; }
}
