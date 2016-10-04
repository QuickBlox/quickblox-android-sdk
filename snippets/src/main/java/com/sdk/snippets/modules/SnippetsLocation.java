package com.sdk.snippets.modules;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.Consts;
import com.quickblox.location.QBLocations;
import com.quickblox.location.model.QBEnvironment;
import com.quickblox.location.model.QBLocation;
import com.quickblox.location.request.QBLocationRequestBuilder;
import com.sdk.snippets.core.SnippetAsync;
import com.sdk.snippets.core.Snippet;
import com.sdk.snippets.core.Snippets;

import java.util.ArrayList;

/**
 * Created by vfite on 10.02.14.
 */
public class SnippetsLocation extends Snippets {
    private static final String TAG = SnippetsLocation.class.getSimpleName();

    public SnippetsLocation(Context context) {
        super(context);

        snippets.add(createLocation);
        snippets.add(createLocationSynchronous);
        //
        snippets.add(createLocationWithPush);
        snippets.add(createLocationWithPushSynchronous);
        //
        snippets.add(getLocationWithId);
        snippets.add(getLocationWithIdSynchronous);
        //
        snippets.add(updateLocation);
        snippets.add(updateLocationSynchronous);
        //
        snippets.add(deleteLocationWithId);
        snippets.add(deleteLocationWithIdSynchronous);
        //
        snippets.add(getLocations);
        snippets.add(getLocationsSynchronous);
        //
        snippets.add(deleteLocations);
        snippets.add(deleteLocationsSynchronous);
    }


    //
    ////////////////////////////////////////// Create Location /////////////////////////////////////
    //


    Snippet createLocation = new Snippet("create location") {
        @Override
        public void execute() {
            double latitude = 25.3433;
            double longitude = -5.3454;
            String status = "Checked here!";
            //
            final QBLocation location = new QBLocation(latitude, longitude, status);

            QBLocations.createLocation(location).performAsync(new QBEntityCallback<QBLocation>() {

                @Override
                public void onSuccess(QBLocation qbLocation, Bundle args) {
                    Log.i(TAG, "Location is: " + qbLocation);
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet createLocationSynchronous = new SnippetAsync("create location (synchronous)", context) {
        @Override
        public void executeAsync() {
            double latitude = 25.3433;
            double longitude = -5.3454;
            String status = "Checked here!";
            //
            final QBLocation location = new QBLocation(latitude, longitude, status);

            QBLocation locationResult = null;
            try {
                locationResult = QBLocations.createLocation(location).perform();
            } catch (QBResponseException e) {
                setException(e);
            }
            if (locationResult != null) {
                Log.i(TAG, "Location is: " + locationResult);
            }
        }
    };


    //
    //////////////////////////////////// Create Location with push /////////////////////////////////
    //


    Snippet createLocationWithPush = new Snippet("create location with push") {
        @Override
        public void execute() {
            double latitude = 25.3433;
            double longitude = -5.3454;
            String status = "Checked here!";
            //
            final QBLocation location = new QBLocation(latitude, longitude, status);

            QBLocations.createLocation(location, "Your friend is near!", QBEnvironment.DEVELOPMENT, 1000).performAsync(
                    new QBEntityCallback<QBLocation>() {

                        @Override
                        public void onSuccess(QBLocation qbLocation, Bundle args) {
                            Log.i(TAG, "Location is: " + qbLocation);
                        }

                        @Override
                        public void onError(QBResponseException errors) {
                            handleErrors(errors);
                        }
                    });
        }
    };

    Snippet createLocationWithPushSynchronous = new SnippetAsync("create location with push (synchronous)", context) {
        @Override
        public void executeAsync() {
            double latitude = 25.3433;
            double longitude = -5.3454;
            String status = "Checked here!";
            //
            final QBLocation location = new QBLocation(latitude, longitude, status);

            QBLocation locationResult = null;
            try {
                locationResult = QBLocations.createLocation(location, "Your friend is near!", QBEnvironment.DEVELOPMENT, 1000).perform();
            } catch (QBResponseException e) {
                setException(e);
            }
            if (locationResult != null) {
                Log.i(TAG, "Location is: " + locationResult);
            }
        }
    };


    //
    /////////////////////////////////////// Get Location with ID ///////////////////////////////////
    //


    Snippet getLocationWithId = new Snippet("get location", "with ID") {
        @Override
        public void execute() {
            QBLocation location = new QBLocation(11308);
            QBLocations.getLocation(location).performAsync(new QBEntityCallback<QBLocation>() {

                @Override
                public void onSuccess(QBLocation qbLocation, Bundle args) {
                    Log.i(TAG, "Location is: " + qbLocation);
                    Log.i(TAG, "Location's user is: " + qbLocation.getUser());
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet getLocationWithIdSynchronous = new SnippetAsync("get location (synchronous)", "with ID", context) {
        @Override
        public void executeAsync() {
            QBLocation location = new QBLocation(11308);
            QBLocation locationResult = null;
            try {
                locationResult = QBLocations.getLocation(location).perform();
            } catch (QBResponseException e) {
                setException(e);
            }
            if (locationResult != null) {
                Log.i(TAG, "Location is: " + locationResult);
                Log.i(TAG, "Location's user is: " + locationResult.getUser());
            }
        }
    };


    //
    /////////////////////////////////////// Update Location ////////////////////////////////////////
    //


    Snippet updateLocation = new Snippet("update location") {
        @Override
        public void execute() {
            QBLocation qbLocation = new QBLocation();
            qbLocation.setId(1141748);
            qbLocation.setStatus("I'am at Pizza");

            QBLocations.updateLocation(qbLocation).performAsync(new QBEntityCallback<QBLocation>() {
                @Override
                public void onSuccess(QBLocation qbLocation, Bundle args) {
                    Log.i(TAG, "Location is: " + qbLocation);
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet updateLocationSynchronous = new SnippetAsync("update location (synchronous)", context) {
        @Override
        public void executeAsync() {
            QBLocation qbLocation = new QBLocation();
            qbLocation.setId(1141748);
            qbLocation.setStatus("I'am at Pizza");

            QBLocation locationResult = null;
            try {
                locationResult = QBLocations.updateLocation(qbLocation).perform();
            } catch (QBResponseException e) {
                setException(e);
            }
            if (locationResult != null) {
                Log.i(TAG, "Location is: " + locationResult);
            }
        }
    };


    //
    /////////////////////////////////// Delete Location with ID/////////////////////////////////////
    //


    Snippet deleteLocationWithId = new Snippet("delete location", "with id") {
        @Override
        public void execute() {
            QBLocation location = new QBLocation(1141748);

            QBLocations.deleteLocation(location).performAsync(new QBEntityCallback<Void>() {

                @Override
                public void onSuccess(Void result, Bundle bundle) {
                    Log.i(TAG, "Location was removed");
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet deleteLocationWithIdSynchronous = new SnippetAsync("update location (synchronous)", "with ID", context) {
        @Override
        public void executeAsync() {
            QBLocation location = new QBLocation(1141748);

            try {
                QBLocations.deleteLocation(location).perform();
                Log.i(TAG, "Location was removed");
            } catch (QBResponseException e) {
                setException(e);
            }
        }
    };


    //
    /////////////////////////////////////// Get Locations //////////////////////////////////////////
    //


    Snippet getLocations = new Snippet("get locations") {
        @Override
        public void execute() {
            double latitude = 25.3433;
            double longitude = -5.3454;
            int distanceInMeters = 1000;

            QBLocationRequestBuilder locationRequestBuilder = new QBLocationRequestBuilder();
//            locationRequestBuilder.setCreatedAt(1326471371);
//            locationRequestBuilder.setUserId(8330);
//            locationRequestBuilder.setUserIds(8330, 53779, 55022);
//            locationRequestBuilder.setUserName("testUser");
//            locationRequestBuilder.setUserExternalIds("987", "123456");
//            locationRequestBuilder.setMinCreatedAt(1326471371);
//            locationRequestBuilder.setMaxCreatedAt(1326471371);
//            locationRequestBuilder.setGeoRect(12.4, 7.4, 8.1, 1.2);
//
//            locationRequestBuilder.setSort(SortField.CREATED_AT, SortOrder.ASCENDING);
//            locationRequestBuilder.setSort(SortField.LATITUDE, SortOrder.ASCENDING);
//            locationRequestBuilder.setSort(SortField.LATITUDE, SortOrder.DESCENDING);
//
//            locationRequestBuilder.setLastOnly();
//            locationRequestBuilder.setHasStatus();
//            locationRequestBuilder.setCurrentPosition(lat1, lng1);

            locationRequestBuilder.setRadius(latitude, longitude, distanceInMeters);

            locationRequestBuilder.setPage(1);
            locationRequestBuilder.setPerPage(10);

            Bundle bundle = new Bundle();

            QBLocations.getLocations(locationRequestBuilder, bundle).performAsync(new QBEntityCallback<ArrayList<QBLocation>>() {

                @Override
                public void onSuccess(ArrayList<QBLocation> locations, Bundle params) {
                    Log.i(TAG, ">>> Locations:" + locations.toString());
                    Log.i(TAG, ">>> currentPage: " + params.getInt(Consts.CURR_PAGE));
                    Log.i(TAG, ">>> perPage: " + params.getInt(Consts.PER_PAGE));
                    Log.i(TAG, ">>> totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet getLocationsSynchronous = new SnippetAsync("get locations (synchronous)", context) {
        @Override
        public void executeAsync() {
            double latitude = 25.3433;
            double longitude = -5.3454;
            int distanceInMeters = 1000;

            QBLocationRequestBuilder locationRequestBuilder = new QBLocationRequestBuilder();
//            locationRequestBuilder.setCreatedAt(1326471371);
//            locationRequestBuilder.setUserId(8330);
//            locationRequestBuilder.setUserIds(8330, 53779, 55022);
//            locationRequestBuilder.setUserName("testUser");
//            locationRequestBuilder.setUserExternalIds("987", "123456");
//            locationRequestBuilder.setMinCreatedAt(1326471371);
//            locationRequestBuilder.setMaxCreatedAt(1326471371);
//            locationRequestBuilder.setGeoRect(borderPointLat1, borderPointLng1, borderPointLat2, borderPointLng2);
//
//            locationRequestBuilder.setSort(SortField.CREATED_AT, SortOrder.ASCENDING);
//            locationRequestBuilder.setSort(SortField.LATITUDE, SortOrder.ASCENDING);
//            locationRequestBuilder.setSort(SortField.LATITUDE, SortOrder.DESCENDING);
//
//            locationRequestBuilder.setLastOnly();
//            locationRequestBuilder.setHasStatus();
//            locationRequestBuilder.setCurrentPosition(lat1, lng1);

//            locationRequestBuilder.setRadius(latitude, longitude, distanceInMeters);

            locationRequestBuilder.setPage(1);
            locationRequestBuilder.setPerPage(10);

            Bundle params = new Bundle();
            ArrayList<QBLocation> locations = null;
            try {
                locations = QBLocations.getLocations(locationRequestBuilder, params).perform();
            } catch (QBResponseException e) {
                setException(e);
            }
            if (locations != null) {
                Log.i(TAG, ">>> Locations:" + locations.toString());
                Log.i(TAG, ">>> currentPage: " + params.getInt(Consts.CURR_PAGE));
                Log.i(TAG, ">>> perPage: " + params.getInt(Consts.PER_PAGE));
                Log.i(TAG, ">>> totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
            }
        }
    };


    //
    /////////////////////////////////////// Delete Locations ///////////////////////////////////////
    //


    Snippet deleteLocations = new Snippet("delete locations") {
        @Override
        public void execute() {
            int days = 2;

            QBLocations.deleteObsoleteLocations(days).performAsync(new QBEntityCallback<Void>() {

                @Override
                public void onSuccess(Void result, Bundle bundle) {
                    Log.i(TAG, ">>> Delete locations OK ");
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet deleteLocationsSynchronous = new SnippetAsync("delete locations (synchronous)", context) {
        @Override
        public void executeAsync() {
            int days = 2;

            try {
                QBLocations.deleteObsoleteLocations(days).perform();
                Log.i(TAG, "Locations was removed");
            } catch (QBResponseException e) {
                setException(e);
            }
        }
    };
}
