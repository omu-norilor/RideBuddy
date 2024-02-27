package com.example.myapplication;

import java.util.List;
import java.util.Map;

public class User {

    private String email;
    private String password;
    private String username;
    private boolean premium;
    private Map<String, List<String>> times;
    private List<String> routes;

    // Default constructor (required for Firebase)
    public User() {
    }

    // Constructor
    public User(String email, String password, String username, boolean premium, Map<String, List<String>> times, List<String> routes) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.premium = premium;
        this.times = times;
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

    public Map<String, List<String>> getTimes() {
        return times;
    }

    public void setTimes(Map<String, List<String>> times) {
        this.times = times;
    }

    public List<String> getRoutes() {
        return routes;
    }

    public void setRoutes(List<String> routes) {
        this.routes = routes;
    }
}
