package com.quickblox.sample.test;

import com.quickblox.core.QBSettings;

/**
 * Created with IntelliJ IDEA.
 * User: vfite
 * Date: 28.11.13
 * Time: 14:16
 * To change this template use File | Settings | File Templates.
 */
public class TestConfig {

    public static String USER_LOGIN = "Gerrit";

    public static String USER_PASSWORD = "qwerty123";

    public static final String[] APP_CONFIG = {


            "99",              // app id

            "63ebrp5VZt7qTOv",  // auth key

            "YavMAxm5T59-BRw"   // auth secret
    };


    public static void loadConfig() {

        loadGenericConfig(APP_CONFIG);

    }


    public static void loadGenericConfig(String[] config) {

        QBSettings.getInstance().setApplicationId(config[0]);

        QBSettings.getInstance().setAuthorizationKey(config[1]);

        QBSettings.getInstance().setAuthorizationSecret(config[2]);

        QBSettings.getInstance().setSynchronous(true);                        // by default FALSE

    }
}
