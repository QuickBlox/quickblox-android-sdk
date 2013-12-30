package com.quickblox.snippets;

import com.quickblox.core.QBSettings;
import com.quickblox.module.auth.QBAuth;

/**
 * User: Oleg Soroka
 * Date: 01.10.12
 * Time: 19:30
 */
public class InitializeSnippets {


    public InitializeSnippets() {

        // App credentials from QB Admin Panel
        QBSettings.getInstance().fastConfigInit("99", "63ebrp5VZt7qTOv", "YavMAxm5T59-BRw");

        //specify custom domains
//        QBSettings.getInstance().setServerApiDomain(Config.SERVER_DOMAIN);
//        QBSettings.getInstance().setContentBucketName(Config.CONTENT_DOMAIN);
//        QBSettings.getInstance().setChatServerDomain(Config.CHAT_DOMAIN);
//        QBSettings.getInstance().setTurnServerDomain(Config.TURN_SERVER_DOMAIN);
        QBAuth.createSession(null);


    }
}