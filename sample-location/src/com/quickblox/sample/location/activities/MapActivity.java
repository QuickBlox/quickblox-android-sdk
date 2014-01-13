package com.quickblox.sample.location.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.module.locations.QBLocations;
import com.quickblox.module.locations.model.QBLocation;
import com.quickblox.module.locations.request.QBLocationRequestBuilder;
import com.quickblox.module.locations.result.QBLocationPagedResult;
import com.quickblox.sample.location.R;
import com.quickblox.sample.location.model.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Date: 1.11.12
 * Time: 12:16
 */

/**
 * Map Activity shows how QuickBlox Location module works.
 * It shows users' locations on the map.
 * It allows to check in in any place - share own position.
 *
 * @author <a href="mailto:igos@quickblox.com">Igor Khomenko</a>
 */
public class MapActivity extends FragmentActivity implements LocationListener {

    private GoogleMap map;
    private Location lastLocation;
    private Map<Marker, Data> storage = new HashMap<Marker, Data>();
    private Marker myMarker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        // Showing status
        if (status != ConnectionResult.SUCCESS) { // Google Play Services are not available
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();
        } else { // Google Play Services are available

            // Init Map
            setUpMapIfNeeded();
            initLocationManager();
        }

        // ================= QuickBlox ====================
        // Retrieve other users' locations from QuickBlox
        QBLocationRequestBuilder getLocationsBuilder = new QBLocationRequestBuilder();
        getLocationsBuilder.setPerPage(10);
        getLocationsBuilder.setLastOnly();
        QBLocations.getLocations(getLocationsBuilder, new QBCallbackImpl() {
            @Override
            public void onComplete(Result result) {
                if (result.isSuccess()) {
                    QBLocationPagedResult locationsResult = (QBLocationPagedResult) result;

                    // Process result
                    // show all locations on map
                    for (QBLocation location : locationsResult.getLocations()) {
                        Marker marker = map.addMarker(new MarkerOptions()
                                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_other)));

                        Data data = new Data(location.getUser().getLogin(), location.getStatus());
                        storage.put(marker, data);
                    }
                } else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MapActivity.this);
                    dialog.setMessage("Error(s) occurred. Look into DDMS log for details, " +
                            "please. Errors: " + result.getErrors()).create().show();
                }
            }
        });
    }

    public void onClickButtons(View v) {
        switch (v.getId()) {
            case R.id.checkIn:

                final AlertDialog.Builder checkInAlert = new AlertDialog.Builder(this);

                checkInAlert.setTitle("Check In");
                checkInAlert.setMessage("Please enter your message");

                // Set an EditText view to get user input (status)
                final EditText input = new EditText(this);
                checkInAlert.setView(input);

                checkInAlert.setPositiveButton("Check In", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // check in
                        double lat = lastLocation.getLatitude();
                        double lng = lastLocation.getLongitude();

                        // ================= QuickBlox ====================
                        // Share own location
                        QBLocation location = new QBLocation(lat, lng, input.getText().toString());
                        QBLocations.createLocation(location, new QBCallbackImpl() {
                            @Override
                            public void onComplete(Result result) {
                                if (result.isSuccess()) {
                                    Toast.makeText(MapActivity.this, "Check In was successful!",
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    AlertDialog.Builder dialog = new AlertDialog.Builder(MapActivity.this);
                                    dialog.setMessage("Error(s) occurred. Look into DDMS log for details, " +
                                            "please. Errors: " + result.getErrors()).create().show();
                                }
                            }
                        });
                    }
                });

                checkInAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

                checkInAlert.show();
        }
    }

    private void setUpMapIfNeeded() {
        if (map == null) {
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            if (map != null) {
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        String message;
                        if (marker.equals(myMarker)) {
                            message = "It's me";
                        } else {
                            Data data = storage.get(marker);
                            message = "User login: " + data.getUserName() +
                                    ", Status: " + (data.getUserStatus() != null ? data.getUserStatus() : "<empty>");
                        }
                        Toast.makeText(MapActivity.this, message,
                                Toast.LENGTH_LONG).show();
                        return false;
                    }
                });
            }
        }
    }

    private void initLocationManager() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(provider, 20000, 0, this);
    }

    @Override
    public void onLocationChanged(Location location) {

        lastLocation = location;
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);

        if (myMarker == null) {
            myMarker = map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_my)));

            map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        } else {
            myMarker.setPosition(latLng);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}