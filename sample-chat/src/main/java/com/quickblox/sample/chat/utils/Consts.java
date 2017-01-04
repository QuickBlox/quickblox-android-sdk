package com.quickblox.sample.chat.utils;

import com.quickblox.sample.chat.R;
import com.quickblox.sample.core.utils.ResourceUtils;

public interface Consts {

    String SAMPLE_CONFIG_FILE_NAME = "sample_config.json";

    int PREFERRED_IMAGE_SIZE_PREVIEW = ResourceUtils.getDimen(R.dimen.chat_attachment_preview_size);
    int PREFERRED_IMAGE_SIZE_FULL = ResourceUtils.dpToPx(320);
}