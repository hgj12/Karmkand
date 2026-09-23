# ProGuard rules for Karmkand app

# Keep data models used in deserialization / reflection / database mapping
-keep class com.karmkand.app.modal.** { *; }

# Google Play Services / AdMob
-keep public class com.google.android.gms.ads.** {
   public *;
}
-keep public class com.google.ads.** {
   public *;
}

# AndroidX and Material Components
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses

# Preserve JavascriptInterface if WebViews use it
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
