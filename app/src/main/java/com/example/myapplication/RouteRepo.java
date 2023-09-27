package com.example.myapplication;

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
    private String fileName;
    public RouteRepo(String fileName) {
        this.fileName = fileName;
        // TODO: Read from file
//        readFromFile();
    }



    public void addRoute(String name, Route route) {
        routes.put(name, route);
    }
    public ArrayList<Route> getRoutesList() {
        return new ArrayList<>(routes.values());
    }

    public void removeRoute(String name) {
        routes.remove(name);
    }

    public List<String> getRouteNames() {
        return new ArrayList<>(routes.keySet());
    }

    public Route getRoute(String name) {
        return routes.get(name);
    }

    public void deleteRoute(String routeName) {
        routes.remove(routeName);
    }
}
