package com.quickblox.snippets;

import com.quickblox.core.QBSettings;
import com.quickblox.core.TransferProtocol;

/**
 * User: Oleg Soroka
 * Date: 01.10.12
 * Time: 19:30
 */
public class InitializeSnippets {


    public InitializeSnippets() {

        // App credentials from QB Admin Panel
        QBSettings.getInstance().fastConfigInit(ApplicationConfig.AppID,
                    ApplicationConfig.AuthKey, ApplicationConfig.AuthSecret);
//
        // specify custom domains
        QBSettings.getInstance().setServerApiDomain(ApplicationConfig.ServerApiDomain);
        QBSettings.getInstance().setChatServerDomain(ApplicationConfig.ServerChatDomain);
        QBSettings.getInstance().setContentBucketName(ApplicationConfig.ContentBucket);

        QBSettings.getInstance().setTransferProtocol(TransferProtocol.HTTP);
    }
}