package com.quickblox.sample.videochat.conference.java.utils.qb;

import com.quickblox.users.model.QBUser;

import java.util.List;

public interface QBUsersHolder {

    void putUser(QBUser user);

    void putUsers(List<QBUser> users);

    QBUser getUserById(int userID);

    List<QBUser> getUsersByIds(List<Integer> usersIDs);

    boolean hasAllUsers(List<Integer> usersIDs);
}