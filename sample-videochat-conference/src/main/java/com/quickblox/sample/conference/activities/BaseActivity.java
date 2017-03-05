package com.quickblox.sample.conference.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.KeyEvent;
import android.view.View;

import com.quickblox.sample.core.gcm.GooglePlayServicesHelper;
import com.quickblox.sample.core.ui.activity.CoreBaseActivity;
import com.quickblox.sample.core.utils.ErrorUtils;
import com.quickblox.sample.core.utils.SharedPrefsHelper;
import com.quickblox.sample.conference.App;
import com.quickblox.sample.conference.R;
import com.quickblox.sample.conference.util.QBResRequestExecutor;
import com.quickblox.sample.conference.utils.Consts;

/**
 * QuickBlox team
 */
public abstract class BaseActivity extends CoreBaseActivity {

    SharedPrefsHelper sharedPrefsHelper;
    private ProgressDialog progressDialog;
    protected GooglePlayServicesHelper googlePlayServicesHelper;
    protected QBResRequestExecutor requestExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestExecutor = App.getInstance().getQbResRequestExecutor();
        sharedPrefsHelper = SharedPrefsHelper.getInstance();
        googlePlayServicesHelper = new GooglePlayServicesHelper();
    }

    public void initDefaultActionBar() {
        String currentUserFullName = "";
        String currentRoomName = sharedPrefsHelper.get(Consts.PREF_CURREN_ROOM_NAME, "");

        if (sharedPrefsHelper.getQbUser() != null) {
            currentUserFullName = sharedPrefsHelper.getQbUser().getFullName();
        }

        setActionBarTitle(currentRoomName);
        setActionbarSubTitle(String.format(getString(R.string.subtitle_text_logged_in_as), currentUserFullName));
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
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);

            // Disable the back button
            DialogInterface.OnKeyListener keyListener = new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    return keyCode == KeyEvent.KEYCODE_BACK;
                }
            };
            progressDialog.setOnKeyListener(keyListener);
        }

        progressDialog.setMessage(getString(messageId));

        progressDialog.show();

    }

    void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    protected void showErrorSnackbar(@StringRes int resId, Exception e,
                                     View.OnClickListener clickListener) {
        if (getSnackbarAnchorView() != null) {
            ErrorUtils.showSnackbar(getSnackbarAnchorView(), resId, e,
                    com.quickblox.sample.core.R.string.dlg_retry, clickListener);
        }
    }

    protected abstract View getSnackbarAnchorView();
}




