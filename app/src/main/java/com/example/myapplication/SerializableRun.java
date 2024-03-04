package com.example.myapplication;
import java.io.Serializable;

public class SerializableRun implements Serializable {
    String time;
    String date;
    String routeName;

    public SerializableRun(String time, String date, String routeName) {
        this.time = time;
        this.date = date;
        this.routeName = routeName;
    }

    public SerializableRun() {
        // Required no-argument constructor for serialization
    }
    public String getTime() { return time; }
    public String getDate() { return date; }
    public String getRouteName() { return routeName; }
    public void setTime(String time) { this.time = time; }
    public void setDate(String date) { this.date = date; }
    public void setRouteName(String routeName) { this.routeName = routeName; }
}
