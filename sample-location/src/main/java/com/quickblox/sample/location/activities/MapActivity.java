package com.quickblox.sample.location.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.location.QBLocations;
import com.quickblox.location.model.QBLocation;
import com.quickblox.location.request.QBLocationRequestBuilder;
import com.quickblox.sample.core.ui.activity.CoreBaseActivity;
import com.quickblox.sample.core.utils.DialogUtils;
import com.quickblox.sample.core.utils.ResourceUtils;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.location.R;
import com.quickblox.sample.location.model.Data;
import com.quickblox.sample.location.utils.Consts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapActivity extends CoreBaseActivity implements LocationListener, OnMapReadyCallback {

    private GoogleMap googleMap;
    private Location lastLocation;
    private Map<Marker, Data> storageMap = new HashMap<Marker, Data>();
    private Marker myMarker;
    private DialogInterface.OnClickListener checkInPositiveButton;
    private DialogInterface.OnClickListener checkInNegativeButton;

    public static void start(Context context) {
        Intent intent = new Intent(context, MapActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initGooglePlayStatus();
        initLocationRequestBuilder();
    }

    private void initGooglePlayStatus() {
        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        // Showing status
        if (status != ConnectionResult.SUCCESS) {
            // Google Play Services are not available
            int requestCode = Consts.PLAY_SERVICE_REQUEST_CODE;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();
        } else {
            // Google Play Services are available
            // Init Map
            initMapFragment();
            initLocationManager();
        }
    }

    private void initLocationRequestBuilder() {

        // Retrieve other users' locations from QuickBlox
        //
        QBLocationRequestBuilder getLocationsBuilder = new QBLocationRequestBuilder();
        getLocationsBuilder.setPerPage(Consts.LOCATION_PER_PAGE);
        getLocationsBuilder.setLastOnly();

        QBLocations.getLocations(getLocationsBuilder).performAsync(new QBEntityCallback<ArrayList<QBLocation>>() {
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

            public void onError(QBResponseException e) {
                Toaster.longToast(getString(R.string.dlg_location_error) + e.getErrors().toString());
            }
        });
    }

    private void setUpMapIfNeeded(GoogleMap googleMapUpdated) {
        if (googleMap == null) {
            googleMap = googleMapUpdated;
            if (googleMap != null) {
                googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        String message;
                        if (marker.equals(myMarker)) {
                            message = getString(R.string.dlg_it_is_me);
                        } else {
                            Data data = storageMap.get(marker);
                            String status = data.getUserStatus() != null ? data.getUserStatus() : getString(R.string.empty);
                            message = getString(R.string.dlg_user_login) + data.getUserName()
                                    + getString(R.string.dlg_status) + status;
                        }
                        Toaster.longToast(message);
                        return false;
                    }
                });
            }
        }
    }

    private void initMapFragment() {
        MapFragment mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment));
        mapFragment.getMapAsync(this);
    }

    private void initLocationManager() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(provider, Consts.LOCATION_MIN_TIME, 0, this);
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
                final EditText inputEditText = new EditText(this);
                inputEditText.setTextColor(ResourceUtils.getColor(R.color.text_color_medium_dark_grey));
                initAlertListeners(inputEditText);

                final Dialog checkInAlert = DialogUtils.createDialog(this, R.string.dlg_check_in,
                        R.string.dlg_enter_message, inputEditText, checkInPositiveButton, checkInNegativeButton);

                initAlertListeners(inputEditText);
                checkInAlert.show();
                break;
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
                QBLocations.createLocation(location).performAsync(new QBEntityCallback<QBLocation>() {
                    @Override
                    public void onSuccess(QBLocation qbLocation, Bundle bundle) {
                        Toaster.longToast(R.string.dlg_check_in_success);
                    }

                    public void onError(QBResponseException errors) {
                        Toaster.longToast(getString(R.string.dlg_location_error) + errors.getErrors().toString());
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        setUpMapIfNeeded(googleMap);
    }
}