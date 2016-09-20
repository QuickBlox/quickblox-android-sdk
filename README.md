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

# Android Studio issues

If you can’t successfully build the gradle after updating to some of the version Quickblox SDK, try to resolve all errors appearing in Gradle Console (but not in code emphasized red!) It may occurs cause some versions of Android Studio can’t completely resolve import dependencies while project has internal errors.

If you still have problems with import SDK library from remote repo follow to the next section.

# Add quickblox dependency as local repository

1. Create local folder, for example /Users/igor/workspace/quickblox

2. By the link https://github.com/QuickBlox/quickblox-android-sdk-releases clone or download repository

3. Put in created quickblox folder downloaded quickblox-android-sdk-releases-master.

4. In project build.grade file replace
```xml
maven {
            url "https://github.com/QuickBlox/quickblox-android-sdk-releases/raw/master/"
        }
```
to
```xml
maven {
            url= "file://Users/igor/workspace/quickblox/quickblox-android-sdk-releases-master"
        }
```
and use any version of quickblox-android-sdk.

# Customize Proguard in Android Studio 

To use Proguard in your project create proguard-rules.pro file and put there the next for quickblox modules:
```xml
##---------------Begin: proguard configuration for Gson  ---------- 
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.quickblox.core.account.model.** { *; }

##---------------End: proguard configuration for Gson  ----------

##---------------Begin: proguard configuration for quickblox  ----------

#quickblox core module
-keep class com.quickblox.auth.parsers.** { *; }
-keep class com.quickblox.auth.model.** { *; }
-keep class com.quickblox.core.parser.** { *; }
-keep class com.quickblox.core.model.** { *; }
-keep class com.quickblox.core.server.** { *; }
-keep class com.quickblox.core.rest.** { *; }
-keep class com.quickblox.core.error.** { *; }
-keep class com.quickblox.core.Query { *; }

#quickblox users module
-keep class com.quickblox.users.parsers.** { *; }
-keep class com.quickblox.users.model.** { *; }

#quickblox messages module
-keep class com.quickblox.messages.parsers.** { *; }
-keep class com.quickblox.messages.model.** { *; }

#quickblox content module
-keep class com.quickblox.content.parsers.** { *; }
-keep class com.quickblox.content.model.** { *; }

#quickblox chat module
-keep class com.quickblox.chat.parser.** { *; }
-keep class com.quickblox.chat.model.** { *; }
-keep class org.jivesoftware.** { *; }
-dontwarn org.jivesoftware.smackx.**

#quickblox videochat-webrtc module
-keep class org.webrtc.** { *; }

##---------------End: proguard configuration for quickblox  ----------

##---------------End: proguard configuration ----------
```
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
