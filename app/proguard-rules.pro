# ProGuard & R8 Optimization Rules for Global SMS Production Release

# Keep Room database & DAO entities
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Moshi models & codegen classes
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}
-keep class com.squareup.moshi.** { *; }

# Keep Kotlin Coroutines & Flow
-keepclassmembers class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Keep WorkManager workers
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Keep Coil image loader components
-keep class coil.** { *; }

# Keep AndroidX Navigation Serializable Routes
-keepattributes *Annotation*,Signature
-keepclassmembers class * implements java.io.Serializable { *; }

# Preserve Line Numbers for Crash Reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

