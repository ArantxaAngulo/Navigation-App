package com.akranka10.campusnavigationapp01;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.annotation.NonNull;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.tasks.OnSuccessListener;
import android.location.Location;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import android.location.Location;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    //List<LatLng> campusLocations = new ArrayList<>();
    Map<String, LatLng> poiMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //initializing client location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this); // Make sure your XML layout has a fragment with this ID

    }

    //my code
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap; //new Google Map object

        // Check for location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request the missing permissions if not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // Enable "My Location" button on the map
        mMap.setMyLocationEnabled(true);

        // Get the last known location and update the map camera
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    // Convert location to LatLng and move the camera
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                }
            }
        });

        LatLng campusLocation = new LatLng(20.60833, -103.41695); // Move the camera to a specific location (Campus coordinates)
        mMap.addMarker(new MarkerOptions().position(campusLocation).title("ITESO")); // New Marker (Campus)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(campusLocation, 15)); // Zoom level

        // Setting POIs (Points of Interest) with LatLng objects, to set coordinates
        poiMap.put("Library", new LatLng(20.605873, -103.415513));
        poiMap.put("El Hueco", new LatLng(20.6049088, -103.4153376));
        LatLng library = new LatLng(20.605873, -103.415513);

        // Adding markers of POIs to the map
        mMap.addMarker(new MarkerOptions().
                position(poiMap.get("Library")).title("Library"));

        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.library_marker)));
        mMap.addMarker(new MarkerOptions().
                position(poiMap.get("El Hueco")).title("El Hueco"));

        // Saving POIs on List
        //campusLocations.add(library);
        //campusLocations.add(elHueco);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Call to super method
        super.onRequestPermissionsResult(requestCode,permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, reattempt to set up the location tracking
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                        }
                    });
                }
            } else {
                // Permission denied;
            }
        }
    }

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Radius of Earth in kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public List<POI> findNearestPOIs(double userLat, double userLng, int k) {
        // Map to hold distances and corresponding POIs
        List<POI> pois = new ArrayList<>(poiMap.size());

        // Populate the list with POIs from map
        for (Map.Entry<String, LatLng> entry : poiMap.entrySet()) {
            pois.add(new POI(entry.getKey(), entry.getValue()));
        }

        // Sort POIs based on distance from user location
        pois.sort(Comparator.comparingDouble(poi -> calculateDistance(userLat, userLng, poi.location.latitude, poi.location.longitude)));

        // Prepare the result list with the nearest K POIs
        List<POI> nearestPOIs = new ArrayList<>();
        for (int i = 0; i < Math.min(k, pois.size()); i++) {
            nearestPOIs.add(pois.get(i)); // Add the nearest POIs to the result
        }

        return nearestPOIs; // Return the list of nearest POIs
    }

    // Helper class to store POI and its distance
    public class POI {
        String name;
        LatLng location;

        public POI(String name, LatLng location) {
            this.name = name;
            this.location = location;
        }
    }



}

