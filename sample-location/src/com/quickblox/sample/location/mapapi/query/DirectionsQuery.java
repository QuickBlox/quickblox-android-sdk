package com.quickblox.sample.location.mapapi.query;

/**
 * Created by vadim on 11.06.15.
 */
import com.google.android.gms.maps.model.LatLng;
import com.quickblox.core.query.JsonQuery;
import com.quickblox.core.rest.RestRequest;
import com.quickblox.sample.location.mapapi.query.model.Directions;

import java.util.Map;

public class DirectionsQuery extends JsonQuery<Directions>{

    private final LatLng source;
    private final LatLng dest;

    public DirectionsQuery(LatLng source, LatLng dest){

        this.source = source;
        this.dest = dest;
    }

    @Override
    protected void setUrl(RestRequest request) {
        super.setUrl(request);
    }

    @Override
    protected void setParams(RestRequest request) {
        super.setParams(request);
        Map<String, Object> parametersMap = request.getParameters();
        putValue(parametersMap, "origin", source.latitude +","+ source.longitude);
        putValue(parametersMap, "destination", dest.latitude + "," + dest.longitude);
        putValue(parametersMap, "sensor", false);
        putValue(parametersMap, "mode", "driving");
        putValue(parametersMap, "alternatives", true);
    }

    @Override
    protected String getUrl() {
        return "http://maps.googleapis.com/maps/api/directions/json";
    }


}

