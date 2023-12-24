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

import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean recording = false;
    private boolean center_start= false;
    private boolean route_selection = false;
    private boolean marker_selection = false;
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
    private LinearLayout selectLayout;
    private Route currentRoute;
    private Marker startSectionMarker;
    private Marker middleSectionMarker;
    private Marker stopSectionMarker;
    private RouteRepo routeRepo;
    private Location currentLocation;

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
        selectLayout = findViewById(R.id.routeSelectLayout);
        selectLayout.setVisibility(View.GONE);

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
            testRoute.addRoutePoint(new LatLng(46.7712, 23.6248));
            testRoute.addRoutePoint(new LatLng(46.7712, 23.6249));
            //slight turn
            testRoute.addRoutePoint(new LatLng(46.7713, 23.6250));
            testRoute.addRoutePoint(new LatLng(46.7714, 23.6251));
            testRoute.addRoutePoint(new LatLng(46.7715, 23.6252));
            testRoute.addRoutePoint(new LatLng(46.7716, 23.6253));
            testRoute.addRoutePoint(new LatLng(46.7717, 23.6254));
            testRoute.addRoutePoint(new LatLng(46.7718, 23.6255));
            //straight line
            testRoute.addRoutePoint(new LatLng(46.7719, 23.6256));
            testRoute.addRoutePoint(new LatLng(46.7720, 23.6257));
            testRoute.addRoutePoint(new LatLng(46.7721, 23.6258));
            testRoute.addRoutePoint(new LatLng(46.7722, 23.6259));
            testRoute.addRoutePoint(new LatLng(46.7723, 23.6260));
            //add start and end points
//            MarkerOptions startMarker = new MarkerOptions();
//            startMarker.position(new LatLng(46.7712, 23.6236));
//            startMarker.title("Start");
//            Marker start = mMap.addMarker(startMarker);
//            MarkerOptions endMarker = new MarkerOptions();
//            endMarker.position(new LatLng(46.7712, 23.6247));
//            endMarker.title("End");
//            Marker end = mMap.addMarker(endMarker);
//            testRoute.setStartMarker(start);
//            testRoute.setStartMarker(end);
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
                            currentLocation = locationResult.getLastLocation();
                            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
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
        TextView distanceTextView = routeLayout.findViewById(R.id.routeDistanceTextView);
        TextView timeTextView = routeLayout.findViewById(R.id.routeTimeTextView);

        routeNameTextView.setText(routeName);
        distanceTextView.setText("Distance: "+selectedRoute.getDistance()+" km");
        timeTextView.setText("Best time: "+selectedRoute.getTime()+" min");
        //zoom on center of route

        List<LatLng> routePoints = selectedRoute.getRoutePoints();
        if (!routePoints.isEmpty()) {
            // Calculate the center point of all LatLng points
            double sumLat = 0;
            double sumLng = 0;

            for (LatLng point : routePoints) {
                sumLat += point.latitude;
                sumLng += point.longitude;
            }

            double avgLat = sumLat / routePoints.size();
            double avgLng = sumLng / routePoints.size();

            LatLng centerLatLng = new LatLng(avgLat, avgLng);

            // Set the camera position to the center point and zoom level (16 in this case)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerLatLng, 16));
        }

//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedRoute.getRoutePoints().get(0), 17));

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
                openSelectRouteDialog(selectedRoute,routeName);
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
    void openSelectRouteDialog(Route selectedRoute, String routeName){
        selectLayout.setVisibility(View.VISIBLE);
        Button startButton = selectLayout.findViewById(R.id.routeSelectStartButton);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if rider is at the start of the route
                // start the timer and prepare to stop the timer when the rider:
                // is at the end of the route
                // stops the timer
                // gets off the route

                selectLayout.setVisibility(View.GONE);
                mMap.setOnPolylineClickListener(null);


                if (startButton.getText().toString().equals("Start Route") && riderAtStart(selectedRoute)) {
                    startButton.setText("Stop Route");
                    startButton.setBackgroundColor(Color.parseColor("#FF6750A3"));
                    startButton.setTextColor(Color.WHITE);
                    recording=true;
                    startRecording(selectedRoute);
                } else {
                    startButton.setText("Start Route");
                    startButton.setBackgroundColor(Color.parseColor("#FFDB91E7"));
                    startButton.setTextColor(Color.BLACK);
                    recording=false;
                }
            }
        });
    }
    Boolean riderAtStart(Route selectedRoute){
        //get current location
        Location location = currentLocation;
        LatLng locationPoint = new LatLng(location.getLatitude(),location.getLongitude());
        List<LatLngWithDistance> closestPoints = getNthClosestPoints(locationPoint,selectedRoute,2);
        double distance_between_first_two_points = distanceBetweenPoints(closestPoints.get(0).getLatLng(), closestPoints.get(1).getLatLng());

        // check if closest point is the start of the route and the distance is less than 5 meters
        if (closestPoints.get(0).getLatLng() == selectedRoute.getRoutePoints().get(0) &&
            closestPoints.get(0).getDistance()<5 &&
            closestPoints.get(1).getLatLng() == selectedRoute.getRoutePoints().get(1) &&
            closestPoints.get(1).getDistance() > distance_between_first_two_points )
            return true;
        else
            return false;
    }

    Boolean riderAtEnd(Route selectedRoute){
        //get current location
        Location location =currentLocation;
        LatLng locationPoint = new LatLng(location.getLatitude(),location.getLongitude());
        List<LatLngWithDistance> closestPoints = getNthClosestPoints(locationPoint,selectedRoute,2);
        double distance_between_first_two_points = distanceBetweenPoints(closestPoints.get(0).getLatLng(), closestPoints.get(1).getLatLng());

        // check if closest point is the end of the route and the distance is less than 5 meters
        // and if the distance from the second closest point is greater than the distance between the first and the second point
        if (closestPoints.get(0).getLatLng() == selectedRoute.getRoutePoints().get(selectedRoute.getRoutePoints().size()-1) &&
                closestPoints.get(1).getLatLng() == selectedRoute.getRoutePoints().get(selectedRoute.getRoutePoints().size()-2) &&
                closestPoints.get(1).getDistance() > distance_between_first_two_points )
            return true;
        else
            return false;

    }

    Boolean stayOnTrail(Route selectedRoute){
        //get current location
        Location location = currentLocation;
        LatLng locationPoint = new LatLng(location.getLatitude(),location.getLongitude());
        List<LatLngWithDistance> closestPoints = getNthClosestPoints(locationPoint,selectedRoute,2);

        //if the distance is greater than 5 meters the rider is off the trail
        double distance_to_line = distanceToLine(locationPoint, closestPoints.get(0).getLatLng(), closestPoints.get(1).getLatLng());
        if (distance_to_line < 5)
            return true;
        else
            return false;
    }
    LocalTime startRecording(Route selectedRoute){
        //get current time
        LocalTime startTime = LocalTime.now();
        while(recording == true){
            //check if current location is on the route
            Boolean onTrail = stayOnTrail(selectedRoute);
            Boolean atEnd = riderAtEnd(selectedRoute);
            if(onTrail == false || atEnd == true){
                recording = false;
            }
        }
        //stop timer and save
        LocalTime endTime = LocalTime.now();
        LocalTime totalTime = endTime.minusHours(startTime.getHour()).minusMinutes(startTime.getMinute()).minusSeconds(startTime.getSecond());
        return totalTime;
    }
    void openEditRouteDialog(Route selectedRoute, String routeName){

        editLayout.setVisibility(View.VISIBLE);
        EditText sectionEditText = editLayout.findViewById(R.id.sectionEditText);
        Button saveButton = editLayout.findViewById(R.id.routeEditSaveButton);
        Button closeButton = editLayout.findViewById(R.id.routeEditCloseButton);
        Button switchButton = editLayout.findViewById(R.id.routeMarkerSwitchButton);
        TextView routeNameTextView = editLayout.findViewById(R.id.routeEditTextView);
        Spinner typeSpinner= editLayout.findViewById(R.id.sectionTypeSpinner);
        Spinner difficultySpinner= editLayout.findViewById(R.id.sectionDifficultySpinner);
        routeNameTextView.setText(routeName);

        route_selection = true;
        startSectionMarker = null;
        middleSectionMarker = null;
        stopSectionMarker = null;


        // Create an ArrayAdapter to populate the type Spinner with data
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.type_options, android.R.layout.simple_spinner_item);

        // Specify the layout for the dropdown items
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Set the adapter on the Spinner
        typeSpinner.setAdapter(adapter);

        // Create an ArrayAdapter to populate the difficulty Spinner with data
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.difficulty_options, android.R.layout.simple_spinner_item);
        // Specify the layout for the dropdown items
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Set the adapter on the Spinner
        difficultySpinner.setAdapter(adapter2);


        // Set an OnItemSelectedListener to handle type selection events
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


        //Set an OnItemSelectedListener to handle difficulty selection events

        difficultySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item from the Spinner
                String selectedDifficulty = parent.getItemAtPosition(position).toString();
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
                        if(marker_selection == false){
                            if (startSectionMarker != null) {// make a switch in the ui to switch between start and end
                                // Remove the old marker
                                startSectionMarker.remove();
                            }
                            // Add the new marker at the closest point on the polyline
                            startSectionMarker = mMap.addMarker(new MarkerOptions().position(closestPoint).title("Start of"+sectionEditText.getText().toString()));
                        }
                        else{
                        if (stopSectionMarker != null) {// make a switch in the ui to switch between start and end
                            // Remove the old marker
                            stopSectionMarker.remove();
                        }
                        // Add the new marker at the closest point on the polyline
                        stopSectionMarker = mMap.addMarker(new MarkerOptions().position(closestPoint).title("End of"+sectionEditText.getText().toString()));
                    }
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

        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(marker_selection == true){
                    marker_selection = false;
                    switchButton.setText("Select Start");
                }
                else{
                    marker_selection = true;
                    switchButton.setText("Select End");
                }
            }
        });


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add the new marker to checkpoints if not null
                //green
                String type = typeSpinner.getSelectedItem().toString();
                String difficulty = difficultySpinner.getSelectedItem().toString();
                //Toast the type
                Toast.makeText(MainActivity.this,type +", "+difficulty, Toast.LENGTH_SHORT).show();
                if (startSectionMarker != null && stopSectionMarker != null) {

                    //check if start and stop are in order and get the middle
                    //include the route
                    middleSectionMarker=placement(startSectionMarker,stopSectionMarker,selectedRoute);
                    //
                    selectedRoute.addSection(startSectionMarker, middleSectionMarker, stopSectionMarker,type,difficulty,MainActivity.this,mMap);
                    //redraw the route
                    startSectionMarker.remove();
                    stopSectionMarker.remove();
                    middleSectionMarker.remove();
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


    private Marker placement(Marker start, Marker stop, Route selectedRoute){
        //iterate through points
        int section_length=0, start_index=-1, i=0;
        boolean record_section=false;
        LatLng startPoint = start.getPosition();
        LatLng stopPoint = stop.getPosition();
        for( LatLng routePoint: selectedRoute.getRoutePoints()){
            if(distanceBetweenPoints(routePoint,startPoint)==0 && start_index == -1){
                start_index = i;
                record_section=true;
            }
            else if(distanceBetweenPoints(routePoint,stopPoint)==0 && start_index == -1){
                start_index = i;
                //switch marker positions
                LatLng temp = start.getPosition();
                start.setPosition(stop.getPosition());
                stop.setPosition(temp);
                record_section=true;
            }
            i++;
            if(distanceBetweenPoints(routePoint,stopPoint)==0){
                record_section=false;
            }
            if(record_section)
                section_length++;

        }
        //get the middle point
        int middle_index = start_index + section_length/2;
        selectedRoute.getRoutePoints().get(middle_index);

        //add the middle marker
        LatLng middle = selectedRoute.getRoutePoints().get(middle_index);
        MarkerOptions middleMarker = new MarkerOptions();
        middleMarker.position(middle);
        middleMarker.title("Middle");
        Marker middleSectionMarker = mMap.addMarker(middleMarker);
        return middleSectionMarker;
    }

    // Function to find the closest point on a polyline to a given LatLng
    private LatLng findClosestPointOnPolyline(LatLng targetLatLng, Polyline polyline) {
        List<LatLng> points = polyline.getPoints();
        double minDistance = Double.MAX_VALUE;
        LatLng closestPoint = null;

        for (LatLng point : points) {
            double distance = distanceBetweenPoints(targetLatLng, point);

            if (distance < minDistance) {
                minDistance = distance;
                closestPoint = point;
            }
        }

        return closestPoint;
    }

    public static List<LatLngWithDistance> getNthClosestPoints(LatLng location, Route selectedRoute, int N) {
        List<LatLngWithDistance> distances = new ArrayList<>();
        for (LatLng point : selectedRoute.getRoutePoints()) {

            double distance = distanceBetweenPoints(location, point);
            distances.add(new LatLngWithDistance(point, distance));
        }

        // Sort by distance
        Collections.sort(distances, new Comparator<LatLngWithDistance>() {
            @Override
            public int compare(LatLngWithDistance o1, LatLngWithDistance o2) {
                return Double.compare(o1.getDistance(), o2.getDistance());
            }
        });

        // Return the Nth closest points with their distances
        return distances.subList(0, Math.min(N, distances.size()));
    }

    // Function to calculate the distance between two LatLng points
    private static double distanceBetweenPoints(LatLng p1, LatLng p2) {
        double dx = p1.latitude - p2.latitude;
        double dy = p1.longitude - p2.longitude;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static double distanceToLine(LatLng point, LatLng lineStart, LatLng lineEnd) {
        //get the line equation and return the distance from the given point to the line
        float x0 = (float) point.longitude;
        float y0 = (float) point.latitude;
        float x1 = (float) lineStart.longitude;
        float y1 = (float) lineStart.latitude;
        float x2 = (float) lineEnd.longitude;
        float y2 = (float) lineEnd.latitude;

        float numerator = Math.abs((y2 - y1) * x0 - (x2 - x1) * y0 + x2 * y1 - y2 * x1);
        float denominator = (float) Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));

        return numerator / denominator;
    }

    public static class LatLngWithDistance {
        private final LatLng latLng;
        private final double distance;

        public LatLngWithDistance(LatLng latLng, double distance) {
            this.latLng = latLng;
            this.distance = distance;
        }

        public LatLng getLatLng() {
            return latLng;
        }

        public double getDistance() {
            return distance;
        }
    }

}