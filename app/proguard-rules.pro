# === General Rules ===
# Keep attributes for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# === Compose Rules ===
# Keep Compose-related classes
-keep class androidx.compose.** { *; }
-keep class * extends androidx.compose.ui.node.ComposeUiNode { *; }

# Keep @Preview annotated functions
-keepclasseswithmembers class * {
    @androidx.compose.ui.tooling.preview.Preview <methods>;
}

# === Room Rules ===
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keep class * {
    @androidx.room.Query <methods>;
    @androidx.room.Insert <methods>;
    @androidx.room.Update <methods>;
    @androidx.room.Delete <methods>;
}
-dontwarn androidx.room.paging.**

# === Gson Rules ===
# Keep Gson annotations
-keepattributes Signature
-keepattributes *Annotation*

# Keep classes with Gson annotations
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# === Kotlin Coroutines & Serialization ===
-keepattributes RuntimeVisibleAnnotations
-keep class kotlin.coroutines.** { *; }
-dontwarn kotlin.coroutines.**

# === Mapbox Rules ===
-keep class com.mapbox.** { *; }
-keep interface com.mapbox.** { *; }
-dontwarn com.mapbox.**

# === Firebase Rules ===
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# === Google Play Services ===
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# === Open Location Code ===
-keep class com.google.openlocationcode.** { *; }

# === Remove logging code in release ===
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}
