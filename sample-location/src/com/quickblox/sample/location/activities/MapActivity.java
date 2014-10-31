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
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.location.QBLocations;
import com.quickblox.location.model.QBLocation;
import com.quickblox.location.request.QBLocationRequestBuilder;
import com.quickblox.location.result.QBLocationPagedResult;
import com.quickblox.sample.location.R;
import com.quickblox.sample.location.model.Data;
import com.quickblox.sample.location.utils.Constants;
import com.quickblox.sample.location.utils.DialogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        // Retrieve other users' locations from QuickBlox
        //
        QBLocationRequestBuilder getLocationsBuilder = new QBLocationRequestBuilder();
        getLocationsBuilder.setPerPage(Constants.LOCATION_PER_PAGE);
        getLocationsBuilder.setLastOnly();

        QBLocations.getLocations(getLocationsBuilder, new QBEntityCallbackImpl<ArrayList<QBLocation>>() {
            @Override
            public void onSuccess(ArrayList<QBLocation> qbLocations, Bundle bundle) {

                // show all locations on the map
                //
                for (QBLocation location : qbLocations) {
                    Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(
                            location.getLatitude(), location.getLongitude())).icon(
                            BitmapDescriptorFactory.fromResource(R.drawable.map_marker_other)));

                    Data data = new Data(location.getUser().getLogin(), location.getStatus());
                    storageMap.put(marker, data);
                }
            }

            @Override
            public void onError(List<String> errors) {
                DialogUtils.showLong(context, resources.getString(R.string.dlg_location_error) + errors);
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
                input.setTextColor(getResources().getColor(R.color.white));
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
                QBLocations.createLocation(location, new QBEntityCallbackImpl<QBLocation>() {
                    @Override
                    public void onSuccess(QBLocation qbLocation, Bundle bundle) {
                        DialogUtils.showLong(context, resources.getString(R.string.dlg_check_in_success));
                    }

                    @Override
                    public void onError(List<String> errors) {
                        DialogUtils.showLong(context, resources.getString(R.string.dlg_location_error) + errors);
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