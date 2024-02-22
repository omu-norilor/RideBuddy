package com.example.myapplication;

import android.os.AsyncTask;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseDatabaseHelper {

    private DatabaseReference routesReference;

    public FirebaseDatabaseHelper() {
        // Get a reference to the "routes" node in the Firebase Realtime Database
        routesReference = FirebaseDatabase.getInstance().getReference("routes");
    }

    // Create
    public void addRoute(Route route, final DatabaseCallback<String> callback) {
        // Push a new child node under "routes" and get its key
        String routeId = routesReference.push().getKey();

        SerializableRoute serializableRoute = new SerializableRoute(route);
        // Set the route data under the new child node
        routesReference.child(routeId).setValue(serializableRoute)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(routeId);
                        } else {
                            callback.onError(task.getException());
                        }
                    }
                });
    }

    // Read
    public void getRoutes(final DatabaseCallback<Map<String, Route>> callback) {
        routesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Route> routes = new HashMap<>();
                    for (DataSnapshot routeSnapshot : dataSnapshot.getChildren()) {
                        SerializableRoute sroute = routeSnapshot.getValue(SerializableRoute.class);
                        Route route = sroute.toRoute();

                        if (route != null) {
                            routes.put(route.getName(), route);
                        }
                    }
                    callback.onSuccess(routes);
                } else {
                    callback.onSuccess(new HashMap<>());  // No data, return an empty map
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    // Update
    public void updateRoute(String routeName, Route updatedRoute, final DatabaseCallback<Void> callback) {
        routesReference.child(routeName)
                .child("name")
                .setValue(new SerializableRoute(updatedRoute))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(null);
                        } else {
                            callback.onError(task.getException());
                        }
                    }
                });
    }

    // Delete
    public void deleteRoute(String routeId, final DatabaseCallback<Void> callback) {
        routesReference.child(routeId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(null);
                        } else {
                            callback.onError(task.getException());
                        }
                    }
                });
    }

    // Callback interface for handling asynchronous database operations
    public interface DatabaseCallback<T> {
        void onSuccess(T data);

        void onError(Exception e);
    }
}
