package com.quickblox.sample.location.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;

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
import com.quickblox.sample.location.utils.Constants;
import com.quickblox.sample.location.utils.DialogUtils;

import java.util.HashMap;
import java.util.Map;

public class MapActivity extends FragmentActivity implements LocationListener {

    private Context context;
    private Resources resources;
    private GoogleMap googleMap;
    private Location lastLocation;
    private Map<Marker, Data> storageMap = new HashMap<Marker, Data>();
    private Marker myMarker;
    private DialogInterface.OnClickListener checkInPositiveButton;
    private DialogInterface.OnClickListener checkInNegativeButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        context = this;
        resources = getResources();

        initGooglePlayStatus();
        initLocationRequestBuilder();
    }

    private void initGooglePlayStatus() {
        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        // Showing status
        if (status != ConnectionResult.SUCCESS) {
            // Google Play Services are not available
            int requestCode = Constants.PLAY_SERVICE_REQUEST_CODE;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();
        } else {
            // Google Play Services are available
            // Init Map
            setUpMapIfNeeded();
            initLocationManager();
        }
    }

    private void initLocationRequestBuilder() {
        // ================= QuickBlox ====================
        // Retrieve other users' locations from QuickBlox
        QBLocationRequestBuilder getLocationsBuilder = new QBLocationRequestBuilder();
        getLocationsBuilder.setPerPage(Constants.LOCATION_PER_PAGE);
        getLocationsBuilder.setLastOnly();
        QBLocations.getLocations(getLocationsBuilder, new QBCallbackImpl() {
            @Override
            public void onComplete(Result result) {
                if (result.isSuccess()) {
                    QBLocationPagedResult locationsResult = (QBLocationPagedResult) result;
                    // Process result
                    // show all locations on map
                    for (QBLocation location : locationsResult.getLocations()) {
                        Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(
                                location.getLatitude(), location.getLongitude())).icon(
                                BitmapDescriptorFactory.fromResource(R.drawable.map_marker_other)));

                        Data data = new Data(location.getUser().getLogin(), location.getStatus());
                        storageMap.put(marker, data);
                    }
                } else {
                    DialogUtils.showLong(context, resources.getString(R.string.dlg_location_error) + result.getErrors());
                }
            }
        });
    }

    private void setUpMapIfNeeded() {
        if (googleMap == null) {
            googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment)).getMap();
            if (googleMap != null) {
                googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        String message;
                        if (marker.equals(myMarker)) {
                            message = resources.getString(R.string.dlg_it_is_me);
                        } else {
                            Data data = storageMap.get(marker);
                            message = resources.getString(R.string.dlg_user_login) + data.getUserName() +
                                    resources.getString(R.string.dlg_status) + (data
                                    .getUserStatus() != null ? data.getUserStatus() : resources.getString(R.string.empty));
                        }
                        DialogUtils.showLong(context, message);
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
        locationManager.requestLocationUpdates(provider, Constants.LOCATION_MIN_TIME, 0, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);

        if (myMarker == null) {
            myMarker = googleMap.addMarker(new MarkerOptions().position(latLng).icon(
                    BitmapDescriptorFactory.fromResource(R.drawable.map_marker_my)));

            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
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

    public void onClickButtons(View view) {
        switch (view.getId()) {
            case R.id.check_in_button:

                final EditText input = new EditText(this);
                initAlertListeners(input);

                final Dialog checkInAlert = DialogUtils.createDialog(context, R.string.dlg_check_in,
                        R.string.dlg_enter_message, input, checkInPositiveButton, checkInNegativeButton);

                initAlertListeners(input);
                checkInAlert.show();
        }
    }

    private void initAlertListeners(final EditText input) {
        checkInPositiveButton = new DialogInterface.OnClickListener() {
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
                            DialogUtils.showLong(context, resources.getString(R.string.dlg_check_in_success));
                        } else {
                            DialogUtils.showLong(context, resources.getString(R.string.dlg_location_error) + result.getErrors());
                        }
                    }
                });
            }
        };

        checkInNegativeButton = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        };
    }
}