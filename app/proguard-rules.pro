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
# R8 full mode (AGP 8 default) strips the generic signatures Retrofit reads for suspend
# functions and Response<T> ("Class cannot be cast to ParameterizedType" at runtime).
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
# Keep the return types of every Retrofit API method (Retrofit 2.9 ships without this rule;
# without it Moshi gets an erased type and returns a Map, which then fails the cast).
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

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
# KotlinJsonAdapterFactory resolves constructor parameter types through kotlin-reflect and
# the Java generic signatures; keep the model classes whole (names, fields, constructors,
# Signature) and kotlin-reflect's internals, otherwise List<DayScores> degrades to List<Map>.
-keep class com.sangeetmind.libs.models.** { *; }
-keep class com.sangeetmind.core.network.** { *; }
-keep class kotlin.reflect.** { *; }
-keep class kotlin.jvm.internal.** { *; }
-dontwarn kotlin.reflect.jvm.internal.**
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*, Exceptions
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

