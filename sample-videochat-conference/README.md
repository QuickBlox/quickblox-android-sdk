<!DOCTYPE HTML>

<html>

<body>
<h1 id="toc_0">Quickblox Android WebRTC Conference documentation.</h1>

<p>This project provides a brand new way for Quickblox WebRTC users to take part in video chats.
That means the user can create a dialog, add occupants there and join to it for video communication.</p>

<h2 id="toc_1">Getting started.</h2>

<p>Integrate QuickBlox Conference sdk in your application.
For using conference chat based on WEBRTC technology in your app, you must add dependency:</p>

<div><pre><code class="language-java">compile &quot;com.quickblox:quickblox-android-sdk-conference:3.8.1&quot;</code></pre></div>

<p>The next params can be set before start conference.</p>

<p><strong>ConferenceConfig</strong> class contains settings for conference:</p>

<div><pre><code class="language-java">ConferenceConfig.setPlugin(&quot;&quot;);
ConferenceConfig.setProtocol(&quot;&quot;);
ConferenceConfig.setUrl(&quot;&quot;);</code></pre></div>

<p>Sign in user, create <strong>QBDialogType.GROUP</strong> dialog with users you want to join and create conference session:</p>

<div><pre><code class="language-java">ConferenceClient client = ConferenceClient.getInstance(getApplicationContext());

// Create session with Video or Audio type conference
QBRTCTypes.QBConferenceType conferenceType = isVideoCall
           ? QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO
           : QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;

client.createSession(userID, conferenceType, new ConferenceEntityCallback<ConferenceSession>() {
     @Override
     public void onSuccess(ConferenceSession session) {
         CallActivity.start(context, dialogID);
     }

});</code></pre></div>

<p><strong>ConferenceClient</strong> instance - is a client model responsible for managing Conference session.</p>

<p>ConferenceClient has <strong>setAutoSubscribeAfterJoin</strong> option, which means your client will be subscribing to all online publisher after join to room. </p>

<p><strong>ConferenceSession</strong> - is a session with certain Dialog, managing all current processes. </p>

<p>Prepare your activity class to join in video room.</p>

<p>In order to have an ability to receive callbacks about current <strong>ConferenceSession</strong> instance state and conference events, you must implement appropriate interfaces:</p>

<p>For tracking connection state implement <strong>QBRTCSessionStateCallback</strong>:</p>

<div><pre><code class="language-java">currentSession.addSessionCallbacksListener(this);
currentSession.removeSessionCallbacksListener(this);
<br>
/**
* Called when session state is changed
*/
void onStateChanged(ConferenceSession session, BaseSession.QBRTCSessionState state);
<br>

/**
*Called in case when connection with opponent is established
*/
void onConnectedToUser(ConferenceSession session, Integer userID);

<br>
/**
* Called in case when opponent disconnected
*/
void onDisconnectedFromUser(ConferenceSession session, Integer userID);

<br>
/**
* Called in case when connection closed with certain user.
*/
void onConnectionClosedForUser(ConferenceSession session, Integer userID);</code></pre></div>

<p>For tracking conference events implement <strong>ConferenceSessionCallbacks</strong>:</p>

<div><pre><code class="language-java">currentSession.addConferenceSessionListener(this);
currentSession.removeConferenceSessionListener(this);

<br>
/**
* Called when some publisher - is a user, joined to the video room
*/
void onPublishersReceived(ArrayList&lt;Integer&gt; publishers);

<br>
/**
* Called when some publisher left room
*/
void onPublisherLeft(Integer userID);

<br>
/**
* Called when Media - audio or video type is received
*/
void onMediaReceived(String type, boolean success);

<br>
/**
* Called when slowLink is received. SlowLink with uplink=true means you notified several missing packets from server,
* while uplink=false means server is not receiving all your packets.
*/
void onSlowLinkReceived(boolean uplink, int nacks);

<br>
/**
* Called when received errors from server
*/
void onError(String error);

<br>
/**
* Called when ConferenceSession is closed
*/
void onSessionClosed(ConferenceSession session);</code></pre></div>

<p>For obtaining video and audio tracks implement interface <strong>QBRTCClientVideoTracksCallbacks</strong> and <strong>QBRTCClientAudioTracksCallbackRender</strong>.
For setting video track - the <strong>QBConferenceSurfaceView</strong> class is provided.</p>

<p><strong>Join to the room.</strong>

<p>You can join to room as a listener or as a publisher. As listener you subscribe only to the publishers, not giving own video and audio streams.</p>

<div><pre><code class="language-java">QBConferenceRole conferenceRole = asListenerRole ? QBConferenceRole.LISTENER : QBConferenceRole.PUBLISHER;</code></pre></div>

<div><pre><code class="language-java">currentSession.joinDialog(dialogID, conferenceRole, new QBEntityCallback&lt;ArrayList&lt;Integer&gt;&gt;());</code></pre></div>

<p>For subscribing to the active publisher:
<code>java
currentSession.subscribeToPublisher(publisherId);
</code></p>

<p>Note: You should subscribe to publishers only when session state becomes <strong>“connected”</strong>. Use <strong>“onStateChanged”</strong> callback method to track session states, as described in <strong>“sample-videochat-conference”</strong> code sample.
<br>If you are listener, then you can subscribe to publishers right after successful joinDialog.</br></p>

<p>For unsubscribing from publisher: </p>

<div><pre><code class="language-java">currentSession.unSubscribeFromPublisher(publisherId);</code></pre></div>

<p>To leave session: </p>

<div><pre><code class="language-java">currentSession.leave();</code></pre></div>

</body>

</html>