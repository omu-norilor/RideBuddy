package com.example.myapplication;
import com.google.android.gms.tasks.TaskCompletionSource;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

public class FirebaseDatabaseHelper {

    private DatabaseReference routesReference;

    private DatabaseReference usersReference;

    public FirebaseDatabaseHelper() {
        // Get a reference to the "routes" node in the Firebase Realtime Database
        routesReference = FirebaseDatabase.getInstance().getReference("routes");
        usersReference = FirebaseDatabase.getInstance().getReference("users");
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

    public void addUser(User user, final DatabaseCallback<String> callback) {
        // Push a new child node under "routes" and get its key
        String userId = usersReference.push().getKey();

        // Set the route data under the new child node
        usersReference.child(userId).setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(userId);
                        } else {
                            callback.onError(task.getException());
                        }
                    }
                });
    }
    public User getUserSync(String email) {
        final CountDownLatch latch = new CountDownLatch(1);
        final User[] user = new User[1]; // Using an array to hold the user object

        DatabaseCallback<User> callback = new DatabaseCallback<User>() {
            @Override
            public void onSuccess(User result) {
                user[0] = result;
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                // Handle error, you might want to throw an exception or log it
                latch.countDown();
            }
        };
        try{
            getUser(email, callback);
        }
        finally {
            latch.countDown();
        }

        try {
            // Wait for the latch until the callback is invoked
            latch.await(4,java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            // Handle interruption, throw an exception or log it
        }

        return user[0];
    }

    public void getUser(String email, final DatabaseCallback<User> databaseCallback) {
        // Query the "users" node to find a user with the specified email
        Query query = usersReference.orderByChild("email").equalTo(email);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User with the specified email found
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        databaseCallback.onSuccess(user);
                        return; // Stop iterating as we found a user
                    }
                } else {
                    // User with the specified email not found
                    databaseCallback.onSuccess(null);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // An error occurred while retrieving data
                databaseCallback.onError(databaseError.toException());
            }
        });
    }


    // Callback interface for handling asynchronous database operations
    public interface DatabaseCallback<T> {
        void onSuccess(T data);

        void onError(Exception e);
    }
}
