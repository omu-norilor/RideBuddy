package com.example.myapplication;

import com.google.android.gms.maps.model.LatLng;

public class Checkpoint {
    public static final Checkpoint DEFAULT = new Checkpoint("Default", new LatLng(0,0), "other");
    String title;
    String color;
    String type;
    //types:
    //jumps: color = #FF6750A3
    //rockgardens: color = #FFA35050
    //drops: color = #FF50A350
    //berms: color = #FFA3A350
    //technical: color = #FFA350A3
    //skinnies: color = #FFA350A3
    //other: color = #FFA350A3


    LatLng location;

    public Checkpoint(String title, LatLng location, String type){
        this.title = title;
        this.location = location;
        this.type = type;
        if (this.type == null){
            this.type = "Other";
            this.color = "#828282";
        }
        else if(this.type.equals("Jumps")){
            this.color = "#00FF00";
        }
        else if(this.type.equals("Rock Gardens")){
            this.color = "#FFFF00";
        }
        else if(this.type.equals("Drops")){
            this.color = "#FFA500";
        }
        else if(this.type.equals("Berms")){
            this.color = "#FF0000";
        }
        else if(this.type.equals("Steep")){
            this.color = "#0000FF";
        }
        else if(this.type.equals("Off-Camber")){
            this.color = "#000000";
        }
        else{
            this.color = "#818181";
        }


    }

    public String getTitle(){
        return title;
    }

    public LatLng getLocation(){
        return location;
    }

    public String getColor(){
        return color;
    }

    public String getType(){ return type; }

    public void setTitle(String title){
        this.title = title;
    }

    public void setLocation(LatLng location){
        this.location = location;
    }

    public void setColor(String color){
        this.color = color;
    }

    public void setType(String type){ this.type = type; }
    public Checkpoint clone() {
        return new Checkpoint(this.title, this.location, this.type);
    }
}
