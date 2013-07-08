package com.quickblox.snippets;

import com.quickblox.core.QBSettings;
import com.quickblox.core.TransferProtocol;
import com.quickblox.module.auth.QBAuth;

/**
 * User: Oleg Soroka
 * Date: 01.10.12
 * Time: 19:30
 */
public class InitializeSnippets {

    public InitializeSnippets() {

        // App credentials from QB Admin Panel
        QBSettings.getInstance().setServerDomain("stage.quickblox.com");
        QBSettings.getInstance().setTransferProtocol(TransferProtocol.HTTP);
        QBSettings.getInstance().fastConfigInit("22", "xGRzM9GDkuC5RB2", "bM3WqUc5Kyu3mWE");
//        QBSettings.getInstance().fastConfigInit("99", "63ebrp5VZt7qTOv", "YavMAxm5T59-BRw");
        QBAuth.createSession(null);
    }
}