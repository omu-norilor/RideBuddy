package com.example.myapplication;

import com.google.android.gms.maps.model.LatLng;

public class Section {
    public static final Section DEFAULT = new Section("Default", new LatLng(0,0), new LatLng(0,0), new LatLng(0,0),"other","default", (float) 0, (long) 0);
    int icon;
    String title;
    String color;
    String type;
    String difficulty;
    Float distance;
    Long time;

    //difficulties
    //Easy = green
    //Medium = blue
    //Hard = red
    //Expert = black
    //Other = grey

    LatLng locationStart;
    LatLng locationMiddle;
    LatLng locationEnd;

    public Section(String title, LatLng locationStart, LatLng locationMiddle, LatLng locationEnd, String type, String difficulty, Float distance, Long time){
        this.title = title;
        this.locationStart = locationStart;
        this.locationMiddle = locationMiddle;
        this.locationEnd = locationEnd;
        this.type = type;
        this.difficulty = difficulty;
        this.distance = distance;
        this.time = time;

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
            this.color = "#808080";
        }

        if(this.type == null && this.type.equals("Other")){
            this.icon = R.drawable.other;
        }
        else if(this.type.equals("Jumps")){
            this.icon = R.drawable.jumps;
        }
        else if(this.type.equals("Drops")){
            this.icon = R.drawable.drops;
        }
        else if(this.type.equals("Berms")){
            this.icon = R.drawable.berms;
        }
        else if(this.type.equals("Rock Garden")){
            this.icon = R.drawable.rock_garden;
        }
        else if(this.type.equals("Steep")){
            this.icon = R.drawable.steep;
        }
        else if(this.type.equals("Off Camber")){
            this.icon = R.drawable.off_camber;
        }
        else{
            this.icon = R.drawable.other;
        }
    }


    public Float getDistance(){ return distance; }

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

    public int getIcon(){ return icon; }

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

    public void setDistance(Float distance){ this.distance = distance; }

    public Section clone() {
        return new Section(this.title, this.locationStart, this.locationMiddle, this.locationEnd, this.type, this.difficulty, this.distance, this.time);
    }

    public String getDifficulty() { return difficulty; }

    public void setTime(long time) {
        this.time = time;
    }
}
