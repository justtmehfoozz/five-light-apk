# FiveLight Project R8 / ProGuard Rules

# Preserve Kotlin Metadata and Attributes
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable

# Room Database & Generated Implementations
-dontwarn androidx.room.paging.**
-keep class androidx.room.RoomDatabase
-keep class * extends androidx.room.RoomDatabase {
    <init>(...);
}
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class * extends androidx.room.RoomOpenHelper
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public abstract *;
}

# Domain & Data Models (Room Entities, Content Repositories, ViewModels, Enums)
-keep class com.example.data.model.** { *; }
-keep class com.example.data.db.** { *; }
-keep class com.example.data.util.** { *; }
-keep class com.example.data.reminder.** { *; }
-keep class com.example.data.repository.** { *; }

# Preserve Enums for reflection/serialization
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Android Architecture Components & ViewModels
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keepclassmembers class * extends androidx.lifecycle.AndroidViewModel {
    <init>(...);
}

# Broadcast Receivers declared in Manifest
-keep class com.example.data.reminder.PrayerReminderReceiver { *; }
-keep class com.example.data.reminder.SmartPrayerNotificationReceiver { *; }
-keep class com.example.data.reminder.BootReceiver { *; }

# Retrofit, OkHttp, Moshi
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn com.squareup.moshi.**
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Coroutines
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Desugar JDK Libs
-dontwarn java.time.**
