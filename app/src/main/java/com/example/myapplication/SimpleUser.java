package com.example.myapplication;

public class SimpleUser {

    private String username;
    private String email;
    private String firebaseId;

    public SimpleUser() {
    }

    public SimpleUser(String username,String email, String firebaseId) {
        this.username = username;
        this.email = email;
        this.firebaseId = firebaseId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return getUsername();  // Display the username in the adapter
    }
}
