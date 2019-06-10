package com.quickblox.sample.videochat.conference.kotlin.activities

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.*
import android.media.AudioManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import com.quickblox.conference.*
import com.quickblox.conference.callbacks.ConferenceEntityCallback
import com.quickblox.conference.callbacks.ConferenceSessionCallbacks
import com.quickblox.sample.videochat.conference.kotlin.R
import com.quickblox.sample.videochat.conference.kotlin.fragments.AudioConversationFragment
import com.quickblox.sample.videochat.conference.kotlin.fragments.BaseConversationFragment
import com.quickblox.sample.videochat.conference.kotlin.fragments.ConversationFragmentCallbackListener
import com.quickblox.sample.videochat.conference.kotlin.fragments.VideoConversationFragment
import com.quickblox.sample.videochat.conference.kotlin.util.NetworkConnectionChecker
import com.quickblox.sample.videochat.conference.kotlin.utils.WebRtcSessionManager
import com.quickblox.sample.videochat.conference.kotlin.utils.setSettingsStrategy
import com.quickblox.sample.videochat.conference.kotlin.utils.shortToast
import com.quickblox.videochat.webrtc.*
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback
import org.webrtc.CameraVideoCapturer
import java.io.Serializable
import java.util.concurrent.CopyOnWriteArraySet

private const val CALL_ACTIVITY_CLOSE_WIFI_DISABLED = 1001
private const val WIFI_DISABLED = "wifi_disabled"
private const val EXTRA_DIALOG_ID = "dialog_id"
const val EXTRA_DIALOG_OCCUPANTS = "dialog_occupants"
const val EXTRA_AS_LISTENER = "as_listener"

private const val COUNTDOWN_TIME = 10000L
private const val COUNTDOWN_INTERVAL = 1000L

class CallActivity : BaseActivity(), QBRTCSessionStateCallback<ConferenceSession>, ConferenceSessionCallbacks,
        ConversationFragmentCallbackListener, NetworkConnectionChecker.OnConnectivityChangedListener {

    private var TAG = CallActivity::class.java.simpleName

    private lateinit var rtcClient: ConferenceClient
    private lateinit var audioManager: AppRTCAudioManager
    private lateinit var networkConnectionChecker: NetworkConnectionChecker
    private lateinit var connectionView: LinearLayout

    private var currentSession: ConferenceSession? = null
    private var onChangeDynamicCallback: OnChangeDynamicToggle? = null

    private var dialogID: String? = null
    private var opponentsIdsList: ArrayList<Int>? = null
    private var asListenerRole: Boolean = false
    private var isVideoCall: Boolean = false
    private var callStarted: Boolean = false
    private var previousDeviceEarPiece: Boolean = false
    private var showToastAfterHeadsetPlugged = true
    private val subscribedPublishers = CopyOnWriteArraySet<Int>()
    private val currentCallStateCallbackList = ArrayList<CurrentCallStateCallback>()

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var btAudioManager: AudioManager

    private lateinit var btCountDown: CountDown
    private lateinit var btBroadcastReceiver: BluetoothBroadcastReceiver
    private var isBTStarting: Boolean = false

    @Volatile
    private var connectedToJanus: Boolean = false
    private var readyToSubscribe: Boolean = false

    companion object {
        private const val ICE_FAILED_REASON = "ICE failed"
        private const val AUDIO_DEVICE_DELAY = 500L

        fun start(context: Context, dialogId: String, occupants: List<Int>, listenerRole: Boolean) {
            val intent = Intent(context, CallActivity::class.java)
            intent.putExtra(EXTRA_DIALOG_ID, dialogId)
            intent.putExtra(EXTRA_DIALOG_OCCUPANTS, occupants as Serializable)
            intent.putExtra(EXTRA_AS_LISTENER, listenerRole)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        parseIntentExtras()
        if (!currentSessionExist()) {
            finish()
            return
        }
        initCurrentSession(currentSession)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        btAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        initConferenceClient()
        initAudioManager()
        startBluetooth()
        initWiFiManagerListener()

        startConversationFragment()
    }

    override fun onResume() {
        super.onResume()
        readyToSubscribe = true
        subscribeToPublishersIfNeed()
        networkConnectionChecker.registerListener(this)
    }

    override fun onPause() {
        super.onPause()
        readyToSubscribe = false
        networkConnectionChecker.unregisterListener(this)
    }

    override fun onStop() {
        super.onStop()
        stopBluetooth()
    }

    private fun parseIntentExtras() {
        dialogID = intent.extras?.getString(EXTRA_DIALOG_ID)
        opponentsIdsList = intent.getSerializableExtra(EXTRA_DIALOG_OCCUPANTS) as ArrayList<Int>
        asListenerRole = intent.getBooleanExtra(EXTRA_AS_LISTENER, false)
    }

    private fun currentSessionExist(): Boolean {
        currentSession = WebRtcSessionManager.getCurrentSession()
        return currentSession != null
    }

    private fun initCurrentSession(session: ConferenceSession?) {
        if (session != null) {
            currentSession = session
            currentSession?.addSessionCallbacksListener(this)
            currentSession?.addConferenceSessionListener(this)
        }
    }

    private fun initConferenceClient() {
        rtcClient = ConferenceClient.getInstance(this)

        rtcClient.setCameraErrorHandler(object : CameraVideoCapturer.CameraEventsHandler {
            override fun onCameraError(message: String) {
                shortToast("Camera error: $message")
            }

            override fun onCameraDisconnected() {
                shortToast("Camera onCameraDisconnected: ")
            }

            override fun onCameraFreezed(message: String) {
                shortToast("Camera freezed: $message")
                if (currentSession != null) {
                    currentSession?.leave()
                }
            }

            override fun onCameraOpening(message: String) {
                shortToast("Camera opening: $message")
            }

            override fun onFirstFrameAvailable() {
                shortToast("onFirstFrameAvailable: ")
            }

            override fun onCameraClosed() {}
        })

        // Configure
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        setSettingsStrategy(opponentsIdsList as ArrayList<Int>,
                sharedPref as SharedPreferences, this)
        QBRTCConfig.setDebugEnabled(true)
    }

    private fun initAudioManager() {
        audioManager = AppRTCAudioManager.create(this)
        isVideoCall = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO == currentSession?.conferenceType

        if (isVideoCall) {
            audioManager.defaultAudioDevice = AppRTCAudioManager.AudioDevice.SPEAKER_PHONE
            previousDeviceEarPiece = false
            Log.d(TAG, "AppRTCAudioManager.AudioDevice.SPEAKER_PHONE")
        } else {
            audioManager.defaultAudioDevice = AppRTCAudioManager.AudioDevice.EARPIECE
            audioManager.setManageSpeakerPhoneByProximity(false)
            previousDeviceEarPiece = true
            Log.d(TAG, "AppRTCAudioManager.AudioDevice.EARPIECE")
        }

        audioManager.setOnWiredHeadsetStateListener { plugged, hasMicrophone ->
            if (callStarted) {
                shortToast("Headset " + if (plugged) "plugged" else "unplugged")
            }
            if (onChangeDynamicCallback != null) {
                if (!plugged) {
                    showToastAfterHeadsetPlugged = false
                    if (previousDeviceEarPiece) {
                        setAudioDeviceDelayed(AppRTCAudioManager.AudioDevice.EARPIECE)
                    } else {
                        setAudioDeviceDelayed(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE)
                    }
                }
                onChangeDynamicCallback?.enableDynamicToggle(plugged, previousDeviceEarPiece)
            }
        }

        audioManager.setBluetoothAudioDeviceStateListener { connected ->
            if (callStarted) {
                shortToast("Bluetooth " + if (connected) "connected" else "disconnected")
            }
        }
    }

    private fun setAudioDeviceDelayed(audioDevice: AppRTCAudioManager.AudioDevice) {
        Handler().postDelayed({
            showToastAfterHeadsetPlugged = true
            audioManager.setAudioDevice(audioDevice)
        }, AUDIO_DEVICE_DELAY)
    }

    private fun initWiFiManagerListener() {
        networkConnectionChecker = NetworkConnectionChecker(application)
    }

    private fun startConversationFragment() {
        val bundle = Bundle()
        bundle.putIntegerArrayList(EXTRA_DIALOG_OCCUPANTS, opponentsIdsList)
        bundle.putBoolean(EXTRA_AS_LISTENER, asListenerRole)
        val conversationFragment = BaseConversationFragment.newInstance(
                if (isVideoCall) {
                    VideoConversationFragment()
                } else {
                    AudioConversationFragment()
                })
        conversationFragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, conversationFragment,
                conversationFragment.javaClass.simpleName).commitAllowingStateLoss()
    }

    override fun onSlowLinkReceived(p0: Boolean, p1: Int) {

    }

    override fun onPublisherLeft(userId: Int?) {
        subscribedPublishers.remove(userId)
    }

    private fun subscribeToPublishers(publishersList: ArrayList<Int>) {
        subscribedPublishers.addAll(currentSession?.activePublishers as Iterable<Int>)
        for (publisher in publishersList) {
            currentSession?.subscribeToPublisher(publisher)
        }
    }

    override fun onPublishersReceived(publishersList: ArrayList<Int>?) {
        if (connectedToJanus && readyToSubscribe) {
            subscribedPublishers.addAll(publishersList as Iterable<Int>)
            subscribeToPublishers(publishersList)
        }
    }

    override fun onSessionClosed(session: ConferenceSession?) {
        if (session == currentSession) {
            audioManager.close()
            releaseCurrentSession()
            finish()
        }
    }

    fun releaseCurrentSession() {
        currentSession?.leave()
        currentSession?.removeSessionCallbacksListener(this)
        currentSession?.removeConferenceSessionListener(this)
        currentSession = null
    }

    override fun onConnectionClosedForUser(conferenceSession: ConferenceSession?, userId: Int?) {
        Log.d(TAG, "QBRTCSessionStateCallbackImpl onConnectionClosedForUser userID=" + userId)
    }

    override fun onConnectedToUser(conferenceSession: ConferenceSession?, userId: Int?) {
        callStarted = true
        notifyCallStateListenersCallStarted()
    }

    override fun onStateChanged(conferenceSession: ConferenceSession?, state: BaseSession.QBRTCSessionState?) {
        if (BaseSession.QBRTCSessionState.QB_RTC_SESSION_CONNECTED == state) {
            connectedToJanus = true
            subscribeToPublishersIfNeed()
        }
    }

    override fun onDisconnectedFromUser(p0: ConferenceSession?, p1: Int?) {

    }

    override fun onMediaReceived(p0: String?, p1: Boolean) {

    }

    override fun onError(exception: WsException?) {
        if (exception is WsHangUpException) {
            if (exception.message != null && exception.message == ICE_FAILED_REASON) {
                shortToast(exception.message!!)
                releaseCurrentSession()
                finish()
            }
        } else {
            val error = if (exception is WsNoResponseException) {
                getString(R.string.packet_failed)
            } else {
                exception?.message as String
            }
            shortToast(error)
        }
    }

    override fun addClientConnectionCallback(clientConnectionCallbacks: QBRTCSessionStateCallback<ConferenceSession>) {
        currentSession?.addSessionCallbacksListener(clientConnectionCallbacks)
    }

    override fun onSetAudioEnabled(isAudioEnabled: Boolean) {
        setAudioEnabled(isAudioEnabled)
    }

    override fun removeClientConnectionCallback(clientConnectionCallbacks: QBRTCSessionStateCallback<*>) {
        currentSession?.removeSessionCallbacksListener(clientConnectionCallbacks)
    }

    override fun addCurrentCallStateCallback(currentCallStateCallback: CallActivity.CurrentCallStateCallback) {
        currentCallStateCallbackList.add(currentCallStateCallback)
    }

    override fun removeCurrentCallStateCallback(currentCallStateCallback: CallActivity.CurrentCallStateCallback) {
        currentCallStateCallbackList.remove(currentCallStateCallback)
    }

    override fun addOnChangeDynamicToggle(onChangeDynamicCallback: OnChangeDynamicToggle) {
        this.onChangeDynamicCallback = onChangeDynamicCallback
    }

    override fun removeOnChangeDynamicToggle(onChangeDynamicCallback: OnChangeDynamicToggle) {
        this.onChangeDynamicCallback = null
    }

    private fun setAudioEnabled(isAudioEnabled: Boolean) {
        currentSession?.mediaStreamManager?.localAudioTrack?.setEnabled(isAudioEnabled)
    }

    override fun onSetVideoEnabled(isNeedEnableCam: Boolean) {
        setVideoEnabled(isNeedEnableCam)
    }

    private fun setVideoEnabled(isVideoEnabled: Boolean) {
        currentSession?.mediaStreamManager?.localVideoTrack?.setEnabled(isVideoEnabled)
    }

    override fun onSwitchAudio() {
        Log.v(TAG, "onSwitchAudio(), SelectedAudioDevice() = " + audioManager.selectedAudioDevice)

        if (audioManager.selectedAudioDevice != AppRTCAudioManager.AudioDevice.SPEAKER_PHONE) {
            audioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE)
        } else {
            if (audioManager.audioDevices.contains(AppRTCAudioManager.AudioDevice.BLUETOOTH)) {
                audioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.BLUETOOTH)
            } else if (audioManager.audioDevices.contains(AppRTCAudioManager.AudioDevice.WIRED_HEADSET)) {
                audioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.WIRED_HEADSET)
            } else {
                audioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE)
            }
        }
    }

    override fun onLeaveCurrentSession() {
        currentSession?.leave()
    }

    override fun onSwitchCamera(cameraSwitchHandler: CameraVideoCapturer.CameraSwitchHandler) {
        val cameraVideoCapturer = currentSession?.mediaStreamManager?.videoCapturer as QBRTCCameraVideoCapturer?
        cameraVideoCapturer?.switchCamera(cameraSwitchHandler)
    }

    override fun onStartJoinConference() {
        val userID = currentSession?.currentUserID
        val conferenceRole = if (asListenerRole) {
            QBConferenceRole.LISTENER
        } else {
            QBConferenceRole.PUBLISHER
        }
        currentSession?.joinDialog(dialogID, conferenceRole, JoinedCallback())
    }

    override fun connectivityChanged(availableNow: Boolean) {
        if (callStarted) {
            shortToast("Internet connection " + if (availableNow) "available" else " unavailable")
        }
    }

    private fun notifyCallStateListenersCallStarted() {
        for (callback in currentCallStateCallbackList) {
            callback.onCallStarted()
        }
    }

    private fun subscribeToPublishersIfNeed() {
        val notSubscribedPublishers = CopyOnWriteArraySet(currentSession?.activePublishers)
        notSubscribedPublishers.removeAll(subscribedPublishers)
        if (notSubscribedPublishers.isNotEmpty()) {
            subscribeToPublishers(ArrayList(notSubscribedPublishers))
        }
    }

    private fun startBluetooth() {
        Log.d(TAG, "startBluetooth")
        if (btAudioManager.isBluetoothScoAvailableOffCall) {
            btBroadcastReceiver = BluetoothBroadcastReceiver()
            registerReceiver(btBroadcastReceiver, IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED))
            registerReceiver(btBroadcastReceiver, IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED))
            registerReceiver(btBroadcastReceiver, IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED))

            // Need to set audio mode to MODE_IN_COMMUNICATION for call to startBluetoothSco() to succeed.
            btAudioManager.mode = AudioManager.MODE_IN_COMMUNICATION

            // CountDown repeatedly tries to start bluetooth Sco audio connection.
            btCountDown = CountDown()
            btCountDown.begin()

            // need for audio sco, see btBroadcastReceiver
            isBTStarting = true
        }
    }

    private fun stopBluetooth() {
        Log.d(TAG, "stopBluetooth")
        btCountDown.stop()

        // Need to stop Sco audio connection here when the app
        // change orientation or close with headset still turns on.
        try {
            unregisterReceiver(btBroadcastReceiver)
        } catch (ignored: IllegalArgumentException) {

        }
        btAudioManager.stopBluetoothSco()
        btAudioManager.mode = AudioManager.MODE_NORMAL
    }

    private inner class BluetoothBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action: String? = intent!!.action

            if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
                val connectedHeadset: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                val bluetoothClass: BluetoothClass? = connectedHeadset.bluetoothClass
                Log.d(TAG, connectedHeadset.name + "connected")

                bluetoothClass?.let {
                    // Check if device is a headset. Besides the 2 below, are there other
                    // device classes also qualified as headset?
                    val deviceClass: Int = it.deviceClass

                    if (deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE
                            || deviceClass == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET) {
                        // start bluetooth Sco audio connection.
                        // Calling startBluetoothSco() always returns fail here,
                        // that's why a count down timer is implemented to call
                        // startBluetoothSco() in the onTick.
                        btAudioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                        btCountDown.begin()
                    }
                }
            } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                Log.d(TAG, "Headset disconnected")
                btCountDown.stop()
                btAudioManager.mode = AudioManager.MODE_NORMAL

            } else if (action.equals(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)) {
                val state: Int = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_ERROR)
                // When the device is connected before the application starts,
                // ACTION_ACL_CONNECTED will not be received

                if (state == AudioManager.SCO_AUDIO_STATE_CONNECTED) {
                    Log.d(TAG, "Sco connected")
                    if (isBTStarting) {
                        isBTStarting = false
                    }
                    btCountDown.stop()

                } else if (state == AudioManager.SCO_AUDIO_STATE_DISCONNECTED) {
                    Log.d(TAG, "Sco disconnected")

                    // Always receive SCO_AUDIO_STATE_DISCONNECTED on call to startBluetooth()
                    // which at that stage we do not want to do anything. Thus the if condition.
                    if (!isBTStarting) {
                        // Need to call stopBluetoothSco(), otherwise startBluetoothSco()
                        // will not be successful.
                        btAudioManager.stopBluetoothSco()
                    }
                }
            }
        }
    }

    private inner class CountDown : CountDownTimer(COUNTDOWN_TIME, COUNTDOWN_INTERVAL) {
        var isCountingOn: Boolean = false

        fun begin() {
            isCountingOn = true
            start()
        }

        fun stop() {
            if (isCountingOn) {
                isCountingOn = false
                cancel()
            }
        }

        override fun onTick(millisUntilFinished: Long) {
            Log.d(TAG, "\nonTick start bluetooth Sco")
            // When this call is successful, this count down timer will be canceled.
            try {
                btAudioManager.startBluetoothSco()
            } catch (ignored: Exception) {

            }
        }

        override fun onFinish() {
            Log.d(TAG, "\nonFinish fail to connect to headset audio")
            // Calls to startBluetoothSco() in onTick are not successful.
            // Should implement something to inform user of this failure
            isCountingOn = false
            btAudioManager.mode = AudioManager.MODE_NORMAL
            btAudioManager
        }
    }

    private inner class JoinedCallback : ConferenceEntityCallback<ArrayList<Int>> {

        override fun onSuccess(publishers: ArrayList<Int>) {
            if (rtcClient.isAutoSubscribeAfterJoin) {
                subscribedPublishers.addAll(publishers)
            }
            if (asListenerRole) {
                connectedToJanus = true
            }
        }

        override fun onError(exception: WsException) {
            shortToast("Join exception: " + exception.message)
            releaseCurrentSession()
            finish()
        }
    }

    interface OnChangeDynamicToggle {
        fun enableDynamicToggle(plugged: Boolean, wasEarpiece: Boolean)
    }

    interface CurrentCallStateCallback {
        fun onCallStarted()
    }
}