package com.quickblox.android.framework.snippets.modules;

import android.content.Context;
import com.quickblox.android.framework.base.definitions.QBCallback;
import com.quickblox.android.framework.base.net.results.Result;
import com.quickblox.android.framework.modules.location.models.QBLocation;
import com.quickblox.android.framework.modules.location.models.QBPlace;
import com.quickblox.android.framework.modules.location.net.result.QBLocationResult;
import com.quickblox.android.framework.modules.location.net.result.QBPlaceResult;
import com.quickblox.android.framework.modules.location.net.server.QBLocations;
import com.quickblox.android.framework.snippets.Snippet;
import com.quickblox.android.framework.snippets.Snippets;

/**
 * User: Oleg Soroka
 * Date: 02.10.12
 * Time: 20:29
 */
public class SnippetsLocations extends Snippets {

    public SnippetsLocations(Context context) {
        super(context);

        snippets.add(createLocation);
        snippets.add(getLocation);
        snippets.add(deleteLocation);
        snippets.add(createPlace);
        snippets.add(getPlace);
        snippets.add(deletePlace);
    }

    int locationId = 0;
    int placeId = 0;

    Snippet createLocation = new Snippet("create location") {
        @Override
        public void execute() {
            final QBLocation location = new QBLocation(35, 35, "hello");

            QBLocations.createLocation(location, new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);

                    if (result.isSuccess()) {
                        // 1 way to get new object -- get object from passed result
                        QBLocationResult locationResult = (QBLocationResult) result;
                        QBLocation newLocation = locationResult.getLocation();
                        System.out.println(">>> new location is: " + newLocation);

                        // and 2 way -- new data comes to base object passed to createLocation method
                        System.out.println(">>> new location is: " + location);

                        locationId = location.getId();
                    }
                }
            });
        }
    };

    Snippet getLocation = new Snippet("get location") {
        @Override
        public void execute() {
            if (locationId != 0) {
                QBLocation location = new QBLocation(locationId);

                QBLocations.getLocation(location, new QBCallback() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);

                        if (result.isSuccess()) {
                            // QBLocation object comes in result
                            QBLocationResult locationResult = (QBLocationResult) result;
                            QBLocation newLocation = locationResult.getLocation();
                        }
                    }
                });
            } else {
                System.out.println(">>> Create Location before retrieving.");
            }
        }
    };

    Snippet deleteLocation = new Snippet("delete location") {
        @Override
        public void execute() {
            if (locationId != 0) {
                QBLocation location = new QBLocation(locationId);

                QBLocations.deleteLocation(location, new QBCallback() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);

                        if (result.isSuccess()) {
                            // In delete methods comes empty result with success code
                            System.out.println(">>> success code: " + result.getStatusCode());
                        }
                    }
                });
            } else {
                System.out.println(">>> Create Location before deleting.");
            }
        }
    };

    /* Places */

    Snippet createPlace = new Snippet("create place") {
        @Override
        public void execute() {
            QBPlace place = new QBPlace(locationId, " the best place on the planet");

            QBLocations.createPlace(place, new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);

                    if (result.isSuccess()) {
                        QBPlaceResult placeResult = (QBPlaceResult) result;
                        QBPlace newPlace = placeResult.getPlace();
                        System.out.println(">> place: " + newPlace);

                        placeId = newPlace.getId();
                    }
                }
            });
        }
    };

    Snippet getPlace = new Snippet("get place") {
        @Override
        public void execute() {
            if (placeId != 0) {
                QBPlace place = new QBPlace(placeId);

                QBLocations.getPlace(place, new QBCallback() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);
                    }
                });
            } else {
                System.out.println(">>> Create Place before retrieving.");
            }
        }
    };

    Snippet deletePlace = new Snippet("delete place") {
        @Override
        public void execute() {
            if (placeId != 0) {
                QBPlace place = new QBPlace(placeId);

                QBLocations.deletePlace(place, new QBCallback() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);
                    }
                });
            } else {
                System.out.println(">>> Create Place before deleting.");
            }
        }
    };
}