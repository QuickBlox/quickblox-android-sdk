package com.quickblox.sample.customobjects.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.server.Performer;
import com.quickblox.extensions.RxJavaPerformProcessor;
import com.quickblox.sample.core.ui.activity.CoreSplashActivity;
import com.quickblox.sample.core.utils.configs.CoreConfigUtils;
import com.quickblox.sample.customobjects.R;
import com.quickblox.sample.customobjects.utils.Consts;
import com.quickblox.sample.customobjects.utils.QBCustomObjectsUtils;
import com.quickblox.users.QBUsers;
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

        if (checkConfigsWithSnackebarError()){
            signInQB();
        }
    }

    private void signInQB() {
        if (!checkSignIn()) {
            QBUser qbUser = CoreConfigUtils.getUserFromConfig(Consts.SAMPLE_CONFIG_FILE_NAME);

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
                                signInQB();
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

    @Override
    protected String getAppName() {
        return getString(R.string.splash_app_title);
    }

    @Override
    protected void proceedToTheNextActivity() {
        MovieListActivity.start(this);
        finish();
    }

    @Override
    protected boolean sampleConfigIsCorrect() {
        boolean result = super.sampleConfigIsCorrect();
        result = result && CoreConfigUtils.getUserFromConfig(Consts.SAMPLE_CONFIG_FILE_NAME) != null;
        return result;
    }
}