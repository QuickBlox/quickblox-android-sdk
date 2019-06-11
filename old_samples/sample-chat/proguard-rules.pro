# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/tereha/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
#-dontusemixedcaseclassnames
#-dontskipnonpubliclibraryclasses
#-verbose
#

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
#quickblox sample chat

-keep class com.quickblox.auth.parsers.** { *; }
-keep class com.quickblox.auth.model.** { *; }
-keep class com.quickblox.core.parser.** { *; }
-keep class com.quickblox.core.model.** { *; }
-keep class com.quickblox.core.server.** { *; }
-keep class com.quickblox.core.rest.** { *; }
-keep class com.quickblox.core.error.** { *; }
-keep class com.quickblox.core.Query { *; }

-keep class com.quickblox.users.parsers.** { *; }
-keep class com.quickblox.users.model.** { *; }

-keep class com.quickblox.chat.parser.** { *; }
-keep class com.quickblox.chat.model.** { *; }

-keep class com.quickblox.messages.parsers.** { *; }
-keep class com.quickblox.messages.model.** { *; }

-keep class com.quickblox.content.parsers.** { *; }
-keep class com.quickblox.content.model.** { *; }

-keep class org.jivesoftware.** { *; }

#sample chat
-keep class android.support.v7.** { *; }
-keep class com.bumptech.** { *; }

-dontwarn org.jivesoftware.smackx.**
-dontwarn android.support.v4.app.**