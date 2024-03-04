package com.example.myapplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class User {

    private String email;
    private String password;
    private String username;
    private boolean premium;
    private List<SerializableRun> runs;
    private List<String> routes;
    private String firebaseId;

    // Default constructor (required for Firebase)
    public User() {
    }

    // Constructor
    public User(String email, String password, String username, boolean premium, List<SerializableRun> runs, List<String> routes) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.premium = premium;
        this.runs = runs;
        this.routes = routes;
    }

    // Getters and setters

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    public List<SerializableRun> getRuns() {
        if (runs == null) {
            runs = new ArrayList<>();
        }
        return runs;
    }

    public void setTimes(List<SerializableRun> runs) {
        this.runs = runs;
    }

    public List<String> getRoutes() {
        return routes;
    }

    public void setRoutes(List<String> routes) {
        this.routes = routes;
    }

    public void setFirebaseId(String firebaseId) { this.firebaseId = firebaseId; }

    public void addRoute(String route) {
        if (routes == null) {
            routes = new ArrayList<>();
        }
        routes.add(route);
    }

    public void addRun(SerializableRun run) {
        if (runs == null) {
            runs = new ArrayList<>();
        }
        runs.add(run);
    }

    public String getFirebaseId() {
        return firebaseId;
    }
}
