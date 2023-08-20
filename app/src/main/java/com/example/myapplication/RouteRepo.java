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

//    private void readFromFile() {
//        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                // Assuming your Route class has a constructor that can parse a line
//
//                Route route = Route.parseFromString(line);
//                routes.put(route.getName(), route);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void writeToFile(String fileName) {
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
//            for (Route route : routes.values()) {
//                writer.write(route.toString()); // Assuming Route has a method to convert to string
//                writer.newLine();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


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
