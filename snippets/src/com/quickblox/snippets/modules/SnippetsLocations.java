package com.quickblox.snippets.modules;

import android.content.Context;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.module.locations.QBLocations;
import com.quickblox.module.locations.model.QBLocation;
import com.quickblox.module.locations.model.QBPlace;
import com.quickblox.module.locations.request.QBLocationRequestBuilder;
import com.quickblox.module.locations.result.QBLocationPagedResult;
import com.quickblox.module.locations.result.QBLocationResult;
import com.quickblox.module.locations.result.QBPlacePagedResult;
import com.quickblox.module.locations.result.QBPlaceResult;
import com.quickblox.snippets.Snippet;
import com.quickblox.snippets.Snippets;

/**
 * User: Oleg Soroka
 * Date: 02.10.12
 * Time: 20:29
 */
public class SnippetsLocations extends Snippets {

    public SnippetsLocations(Context context) {
        super(context);

        snippets.add(createLocation);
        snippets.add(getLocationWithId);
        snippets.add(deleteLocationWithId);
        snippets.add(createPlace);
        snippets.add(getPlaceWithId);
        snippets.add(deletePlace);
        snippets.add(getPlaces);
        snippets.add(updatePlace);
        snippets.add(getLocations);
        snippets.add(updateLocation);
        snippets.add(deleteLocations);
    }

    Snippet createLocation = new Snippet("create location") {
        @Override
        public void execute() {
            final QBLocation location = new QBLocation(35, 35, "hello");

            QBLocations.createLocation(location, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {

                    if (result.isSuccess()) {
                        QBLocationResult locationResult = (QBLocationResult) result;

                        System.out.println(">>> Location is: " + locationResult.getLocation());
                    } else{
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getLocationWithId = new Snippet("get location with id") {
        @Override
        public void execute() {
            QBLocation location = new QBLocation(4223);
            QBLocations.getLocation(location, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBLocationResult locationResult = (QBLocationResult) result;

                        System.out.println(">>> Location is: " + locationResult.getLocation());
                    } else{
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet deleteLocationWithId = new Snippet("delete location with id") {
        @Override
        public void execute() {
            QBLocation location = new QBLocation(4233);

            QBLocations.deleteLocation(location, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        System.out.println(">>> Delete location OK ");
                    } else{
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
            QBLocations.getLocations(qbLocationRequestBuilder, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBLocationPagedResult qbLocationPagedResult = (QBLocationPagedResult) result;
                        System.out.println(">>> Locations:" + qbLocationPagedResult.getLocations().toString());
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
            qbLocation.setId(432);
            qbLocation.setStatus("I'am at Pizza");
            QBLocations.updateLocation(qbLocation, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBLocationResult locationResult = (QBLocationResult) result;

                        System.out.println(">>> Location is: " + locationResult.getLocation());
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
                        System.out.println(">>> Delete locations OK ");
                    } else{
                        handleErrors(result);
                    }
                }
            });
        }
    };

    /* Places */

    Snippet createPlace = new Snippet("create place") {
        @Override
        public void execute() {
            QBPlace place = new QBPlace();
            place.setLocationId(412);
            place.setTitle("the best place on the planet");
            place.setPhotoId(542);

            QBLocations.createPlace(place, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBPlaceResult placeResult = (QBPlaceResult) result;

                        System.out.println(">> Place: " + placeResult.getPlace());
                    } else{
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getPlaceWithId = new Snippet("get place") {
        @Override
        public void execute() {
            QBPlace place = new QBPlace(432);

            QBLocations.getPlace(place, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBPlaceResult placeResult = (QBPlaceResult) result;

                        System.out.println(">> Place: " + placeResult.getPlace());
                    } else{
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet deletePlace = new Snippet("delete place") {
        @Override
        public void execute() {
            QBPlace place = new QBPlace(433);

            QBLocations.deletePlace(place, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        System.out.println(">> Place was deleted");
                    } else{
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
                        System.out.println(">>> Places:" + qbPlacePagedResult.getLocations());
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
            QBLocation qbLocation = new QBLocation();
            qbLocation.setId(435);
            qbLocation.setLongitude(0.23);

            QBLocations.updateLocation(qbLocation, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    QBLocationResult locationResult = (QBLocationResult) result;
                    if (result.isSuccess()) {
                        System.out.println(">>> Location" + locationResult.getLocation());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };
}