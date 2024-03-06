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
                        String firebaseId = routeSnapshot.getKey();
                        SerializableRoute sroute = routeSnapshot.getValue(SerializableRoute.class);
                        Route route = sroute.toRoute();
                        route.setFirebaseId(firebaseId);

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
    public void updateRoute(String firebaseId, Route updatedRoute, final DatabaseCallback<Void> callback) {
        // Update the route data
        routesReference.child(updatedRoute.getFirebaseId()).setValue(new SerializableRoute(updatedRoute))
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

    public void getUser(String email, final DatabaseCallback<User> databaseCallback) {
        // Query the "users" node to find a user with the specified email
        Query query = usersReference.orderByChild("email").equalTo(email);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User with the specified email found
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String firebaseId = snapshot.getKey();
                        User user = snapshot.getValue(User.class);
                        user.setFirebaseId(firebaseId);
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

    public void getUsers(final DatabaseCallback<List<SimpleUser>> callback) {
        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<SimpleUser> users = new ArrayList<>();
                    for (DataSnapshot routeSnapshot : dataSnapshot.getChildren()) {
                        String firebaseId = routeSnapshot.getKey();
                        User user = routeSnapshot.getValue(User.class);
                        SimpleUser simpleUser = new SimpleUser(user.getUsername(),user.getEmail(), firebaseId);
                        if (user != null) {
                            users.add(simpleUser);
                        }
                    }
                    callback.onSuccess(users);
                } else {
                    callback.onSuccess(new ArrayList<>());  // No data, return an empty map
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }


    public void updateUser(String firebaseId, User updatedUser, final DatabaseCallback<Void> callback) {
        // Update the route data
        usersReference.child(updatedUser.getFirebaseId()).setValue(updatedUser)
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
