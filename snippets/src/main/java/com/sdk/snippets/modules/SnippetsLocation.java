package com.sdk.snippets.modules;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.Consts;
import com.quickblox.location.QBLocations;
import com.quickblox.location.model.QBEnvironment;
import com.quickblox.location.model.QBLocation;
import com.quickblox.location.model.QBPlace;
import com.quickblox.location.request.QBLocationRequestBuilder;
import com.sdk.snippets.core.AsyncSnippet;
import com.sdk.snippets.core.Snippet;
import com.sdk.snippets.core.Snippets;

import java.util.ArrayList;
import java.util.List;

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
        //
        //
        snippets.add(createPlace);
        snippets.add(createPlaceSynchronous);
        //
        snippets.add(getPlaceWithId);
        snippets.add(getPlaceWithIdSynchronous);
        //
        snippets.add(updatePlace);
        snippets.add(updatePlaceSynchronous);
        //
        snippets.add(deletePlace);
        snippets.add(deletePlaceSynchronous);
        //
        snippets.add(getPlaces);
        snippets.add(getPlacesSynchronous);
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

            QBLocations.createLocation(location, new QBEntityCallbackImpl<QBLocation>() {

                @Override
                public void onSuccess(QBLocation qbLocation, Bundle args) {
                    Log.i(TAG, "Location is: " + qbLocation);
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet createLocationSynchronous = new AsyncSnippet("create location (synchronous)", context) {
        @Override
        public void executeAsync() {
            double latitude = 25.3433;
            double longitude = -5.3454;
            String status = "Checked here!";
            //
            final QBLocation location = new QBLocation(latitude, longitude, status);

            QBLocation locationResult = null;
            try {
                locationResult = QBLocations.createLocation(location);
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

            QBLocations.createLocation(location, new QBEntityCallbackImpl<QBLocation>() {

                @Override
                public void onSuccess(QBLocation qbLocation, Bundle args) {
                    Log.i(TAG, "Location is: " + qbLocation);
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            }, "Your friend is near!", QBEnvironment.DEVELOPMENT, 1000);
        }
    };

    Snippet createLocationWithPushSynchronous = new AsyncSnippet("create location with push (synchronous)", context) {
        @Override
        public void executeAsync() {
            double latitude = 25.3433;
            double longitude = -5.3454;
            String status = "Checked here!";
            //
            final QBLocation location = new QBLocation(latitude, longitude, status);

            QBLocation locationResult = null;
            try {
                locationResult = QBLocations.createLocation(location, "Your friend is near!", QBEnvironment.DEVELOPMENT, 1000);
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
            QBLocations.getLocation(location, new QBEntityCallbackImpl<QBLocation>() {

                @Override
                public void onSuccess(QBLocation qbLocation, Bundle args) {
                    Log.i(TAG, "Location is: " + qbLocation);
                    Log.i(TAG, "Location's user is: " + qbLocation.getUser());
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet getLocationWithIdSynchronous = new AsyncSnippet("get location (synchronous)", "with ID", context) {
        @Override
        public void executeAsync() {
            QBLocation location = new QBLocation(11308);
            QBLocation locationResult = null;
            try {
                locationResult = QBLocations.getLocation(location);
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

            QBLocations.updateLocation(qbLocation, new QBEntityCallbackImpl<QBLocation>() {
                @Override
                public void onSuccess(QBLocation qbLocation, Bundle args) {
                    Log.i(TAG, "Location is: " + qbLocation);
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet updateLocationSynchronous = new AsyncSnippet("update location (synchronous)", context) {
        @Override
        public void executeAsync() {
            QBLocation qbLocation = new QBLocation();
            qbLocation.setId(1141748);
            qbLocation.setStatus("I'am at Pizza");

            QBLocation locationResult = null;
            try {
                locationResult = QBLocations.updateLocation(qbLocation);
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

            QBLocations.deleteLocation(location, new QBEntityCallbackImpl<Void>() {

                @Override
                public void onSuccess() {
                    Log.i(TAG, "Location was removed");
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet deleteLocationWithIdSynchronous = new AsyncSnippet("update location (synchronous)", "with ID", context) {
        @Override
        public void executeAsync() {
            QBLocation location = new QBLocation(1141748);

            try {
                QBLocations.deleteLocation(location);
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

            QBLocations.getLocations(locationRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBLocation>>() {

                @Override
                public void onSuccess(ArrayList<QBLocation> locations, Bundle params) {
                    Log.i(TAG, ">>> Locations:" + locations.toString());
                    Log.i(TAG, ">>> currentPage: " + params.getInt(Consts.CURR_PAGE));
                    Log.i(TAG, ">>> perPage: " + params.getInt(Consts.PER_PAGE));
                    Log.i(TAG, ">>> totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet getLocationsSynchronous = new AsyncSnippet("get locations (synchronous)", context) {
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
                locations = QBLocations.getLocations(locationRequestBuilder, params);
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

            QBLocations.deleteObsoleteLocations(days, new QBEntityCallbackImpl<Void>() {

                @Override
                public void onSuccess() {
                    Log.i(TAG, ">>> Delete locations OK ");
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet deleteLocationsSynchronous = new AsyncSnippet("delete locations (synchronous)", context) {
        @Override
        public void executeAsync() {
            int days = 2;

            try {
                QBLocations.deleteObsoleteLocations(days);
                Log.i(TAG, "Locations was removed");
            } catch (QBResponseException e) {
                setException(e);
            }
        }
    };


    //
    ///////////////////////////////////////// Create Place /////////////////////////////////////////
    //


    Snippet createPlace = new Snippet("create place") {
        @Override
        public void execute() {
            QBPlace place = new QBPlace();
            place.setDescription("Pizza house");
            place.setAddress("London bridge 17");
            place.setLocationId(1141759);
            place.setTitle("the best place on the planet");
            place.setPhotoId(212247);

            QBLocations.createPlace(place, new QBEntityCallbackImpl<QBPlace>() {

                @Override
                public void onSuccess(QBPlace qbPlace, Bundle params) {
                    Log.i(TAG, ">> Place: " + qbPlace);
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet createPlaceSynchronous = new AsyncSnippet("create place(synchronous)", context) {
        @Override
        public void executeAsync() {
            QBPlace place = new QBPlace();
            place.setDescription("Pizza house");
            place.setAddress("London bridge 17");
            place.setLocationId(1141762);
            place.setTitle("the best place on the planet");
            place.setPhotoId(212249);

            QBPlace newPlace = null;
            try {
                newPlace = QBLocations.createPlace(place);
            } catch (QBResponseException e) {
                setException(e);
            }

            if (newPlace != null) {
                Log.i(TAG, "Place is: " + newPlace);
            }
        }
    };


    //
    //////////////////////////////////////// Get Place with ID /////////////////////////////////////
    //


    Snippet getPlaceWithId = new Snippet("get place", "with ID") {
        @Override
        public void execute() {
            QBPlace place = new QBPlace(33261);

            QBLocations.getPlace(place, new QBEntityCallbackImpl<QBPlace>() {

                @Override
                public void onSuccess(QBPlace qbPlace, Bundle params) {
                    Log.i(TAG, ">> Place: " + qbPlace);
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet getPlaceWithIdSynchronous = new AsyncSnippet("get place(synchronous)", "with ID", context) {
        @Override
        public void executeAsync() {
            QBPlace place = new QBPlace(33261);

            QBPlace getPlace = null;
            try {
                getPlace = QBLocations.getPlace(place);
            } catch (QBResponseException e) {
                setException(e);
            }

            if (getPlace != null) {
                Log.i(TAG, "Place is: " + getPlace);
            }
        }
    };


    //
    ///////////////////////////////////// Update Place with ID /////////////////////////////////////
    //


    Snippet updatePlace = new Snippet("update place") {
        @Override
        public void execute() {

            QBPlace place = new QBPlace();
            place.setId(33261);
            place.setTitle("Great title");

            QBLocations.updatePlace(place, new QBEntityCallbackImpl<QBPlace>() {

                @Override
                public void onSuccess(QBPlace qbPlace, Bundle params) {
                    Log.i(TAG, ">> Place: " + qbPlace);
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet updatePlaceSynchronous = new AsyncSnippet("update place(synchronous)", context) {
        @Override
        public void executeAsync() {
            QBPlace place = new QBPlace();
            place.setId(33261);
            place.setTitle("Great title");

            QBPlace updatedPlace = null;
            try {
                updatedPlace = QBLocations.updatePlace(place);
            } catch (QBResponseException e) {
                setException(e);
            }

            if (updatedPlace != null) {
                Log.i(TAG, "Place is: " + updatedPlace);
            }
        }
    };


    //
    /////////////////////////////////////// Delete Place ///////////////////////////////////////////
    //


    Snippet deletePlace = new Snippet("delete place") {
        @Override
        public void execute() {
            QBPlace place = new QBPlace(33261);

            QBLocations.deletePlace(place, new QBEntityCallbackImpl<Void>(){
                @Override
                public void onSuccess() {
                    Log.i(TAG, ">> Place was deleted");
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet deletePlaceSynchronous = new AsyncSnippet("delete place(synchronous)", context) {
        @Override
        public void executeAsync() {
            QBPlace place = new QBPlace(33261);

            try {
                QBLocations.deletePlace(place);
                Log.i(TAG, "Place deleted");
            } catch (QBResponseException e) {
                setException(e);
            }
        }
    };


    //
    /////////////////////////////////////// Get Places /////////////////////////////////////////////
    //


    Snippet getPlaces = new Snippet("get places") {
        @Override
        public void execute() {
            QBLocations.getPlaces(new QBEntityCallbackImpl<ArrayList<QBPlace>>() {

                @Override
                public void onSuccess(ArrayList<QBPlace> qbPlaces, Bundle args) {
                    Log.i(TAG, ">>> Places:" + qbPlaces);
                    Log.i(TAG, ">>> currentPage: " + args.getInt(Consts.CURR_PAGE));
                    Log.i(TAG, ">>> perPage: " + args.getInt(Consts.PER_PAGE));
                    Log.i(TAG, ">>> totalPages: " + args.getInt(Consts.TOTAL_ENTRIES));
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet getPlacesSynchronous = new AsyncSnippet("get places (synchronous)", context) {
        @Override
        public void executeAsync() {
            Bundle params = new Bundle();
            ArrayList<QBPlace> places = null;
            try {
                places = QBLocations.getPlaces(params);
            } catch (QBResponseException e) {
               setException(e);
            }
            if (places != null) {
                Log.i(TAG, ">>> Places:" + places.toString());
                Log.i(TAG, ">>> currentPage: " + params.getInt(Consts.CURR_PAGE));
                Log.i(TAG, ">>> perPage: " + params.getInt(Consts.PER_PAGE));
                Log.i(TAG, ">>> totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
            }
        }
    };
}
