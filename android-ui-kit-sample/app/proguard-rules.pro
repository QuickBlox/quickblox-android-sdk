# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
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

# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes Exceptions

# GSON @Expose annotation
-keepattributes *Annotation*

# Quickblox sdk
-keep class com.quickblox.** { *; }

# Smack xmpp library
-keep class org.jxmpp.** { *; }
-keep class org.jivesoftware.** { *; }
-dontwarn org.jivesoftware.**

# Glide
-keep class com.bumptech.** { *; }

# Google gms
-keep class com.google.android.gms.** { *; }

# Json
-keep class org.json.** { *; }