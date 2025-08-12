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
