package com.quickblox.sample.customobjects.utils;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.content.ContextCompat;
import android.widget.RatingBar;

import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.sample.customobjects.R;
import com.quickblox.sample.customobjects.definition.Consts;
import com.quickblox.sample.customobjects.model.Movie;

import java.util.HashMap;

public class QBCustomObjectsUtils {

    public static String parseField(String field, QBCustomObject customObject) {
        Object object = customObject.getFields().get(field);
        if (object != null) {
            return object.toString();
        }
        return null;
    }

    public static QBCustomObject createCustomObject(String title, String description, String year, float rating) {
        HashMap<String, Object> fields = new HashMap<>();
        fields.put(Movie.Contract.NAME, title);
        fields.put(Movie.Contract.DESCRIPTION, description);
        fields.put(Movie.Contract.YEAR, year);
        fields.put(Movie.Contract.RATING, rating);

        QBCustomObject qbCustomObject = new QBCustomObject();
        qbCustomObject.setClassName(Consts.CLASS_NAME);
        qbCustomObject.setFields(fields);

        return qbCustomObject;
    }

    public static void setTintRatingBar(Context context, RatingBar ratingBar) {
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(ContextCompat.getColor(context, R.color.color_accent), PorterDuff.Mode.SRC_ATOP);
    }
}