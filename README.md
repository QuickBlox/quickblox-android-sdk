# QuickBlox Android SDK

# This project contains:

* New Quickblox Samples:
  * [Sample Chat Java](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-chat-java)
  * [Sample Chat Kotlin](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-chat-kotlin)
  * [Sample Conference Java](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-conference-java)
  * [Sample VideoChat Java](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-videochat-java)
  * [Sample VideoChat Kotlin](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-videochat-kotlin)
  * [Sample Push Notifications Java](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-pushnotifications-java)
  * [Sample Push Notifications Kotlin](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-pushnotifications-kotlin)

* QuickBlox Android SDK, which includes
  * [Core module](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/old_samples/sample-core) which contains base classes and util components for old samples
  * [Chat Sample](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/old_samples/sample-chat)
  * [Video Chat WebRTC Sample](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/old_samples/sample-videochat-webrtc)
  * [Users Sample](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/old_samples/sample-users)
  * [Push Notifications Sample](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/old_samples/sample-pushnotifications)
  * [Custom Objects Sample](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/old_samples/sample-custom-objects)
  * [Content Sample](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/old_samples/sample-content)

# Overview 

QuickBlox is a communications service provider. The platform provides chat using the XMPP protocol, WebRTC signaling for video/voice calling and API for sending push notifications. It provides a user management system, data storage and more. 

# Sample structure

Each **"NEW"** sample is an independent project, which contains final dependencies and all "Utils" and "Helpers" are included into it to be more understandable and clever. Each sample shows how to create your own project with QuickBlox SDK, and how to use it to extend your existing project.

Each **"OLD"** sample depends on the Sample-Core module, which contains mutual dependencies such as CoreApp, BaseActivity, BaseListAdapter and other useful utils such as ImagePicker, KeyboardUtils, NotificationUtils, etc. Also the Sample-Core module keeps common resources colors, strings, dimens and others. It makes your code more clean and clear, and also more object-oriented. In addition the Samples have been renewed with up-to-date design.

# How to run samples

To run samples on Android Studio go to menu **File - Import Project**. Select path to sample, select **Use default gradle wrapper(recommended)** and click OK.

# Configure sample credentials
To create Quickblox account (if you don't have one) and find your own credentials you should visit our [5 minute guide](https://quickblox.com/developers/5_Minute_Guide)

* **New samples**
  * To set your own app credentials you should open **App.java** (or App.kt if you have downloaded the Kotlin sample) and paste the credentials into the values of constants:
<img src="/sample-chat-java/screenshots/QB_Credentials_new_samples.png" border="5" alt="New Samples Credentials" >

* **Old samples**
  * To set your own app credentials for the sample just update configuration file **qb_config.json** inside appropriate sample project in assets folder:
<img src="/old_samples/screenshots/QB_Credentials_old_samples.png" border="5" alt="Old Samples Credentials" >

   * To set additional sample settings use **sample_config.json** file inside assets folder.

# Connect SDK to your existing apps 

To get the QuickBlox SDK project running you will need to have installed Android Studio and Maven.

The repository https://github.com/QuickBlox/quickblox-android-sdk-releases contains binary distributions of QuickBlox Android SDK and manual on how to connect SDK to your project. Check it out.

# Android Studio issues

If you can’t successfully build the gradle after updating to some of the Quickblox SDK version, try to resolve all errors appearing in Gradle Console (but not in code emphasized red!) It may occur because of some versions of Android Studio can’t completely resolve import dependencies while a project has internal errors.

If you still have problems with importing the SDK library from remote repo, go to the next section.

# Add quickblox dependency as local repository

1. Create local folder, for example /Users/igor/workspace/quickblox

2. Clone or download the repository from https://github.com/QuickBlox/quickblox-android-sdk-releases 

3. Put the downloaded quickblox-android-sdk-releases-master into the created quickblox folder.

4. In project replace build.grade file
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

To use Proguard in your project create proguard-rules.pro file and put below rules for Quickblox modules :
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
-keep class org.jxmpp.** { *; }
-dontwarn org.jivesoftware.smackx.**

#quickblox videochat-webrtc module
-keep class org.webrtc.** { *; }

##---------------End: proguard configuration for quickblox  ----------

##---------------End: proguard configuration ----------
```
To fix errors and force ProGuard to keep certain code, add a -keep line in the ProGuard configuration file. 
```xml
For example:
-keep public class MyClass
```
 Alternatively, you can add the @Keep annotation to the code you want to keep. Adding @Keep on a class keeps the entire class as-is. Adding it on a method or field will keep the method/field (and its name) as well as the class name intact.

# Documentation

* [Official QuickBlox Documentation](https://docs.quickblox.com/docs/android-quick-start)
* [Framework reference in JavaDoc format](http://sdk.quickblox.com/android/)

# Questions and feedback

Please ask questions, requests for help etc.
* [Ask question on GitHub](https://github.com/QuickBlox/quickblox-android-sdk/issues) 
* [Ask question on StackOverflow](http://stackoverflow.com/questions/tagged/quickblox)
* [Ask question on Quickblox Help Center](https://assist.quickblox.com/)

# License
BSD
