package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean recording = false;
    private FusedLocationProviderClient fusedLocationProviderClient;
    //Buttons
    private Button startButton;
    private GoogleMap mMap;
    private EditText routeName;
    private LinearLayout saveLayout;
    private LinearLayout searchLayout;
    private LinearLayout routeLayout;
    private LinearLayout deleteLayout;
    private Route currentRoute;
    private RouteRepo routeRepo;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        currentRoute = new Route("current");
        //initialize repo from file
        routeRepo = new RouteRepo("routerepo.json");

        saveLayout = findViewById(R.id.routeSaveLayout);
        saveLayout.setVisibility(View.GONE);
        searchLayout = findViewById(R.id.searchLayout);
        searchLayout.setVisibility(View.GONE);
        routeLayout = findViewById(R.id.routeViewLayout);
        routeLayout.setVisibility(View.GONE);
        deleteLayout = findViewById(R.id.routeDeleteLayout);
        deleteLayout.setVisibility(View.GONE);
        
        routeName = findViewById(R.id.routeName);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.maps);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        // Start button
        startButton = findViewById(R.id.startButton);
        startButton.setBackgroundColor(Color.parseColor("#FFDB91E7"));
        startButton.setOnClickListener(v -> {
            if (startButton.getText().toString().equals("Start Route Recording")) {
                startButton.setText("Stop Route Recording");
                startButton.setBackgroundColor(Color.parseColor("#FF6750A3"));
                startButton.setTextColor(Color.WHITE);
                recording=true;
            } else {
                saveLayout.setVisibility(View.VISIBLE);
                startButton.setText("Start Route Recording");
                startButton.setBackgroundColor(Color.parseColor("#FFDB91E7"));
                startButton.setTextColor(Color.BLACK);
                recording=false;
            }
            });

        // Save button
        Button saveRouteButton = findViewById(R.id.save);
        saveRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = routeName.getText().toString();
                if (!name.isEmpty()) {
                    // Save the route
                    Route routeToSave = currentRoute.clone();
                    routeToSave.setName(name);
                    routeRepo.addRoute(name, routeToSave);
                    addRouteToMap(routeToSave,name);
                    routeName.setText("");
                    saveLayout.setVisibility(View.GONE); // Hide the dialog
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a name for the route", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Discard button
        Button discardRouteButton = findViewById(R.id.discard);
        discardRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /// Handle discard button click
                currentRoute.removeRoute(); // Remove the current route

                //redwraw the map
                redrawMap();

                // Reset the dialog
                routeName.setText("");
                saveLayout.setVisibility(View.GONE); // Hide the dialog
            }
        });

        Button showRouteListButton = findViewById(R.id.showRouteButton);
        showRouteListButton.setBackgroundColor(Color.parseColor("#6750A3"));
        showRouteListButton.setTextColor(Color.WHITE);
        showRouteListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRouteListDialog();
            }
        });

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        // Check for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdates(){
        mMap.setMyLocationEnabled(true);
        // Request continuous location updates
        fusedLocationProviderClient.requestLocationUpdates(createLocationRequest(),
                new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (locationResult != null && locationResult.getLastLocation() != null) {
                            Location location = locationResult.getLastLocation();
                            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17));
                            if(recording) {
                                Marker marker = mMap.addMarker(new MarkerOptions().position(currentLatLng).title("My Location"));
                                currentRoute.addRoutePoint(currentLatLng);
                                if (currentRoute.getStartMarker() == null) {
                                    currentRoute.setStartMarker(marker);
                                }
                                // delete previous end marker
                                if (currentRoute.getEndMarker() != null) {
                                    currentRoute.getEndMarker().remove();
                                }
                                currentRoute.updateEndMarker(marker);
                            }
                        }
                    }
                },
                null);
    }


    private void redrawMap() {
        mMap.clear(); // Clear the map

        //Re-draw the saved routes from the repository
        for (String name : routeRepo.getRouteNames()) {
            addRouteToMap(routeRepo.getRoute(name),name);
        }
    }
    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(3000); // Update interval in milliseconds (10 seconds)
        locationRequest.setFastestInterval(1000); // Fastest update interval in milliseconds (5 seconds)
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private void addRouteToMap(Route route, String name) {
        mMap.addPolyline(route.getPolylineOptions());
        mMap.addMarker(new MarkerOptions().position(route.getRoutePoints().get(0)).title("Start of " + name));
        mMap.addMarker(new MarkerOptions().position(route.getRoutePoints().get(route.getRoutePoints().size() - 1)).title("End of " + name ));

    }

    private void showRouteListDialog() {
        // Find the dialog layout in activity_main.xml

        // Find elements inside the dialog layout
        EditText searchEditText = searchLayout.findViewById(R.id.searchEditText);
        ListView routesListView = searchLayout.findViewById(R.id.routesListView);
        Button closeButton = searchLayout.findViewById(R.id.closeSearchButton);

        // Get the list of route names from the repository
        List<String> routeNames = routeRepo.getRouteNames();

        // Create an adapter to display the route names in the ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, routeNames);
        routesListView.setAdapter(adapter);
        // if empty show message
        if (routeNames.isEmpty()) {
            Toast.makeText(MainActivity.this, "Cam gol p-aicia namasatemint", Toast.LENGTH_SHORT).show();
        }

        // Set up a text filter for the search EditText
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchLayout.setVisibility(View.GONE);
            }
        });

        routesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String routeName = (String) parent.getItemAtPosition(position);
                Route route = routeRepo.getRoute(routeName);
                searchEditText.setText("");

                searchLayout.setVisibility(View.GONE);
                openRouteDialog(route, routeName);
            }
        });

        // Show the dialog layout
        searchLayout.setVisibility(View.VISIBLE);
    }

    private void openRouteDialog(Route selected_route,String routeName){
        //make layout visible
        routeLayout.setVisibility(View.VISIBLE);

        Button deleteButton = routeLayout.findViewById(R.id.routeViewDeleteButton);
        Button closeButton = routeLayout.findViewById(R.id.routeViewCloseButton);
        Button editButton = routeLayout.findViewById(R.id.routeViewEditButton);
        Button selectButton = routeLayout.findViewById(R.id.routeViewSelectButton);
        TextView routeNameTextView = routeLayout.findViewById(R.id.routeNameTextView);

        routeNameTextView.setText(routeName);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selected_route.getRoutePoints().get(0), 17));

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routeLayout.setVisibility(View.GONE);
                //make sure the user wants to delete the route
                safeDeleteRoute(routeName);
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routeLayout.setVisibility(View.GONE);
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routeLayout.setVisibility(View.GONE);
//                openEditRouteDialog(selected_route,routeName);
            }
        });

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routeLayout.setVisibility(View.GONE);
//                openSelectRouteDialog(selected_route,routeName);
            }
        });

    }


    private void safeDeleteRoute(String routeName){

        deleteLayout.setVisibility(View.VISIBLE);

        Button deleteButton = deleteLayout.findViewById(R.id.routeSureDelete);
        Button cancelButton = deleteLayout.findViewById(R.id.routeCancelDelete);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routeRepo.deleteRoute(routeName);
                redrawMap();
                deleteLayout.setVisibility(View.GONE);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteLayout.setVisibility(View.GONE);
            }
        });

    }

}