package com.quickblox.sample.chat;

import com.quickblox.sample.chat.models.SampleConfigs;
import com.quickblox.sample.chat.utils.Consts;
import com.quickblox.sample.chat.utils.configs.AppConfigUtils;
import com.quickblox.sample.core.CoreApp;
import com.quickblox.sample.core.utils.ActivityLifecycle;

import java.io.IOException;

public class App extends CoreApp {
    private static final String TAG = App.class.getSimpleName();
    private static SampleConfigs sampleConfigs;

    @Override
    public void onCreate() {
        super.onCreate();
        ActivityLifecycle.init(this);
        initSampleConfigs();
    }

    private void initSampleConfigs() {
        try {
            sampleConfigs = AppConfigUtils.getConfigs(Consts.SAMPLE_CONFIG_FILE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static SampleConfigs getSampleConfigs() {
        return sampleConfigs;
    }
}