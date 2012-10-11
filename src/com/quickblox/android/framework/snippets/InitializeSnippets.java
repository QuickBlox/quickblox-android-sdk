package com.quickblox.android.framework.snippets;

import com.quickblox.android.framework.base.models.QBSettings;
import com.quickblox.android.framework.modules.auth.net.server.QBAuth;

/**
 * User: Oleg Soroka
 * Date: 01.10.12
 * Time: 19:30
 */
public class InitializeSnippets {

    public InitializeSnippets() {

        // App credentials from QB Admin Panel (http://admin.quickblox.com/apps/961/edit)
        // http://image.quickblox.com/5217b9d67bb30191f07f9ff385ed.injoit.png
        QBSettings.getInstance().fastConfigInit("961", "PBZxXW3WgGZtFZv", "vvHjRbVFF6mmeyJ");

        QBAuth.authorizeApp(null);
    }
}