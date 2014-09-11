package com.quickblox.videochatsample.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.videochat.core.QBVideoChatController;
import com.quickblox.module.videochat.model.listeners.OnQBVideoChatListener;
import com.quickblox.module.videochat.model.objects.CallState;
import com.quickblox.module.videochat.model.objects.CallType;
import com.quickblox.module.videochat.model.objects.VideoChatConfig;
import com.quickblox.module.videochat.model.utils.Debugger;
import com.quickblox.videochatsample.R;
import com.quickblox.videochatsample.model.DataHolder;
import com.quickblox.videochatsample.model.listener.OnCallDialogListener;
import com.quickblox.videochatsample.model.utils.DialogHelper;

import org.jivesoftware.smack.XMPPException;

public class ActivityCallUser extends Activity {

    private ProgressDialog progressDialog;
    private Button audioCallBtn;
    private Button videoCallBtn;
    private QBUser qbUser;
    private VideoChatConfig videoChatConfig;
    private TextView txtName;
    private AlertDialog alertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_layout);
        initViews();
    }

    private void initViews() {
        int userId = getIntent().getIntExtra("userId", 0);
        String myName = getIntent().getStringExtra("myName");
        qbUser = new QBUser(userId);

        // Setup UI
        //
        txtName = (TextView) findViewById(R.id.txtName);
        audioCallBtn = (Button) findViewById(R.id.audioCallBtn);
        videoCallBtn = (Button) findViewById(R.id.videoCallBtn);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                //TODO add stopCalling here, send Cancel message if need
//                XMPPSender.sendCancelCallMsg(videoChatConfig);
//                QBVideoChatController.getInstance().stopCalling();
            }
        });
        txtName.setText("You logged in as the " + myName);

        videoCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (progressDialog != null && !progressDialog.isShowing()) {
                    progressDialog.show();
                }
                videoChatConfig = QBVideoChatController.getInstance().callFriend(qbUser, CallType.VIDEO_AUDIO, null);
            }
        });

        audioCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (progressDialog != null && !progressDialog.isShowing()) {
                    progressDialog.show();
                }
                videoChatConfig = QBVideoChatController.getInstance().callFriend(qbUser, CallType.AUDIO, null);
            }
        });
    }

    private OnQBVideoChatListener qbVideoChatListener = new OnQBVideoChatListener() {

        @Override
        public void onVideoChatStateChange(CallState state, VideoChatConfig receivedVideoChatConfig) {
            Debugger.logConnection("onVideoChatStateChange: " + state);
            videoChatConfig = receivedVideoChatConfig;
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            switch (state) {
                case ACCEPT:
                    showCallDialog();
                    break;
                case ON_ACCEPT_BY_USER:
                    QBVideoChatController.getInstance().onAcceptFriendCall(videoChatConfig, null);
                    startVideoChatActivity();
                    break;
                case ON_REJECTED_BY_USER:
                    Toast.makeText(ActivityCallUser.this, "Rejected by user", Toast.LENGTH_SHORT).show();
                    break;
                case ON_DID_NOT_ANSWERED:
                    Toast.makeText(ActivityCallUser.this, "User did not answer", Toast.LENGTH_SHORT).show();
                    break;
                case ON_CANCELED_CALL:
                    videoChatConfig = null;
                    if (alertDialog != null && alertDialog.isShowing()){
                        alertDialog.dismiss();
                    }
                    autoCancelHandler.removeCallbacks(autoCancelTask);
                    break;
            }
        }
    };

    private Handler autoCancelHandler = new Handler(Looper.getMainLooper());
    private Runnable autoCancelTask = new Runnable() {
        @Override
        public void run() {
            if (alertDialog != null && alertDialog.isShowing()){
                alertDialog.dismiss();
            }
        }
    };

    private void showCallDialog() {
        autoCancelHandler.postDelayed(autoCancelTask, 30000);
        alertDialog = DialogHelper.showCallDialog(this, new OnCallDialogListener() {
            @Override
            public void onAcceptCallClick() {
                QBVideoChatController.getInstance().acceptCallByFriend(videoChatConfig, null);
                startVideoChatActivity();
                autoCancelHandler.removeCallbacks(autoCancelTask);
            }

            @Override
            public void onRejectCallClick() {
                QBVideoChatController.getInstance().rejectCall(videoChatConfig);
                autoCancelHandler.removeCallbacks(autoCancelTask);
            }
        });
    }

    @Override
    public void onResume() {
        try {
            QBVideoChatController.getInstance().setQBVideoChatListener(DataHolder.getInstance().getCurrentQbUser(), qbVideoChatListener);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        } catch (XMPPException e) {
            e.printStackTrace();
        }
        super.onResume();
    }


    private void startVideoChatActivity() {
        Intent intent = new Intent(getBaseContext(), ActivityVideoChat.class);
        intent.putExtra(VideoChatConfig.class.getCanonicalName(), videoChatConfig);
        startActivity(intent);
    }

}
