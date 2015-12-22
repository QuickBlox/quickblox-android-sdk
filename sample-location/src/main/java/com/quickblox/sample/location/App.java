package com.quickblox.sample.location;

import com.quickblox.core.QBSettings;
import com.quickblox.sample.core.CoreApp;
import com.quickblox.sample.location.utils.Constants;

public class App extends CoreApp {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize QuickBlox application with credentials.
        QBSettings.getInstance().fastConfigInit(Constants.APP_ID, Constants.AUTH_KEY, Constants.AUTH_SECRET);
    }
}
