package com.example.myapplication;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouteRepo {
    private Map<String, Route> routes = new HashMap<>();
    FirebaseDatabaseHelper DBHandler;
    private String fileName;
    public RouteRepo(Context context) {

        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context);
        }
        DBHandler = new FirebaseDatabaseHelper();
        DBHandler.getRoutes(new FirebaseDatabaseHelper.DatabaseCallback<Map<String, Route>>() {
            @Override
            public void onSuccess(Map<String, Route> data) {
                routes = data;
                Log.d("RouteRepo", "Routes loaded from database: " + routes.size());
            }

            @Override
            public void onError(Exception e) {
                // Handle error
                Log.d("RouteRepo", "Error loading routes: " + e.getMessage());
            }
        });
    }

    public void addRoute(String name, Route route) {
        routes.put(name, route);
        DBHandler.addRoute(route, new FirebaseDatabaseHelper.DatabaseCallback<String>() {
            @Override
            public void onSuccess(String routeId) {
                // Optionally handle success
                Log.d("RouteRepo", "Route added with id: " + routeId);
            }

            @Override
            public void onError(Exception e) {
                // Handle error
                Log.d("RouteRepo", "Error adding route: " + e.getMessage());
            }
        });
    }

    public void updateRoute(String name, Route route) {
        routes.put(name, route);
        DBHandler.updateRoute(name, route, new FirebaseDatabaseHelper.DatabaseCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                // Optionally handle success
            }

            @Override
            public void onError(Exception e) {
                // Handle error
                e.printStackTrace();
            }
        });
    }
    public ArrayList<Route> getRoutesList() {
        return new ArrayList<>(routes.values());
    }

    public void removeRoute(String name) {
        Route removedRoute = routes.remove(name);
        if (removedRoute != null) {
            DBHandler.deleteRoute(name, new FirebaseDatabaseHelper.DatabaseCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    // Optionally handle success
                }

                @Override
                public void onError(Exception e) {
                    // Handle error
                    e.printStackTrace();
                }
            });
        }
    }

    public List<String> getRouteNames() {
        return new ArrayList<>(routes.keySet());
    }

    public Route getRoute(String name) {
        return routes.get(name);
    }
}
