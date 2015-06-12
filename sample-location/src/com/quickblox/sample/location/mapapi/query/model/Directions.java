package com.quickblox.sample.location.mapapi.query.model;

import com.qb.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by vadim on 11.06.15.
 */
public class Directions {

    @SerializedName("routes")
    public ArrayList<RouteItem> route;

}
