# QuickBlox Android SDK

This project contains QuickBlox Android SDK, that includes

  * [Core module](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-core) contains base classes and util components
  * [Chat Sample](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-chat)
  * [Video Chat WebRTC Sample](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-videochat-webrtc)
  * [Users Sample](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-users)
  * [Push Notifications Sample](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-pushnotifications)
  * [Location Sample](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-location)
  * [Custom Objects Sample](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-custom-objects)
  * [Content Sample](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-content)

# Overview 

QuickBlox  is Communication as a Service provider. The platform provides chat using the XMPP protocol, WebRTC signalling for video/voice calling and an API for sending push notifications. It provides a user management system, data storage and more. 

# Sample structure

Each sample depends from core module, which contains mutual dependencies such as CoreApp, BaseActivity, BaseListAdapter and other useful utils like a ImagePicker, KeyboardUtils, NotificationUtils, etc. Also core module keeps common resources  colors, strings, dimens and others. It makes code more clean and clear, and also more object-oriented. In addition the Samples have renewed up-to-date design.

# How to run samples

To run samples on Android Studio go to menu **File - Import Project**. Select path to sample, select **Use default gradle wrapper(recommended)** and click OK.

# Connect SDK to your existing apps 

To get the QuickBlox SDK project running you will need Android Studio and Maven installed.

The repository https://github.com/QuickBlox/quickblox-android-sdk-releases contains binary distributions of QuickBlox Android SDK and an instruction how to connect SDK to your project. Check it out.

# TUTORIAL  Customize Proguard in Android Studio (debug mode)

In build.gradle (for ex. Module: sample chat) you need to add next params:

buildTypes { <br />
   debug { <br />
       signingConfig signingConfigs.debug <br />
//     shrinkResources true //for more reducing code <br />
       minifyEnabled true <br />
       proguardFile 'proguard-rules.pro' <br />
       zipAlignEnabled false <br />
   } <br />
} <br />

Then, in your module root directory create proguard-rules.pro file and put there the next:

\##---------------Begin: proguard configuration for Gson  ---------- <br />
\# Gson uses generic type information stored in a class file when working with fields. Proguard <br />
\# removes such information by default, so configure it to keep all of it. <br />
-keepattributes Signature <br />

\# For using GSON @Expose annotation <br />
-keepattributes *Annotation* <br />

\# Gson specific classes <br />
-keep class sun.misc.Unsafe { *; } <br />
\#-keep class com.google.gson.stream.** { *; } <br />

\# Application classes that will be serialized/deserialized over Gson <br />
-keep class com.quickblox.core.account.model.** { *; } <br />


\##---------------End: proguard configuration for Gson  ---------- <br />
\##---------------Begin: proguard configuration for quickblox  ---------- <br />
\#quickblox sample chat <br />

-keep class com.quickblox.auth.parsers.** { *; } <br />
-keep class com.quickblox.auth.model.** { *; } <br />
-keep class com.quickblox.core.parser.** { *; } <br />
-keep class com.quickblox.core.model.** { *; } <br />
-keep class com.quickblox.core.server.** { *; } <br />
-keep class com.quickblox.core.rest.** { *; } <br />
-keep class com.quickblox.core.error.** { *; } <br />
-keep class com.quickblox.core.Query { *; } <br />

-keep class com.quickblox.users.parsers.** { *; } <br />
-keep class com.quickblox.users.model.** { *; } <br />

-keep class com.quickblox.chat.parser.** { *; } <br />
-keep class com.quickblox.chat.model.** { *; } <br />

-keep class com.quickblox.messages.parsers.** { *; } <br />
-keep class com.quickblox.messages.model.** { *; } <br />

-keep class com.quickblox.content.parsers.** { *; } <br />
-keep class com.quickblox.content.model.** { *; } <br />

-keep class org.jivesoftware.** { *; } <br />

\#sample chat <br />
-keep class android.support.v7.** { *; } <br />
-keep class com.bumptech.** { *; } <br />


\##---------------End: proguard configuration for quickblox  ---------- <br />

-dontwarn org.jivesoftware.smackx.** <br />
-dontwarn android.support.v4.app.** <br />
\##---------------End: proguard configuration ----------

To fix errors and force ProGuard to keep certain code, add a -keep line in the ProGuard configuration file. For example:
-keep public class MyClass
Alternatively, you can add the @Keep annotation to the code you want to keep. Adding @Keep on a class keeps the entire class as-is. Adding it on a method or field will keep the method/field (and it's name) as well as the class name intact.

# Documentation

* [Project page on QuickBlox developers section](http://quickblox.com/developers/Android)
* [Framework reference in JavaDoc format](http://sdk.quickblox.com/android/)

# Questions and feedback

Please raise questions, requests for help etc. via http://stackoverflow.com/questions/tagged/quickblox

# License
BSD
