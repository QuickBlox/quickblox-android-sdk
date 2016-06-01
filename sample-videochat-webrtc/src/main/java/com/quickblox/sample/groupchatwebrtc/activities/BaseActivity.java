package com.quickblox.sample.groupchatwebrtc.activities;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.View;

import com.quickblox.auth.QBAuth;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.sample.core.ui.activity.CoreBaseActivity;
import com.quickblox.sample.core.ui.dialog.ProgressDialogFragment;
import com.quickblox.sample.core.utils.ErrorUtils;
import com.quickblox.sample.core.utils.SharedPrefsHelper;
import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.utils.Consts;

import java.util.Date;

/**
 * QuickBlox team
 */
public abstract class BaseActivity extends CoreBaseActivity {

    SharedPrefsHelper sharedPrefsHelper;
    private String TOKEN = "token";
    private String DATE = "date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPrefsHelper = SharedPrefsHelper.getInstance();
        if (savedInstanceState != null) {
            restoreSession(savedInstanceState);
        }
    }

    //Todo Maybe set restore session logic to CoreBaseActivity?
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        try {
            savedInstanceState.putString(TOKEN, QBAuth.getBaseService().getToken());
            savedInstanceState.putSerializable(DATE, QBAuth.getBaseService().getTokenExpirationDate());
        } catch (BaseServiceException e) {
            e.printStackTrace();
        }
    }

    public void initDefaultActionBar() {
        String currentUserFullName = "";
        String currentRoomName = sharedPrefsHelper.get(Consts.PREF_CURREN_ROOM_NAME, "");

        if (sharedPrefsHelper.getQbUser() != null) {
            currentUserFullName = sharedPrefsHelper.getQbUser().getFullName();
        }

        setActionBarTitle(currentRoomName);
        setActionbarSubTitle(String.format(getString(R.string.logged_in_as), currentUserFullName));
    }


    public void setActionbarSubTitle(String subTitle) {
        if (actionBar != null)
            actionBar.setSubtitle(subTitle);
    }

    public void removeActionbarSubTitle() {
        if (actionBar != null)
            actionBar.setSubtitle(null);
    }

    void showProgressDialog(@StringRes int messageId) {
        ProgressDialogFragment.show(getSupportFragmentManager(), messageId);
    }

    void hideProgressDialog() {
        ProgressDialogFragment.hide(getSupportFragmentManager());
    }

    protected void showErrorSnackbar(@StringRes int resId, Exception e,
                                     View.OnClickListener clickListener) {
        if (getSnackbarAnchorView() != null) {
            ErrorUtils.showSnackbar(getSnackbarAnchorView(), resId, e,
                    com.quickblox.sample.core.R.string.dlg_retry, clickListener);
        }
    }

    public void restoreSession(Bundle savedInstanceState) {
        try {
            Log.d("BaseActivity", "restoreSession");
            String token = savedInstanceState.getString(TOKEN);
            Date date = (Date) savedInstanceState.getSerializable(DATE);

            QBAuth.createFromExistentToken(token, date);
        } catch (BaseServiceException e) {
            e.printStackTrace();
        }
    }

    protected abstract View getSnackbarAnchorView();
}




