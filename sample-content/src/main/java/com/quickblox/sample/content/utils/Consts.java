package com.quickblox.sample.content.utils;

import com.quickblox.sample.content.R;
import com.quickblox.sample.core.utils.ResourceUtils;

public interface Consts {

    String USER_LOGIN = "bobbobbob";
    String USER_PASSWORD = "bobbobbob";

    int PREFERRED_IMAGE_WIDTH_PREVIEW = ResourceUtils.getDimen(R.dimen.item_gallery_width);
    int PREFERRED_IMAGE_HEIGHT_PREVIEW = ResourceUtils.getDimen(R.dimen.item_gallery_height);

    int PREFERRED_IMAGE_WIDTH_FULL = ResourceUtils.dpToPx(320);
    int PREFERRED_IMAGE_HEIGHT_FULL = ResourceUtils.dpToPx(320);

    int PRIORITY_MAX_IMAGE_SIZE = 1024 * 1024 * 20;
}