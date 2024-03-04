package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.FirebaseApp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class RouteRepo {
    private Map<String, Route> routes = new HashMap<>();
    private User user = new User();
    FirebaseDatabaseHelper DBHandler;
    AtomicInteger operationsCompleted = new AtomicInteger(0);

    public RouteRepo(Context context, User user, boolean isNewUser,Callback callback) {

        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context);
        }
        DBHandler = new FirebaseDatabaseHelper();
        FirebaseDatabaseHelper.DatabaseCallback<Void> operationCallback = new FirebaseDatabaseHelper.DatabaseCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (operationsCompleted.incrementAndGet() == 2) {
                    callback.onSuccess();
                }
            }
            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        };

        DBHandler.getRoutes(new FirebaseDatabaseHelper.DatabaseCallback<Map<String, Route>>() {
            @Override
            public void onSuccess(Map<String, Route> data) {
                routes = data;
                Log.d("RouteRepo", "Routes loaded from database: " + routes.size());
                operationCallback.onSuccess(null);
            }
            @Override
            public void onError(Exception e) {
                // Handle error
                Log.d("RouteRepo", "Error loading routes: " + e.getMessage());
                operationCallback.onError(e);
            }
        });

        if (isNewUser) {
            this.user = user;
            DBHandler.addUser(user, new FirebaseDatabaseHelper.DatabaseCallback<String>() {
                @Override
                public void onSuccess(String userId) {
                    // Optionally handle success
                    Log.d("RouteRepo", "User added with id: " + userId);
                    operationCallback.onSuccess(null);
                }
                @Override
                public void onError(Exception e) {
                    // Handle error
                    Log.d("RouteRepo", "Error adding user: " + e.getMessage());
                    operationCallback.onError(e);
                }
            });
        } else {
            DBHandler.getUser(user.getEmail(), new FirebaseDatabaseHelper.DatabaseCallback<User>() {
                @Override
                public void onSuccess(User data) {
                    Log.d("RouteRepo", "User instanceOld loaded from database: " + data.getEmail() + " " + data.getPassword() + " " + data.getUsername());
                    setUser(data);
                    operationCallback.onSuccess(null);
                }

                @Override
                public void onError(Exception e) {
                    // Handle error
                    Log.d("RouteRepo", "Error loading user: " + e.getMessage());
                    operationCallback.onError(e);
                }
            });
        }
    }

    private void setUser(User data) {
        user = data;
    }

    public List<String> getRouteNames() {
        return new ArrayList<>(routes.keySet());
    }

    public Route getRoute(String name) {
        return routes.get(name);
    }

    public User getUser() {
        return user;
    }

    private String getShortestTime(List<String> times) {
        String smallestTime = null;

        if (times != null && !times.isEmpty()) {
            for (String currentTime : times) {
                if (smallestTime == null || currentTime.compareTo(smallestTime) < 0) {
                    smallestTime = currentTime;
                }
            }
        }

        return smallestTime;
    }

    public void removeRoute(String name) {
        if (!user.getRoutes().contains(name))
            return;
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

    public void setRouteTimes() {
        // set personal times for the routes
        if( user.getTimes() == null)
            return;
        for (String routeName : user.getTimes().keySet()) {
            List<String> times = user.getTimes().get(routeName);
            String shortestTime = getShortestTime(times);
            Objects.requireNonNull(routes.get(routeName)).setTime(shortestTime);
        }
    }

    public void updateRouteTime(String name, String time) {
        Route route = routes.get(name);
        if (route.getTime() == null || time.compareTo(route.getTime()) < 0) // if the new time is the global best time, update it
        {
            route.setTime(time);
            routes.put(name, route);
            this.updateRoute(name, route);
        }
    }

    public void updateUserTime(String name, String time) {
        if(user.getRoutes().contains(name)) {
            List<String> times = user.getTimes().get(name);
            if (times == null) {
                times = new ArrayList<>();
                user.getTimes().put(name, times);
            }
            times.add(time);
            user.getTimes().put(name, times);
        }
        else {
            List<String> times = new ArrayList<>();
            times.add(time);
            user.getTimes().put(name, times);
        }
        this.updateUser(user);

    }

    private void updateUser(User user) {
        DBHandler.updateUser(user, new FirebaseDatabaseHelper.DatabaseCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                // Optionally handle success
                Log.d("RouteRepo", "User updated");
            }

            @Override
            public void onError(Exception e) {
                // Handle error
                e.printStackTrace();
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
                route.setFirebaseId(routeId);
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
                Log.d("RouteRepo", "Route updated");
            }

            @Override
            public void onError(Exception e) {
                // Handle error
                e.printStackTrace();
            }
        });
    }

}
