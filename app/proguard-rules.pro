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
#-libraryjars libs/rt.jar
#-libraryjars libs/bcpkix-jdk15on-1.60.jar
#-libraryjars libs/bcprov-jdk15on-1.60.jar
-obfuscationdictionary keywords.txt
-classobfuscationdictionary windows.txt
-packageobfuscationdictionary windows.txt
-repackageclasses 'o'
#-keep class com.google.**
#-dontwarn com.google.**
-keep class org.bouncycastle.** { *; }
-keepclassmembers class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**
-keep class com.google.protobuf.** { *; }

-keep class android.view.**  { *; }

# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

-keepnames class org.joda.time.**
-keepnames class org.joda.time.format.**
-keepnames class com.roughike.bottombar.**
-keepnames class net.danlew.android.joda.**
-dontwarn org.joda.time.*
-dontwarn com.roughike.bottombar.*
-dontwarn net.danlew.android.joda.*
-dontwarn org.joda.time.format.**
# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

-dontwarn org.conscrypt.**
-dontwarn sun.misc.Unsafe
#-keep class java.util.Base64 { *; }

-keepattributes Signature

-keep class com.google.android.material.** { *; }

-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**

-dontwarn androidx.**
-keep class androidx.** { *; }
-keep interface androidx.** { *; }

-keep class com.google.protobuf.** { *; }
-dontwarn com.google.protobuf.**

## Joda Convert 1.6

-keep class org.joda.convert.** { *; }
-keep interface org.joda.convert.** { *; }
-dontwarn org.joda.convert.**

-keep class com.mad.pogoenhancer.gpx.LoadedGpxRoutes { *; }
-keep class io.ticofab.androidgpxparser.** { *; }

#-keep class POGOProtos.** { *; }
#-ignorewarnings
#-dontwarn javax.naming.**

#-ignorewarnings
#
#-keep class * {
#    public private *;
#}