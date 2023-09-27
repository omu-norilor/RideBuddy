package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
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
import android.widget.Spinner;
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
import com.google.android.gms.maps.model.Polyline;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean recording = false;
    private boolean center_start= false;
    private boolean route_selection = false;
    private FusedLocationProviderClient fusedLocationProviderClient;
    //Buttons
    private Button startButton;
    private GoogleMap mMap;
    private EditText routeName;
    private LinearLayout saveLayout;
    private LinearLayout searchLayout;
    private LinearLayout routeLayout;
    private LinearLayout deleteLayout;
    private LinearLayout editLayout;
    private Route currentRoute;
    private Marker currentMarker;
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
        editLayout = findViewById(R.id.routeEditLayout);
        editLayout.setVisibility(View.GONE);
        
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
                currentRoute.removeRoute();
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
                    routeToSave.addRouteToMap(mMap,MainActivity.this);
                    routeName.setText("");
                    saveLayout.setVisibility(View.GONE); // Hide the dialog
                    //reset current route
                    currentRoute.removeRoute();
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
            //center map on current location

            //add a debugging route to the map for testing
            Route testRoute = new Route("test");
            //make it a straight line
            testRoute.addRoutePoint(new LatLng(46.7712, 23.6236));
            testRoute.addRoutePoint(new LatLng(46.7712, 23.6237));
            testRoute.addRoutePoint(new LatLng(46.7712, 23.6238));
            testRoute.addRoutePoint(new LatLng(46.7712, 23.6239));
            testRoute.addRoutePoint(new LatLng(46.7712, 23.6240));
            testRoute.addRoutePoint(new LatLng(46.7712, 23.6241));
            testRoute.addRoutePoint(new LatLng(46.7712, 23.6242));
            testRoute.addRoutePoint(new LatLng(46.7712, 23.6243));
            testRoute.addRoutePoint(new LatLng(46.7712, 23.6244));
            testRoute.addRoutePoint(new LatLng(46.7712, 23.6245));
            testRoute.addRoutePoint(new LatLng(46.7712, 23.6246));
            testRoute.addRoutePoint(new LatLng(46.7712, 23.6247));
            //add start and end points
            MarkerOptions startMarker = new MarkerOptions();
            startMarker.position(new LatLng(46.7712, 23.6236));
            startMarker.title("Start");
            Marker start = mMap.addMarker(startMarker);
            MarkerOptions endMarker = new MarkerOptions();
            endMarker.position(new LatLng(46.7712, 23.6247));
            endMarker.title("End");
            Marker end = mMap.addMarker(endMarker);
            testRoute.setStartMarker(start);
            testRoute.setStartMarker(end);
            //add it to the map
            testRoute.addRouteToMap(mMap,this);
            //add it to the repo
            routeRepo.addRoute("test",testRoute);

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
                            if(!center_start){
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17));
                                center_start=true;
                            }
                            if(recording) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17));
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
            routeRepo.getRoute(name).addRouteToMap(mMap,MainActivity.this);
        }
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(3000); // Update interval in milliseconds (10 seconds)
        locationRequest.setFastestInterval(1000); // Fastest update interval in milliseconds (5 seconds)
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
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
                routesListView.setOnItemClickListener(null);
                closeButton.setOnClickListener(null);
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

    private void openRouteDialog(Route selectedRoute,String routeName){
        //make layout visible
        routeLayout.setVisibility(View.VISIBLE);

        Button deleteButton = routeLayout.findViewById(R.id.routeViewDeleteButton);
        Button closeButton = routeLayout.findViewById(R.id.routeViewCloseButton);
        Button editButton = routeLayout.findViewById(R.id.routeViewEditButton);
        Button selectButton = routeLayout.findViewById(R.id.routeViewSelectButton);
        TextView routeNameTextView = routeLayout.findViewById(R.id.routeNameTextView);

        routeNameTextView.setText(routeName);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedRoute.getRoutePoints().get(0), 17));

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
                selectButton.setOnClickListener(null);
                deleteButton.setOnClickListener(null);
                editButton.setOnClickListener(null);
                closeButton.setOnClickListener(null);
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routeLayout.setVisibility(View.GONE);
                openEditRouteDialog(selectedRoute,routeName);
                closeButton.setOnClickListener(null);
                selectButton.setOnClickListener(null);
                deleteButton.setOnClickListener(null);
                editButton.setOnClickListener(null);
            }
        });

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routeLayout.setVisibility(View.GONE);
//                openSelectRouteDialog(selectedRoute,routeName);
                closeButton.setOnClickListener(null);
                deleteButton.setOnClickListener(null);
                editButton.setOnClickListener(null);
                selectButton.setOnClickListener(null);
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
                deleteButton.setOnClickListener(null);
                cancelButton.setOnClickListener(null);

            }
        });

    }

    void openEditRouteDialog(Route selectedRoute, String routeName){

        editLayout.setVisibility(View.VISIBLE);
        EditText sectionEditText = editLayout.findViewById(R.id.sectionEditText);
        Button saveButton = editLayout.findViewById(R.id.routeEditSaveButton);
        Button closeButton = editLayout.findViewById(R.id.routeEditCloseButton);
        TextView routeNameTextView = editLayout.findViewById(R.id.routeEditTextView);
        Spinner typeSpinner= editLayout.findViewById(R.id.sectionTypeSpinner);
        routeNameTextView.setText(routeName);

        route_selection = true;
        currentMarker = null;


        // Create an ArrayAdapter to populate the Spinner with data
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.type_options, android.R.layout.simple_spinner_item);

        // Specify the layout for the dropdown items
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the adapter on the Spinner
        typeSpinner.setAdapter(adapter);

        // Set an OnItemSelectedListener to handle item selection events
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item from the Spinner
                String selectedType = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle the case where nothing is selected (if needed)
            }
        });

        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        LatLng closestPoint = findClosestPointOnPolyline(latLng, polyline);

                        if (currentMarker != null) {
                            // Remove the old marker
                            currentMarker.remove();
                        }
                        // Add the new marker at the closest point on the polyline
                        currentMarker = mMap.addMarker(new MarkerOptions().position(closestPoint).title(sectionEditText.getText().toString()));
                    }
                });
            }
        });
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sectionEditText.setText("");
                editLayout.setVisibility(View.GONE);
                mMap.setOnPolylineClickListener(null);
                saveButton.setOnClickListener(null);
                closeButton.setOnClickListener(null);
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add the new marker to checkpoints if not null
                //green
                String type = typeSpinner.getSelectedItem().toString();
                //Toast the type
                Toast.makeText(MainActivity.this,type, Toast.LENGTH_SHORT).show();
                if (currentMarker != null) {
                    selectedRoute.addCheckpoint(currentMarker,type,MainActivity.this,mMap);
                    //redraw the route
                    currentMarker.remove();
                    redrawMap();
                    //clear the edit layout
                    sectionEditText.setText("");
//                    editLayout.setVisibility(View.GONE);
//                    mMap.setOnPolylineClickListener(null);
//                    closeButton.setOnClickListener(null);
//                    saveButton.setOnClickListener(null);
                }
            }
        });


    }
    // Function to find the closest point on a polyline to a given LatLng
    private LatLng findClosestPointOnPolyline(LatLng targetLatLng, Polyline polyline) {
        List<LatLng> points = polyline.getPoints();
        double minDistance = Double.MAX_VALUE;
        LatLng closestPoint = null;

        for (int i = 0; i < points.size() - 1; i++) {
            LatLng start = points.get(i);
            LatLng end = points.get(i + 1);

            double distance = distanceToSegment(targetLatLng, start, end);

            if (distance < minDistance) {
                minDistance = distance;
                closestPoint = projectPointOnSegment(targetLatLng, start, end);
            }
        }

        return closestPoint;
    }

    // Function to calculate the distance from a point to a line segment
    private double distanceToSegment(LatLng p, LatLng v, LatLng w) {
        double l2 = squareDistance(v, w);
        if (l2 == 0) return squareDistance(p, v);
        double t = ((p.latitude - v.latitude) * (w.latitude - v.latitude) + (p.longitude - v.longitude) * (w.longitude - v.longitude)) / l2;
        t = Math.max(0, Math.min(1, t));
        return squareDistance(p, new LatLng(v.latitude + t * (w.latitude - v.latitude), v.longitude + t * (w.longitude - v.longitude)));
    }

    // Function to calculate the square of the distance between two LatLng points
    private double squareDistance(LatLng p1, LatLng p2) {
        double dx = p1.latitude - p2.latitude;
        double dy = p1.longitude - p2.longitude;
        return dx * dx + dy * dy;
    }

    // Function to project a point onto a line segment
    private LatLng projectPointOnSegment(LatLng p, LatLng v, LatLng w) {
        double t = ((p.latitude - v.latitude) * (w.latitude - v.latitude) + (p.longitude - v.longitude) * (w.longitude - v.longitude)) /
                squareDistance(v, w);
        t = Math.max(0, Math.min(1, t));
        return new LatLng(v.latitude + t * (w.latitude - v.latitude), v.longitude + t * (w.longitude - v.longitude));
    }

}