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
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes Exceptions

# For using GSON @Expose annotation
-keepattributes *Annotation*

#quickblox sdk
-keep class com.quickblox.** { *; }

#smack xmpp library
-keep class org.jxmpp.** { *; }
-keep class org.jivesoftware.** { *; }
-dontwarn org.jivesoftware.**

#glide
-keep class com.bumptech.** { *; }

#google gms
-keep class com.google.android.gms.** { *; }