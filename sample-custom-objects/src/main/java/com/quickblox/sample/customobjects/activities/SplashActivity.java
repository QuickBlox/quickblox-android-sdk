package com.quickblox.sample.customobjects.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.server.Performer;
import com.quickblox.extensions.RxJavaPerformProcessor;
import com.quickblox.sample.core.ui.activity.CoreSplashActivity;
import com.quickblox.sample.core.utils.configs.AppConfigParser;
import com.quickblox.sample.customobjects.R;
import com.quickblox.sample.customobjects.utils.Consts;
import com.quickblox.sample.customobjects.utils.QBCustomObjectsUtils;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SplashActivity extends CoreSplashActivity {
    private static final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initAppConfig();
    }

    private void initAppConfig() {
        String userLogin;
        String userPassword;

        try {
            JSONObject appConfigs = new AppConfigParser().getAppConfigsAsJson(Consts.APP_CONFIG_FILE_NAME);
            userLogin = appConfigs.getString(Consts.USER_LOGIN_FIELD_NAME);
            userPassword = appConfigs.getString(Consts.USER_PASSWORD_FIELD_NAME);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            showSnackbarError(null, R.string.init_configs_error, null, null);
            return;
        }

        signInQB(new QBUser(userLogin, userPassword));
    }

    private void signInQB(final QBUser qbUser) {
        if (!checkSignIn()) {
            Performer<QBUser> performer = QBUsers.signIn(qbUser);
            Observable<QBUser> observable =
                    performer.convertTo(RxJavaPerformProcessor.INSTANCE);

            observable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<QBUser>() {
                @Override
                public void onCompleted() {
                }

                @Override
                public void onError(Throwable e) {
                    if (QBCustomObjectsUtils.checkQBException(e)) {
                        showSnackbarError(null, R.string.splash_create_session_error, (QBResponseException) e, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                signInQB(qbUser);
                            }
                        });
                    } else {
                        Log.d(TAG, "onError" + e.getMessage());
                    }
                }

                @Override
                public void onNext(QBUser qbUser) {
                    proceedToTheNextActivity();
                }
            });
        } else {
            proceedToTheNextActivityWithDelay();
        }
    }

    private boolean checkSignIn() {
        return QBSessionManager.getInstance().getSessionParameters() != null;
    }

    @Override
    protected String getAppName() {
        return getString(R.string.splash_app_title);
    }

    @Override
    protected void proceedToTheNextActivity() {
        MovieListActivity.start(this);
        finish();
    }
}