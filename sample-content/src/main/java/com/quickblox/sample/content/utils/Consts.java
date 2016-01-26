package com.quickblox.sample.content.utils;

import com.quickblox.sample.content.R;
import com.quickblox.sample.core.utils.ResourceUtils;

public final class Consts {

    public static final String APP_ID = "92";
    public static final String AUTH_KEY = "wJHdOcQSxXQGWx5";
    public static final String AUTH_SECRET = "BTFsj7Rtt27DAmT";

    public static final String USER_LOGIN = "supersample-ios";
    public static final String USER_PASSWORD = "supersample-ios";

    public static final int IMAGES_PER_PAGE = 80;
    public static final int START_PAGE = 1;

    public static final int PREFERRED_IMAGE_WIDTH_PREVIEW = ResourceUtils.getDimen(R.dimen.item_gallery_width);
    public static final int PREFERRED_IMAGE_HEIGHT_PREVIEW = ResourceUtils.getDimen(R.dimen.item_gallery_height);

    public static final int PREFERRED_IMAGE_WIDTH_FULL = ResourceUtils.dpToPx(320);
    public static final int PREFERRED_IMAGE_HEIGHT_FULL = ResourceUtils.dpToPx(320);

    public static final int PRIORITY_MAX_IMAGE_SIZE = 1024 * 1024 * 20;
}