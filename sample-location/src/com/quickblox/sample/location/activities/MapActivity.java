package com.quickblox.sample.location.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.location.QBLocations;
import com.quickblox.location.model.QBLocation;
import com.quickblox.sample.location.R;
import com.quickblox.sample.location.mapapi.query.ApiRequester;
import com.quickblox.sample.location.utils.Constants;
import com.quickblox.sample.location.utils.DialogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapActivity extends FragmentActivity implements LocationListener {

    private static final String MARKER_RESOURCE = "marker_resource";
    private Context context;
    private Resources resources;
    private GoogleMap googleMap;
    private Location lastLocation;
    private Marker myMarker;
    private DialogInterface.OnClickListener checkInPositiveButton;
    private DialogInterface.OnClickListener checkInNegativeButton;
    private Marker bankmarker;
    private int markerResource;
    private Polyline curPolyline;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        context = this;
        resources = getResources();
        markerResource = getIntent().getIntExtra(MARKER_RESOURCE, R.drawable.map_marker_other);
        initGooglePlayStatus();
        initLocationRequestBuilder();
    }

    public static void start(Activity activity, int resMarker){
        Intent intent = new Intent(activity, MapActivity.class);
        intent.putExtra(MARKER_RESOURCE, resMarker);
        activity.startActivity(intent);
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


        QBCustomObjects.getObjects("bank", new QBEntityCallbackImpl<ArrayList<QBCustomObject>>() {
            @Override
            public void onSuccess(ArrayList<QBCustomObject> qbLocations, Bundle bundle) {

                // show all locations on the map
                //
                for (QBCustomObject location : qbLocations) {
                    HashMap<String, Object> fields = location.getFields();
                    double lat = Double.parseDouble((String) fields.get("latitude"));
                    double longitude = Double.parseDouble((String) fields.get("longitude"));
                    String title = fields.get("name") +", " + fields.get("branch") + ", " + fields.get("address") +
                            ", " +
                            fields.get("city_name");
                    Log.i(MapActivity.class.getSimpleName(), "bank item:" +  "lat="+lat + ", longitude="+longitude);
                    Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(
                            lat, longitude)).icon(
                            BitmapDescriptorFactory.fromResource(markerResource)).title(title));
                    bankmarker = marker;
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
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        if (myMarker == null ||  marker.getPosition().equals(myMarker.getPosition())){
                            return false;
                        }
                        if (curPolyline != null) {
                            curPolyline.remove();
                        }
                        new DirAysncTask().execute(marker.getPosition(), myMarker.getPosition());
                        return false;
                    }
                });
            }
            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    myMarker = googleMap.addMarker(new MarkerOptions().position(latLng).icon(
                            BitmapDescriptorFactory.fromResource(markerResource)));
                }
            });
        }
    }

    private class DirAysncTask extends AsyncTask<LatLng, Void, List<LatLng>>{

        private final String TAG = DirAysncTask.class.getSimpleName();

        @Override
        protected List<LatLng> doInBackground(LatLng... params) {
            try {
                return ApiRequester.getDirections(params[0], params[1]);
            } catch (QBResponseException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<LatLng> directions) {
            super.onPostExecute(directions);
            if (directions != null){
                drawRoute(directions);
            } else {
                DialogUtils.showLong(context, "Sorry. Errors occurred while requesting data ");
            }
        }
    }

    private void drawRoute(List<LatLng> latLngs){
        curPolyline = googleMap.addPolyline(createPolyLine(latLngs));
    }

    private PolylineOptions createPolyLine(List<LatLng> locations){
        PolylineOptions options = new PolylineOptions();

        options.color( Color.BLUE);
        options.width( 10 );
        options.visible( true );

        for ( LatLng locRecorded : locations )
        {
            options.add(locRecorded );
        }
        return options;
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
                    BitmapDescriptorFactory.fromResource(R.drawable.map_marker_my)).title("It's me"));

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5.0f));
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
            case R.id.bank:
                if (bankmarker != null) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bankmarker.getPosition(), 10.0f));
                }
                break;
            case R.id.my_location:
                if (myMarker != null) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myMarker.getPosition(), 10.0f));
                }
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