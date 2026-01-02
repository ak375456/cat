########################################
## Hilt / Dagger 2
########################################
# Keep Hilt-generated classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep class * extends dagger.hilt.EntryPoint { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep @dagger.Module class * { *; }

# Keep annotations for Hilt
-keepattributes *Annotation*

########################################
## Gson (reflection-based serialization)
########################################
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepattributes Signature
-keepattributes *Annotation*

########################################
## Parcelable models
########################################
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

########################################
## Prevent stripping of MotionSensorManager & SimpleOverlayManager
########################################
-keep class com.lexur.yumo.MotionSensorManager { *; }
-keep class com.lexur.yumo.SimpleOverlayManager { *; }
-keep class com.lexur.yumo.OverlayService { *; }

########################################
## General
########################################
-dontwarn javax.annotation.**
-dontwarn dagger.hilt.**
-dontwarn kotlin.**

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keep class com.lexur.yumo.home_screen.data.model.** { *; }

########################################
## Parcelable models - CRITICAL FOR YOUR CRASH
########################################
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep your specific Characters data class
-keep class com.lexur.yumo.home_screen.data.model.Characters { *; }
-keep class com.lexur.yumo.home_screen.data.model.CharacterCategory { *; }

-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

########################################
## Google AdMob / Play Services
########################################
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.common.** { *; }
-keep class com.google.android.gms.internal.ads.** { *; }
-keep class com.google.ads.** { *; }

# Keep AdMob mediation adapters
-keep class * implements com.google.android.gms.ads.mediation.** { *; }
-keep class com.google.android.gms.ads.mediation.** { *; }

# Keep MobileAds class and its methods
-keep class com.google.android.gms.ads.MobileAds { *; }

# Keep User Messaging Platform (UMP) classes
-keep class com.google.android.ump.** { *; }

# Keep AdLoader and related classes
-keep class com.google.android.gms.ads.AdLoader { *; }
-keep class com.google.android.gms.ads.formats.** { *; }
-keep class com.google.android.gms.ads.nativead.** { *; }

# Keep AdRequest and related classes
-keep class com.google.android.gms.ads.AdRequest { *; }
-keep class com.google.android.gms.ads.AdRequest$Builder { *; }

# Keep AdView classes
-keep class com.google.android.gms.ads.AdView { *; }
-keep class com.google.android.gms.ads.BaseAdView { *; }

# Keep InterstitialAd and RewardedAd classes
-keep class com.google.android.gms.ads.interstitial.** { *; }
-keep class com.google.android.gms.ads.rewarded.** { *; }
-keep class com.google.android.gms.ads.rewardedinterstitial.** { *; }

# Keep AdListener and callback interfaces
-keep interface com.google.android.gms.ads.AdListener { *; }
-keep class * implements com.google.android.gms.ads.AdListener {
    public <methods>;
}

# Keep InitializationCompleteListener for AdMob initialization
-keep interface com.google.android.gms.ads.initialization.** { *; }
-keep class * implements com.google.android.gms.ads.initialization.** { *; }

# Keep identifiers for AdMob
-keepattributes Signature
-keepattributes *Annotation*

# Keep these packages for reflection
-keepnames class com.google.android.gms.ads.internal.** { *; }

# Keep the application ID safe (used in AndroidManifest)
-keep class * extends android.app.Application {
    public <fields>;
}

# Fix for common AdMob warnings
-dontwarn com.google.android.gms.**
-dontwarn com.google.ads.**

# Keep R class fields for AdMob resource access
-keepclassmembers class **.R$* {
    public static <fields>;
}