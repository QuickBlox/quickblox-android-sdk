package com.quickblox.sample.content.utils;

import com.quickblox.sample.content.R;
import com.quickblox.sample.core.utils.ResourceUtils;

public interface Consts {

    String APP_ID = "92";
    String AUTH_KEY = "wJHdOcQSxXQGWx5";
    String AUTH_SECRET = "BTFsj7Rtt27DAmT";
    String ACCOUNT_KEY = "rz2sXxBt5xgSxGjALDW6";

    String USER_LOGIN = "supersample-ios";
    String USER_PASSWORD = "supersample-ios";

    int PREFERRED_IMAGE_WIDTH_PREVIEW = ResourceUtils.getDimen(R.dimen.item_gallery_width);
    int PREFERRED_IMAGE_HEIGHT_PREVIEW = ResourceUtils.getDimen(R.dimen.item_gallery_height);

    int PREFERRED_IMAGE_WIDTH_FULL = ResourceUtils.dpToPx(320);
    int PREFERRED_IMAGE_HEIGHT_FULL = ResourceUtils.dpToPx(320);

    int PRIORITY_MAX_IMAGE_SIZE = 1024 * 1024 * 20;
}