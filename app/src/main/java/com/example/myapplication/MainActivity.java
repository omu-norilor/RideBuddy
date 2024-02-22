package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.firebase.FirebaseApp;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    LocationUpdaterThread locationUpdaterThread;
    private TextToSpeech tts;
    private boolean recording = false;
    private boolean recording_run = false;
    private boolean center_start = false;
    private boolean route_selection = false;
    private boolean marker_selection = false;
    private String last_section = "";
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
    public LocalTime startTime;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        currentRoute = new Route("current");


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

        routeRepo = new RouteRepo(getApplicationContext());

        // Start button
        startButton = findViewById(R.id.startButton);
        startButton.setBackgroundColor(Color.parseColor("#FFDB91E7"));
        startButton.setOnClickListener(v -> {
            if (startButton.getText().toString().equals("Start Route Recording")) {
                currentRoute.removeRoute();
                startButton.setText("Stop Route Recording");
                startButton.setBackgroundColor(Color.parseColor("#FF6750A3"));
                startButton.setTextColor(Color.WHITE);
                recording = true;
            } else {
                saveLayout.setVisibility(View.VISIBLE);
                startButton.setText("Start Route Recording");
                startButton.setBackgroundColor(Color.parseColor("#FFDB91E7"));
                startButton.setTextColor(Color.BLACK);
                recording = false;
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
                    routeToSave.addRouteToMap(mMap, MainActivity.this);
                    routeRepo.addRoute(name, routeToSave);
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

                // Redraw the map
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
                redrawMap();
                showRouteListDialog();
                selectLayout.setVisibility(View.GONE);
                editLayout.setVisibility(View.GONE);
                deleteLayout.setVisibility(View.GONE);
                routeLayout.setVisibility(View.GONE);
                saveLayout.setVisibility(View.GONE);

            }
        });

        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.ENGLISH);
            }
        });


    }

    private void testCluj() {
        Route testRoute = new Route("test Cluj");

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
        //add it to the map
        testRoute.addRouteToMap(mMap, this);
        //add it to the repo
        routeRepo.addRoute("test Cluj", testRoute);
    }

    private void testBaiaMare() {
        Route testRoute = new Route("test Baia Mare");

        //make it a straight line, start at 47.6489, 23.5646
        testRoute.addRoutePoint(new LatLng(47.6489, 23.5646));
        testRoute.addRoutePoint(new LatLng(47.6489, 23.5647));
        testRoute.addRoutePoint(new LatLng(47.6489, 23.5648));
        testRoute.addRoutePoint(new LatLng(47.6489, 23.5649));
        testRoute.addRoutePoint(new LatLng(47.6489, 23.5650));
        testRoute.addRoutePoint(new LatLng(47.6489, 23.5651));
        testRoute.addRoutePoint(new LatLng(47.6489, 23.5652));
        testRoute.addRoutePoint(new LatLng(47.6489, 23.5653));
        testRoute.addRoutePoint(new LatLng(47.6489, 23.5654));
        testRoute.addRoutePoint(new LatLng(47.6489, 23.5655));
        testRoute.addRoutePoint(new LatLng(47.6489, 23.5656));
        testRoute.addRoutePoint(new LatLng(47.6489, 23.5657));
        testRoute.addRoutePoint(new LatLng(47.6489, 23.5658));
        testRoute.addRoutePoint(new LatLng(47.6489, 23.5659));
        //slight turn
        testRoute.addRoutePoint(new LatLng(47.6490, 23.5660));
        testRoute.addRoutePoint(new LatLng(47.6491, 23.5661));
        testRoute.addRoutePoint(new LatLng(47.6492, 23.5662));
        testRoute.addRoutePoint(new LatLng(47.6493, 23.5663));
        testRoute.addRoutePoint(new LatLng(47.6494, 23.5664));
        testRoute.addRoutePoint(new LatLng(47.6495, 23.5665));
        //straight line
        testRoute.addRoutePoint(new LatLng(47.6496, 23.5666));
        testRoute.addRoutePoint(new LatLng(47.6497, 23.5667));
        testRoute.addRoutePoint(new LatLng(47.6498, 23.5668));
        testRoute.addRoutePoint(new LatLng(47.6499, 23.5669));
        testRoute.addRoutePoint(new LatLng(47.6500, 23.5670));

        //add it to the map
        testRoute.addRouteToMap(mMap, this);
        //add it to the repo
        routeRepo.addRoute("test Baia Mare", testRoute);
    }

    private void testLangaBlocCluj(){
        Route testRoute = new Route("test langa Bloc Cluj");
        //make it a straight line
        testRoute.addRoutePoint(new LatLng(46.766295, 23.625854));
        testRoute.addRoutePoint(new LatLng(46.766314, 23.625970));
        testRoute.addRoutePoint(new LatLng(46.766335, 23.626100));
        testRoute.addRoutePoint(new LatLng(46.766395, 23.626433));
        testRoute.addRoutePoint(new LatLng(46.766398, 23.626702));


        //add it to the map
        testRoute.addRouteToMap(mMap, this);
        //add it to the repo
        routeRepo.addRoute("test langa Bloc Cluj", testRoute);

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        locationUpdaterThread = new LocationUpdaterThread(this);
        // Check for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            // Start location updates
            startLocationUpdatesWithThread(locationUpdaterThread);

            // Center map on current location
            Location currentLocation = locationUpdaterThread.getCurrentLocation();
            if (currentLocation != null) {
                LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17));
            }

            //add a debugging routes to the map for testing
//            testCluj();
//            testBaiaMare();
//            testLangaBlocCluj();


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
    public void startLocationUpdatesWithThread(LocationUpdaterThread locationUpdaterThread) {
        mMap.setMyLocationEnabled(true);

        locationUpdaterThread.startLocationUpdates();

        locationUpdaterThread.setOnLocationChangeListener(location -> {
            if (location != null) {
                currentLocation = location;
                LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                if (!center_start) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17));
                    center_start = true;
                }
                if (recording) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17));
                    Marker marker = mMap.addMarker(new MarkerOptions().position(currentLatLng).title("My Location"));
                    currentRoute.addRoutePoint(currentLatLng);
                    if (currentRoute.getStartMarker() == null) {
                        currentRoute.setStartMarker(marker);
                    }
                    // Delete previous end marker
                    if (currentRoute.getEndMarker() != null) {
                        currentRoute.getEndMarker().remove();
                    }
                    currentRoute.setEndMarker(marker);
                }
                if (recording_run){
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17));
                    List<LatLngWithDistance> closestPoints = getNthClosestPoints(currentLatLng,currentRoute,2);
                    Boolean onTrail = stayOnTrail(currentLatLng,currentRoute,closestPoints);
                    Boolean atEnd = riderAtEnd(currentLatLng,currentRoute,closestPoints);
                    Pair<Double,String> section = getClosestSection(currentLatLng,currentRoute);
//                    Toast.makeText(MainActivity.this, "Your coordinates are: "+currentLatLng.toString(), Toast.LENGTH_SHORT).show();
                    //log ontrail and atend
                    // Log the values of onTrail and atEnd
                    Log.d("RunStatus", "onTrail: " + onTrail + ", atEnd: " + atEnd);
                    Toast.makeText(MainActivity.this, "onTrail: " + onTrail + ", atEnd: " + atEnd, Toast.LENGTH_SHORT).show();

                    if(section.first < 20){
                        Toast.makeText(MainActivity.this, "You are in section: "+section.second, Toast.LENGTH_SHORT).show();
                        if(last_section.equals(section.second) == false){
                            last_section = section.second;
                            tts.speak("Incoming "+section.second, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }
                    if(onTrail == false || atEnd == true){
                        recording_run = false;
                        //stop timer and save
                        LocalTime endTime = LocalTime.now();
                        LocalTime totalTime = endTime.minusHours(startTime.getHour()).minusMinutes(startTime.getMinute()).minusSeconds(startTime.getSecond());
                        //convert to string, only time minutes, seconds
                        String time = totalTime.toString();
                        time= time.substring(3);
                        if (onTrail == false){
                            Toast.makeText(MainActivity.this, "You got off the trail. Run ended in "+totalTime.toString(), Toast.LENGTH_SHORT).show();
                            tts.speak("You got off the trail. Run ended in "+totalTime.toString(), TextToSpeech.QUEUE_FLUSH, null);
                        }
                        if (atEnd == true){
                            currentRoute.setTime(time);
                            Toast.makeText(MainActivity.this, "You finished the route in "+totalTime.toString(), Toast.LENGTH_SHORT).show();
                            tts.speak("You finished the route in "+totalTime.toString(), TextToSpeech.QUEUE_FLUSH, null);
                        }
                        selectLayout.setVisibility(View.GONE);
                        last_section = "";
                    }
                }
            }
        });
    }

    private Pair<Double, String> getClosestSection(LatLng currentLatLng, Route currentRoute) {
        Map<LatLng, Section> sections = currentRoute.getSections();
        double min_distance = 1000000;
        String min_section_description = "";
        //for each LatLng in the sections calculate distance to currentLatLng
        for (LatLng point : sections.keySet()) {
            double distance = distanceBetweenPoints(currentLatLng, point);
            if(distance < min_distance) {
                min_distance = distance;
                min_section_description = sections.get(point).getType() + ". Difficulty: " + sections.get(point).getDifficulty();
            }
        }
        return new Pair<>(min_distance, min_section_description);
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
        locationRequest.setInterval(3000); // Update interval in milliseconds (3 seconds)
        locationRequest.setFastestInterval(1000); // Fastest update interval in milliseconds (1 second)
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
        String formattedDistance = "";
        if (selectedRoute.getDistance() >= 1)
            formattedDistance = String.format("Distance:  %.2f kilometers", selectedRoute.getDistance());
        else
            formattedDistance = String.format("Distance: %.2f meters", selectedRoute.getDistance()*1000);
        distanceTextView.setText(formattedDistance);
        timeTextView.setText("Best time: "+selectedRoute.getTime());
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
                routeRepo.removeRoute(routeName);
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
        Button selectStartButton = selectLayout.findViewById(R.id.routeSelectStartButton);
        TextView routeNameTextView = selectLayout.findViewById(R.id.routeSelectTextView);
        routeNameTextView.setText(routeName);
        selectStartButton.setBackgroundColor(Color.parseColor("#FF6750A3"));
        selectStartButton.setTextColor(Color.WHITE);
        selectStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if rider is at the start of the route
                // start the timer and prepare to stop the timer when the rider:
                // is at the end of the route
                // stops the timer
                // gets off the route

//                selectLayout.setVisibility(View.GONE);
                mMap.setOnPolylineClickListener(null);

                if (selectStartButton.getText().toString().equals("Start Route")) {

                    Location location = locationUpdaterThread.getCurrentLocation();
                    LatLng locationPoint = new LatLng(location.getLatitude(),location.getLongitude());
                    List<LatLngWithDistance> closestPoints = getNthClosestPoints(locationPoint,selectedRoute,2);
                    if(riderAtStart(locationPoint, selectedRoute, closestPoints) == true) {

                        selectStartButton.setText("Stop Route");
                        startTime = LocalTime.now();
                        currentRoute= selectedRoute;
                        recording_run = true;
                    }
                    else {
                        Toast.makeText(MainActivity.this, "You are not at the start of the route", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    selectStartButton.setText("Start Route");
                    recording_run = false;
                    selectLayout.setVisibility(View.GONE);
                }
            }
        });
    }
    Boolean riderAtStart(LatLng locationPoint, Route selectedRoute, List<LatLngWithDistance> closestPoints){
        double distance_between_first_two_points = distanceBetweenPoints(closestPoints.get(0).getLatLng(), closestPoints.get(1).getLatLng());
        // check if closest point is the end of the route and the distance is less than 5 meters
        // and if the distance from the second closest point is greater than the distance between the first and the second point
        if (closestPoints.get(0).getLatLng() == selectedRoute.getRoutePoints().get(0) &&
            closestPoints.get(0).getDistance()<10 &&
            closestPoints.get(1).getLatLng() == selectedRoute.getRoutePoints().get(1) &&
            closestPoints.get(1).getDistance() > distance_between_first_two_points )
            return true;
        else
            return false;
    }

    Boolean riderAtEnd(LatLng locationPoint, Route selectedRoute, List<LatLngWithDistance> closestPoints){
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

    Boolean stayOnTrail(LatLng locationPoint, Route selectedRoute, List<LatLngWithDistance> closestPoints){
        double distance_to_line = distanceToLine(locationPoint, closestPoints.get(0).getLatLng(), closestPoints.get(1).getLatLng());
        //if the distance is greater than 5 meters the rider is off the trail
        Log.d("StayOnTrail", "Distance to line: " + distance_to_line);
        if (distance_to_line < 10)
            return true;
        else
            return false;
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
                // Add the new marker to checkpoints if not null
                // Green
                String type = typeSpinner.getSelectedItem().toString();
                String difficulty = difficultySpinner.getSelectedItem().toString();
                // Toast the type
                Toast.makeText(MainActivity.this,type +", "+difficulty, Toast.LENGTH_SHORT).show();
                if (startSectionMarker != null && stopSectionMarker != null) {

                    // Check if start and stop are in order and get the middle
                    middleSectionMarker=placement(startSectionMarker,stopSectionMarker,selectedRoute);
                    selectedRoute.addSection(startSectionMarker, middleSectionMarker, stopSectionMarker,type,difficulty,MainActivity.this,mMap);
                    // Redraw the route
                    startSectionMarker.remove();
                    stopSectionMarker.remove();
                    middleSectionMarker.remove();
                    redrawMap();
                    // Clear the edit layout
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


    public static double distanceBetweenPoints(LatLng p1, LatLng p2){
        // Haversine formula
        final double R = 6371.0;

        // Convert latitude and longitude from degrees to radians
        double lat1Rad = Math.toRadians(p1.latitude);
        double lon1Rad = Math.toRadians(p1.longitude);
        double lat2Rad = Math.toRadians(p2.latitude);
        double lon2Rad = Math.toRadians(p2.longitude);

        // Calculate the differences in coordinates
        double dlat = lat2Rad - lat1Rad;
        double dlon = lon2Rad - lon1Rad;

        // Haversine formula
        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.sin(dlon / 2) * Math.sin(dlon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Calculate and return the distance
        return R * c;
    }
    private static double badDistanceBetweenPoints(LatLng p1, LatLng p2) {
        double lat1 = Math.toRadians(p1.latitude);
        double lon1 = Math.toRadians(p1.longitude);
        double lat2 = Math.toRadians(p2.latitude);
        double lon2 = Math.toRadians(p2.longitude);

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double EARTH_RADIUS = 6371000; // Radius of the earth in meters
        return EARTH_RADIUS * c; // Distance in meters
    }

    public static double badDistanceToLine(LatLng point, LatLng lineStart, LatLng lineEnd) {
        //get the line equation and return the distance from the given point to the line
        float x0 = (float) point.longitude;
        float y0 = (float) point.latitude;
        float x1 = (float) lineStart.longitude;
        float y1 = (float) lineStart.latitude;
        float x2 = (float) lineEnd.longitude;
        float y2 = (float) lineEnd.latitude;

        float numerator = Math.abs((y2 - y1) * x0 - (x2 - x1) * y0 + x2 * y1 - y2 * x1);
        float denominator = (float) Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));

        Log.d ("StayOnTrail", "point: " + point);
        Log.d("StayOnTrail", "Line start: " + lineStart);
        Log.d("StayOnTrail", "Line end: " + lineEnd);
        Log.d("StayOnTrail", "Distance to line: " + numerator / denominator);

        return numerator / denominator;
    }

    public static double distanceToLine(LatLng point, LatLng lineStart, LatLng lineEnd) {
        double dStartPoint = distanceBetweenPoints(point, lineStart);
        double dEndPoint = distanceBetweenPoints(point, lineEnd);
        double dStartEnd = distanceBetweenPoints(lineStart, lineEnd);

        // Use Heron's formula to calculate the area of the triangle
        double s = (dStartPoint + dEndPoint + dStartEnd) / 2;
        double areaTriangle = Math.sqrt(s * (s - dStartPoint) * (s - dEndPoint) * (s - dStartEnd));

        // Calculate the perpendicular distance
        double distance = (2 * areaTriangle) / dStartEnd;

        Log.d("StayOnTrail", "Point: " + point);
        Log.d("StayOnTrail", "Line start: " + lineStart);
        Log.d("StayOnTrail", "Line end: " + lineEnd);
        Log.d("StayOnTrail", "Distance to line: " + distance);


        return distance*1000;
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