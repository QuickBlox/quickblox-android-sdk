package com.quickblox.sample.content;

import android.content.Context;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.quickblox.core.QBSettings;
import com.quickblox.sample.content.utils.Constants;
import com.quickblox.sample.core.CoreApp;

public class App extends CoreApp {

    @Override
    public void onCreate() {
        super.onCreate();

        QBSettings.getInstance().fastConfigInit(Constants.APP_ID, Constants.AUTH_KEY, Constants.AUTH_SECRET);

        initImageLoader(this);
    }

    public static void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by calling ImageLoaderConfiguration.createDefault(this)
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }
}
