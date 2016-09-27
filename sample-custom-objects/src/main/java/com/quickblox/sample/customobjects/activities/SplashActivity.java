package com.quickblox.sample.customobjects.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.server.Performer;
import com.quickblox.extensions.RxJavaPerformProcessor;
import com.quickblox.sample.core.ui.activity.CoreSplashActivity;
import com.quickblox.sample.customobjects.R;
import com.quickblox.sample.customobjects.utils.Consts;
import com.quickblox.sample.customobjects.utils.QBCustomObjectsUtils;
import com.quickblox.users.model.QBUser;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SplashActivity extends CoreSplashActivity {
    private static final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createSession();
    }

    private void createSession() {
        QBUser qbUser = new QBUser(Consts.USER_LOGIN, Consts.USER_PASSWORD);

        Performer<QBSession> performer = QBAuth.createSession(qbUser);
        Observable<QBSession> observable =
                performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<QBSession>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                if (QBCustomObjectsUtils.checkQBException(e)) {
                    showSnackbarError(null, R.string.splash_create_session_error, (QBResponseException) e, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            createSession();
                        }
                    });
                } else {
                    Log.d(TAG, "onError" + e.getMessage());
                }
            }

            @Override
            public void onNext(QBSession qbSession) {
                // session created
                proceedToTheNextActivity();
            }
        });
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