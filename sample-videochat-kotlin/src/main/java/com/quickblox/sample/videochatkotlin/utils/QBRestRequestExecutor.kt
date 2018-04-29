package com.quickblox.sample.videochatkotlin.utils

import com.quickblox.core.QBEntityCallback
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import java.util.*

/**
 * Created by Roman on 28.04.2018.
 */
fun loadUsersByLogins(usersLogins: Collection<String>, callback: QBEntityCallback<ArrayList<QBUser>>) {
    QBUsers.getUsersByLogins(usersLogins, null).performAsync(callback)
}