# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-repackageclasses com.streamwide.smartms.altbeacon

-keep class com.streamwide.smartms.altbeacon.beacon.Beacon {*;}
-keep class com.streamwide.smartms.altbeacon.beacon.BeaconConsumer {*;}
-keep class com.streamwide.smartms.altbeacon.beacon.BeaconManager {*;}
-keep class android.os.RemoteException {*;}
-keepattributes Exceptions
-keep class com.streamwide.smartms.altbeacon.beacon.BeaconParser {*;}
-keep class com.streamwide.smartms.altbeacon.beacon.Identifier {*;}
-keep class com.streamwide.smartms.altbeacon.beacon.MonitorNotifier {*;}
-keep class com.streamwide.smartms.altbeacon.beacon.RangeNotifier {*;}
-keep class com.streamwide.smartms.altbeacon.beacon.Region {*;}
-keep class com.streamwide.smartms.altbeacon.beacon.logging.LogManager {*;}
-keep class com.streamwide.smartms.altbeacon.beacon.logging.Loggers {*;}
-keep class com.streamwide.smartms.altbeacon.beacon.logging.LogManager {*;}
-keep class com.streamwide.smartms.altbeacon.beacon.logging.Loggers {*;}
-keep class com.streamwide.smartms.altbeacon.beacon.io.IoFileConfiguration {*;}
-keep class com.streamwide.smartms.altbeacon.beacon.logging.Logger {*;}
