package com.quickblox.sample.videochatkotlin.utils

import java.util.ArrayList

object StringUtils {

    fun createCompositeString(permissions: ArrayList<String>): String {
        val stringBuilder = StringBuilder()

        for (string in permissions) {
            stringBuilder.append(createHumanNameFromSystemPermission(string))
            if (permissions.indexOf(string) == permissions.size - 2) {
                stringBuilder.append(" and ")
            } else if (permissions.indexOf(string) == permissions.size - 1) {
                stringBuilder.append("")
            } else {
                stringBuilder.append(", ")
            }
        }

        return stringBuilder.toString()
    }

    fun createHumanNameFromSystemPermission(permission: String): String {
        val permissionName = permission.replace("android.permission.", "")
        val words = permissionName.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var newPermissionName = ""
        for (word in words) {
            newPermissionName += word.substring(0, 1) + word.substring(1).toLowerCase() + " "
        }

        return newPermissionName
    }
}