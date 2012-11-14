package com.quickblox.sample.location.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.maps.*;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.module.locations.QBLocations;
import com.quickblox.module.locations.model.QBLocation;
import com.quickblox.module.locations.request.QBLocationRequestBuilder;
import com.quickblox.module.locations.result.QBLocationPagedResult;
import com.quickblox.sample.location.R;
import com.quickblox.sample.location.objects.MapOverlayItem;

import java.util.ArrayList;
import java.util.List;

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
public class MapActivity extends com.google.android.maps.MapActivity {

    private MapView mapView;
    private MapController mapController;
    private Drawable marker;
    private WhereAmI ownOverlay;
    private Location lastLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_view);

        // Init Map
        initMapView();

        initMyLocation();

        // get a latitude and a longitude of the current user
        LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locListener = new LocationListener() {

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}

            // if a location of the device will be changed,
            // send the data on the server
            @Override
            public void onLocationChanged(Location location) {
                if (location != null){

                    // save current location
                    lastLocation = location;
                }
            }
        };

        List<String> providers = locManager.getProviders(true);
        for (String provider : providers) {
            // registration of the LocationListener.
            locManager.requestLocationUpdates(provider, 1000,
                    10, locListener);

            lastLocation = locManager.getLastKnownLocation(provider);
        }

        marker = getResources().getDrawable(R.drawable.map_marker_other);
        marker.setBounds(0, 0, marker.getIntrinsicWidth(),
                marker.getIntrinsicHeight());


        // ================= QuickBlox ====================
        // Retrieve other users' locations from QuickBlox
        QBLocationRequestBuilder getLocationsBuilder = new QBLocationRequestBuilder();
        getLocationsBuilder.setPerPage(10);
        getLocationsBuilder.setLastOnly();
        QBLocations.getLocations(getLocationsBuilder, new QBCallbackImpl() {
            @Override
            public void onComplete(Result result) {
                if (result.isSuccess()) {
                    final List<MapOverlayItem> locationsList = new ArrayList<MapOverlayItem>();

                    QBLocationPagedResult locationsResult = (QBLocationPagedResult)result;

                    // Process result
                    // show all locations on map
                    for(QBLocation location : locationsResult.getLocations()){
                        int lat = (int) (location.getLatitude() * 1000000);
                        int lng = (int) (location.getLongitude() * 1000000);

                        final MapOverlayItem overlayItem = new MapOverlayItem(new GeoPoint(lat, lng), "", "");
                        overlayItem.setUserStatus(location.getStatus());
                        overlayItem.setUserName("" + location.getUserId());
                        locationsList.add(overlayItem);
                    }

                    // add locations to map
                    mapView.getOverlays().add(new ShowAllUsers(marker, locationsList));
                    mapView.invalidate();

                }else{
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
                                }else{
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

    private void initMapView() {
        mapView = (MapView) findViewById(R.id.mapview);
        mapController = mapView.getController();
        mapView.setSatellite(true);
        mapView.setBuiltInZoomControls(true);
    }

    private void initMyLocation() {
        if(ownOverlay != null){
            return;
        }
        
        ownOverlay = new WhereAmI(this, mapView);
        // to begin follow for the updates of the location
        ownOverlay.enableMyLocation();
        ownOverlay.enableCompass(); // it's no works in the emulator
        ownOverlay.runOnFirstFix(new Runnable() {

            @Override
            public void run() {
                // Show current location and change a zoom
                mapController.setZoom(4);

                if (ownOverlay.getMyLocation() != null) {
                    mapController.animateTo(ownOverlay.getMyLocation());
                }
            }
        });
        mapView.getOverlays().add(ownOverlay);
    }

    @Override
    protected boolean isLocationDisplayed() {
        return true;
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }


    // Other Users overlays
    class ShowAllUsers extends ItemizedOverlay<MapOverlayItem> {

        private List<MapOverlayItem> locations = new ArrayList<MapOverlayItem>();
        private Drawable marker;

        public ShowAllUsers(Drawable marker, List<MapOverlayItem> overlayItems) {
            super(marker);

            this.marker = marker;

            // populate items
            for (MapOverlayItem overlayItem : overlayItems) {
                locations.add(overlayItem);
                populate();
            }
        }

        // a shadow of the marker
        @Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow) {
            super.draw(canvas, mapView, shadow);
            boundCenterBottom(marker);
        }

        @Override
        protected MapOverlayItem createItem(int i) {
            return locations.get(i);
        }

        @Override
        public int size() {
            return locations.size();
        }

        // tab on marker
        @Override
        protected boolean onTap(int i) {

            MapOverlayItem item = (MapOverlayItem) getItem(i);

            String message = "User ID: " + item.getUserName() +
                    ", Status: " + (item.getUserStatus() != null ? item.getUserStatus() : "<empty>");
            Toast.makeText(MapActivity.this, message,
                    Toast.LENGTH_LONG).show();

            return true;
        }
    }


    // Current User overlay
    public class WhereAmI extends MyLocationOverlay {

        private Context mContext;
        private Rect markerRect;

        public WhereAmI(Context context, MapView mapView) {
            super(context, mapView);
            mContext = context;
        }

        @Override
        protected void drawMyLocation(Canvas canvas, MapView mapView, Location lastFix, GeoPoint myLocation, long when) {
            // translate the GeoPoint to screen pixels
            Point screenPts = mapView.getProjection().toPixels(myLocation, null);

            // create a rotated copy of the marker
            Bitmap arrowBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.map_marker_my);
            Matrix matrix = new Matrix();
            Bitmap rotatedBmp = Bitmap.createBitmap(
                    arrowBitmap,
                    0, 0,
                    arrowBitmap.getWidth(),
                    arrowBitmap.getHeight(),
                    matrix,
                    true);
            // add the rotated marker to the canvas
            canvas.drawBitmap(
                    rotatedBmp,
                    screenPts.x - (rotatedBmp.getWidth() / 2),
                    screenPts.y - (rotatedBmp.getHeight() / 2),
                    null);

            markerRect = new Rect(screenPts.x - (rotatedBmp.getWidth() / 2), screenPts.y - (rotatedBmp.getHeight() / 2),
                    screenPts.x + (rotatedBmp.getWidth() / 2), screenPts.y + (rotatedBmp.getHeight() / 2));

            rotatedBmp.recycle();
        }

        @Override
        public boolean onTap(GeoPoint p, MapView map) {

            Point tapPts = mapView.getProjection().toPixels(p, null);

            if (markerRect == null || tapPts == null) {
                return false;
            }

            if (!markerRect.contains(tapPts.x, tapPts.y)) {
                return false;
            }

            Toast.makeText(MapActivity.this, "It's me!",
                    Toast.LENGTH_LONG).show();

            return true;
        }
    }
}