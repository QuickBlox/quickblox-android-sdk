package com.quickblox.sample.chat.java.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.listeners.QBMessageStatusListener;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.chat.java.R;
import com.quickblox.sample.chat.java.ui.adapter.UsersAdapter;
import com.quickblox.sample.chat.java.utils.ToastUtils;
import com.quickblox.sample.chat.java.utils.chat.ChatHelper;
import com.quickblox.sample.chat.java.utils.qb.QbUsersHolder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class MessageInfoActivity extends BaseActivity implements QBMessageStatusListener {
    private static final String TAG = MessageInfoActivity.class.getSimpleName();

    private static final String EXTRA_MESSAGE = "extra_message";
    private static final String EXTRA_MSG_INFO_TYPE = "extra_message_info_type";
    public static final String MESSAGE_INFO_DELIVERED_TO = "delivered_to";
    public static final String MESSAGE_INFO_READ_BY = "read_by";

    private QBChatMessage chatMessage;
    private String messageInfoType;
    private ListView usersListView;
    private ArrayList<QBUser> deliveredUsers = new ArrayList<>();
    private ArrayList<QBUser> readUsers = new ArrayList<>();

    public static void start(Context context, QBChatMessage chatMessage, String messageInfoType) {
        Intent intent = new Intent(context, MessageInfoActivity.class);
        intent.putExtra(EXTRA_MESSAGE, chatMessage);
        intent.putExtra(EXTRA_MSG_INFO_TYPE, messageInfoType);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_info);

        chatMessage = (QBChatMessage) getIntent().getSerializableExtra(EXTRA_MESSAGE);
        messageInfoType = getIntent().getStringExtra(EXTRA_MSG_INFO_TYPE);
        usersListView = findViewById(R.id.list_chat_info_users);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (messageInfoType.equals(MESSAGE_INFO_DELIVERED_TO)) {
            fillByDeliveredUsers();
        } else if (messageInfoType.equals(MESSAGE_INFO_READ_BY)) {
            fillByReadUsers();
        }
    }

    @Override
    public void onResumeFinished() {
        super.onResumeFinished();
        try {
            QBChatService.getInstance().getMessageStatusesManager().addMessageStatusListener(this);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        QBChatService.getInstance().getMessageStatusesManager().removeMessageStatusListener(this);
    }

    private void fillByDeliveredUsers() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.message_info_delivered_to);
        }
        loadDeliveredUsers();
    }

    private void fillByReadUsers() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.message_info_read_by);
        }
        loadReadUsers();
    }

    private void loadDeliveredUsers() {
        final List<Integer> deliveredIDs = new ArrayList<>(chatMessage.getDeliveredIds());

        ChatHelper.getInstance().getUsersFromMessage(chatMessage, new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                if (qbUsers != null && qbUsers.size() > 0) {
                    for (QBUser user : qbUsers) {
                        if (deliveredIDs.contains(user.getId())) {
                            deliveredUsers.add(user);
                        }
                    }
                }
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setSubtitle(makeSubtitle(deliveredUsers.size()));
                }
                fillAdapter(deliveredUsers);
            }

            @Override
            public void onError(QBResponseException e) {
                showErrorSnackbar(R.string.select_users_get_users_error, e, null);
            }
        });
    }

    private void loadReadUsers() {
        final List<Integer> readIDs = new ArrayList<>(chatMessage.getReadIds());

        ChatHelper.getInstance().getUsersFromMessage(chatMessage, new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                if (qbUsers != null && qbUsers.size() > 0) {
                    for (QBUser user : qbUsers) {
                        if (readIDs.contains(user.getId())) {
                            readUsers.add(user);
                        }
                    }
                }
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setSubtitle(makeSubtitle(readUsers.size()));
                }
                fillAdapter(readUsers);
            }

            @Override
            public void onError(QBResponseException e) {
                showErrorSnackbar(R.string.select_users_get_users_error, e, null);
            }
        });
    }

    private String makeSubtitle(int usersSize) {
        String result = "";
        if (usersSize == 0) {
            result = getString(R.string.message_info_noone);
        } else if (usersSize == 1) {
            result = getString(R.string.message_info_single_user);
        } else {
            result = usersSize + " " + getString(R.string.message_info_multiple_users);
        }
        return result;
    }

    private void fillAdapter(ArrayList<QBUser> qbUsers) {
        usersListView.setAdapter(new UsersAdapter(this, qbUsers));
        if (messageInfoType.equals(MESSAGE_INFO_DELIVERED_TO) && getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(makeSubtitle(deliveredUsers.size()));
        }
        if (messageInfoType.equals(MESSAGE_INFO_READ_BY) && getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(makeSubtitle(readUsers.size()));
        }
    }

    @Override
    public void processMessageDelivered(String messageID, String dialogID, Integer userID) {
        if (messageInfoType.equals(MESSAGE_INFO_DELIVERED_TO) && dialogID.equals(chatMessage.getDialogId())
                && messageID.equals(chatMessage.getId()) && userID != null) {
            QBUser user = QbUsersHolder.getInstance().getUserById(userID);
            if (user != null) {
                deliveredUsers.add(user);
                fillAdapter(deliveredUsers);
            } else {
                QBUsers.getUser(userID).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        if (qbUser != null) {
                            deliveredUsers.add(qbUser);
                        }
                        fillAdapter(deliveredUsers);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        ToastUtils.shortToast(e.getMessage());
                        Log.d(TAG, e.getMessage());
                    }
                });
            }
        }
    }

    @Override
    public void processMessageRead(String messageID, String dialogID, Integer userID) {
        if (messageInfoType.equals(MESSAGE_INFO_READ_BY) && dialogID.equals(chatMessage.getDialogId())
                && messageID.equals(chatMessage.getId()) && userID != null) {
            QBUser user = QbUsersHolder.getInstance().getUserById(userID);
            if (user != null) {
                readUsers.add(user);
                fillAdapter(readUsers);
            } else {
                QBUsers.getUser(userID).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        if (qbUser != null) {
                            readUsers.add(qbUser);
                        }
                        fillAdapter(readUsers);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        ToastUtils.shortToast(e.getMessage());
                        Log.d(TAG, e.getMessage());
                    }
                });
            }
        }
    }
}