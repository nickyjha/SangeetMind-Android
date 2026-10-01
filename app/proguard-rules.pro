# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep data classes for JSON serialization
-keepclassmembers class com.sangeetmind.libs.models.** {
    <fields>;
    <init>(...);
}

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# Moshi
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keep @com.squareup.moshi.JsonQualifier @interface *
-keepclassmembers @com.squareup.moshi.JsonClass class * extends java.lang.Enum {
    <fields>;
    **[] values();
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# ExoPlayer
-keep class com.google.android.exoplayer2.** { *; }
-dontwarn com.google.android.exoplayer2.**

# Moshi reflection (KotlinJsonAdapterFactory) needs Kotlin metadata and every @Json field;
# models live in libs/models but a few request/response classes sit in core/network and
# the feature modules.
-keep class kotlin.Metadata { *; }
-keepclassmembers class com.sangeetmind.** {
    @com.squareup.moshi.Json <fields>;
}
-keep @com.squareup.moshi.JsonClass class com.sangeetmind.** {
    <fields>;
    <init>(...);
}
-dontwarn org.jetbrains.annotations.**

# Crashlytics: keep file names and line numbers in stack traces.
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

