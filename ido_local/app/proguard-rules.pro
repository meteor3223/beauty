# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/android-sdk-linux/tools/proguard/proguard-android.txt
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
-dontwarn org.apache.http.**
-dontwarn android.net.http.*
-dontwarn com.umeng.**

#talkingdata
-dontwarn com.tendcloud.tenddata.**
-keep class com.tendcloud.** {*;}
-keep public class com.tendcloud.tenddata.** { public protected *;}
-keepclassmembers class com.tendcloud.tenddata.**{
public void *(***);
}
-keep class com.talkingdata.sdk.TalkingDataSDK {public *;}
-keep class com.apptalkingdata.** {*;}


-keepclassmembers class * {
    public <init>(org.json.JSONObject);
}

# 如"com.example.R$*"
-keep public class com.xym.beautygallery.R$*{
    public static final int *;
}

#gdt ad sdk
-keep class com.qq.e.** {
    public protected *;
}
-keep class android.support.v4.app.NotificationCompat**{
    public *;
}

#picasso
-dontwarn com.squareup.okhttp.**