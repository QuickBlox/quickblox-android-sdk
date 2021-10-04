# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.quickblox.core.account.model.** { *; }

-keep class com.quickblox.auth.parsers.** { *; }
-keep class com.quickblox.auth.model.** { *; }
-keep class com.quickblox.core.parser.** { *; }
-keep class com.quickblox.core.model.** { *; }
-keep class com.quickblox.core.server.** { *; }
-keep class com.quickblox.core.rest.** { *; }
-keep class com.quickblox.core.error.** { *; }
-keep class com.quickblox.core.Query { *; }

-keep class com.quickblox.content.model.** { *; }

-keep class com.quickblox.users.parsers.** { *; }
-keep class com.quickblox.users.model.** { *; }

-keep class com.quickblox.messages.parsers.** { *; }
-keep class com.quickblox.messages.QBPushNotifications { *; }
-keep class com.quickblox.messages.model.** { *; }
-keep class com.quickblox.messages.services.** { *; }

-keep class com.quickblox.chat.parser.** { *; }
-keep class com.quickblox.chat.model.** { *; }

-keep class org.jivesoftware.** { *; }
-keep class org.jxmpp.** { *; }
-keep class org.webrtc.** { *; }
-keep class com.quickblox.conference.** { *; }

-keep class com.bumptech.** { *; }

-dontwarn org.jivesoftware.smackx.**