package com.quickblox.sample.chat.utils;

import com.quickblox.sample.chat.R;
import com.quickblox.sample.core.utils.ResourceUtils;

public interface Consts {

    String APP_CONFIG_FILE_NAME = "app_config.json";

    int PREFERRED_IMAGE_SIZE_PREVIEW = ResourceUtils.getDimen(R.dimen.chat_attachment_preview_size);
    int PREFERRED_IMAGE_SIZE_FULL = ResourceUtils.dpToPx(320);
}