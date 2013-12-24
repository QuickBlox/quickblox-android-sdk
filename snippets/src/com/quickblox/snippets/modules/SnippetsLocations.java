package com.quickblox.snippets.modules;

import android.content.Context;
import android.util.Log;

import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.FileHelper;
import com.quickblox.module.locations.QBLocations;
import com.quickblox.module.locations.model.QBLocation;
import com.quickblox.module.locations.model.QBPlace;
import com.quickblox.module.locations.request.QBLocationRequestBuilder;
import com.quickblox.module.locations.result.QBLocationPagedResult;
import com.quickblox.module.locations.result.QBLocationResult;
import com.quickblox.module.locations.result.QBPlacePagedResult;
import com.quickblox.module.locations.result.QBPlaceResult;
import com.quickblox.snippets.R;
import com.quickblox.snippets.Snippet;
import com.quickblox.snippets.Snippets;

import java.io.File;
import java.io.InputStream;

/**
 * User: Oleg Soroka
 * Date: 02.10.12
 * Time: 20:29
 */
public class SnippetsLocations extends Snippets {

    private static final String TAG = SnippetsLocations.class.getSimpleName();

    // Coordinates for follow points
    // First point is SW, second is NE

    // SW point to build rect
    double borderPointLat1 = 49.990106;
    double borderPointLng1 = 36.185703;

    // NE point to build rect
    double borderPointLat2 = 50.043934;
    double borderPointLng2 = 36.28767;


    // Center point for radius area
    public static double lat1 = 50.010431;
    public static double lng1 = 36.224327;

    public static double lat2 = 50.004694;
    public static double lng2 = 36.240807;

    int distanceInMeters = 1338;

    public SnippetsLocations(Context context) {
        super(context);

        snippets.add(createLocation);
        snippets.add(getLocationWithId);
        snippets.add(updateLocation);
        snippets.add(deleteLocationWithId);

        snippets.add(getLocations);
        snippets.add(getLocationWithFilters);
        snippets.add(deleteLocations);

        snippets.add(createPlace);
        snippets.add(getPlaceWithId);
        snippets.add(updatePlace);
        snippets.add(deletePlace);
        snippets.add(getPlaces);

        snippets.add(createPlaceTask);
    }


    //
    ///////////////////////////////////////////// Location /////////////////////////////////////////////
    //
    Snippet createLocation = new Snippet("create location") {
        @Override
        public void execute() {
            final QBLocation location = new QBLocation(35, 35, "hello");

            QBLocations.createLocation(location, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {

                    if (result.isSuccess()) {
                        QBLocationResult locationResult = (QBLocationResult) result;

                        Log.i(TAG, ">>> Location is: " + locationResult.getLocation());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getLocationWithId = new Snippet("get location with id") {
        @Override
        public void execute() {
            QBLocation location = new QBLocation(11308);
            QBLocations.getLocation(location, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBLocationResult locationResult = (QBLocationResult) result;

                        Log.i(TAG, ">>> Location is: " + locationResult.getLocation());
                    } else {
                        handleErrors(result);
                    }
                }

            });
        }
    };

    Snippet updateLocation = new Snippet("update location") {
        @Override
        public void execute() {
            QBLocation qbLocation = new QBLocation();
            qbLocation.setId(89895);
            qbLocation.setStatus("I'am at Pizza");
            QBLocations.updateLocation(qbLocation, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBLocationResult locationResult = (QBLocationResult) result;

                        Log.i(TAG, ">>> Location is: " + locationResult.getLocation());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet deleteLocationWithId = new Snippet("delete location with id") {
        @Override
        public void execute() {
            QBLocation location = new QBLocation(89895);

            QBLocations.deleteLocation(location, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        Log.i(TAG, ">>> Delete location OK ");
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getLocations = new Snippet("get locations") {
        @Override
        public void execute() {
            QBLocationRequestBuilder qbLocationRequestBuilder = new QBLocationRequestBuilder();
            qbLocationRequestBuilder.setPerPage(10);
            qbLocationRequestBuilder.setPage(1);

            QBLocations.getLocations(qbLocationRequestBuilder, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBLocationPagedResult qbLocationPagedResult = (QBLocationPagedResult) result;
                        Log.i(TAG, ">>> Locations:" + qbLocationPagedResult.getLocations().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getLocationWithFilters = new Snippet("get locations with filters") {
        @Override
        public void execute() {
            QBLocationRequestBuilder locationRequestBuilder = new QBLocationRequestBuilder();
//            locationRequestBuilder.setCreatedAt(1326471371);
//            locationRequestBuilder.setUserId(8330);
//            locationRequestBuilder.setUserIds(8330, 53779, 55022);
//            locationRequestBuilder.setUserName("testUser");
//            locationRequestBuilder.setUserExternalIds("987", "123456");
//            locationRequestBuilder.setMinCreatedAt(1326471371);
//            locationRequestBuilder.setMaxCreatedAt(1326471371);
//            locationRequestBuilder.setGeoRect(borderPointLat1, borderPointLng1, borderPointLat2, borderPointLng2);
//            locationRequestBuilder.setRadius(lat1, lng1, distanceInMeters);
//
//            locationRequestBuilder.setSort(SortField.CREATED_AT, SortOrder.ASCENDING);
//            locationRequestBuilder.setSort(SortField.LATITUDE, SortOrder.ASCENDING);
//            locationRequestBuilder.setSort(SortField.LATITUDE, SortOrder.DESCENDING);

//            locationRequestBuilder.setLastOnly();
//            locationRequestBuilder.setHasStatus();
//            locationRequestBuilder.setCurrentPosition(lat1, lng1);
            locationRequestBuilder.setPage(1);
            locationRequestBuilder.setPerPage(10);


            QBLocations.getLocations(locationRequestBuilder, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBLocationPagedResult qbLocationPagedResult = (QBLocationPagedResult) result;


                        ;

                        Log.i(TAG, ">>> Locations:" + qbLocationPagedResult.getLocations().get(0).getUser());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet deleteLocations = new Snippet("delete locations") {
        @Override
        public void execute() {
            QBLocations.deleteObsoleteLocations(2, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        Log.i(TAG, ">>> Delete locations OK ");
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    //
    ///////////////////////////////////////////// Places /////////////////////////////////////////////
    //
    Snippet createPlace = new Snippet("create place") {
        @Override
        public void execute() {
            QBPlace place = new QBPlace();
            place.setDescription("asdasd");
            place.setAddress("asdad");
            place.setLocationId(88973);
            place.setTitle("the best place on the planet");
            place.setPhotoId(20012);

            QBLocations.createPlace(place, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBPlaceResult placeResult = (QBPlaceResult) result;

                        Log.i(TAG, ">> Place: " + placeResult.getPlace());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };


    Snippet getPlaceWithId = new Snippet("get place") {
        @Override
        public void execute() {
            QBPlace place = new QBPlace(1832);

            QBLocations.getPlace(place, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBPlaceResult placeResult = (QBPlaceResult) result;

                        Log.i(TAG, ">> Place: " + placeResult.getPlace());
                    } else {
                        handleErrors(result);
                    }
                }

            });
        }
    };

    Snippet updatePlace = new Snippet("update place") {
        @Override
        public void execute() {

            QBPlace place = new QBPlace();
            place.setId(1832);
            place.setTitle("Great title");

            QBLocations.updatePlace(place, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    QBPlaceResult qbPlaceResult = (QBPlaceResult) result;
                    if (result.isSuccess()) {
                        Log.i(TAG, ">>> Place" + qbPlaceResult.getPlace());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet deletePlace = new Snippet("delete place") {
        @Override
        public void execute() {
            QBPlace place = new QBPlace(1832);

            QBLocations.deletePlace(place, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        Log.i(TAG, ">> Place was deleted");
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getPlaces = new Snippet("get places") {
        @Override
        public void execute() {
            QBLocations.getPlaces(new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBPlacePagedResult qbPlacePagedResult = (QBPlacePagedResult) result;
                        Log.i(TAG, ">>> Places:" + qbPlacePagedResult.getPlaces());
                    } else {
                        handleErrors(result);
                    }
                }

            });
        }
    };

    //
    ///////////////////////////////////////////// Tasks /////////////////////////////////////////////
    //
    Snippet createPlaceTask = new Snippet("create place task") {
        @Override
        public void execute() {
            String placeTitle = "Kharkov city - all the best!";
            String placeDescription = "place description";
            String placeAddress = "Ukraine, Kharkov";
            double placeLongitude = -1.23;
            double placeLatitude = 1.23;

            int fileId = R.raw.kharkov;
            InputStream is = context.getResources().openRawResource(fileId);
            File placePhoto = FileHelper.getFileInputStream(is, "kharkov.jpg", "qb_snippets12");

            QBLocations.createPlaceTask(placeTitle, placeDescription, placeAddress, placeLongitude, placeLatitude, placePhoto, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBPlaceResult placeResult = (QBPlaceResult) result;

                        Log.i(TAG, ">> Place: " + placeResult.getPlace());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };
}