# Overview
QuickBlox provides the Multiparty Video Conferencing Kotlin solution which allows to setup video conference between 10-12 people. It's built on top of WebRTC SFU technologies.

Multi-conference server is available only for **Enterprise** plans, with additional **fee**. Please refer to https://quickblox.com/developers/EnterpriseFeatures for more information and contacts.

# Features supported
* Video/Audio Conference with 10-12 people
* Join-Rejoin video room functionality (like Skype)
* Mute/Unmute audio/video stream (own and opponents)
* Display bitrate
* Switch video input device (camera)

# Getting started

<p>Integrate QuickBlox Conference sdk in your application.
For using conference chat based on WEBRTC technology in your app, you must add dependency:</p>

<div><pre><code class="language-java">compile &quot;com.quickblox:quickblox-android-sdk-conference:3.9.0&quot;</code></pre></div>

<p>The next params can be set before start conference.</p>

<p><strong>ConferenceConfig</strong> class contains settings for conference:</p>

<div><pre><code class="language-java">
ConferenceConfig.setPlugin(&quot;&quot;)
ConferenceConfig.setProtocol(&quot;&quot;)
ConferenceConfig.setUrl(&quot;&quot;)
</code></pre></div>

<p>Sign in user, create <strong>QBDialogType.GROUP</strong> dialog with users you want to join and create conference session:</p>

<div><pre><code class="language-java">
ConferenceClient client = ConferenceClient.getInstance(applicationContext)

<br>
// Create session with Video or Audio type conference
val conferenceType = if (isVideoCall) {
            QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO
        } else {
            QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO
        }
        
<br>
client.createSession(userID, conferenceType, object : ConferenceEntityCallback<ConferenceSession> {
            override fun onSuccess(session: ConferenceSession) {
            }
            override fun onError(responseException: WsException) { 
            }
        })
</code></pre></div>

<p><strong>ConferenceClient</strong> instance - is a client model responsible for managing Conference session.</p>

<p>ConferenceClient has <strong>setAutoSubscribeAfterJoin</strong> option, which means your client will be subscribing to all online publisher after join to room. </p>

<p><strong>ConferenceSession</strong> - is a session with certain Dialog, managing all current processes. </p>

<p>Prepare your activity class to join in video room.</p>

<p>In order to have an ability to receive callbacks about current <strong>ConferenceSession</strong> instance state and conference events, you must implement appropriate interfaces:</p>

<p>For tracking connection state implement <strong>QBRTCSessionStateCallback</strong>:</p>

<div><pre><code class="language-java">
currentSession?.addSessionCallbacksListener(this);
currentSession?.addSessionCallbacksListener(clientConnectionCallbacks)
currentSession?.removeSessionCallbacksListener(clientConnectionCallbacks)

<br>
/**
* Called when session state is changed
*/
fun onStateChanged(conferenceSession: ConferenceSession?, state: BaseSession.QBRTCSessionState?)

<br>
/**
*Called in case when connection with opponent is established
*/
fun onConnectedToUser(conferenceSession: ConferenceSession?, userId: Int?)

<br>
/**
* Called in case when opponent disconnected
*/
fun onDisconnectedFromUser(p0: ConferenceSession?, p1: Int?)

<br>
/**
* Called in case when connection closed with certain user.
*/
fun onConnectionClosedForUser(conferenceSession: ConferenceSession?, userId: Int?)
</code></pre></div>

<p>For tracking conference events implement <strong>ConferenceSessionCallbacks</strong>:</p>

<div><pre><code class="language-java">
currentSession?.addConferenceSessionListener(this);
currentSession?.removeConferenceSessionListener(this);

<br>
/**
* Called when some publisher - is a user, joined to the video room
*/
fun onPublishersReceived(publishersList: ArrayList<Int>?)

<br>
/**
* Called when some publisher left room
*/
fun onPublisherLeft(userId: Int?)

<br>
/**
* Called when Media - audio or video type is received
*/
fun onMediaReceived(p0: String?, p1: Boolean)

<br>
/**
* Called when slowLink is received. SlowLink with uplink=true means you notified several missing packets from server,
* while uplink=false means server is not receiving all your packets.
*/
fun onSlowLinkReceived(p0: Boolean, p1: Int)

<br>
/**
* Called when received errors from server
*/
fun onError(exception: WsException?)

<br>
/**
* Called when ConferenceSession is closed
*/
fun onSessionClosed(session: ConferenceSession?)
</code></pre></div>

<p>For obtaining video and audio tracks implement interface <strong>QBRTCClientVideoTracksCallbacks</strong> and <strong>QBRTCClientAudioTracksCallbackRender</strong>.
For setting video track - the <strong>QBConferenceSurfaceView</strong> class is provided.</p>

<p><strong>Join to the room.</strong>

<p>You can join to room as a listener or as a publisher. As listener you subscribe only to the publishers, not giving own video and audio streams.</p>

<div><pre><code class="language-java"> 
val conferenceRole = if (asListenerRole) {
    QBConferenceRole.LISTENER
} else {
    QBConferenceRole.PUBLISHER
}
</code></pre></div>

<div><pre><code class="language-java">
currentSession?.joinDialog(dialogID, conferenceRole, JoinedCallback())
</code></pre></div>

<p>For subscribing to the active publisher:
<code>java
currentSession?.subscribeToPublisher(publisherId);
</code></p>

<p>Note: You should subscribe to publishers only when session state becomes <strong>“connected”</strong>. Use <strong>“onStateChanged”</strong> callback method to track session states, as described in <strong>“sample-videochat-conference”</strong> code sample.
<br>If you are listener, then you can subscribe to publishers right after successful joinDialog.</br></p>

<p>For unsubscribing from publisher: </p>

<div><pre><code class="language-java">
currentSession?.unSubscribeFromPublisher(publisherId)
</code></pre></div>

<p>To leave session: </p>

<div><pre><code class="language-java">currentSession?.leave();</code></pre></div>

</body>

</html>
