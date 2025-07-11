/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Thu, 16 May 2024 10:00:18 +0100
 * @copyright  Copyright (c) 2024 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2024 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Thu, 16 May 2024 09:56:50 +0100
 */

package com.streamwide.smartms.altbeacon.bluetooth;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.app.job.JobScheduler;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.AdvertiseSettings.Builder;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.streamwide.smartms.altbeacon.beacon.logging.LogManager;

import java.util.List;

/**
 *
 * Utility class for checking the health of the bluetooth stack on the device by running two kinds
 * of tests: scanning and transmitting.  The class looks for specific failure codes from these
 * tests to determine if the bluetooth stack is in a bad state and if so, optionally cycle power to
 * bluetooth to try and fix the problem.  This is known to work well on some Android devices.
 *
 * The tests may be called directly, or set up to run automatically approximately every 15 minutes.
 * To set up in an automated way:
 *
 * <code>
 *   BluetoothMedic medic = BluetoothMedic.getInstance();
 *   medic.enablePowerCycleOnFailures(context);
 *   medic.enablePeriodicTests(context, BluetoothMedic.SCAN_TEST | BluetoothMedic.TRANSMIT_TEST);
 * </code>
 *
 * To set up in a manual way:
 *
 * <code>
 *   BluetoothMedic medic = BluetoothMedic.getInstance();
 *   medic.enablePowerCycleOnFailures(context);
 *   if (!medic.runScanTest(context)) {
 *     // Bluetooth stack is in a bad state
 *   }
 *   if (!medic.runTransmitterTest(context)) {
 *     // Bluetooth stack is in a bad state
 *   }
 *
 */

@SuppressWarnings("javadoc")
public class BluetoothMedic {

    /**
     * Indicates that no test should be run by the BluetoothTestJob
     */
    @SuppressWarnings("WeakerAccess")
    public static final int NO_TEST = 0;
    /**
     * Indicates that the transmitter test should be run by the BluetoothTestJob
     */
    @SuppressWarnings("WeakerAccess")
    public static final int TRANSMIT_TEST = 2;
    /**
     * Indicates that the bluetooth scan test should be run by the BluetoothTestJob
     */
    @SuppressWarnings("WeakerAccess")
    public static final int SCAN_TEST = 1;
    protected static final String TAG = BluetoothMedic.class.getSimpleName();
    @Nullable
    protected BluetoothAdapter mAdapter;
    @Nullable
    protected LocalBroadcastManager mLocalBroadcastManager;
    @NonNull
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private int mTestType = 0;
    @Nullable
    protected Boolean mTransmitterTestResult = null;
    @Nullable
    protected Boolean mScanTestResult = null;
    private boolean mNotificationsEnabled = false;
    private boolean mNotificationChannelCreated = false;
    private int mNotificationIcon = 0;
    private long mLastBluetoothPowerCycleTime = 0L;
    private static final long MIN_MILLIS_BETWEEN_BLUETOOTH_POWER_CYCLES = 60000L;
    @Nullable
    private static BluetoothMedic sInstance;

    @NonNull
    protected BroadcastReceiver mBluetoothEventReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, @NonNull Intent intent) {
            LogManager.d(BluetoothMedic.TAG, "Broadcast notification received.");
            int errorCode;
            String action = intent.getAction();
            if (action != null) {
                if(action.equalsIgnoreCase("onScanFailed")) {
                    errorCode = intent.getIntExtra("errorCode", -1);
                    if(errorCode == 2) {
                        BluetoothMedic.this.sendNotification(context, "scan failed",
                                "Power cycling bluetooth");
                        LogManager.d(BluetoothMedic.TAG,
                                "Detected a SCAN_FAILED_APPLICATION_REGISTRATION_FAILED.  We need to cycle bluetooth to recover");
                        if(!BluetoothMedic.this.cycleBluetoothIfNotTooSoon()) {
                            BluetoothMedic.this.sendNotification(context, "scan failed", "" +
                                    "Cannot power cycle bluetooth again");
                        }
                    }
                } else if(action.equalsIgnoreCase("onStartFailed")) {
                    errorCode = intent.getIntExtra("errorCode", -1);
                    if(errorCode == 4) {
                        BluetoothMedic.this.sendNotification(context, "advertising failed",
                                "Expected failure.  Power cycling.");
                        if(!BluetoothMedic.this.cycleBluetoothIfNotTooSoon()) {
                            BluetoothMedic.this.sendNotification(context, "advertising failed",
                                    "Cannot power cycle bluetooth again");
                        }
                    }
                } else {
                    LogManager.d(BluetoothMedic.TAG, "Unknown event.");
                }
            }
        }
    };


    /**
     * Get a singleton instance of the BluetoothMedic
     * @return
     */
    @NonNull
    public static BluetoothMedic getInstance() {
        if(sInstance == null) {
            sInstance = new BluetoothMedic();
        }
        return sInstance;
    }

    private BluetoothMedic() {
    }

    private void initializeWithContext(Context context) {
        if (this.mAdapter == null || this.mLocalBroadcastManager == null) {
            BluetoothManager manager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
            if(manager == null) {
                throw new NullPointerException("Cannot get BluetoothManager");
            } else {
                this.mAdapter = manager.getAdapter();
                this.mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
            }
        }
    }

    /**
     * If set to true, bluetooth will be power cycled on any tests run that determine bluetooth is
     * in a bad state.
     *
     * @param context
     */
    @SuppressWarnings("unused")
    public void enablePowerCycleOnFailures(@NonNull Context context) {
        initializeWithContext(context);
        if (this.mLocalBroadcastManager != null) {
            this.mLocalBroadcastManager.registerReceiver(this.mBluetoothEventReceiver,
                    new IntentFilter("onScanFailed"));
            this.mLocalBroadcastManager.registerReceiver(this.mBluetoothEventReceiver,
                    new IntentFilter("onStartFailure"));
            LogManager.d(TAG,
                    "Medic monitoring for transmission and scan failure notifications with receiver: "
                            + this.mBluetoothEventReceiver);
        }
    }

    /**
     * Calling this method starts a scheduled job that will run tests of the specified type to
     * make sure bluetooth is OK and cycle power to bluetooth if needed and configured by
     * enablePowerCycleOnFailures
     *
     * @param context
     * @param testType e.g. BluetoothMedic.TRANSMIT_TEST | BluetoothMedic.SCAN_TEST
     */
    @SuppressWarnings("unused")
    public void enablePeriodicTests(@NonNull Context context, int testType) {
        initializeWithContext(context);
        this.mTestType = testType;
        LogManager.d(TAG, "Medic scheduling periodic tests of types " + testType);
        this.scheduleRegularTests(context);
    }

    /**
     * Starts up a brief blueooth scan with the intent of seeing if it results in an error condition
     * indicating the bluetooth stack may be in a bad state.
     *
     * If the failure error code matches a pattern known to be associated with a bad bluetooth stack
     * state, then the bluetooth stack is turned off and then back on after a short delay in order
     * to try to recover.
     *
     * @return false if the test indicates a failure indicating a bad state of the bluetooth stack
     */
    @SuppressWarnings({"unused","WeakerAccess"})
    public boolean runScanTest(@NonNull final Context context) {
        initializeWithContext(context);
        this.mScanTestResult = null;
        LogManager.i(TAG, "Starting scan test");
        final long testStartTime = System.currentTimeMillis();
        if (this.mAdapter != null) {
            final BluetoothLeScanner scanner = this.mAdapter.getBluetoothLeScanner();
            final ScanCallback callback = new ScanCallback() {
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    BluetoothMedic.this.mScanTestResult = true;
                    LogManager.i(BluetoothMedic.TAG, "Scan test succeeded");
                    try {
                        scanner.stopScan(this);
                    }
                    catch (IllegalStateException | SecurityException e) { /* do nothing */ } // caught if bluetooth is off here
                }

                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);
                }

                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                    LogManager.d(BluetoothMedic.TAG, "Sending onScanFailed broadcast with " +
                            BluetoothMedic.this.mLocalBroadcastManager);
                    Intent intent = new Intent("onScanFailed");
                    intent.putExtra("errorCode", errorCode);
                    if (BluetoothMedic.this.mLocalBroadcastManager != null) {
                        BluetoothMedic.this.mLocalBroadcastManager.sendBroadcast(intent);
                    }
                    LogManager.d(BluetoothMedic.TAG, "broadcast: " + intent +
                            " should be received by " + BluetoothMedic.this.mBluetoothEventReceiver);
                    if(errorCode == 2) {
                        LogManager.w(BluetoothMedic.TAG,
                                "Scan test failed in a way we consider a failure");
                        BluetoothMedic.this.sendNotification(context,
                                "scan failed", "bluetooth not ok");
                        BluetoothMedic.this.mScanTestResult = false;
                    } else {
                        LogManager.i(BluetoothMedic.TAG,
                                "Scan test failed in a way we do not consider a failure");
                        BluetoothMedic.this.mScanTestResult = true;
                    }

                }
            };
            if(scanner != null) {
                try {
                    scanner.startScan(callback);
                    while (this.mScanTestResult == null) {
                        LogManager.d(TAG, "Waiting for scan test to complete...");

                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException e) {
                            LogManager.e(TAG, "InterruptedException",e);
                            Thread.currentThread().interrupt(); }

                        if (System.currentTimeMillis() - testStartTime > 5000L) {
                            LogManager.d(TAG, "Timeout running scan test");
                            break;
                        }
                    }
                    scanner.stopScan(callback);
                } catch (IllegalStateException e) {
                    LogManager.d(TAG, "Bluetooth is off.  Cannot run scan test.");
                } catch (NullPointerException | SecurityException e) {
                    // Needed to stop a crash caused by internal NPE thrown by Android.  See issue #636
                    LogManager.e(e, TAG, "Exception. Cannot run scan test.", e);
                }
            }
            else {
                LogManager.d(TAG, "Cannot get scanner");
            }
        }

        LogManager.d(TAG, "scan test complete");
        return this.mScanTestResult == null || this.mScanTestResult;
    }

    /**
     * Starts up a beacon transmitter with the intent of seeing if it results in an error condition
     * indicating the bluetooth stack may be in a bad state.
     *
     * If the failure error code matches a pattern known to be associated with a bad bluetooth stack
     * state, then the bluetooth stack is turned off and then back on after a short delay in order
     * to try to recover.
     *
     * @return false if the test indicates a failure indicating a bad state of the bluetooth stack
     */
    @SuppressWarnings({"unused","WeakerAccess"})
    public boolean runTransmitterTest(@NonNull final Context context) {
        initializeWithContext(context);
        this.mTransmitterTestResult = null;
        long testStartTime = System.currentTimeMillis();
        if (mAdapter != null) {
            final BluetoothLeAdvertiser advertiser = getAdvertiserSafely(mAdapter);
            if(advertiser != null &&
                    ContextCompat.checkSelfPermission(context,
                            Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED) {

                AdvertiseSettings settings = (new Builder()).setAdvertiseMode(0).build();
                AdvertiseData data = (new android.bluetooth.le.AdvertiseData.Builder())
                        .addManufacturerData(0, new byte[]{0}).build();
                LogManager.i(TAG, "Starting transmitter test");
                advertiser.startAdvertising(settings, data, new AdvertiseCallback() {
                    @Override
                    public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                        super.onStartSuccess(settingsInEffect);
                        if (ContextCompat.checkSelfPermission(context,
                                Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) return;
                        LogManager.i(BluetoothMedic.TAG, "Transmitter test succeeded");
                        advertiser.stopAdvertising(this);
                        BluetoothMedic.this.mTransmitterTestResult = true;
                    }

                    @Override
                    public void onStartFailure(int errorCode) {
                        super.onStartFailure(errorCode);
                        Intent intent = new Intent("onStartFailed");
                        intent.putExtra("errorCode", errorCode);
                        LogManager.d(BluetoothMedic.TAG, "Sending onStartFailure broadcast with "
                                + BluetoothMedic.this.mLocalBroadcastManager);
                        if (BluetoothMedic.this.mLocalBroadcastManager != null) {
                            BluetoothMedic.this.mLocalBroadcastManager.sendBroadcast(intent);
                        }
                        if(errorCode == 4) {
                            BluetoothMedic.this.mTransmitterTestResult = false;
                            LogManager.w(BluetoothMedic.TAG,
                                    "Transmitter test failed in a way we consider a test failure");
                            BluetoothMedic.this.sendNotification(context, "transmitter failed",
                                    "bluetooth not ok");
                        } else {
                            BluetoothMedic.this.mTransmitterTestResult = true;
                            LogManager.i(BluetoothMedic.TAG,
                                    "Transmitter test failed, but not in a way we consider a test failure");
                        }

                    }
                });
            } else {
                LogManager.d(TAG, "Cannot get advertiser");
            }
            while(this.mTransmitterTestResult == null) {
                LogManager.d(TAG, "Waiting for transmitter test to complete...");

                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    LogManager.e(TAG, "InterruptedException",e);
                    Thread.currentThread().interrupt();
                }

                if(System.currentTimeMillis() - testStartTime > 5000L) {
                    LogManager.d(TAG, "Timeout running transmitter test");
                    break;
                }
            }
        }

        LogManager.d(TAG, "transmitter test complete");
        return this.mTransmitterTestResult != null && this.mTransmitterTestResult;
    }

    /**
     *
     * Configure whether to send user-visible notification warnings when bluetooth power is cycled.
     *
     * @param enabled if true, a user-visible notification is sent to tell the user when
     * @param icon the icon drawable to use in notifications (e.g. R.drawable.notification_icon)
     */
    @SuppressWarnings("unused")
    public void setNotificationsEnabled(boolean enabled, int icon) {
        this.mNotificationsEnabled = enabled;
        this.mNotificationIcon = icon;
    }

    protected boolean cycleBluetoothIfNotTooSoon() {
        long millisSinceLastCycle = System.currentTimeMillis() - this.mLastBluetoothPowerCycleTime;
        if(millisSinceLastCycle < MIN_MILLIS_BETWEEN_BLUETOOTH_POWER_CYCLES) {
            LogManager.d(TAG, "Not cycling bluetooth because we just did so " +
                    millisSinceLastCycle + " milliseconds ago.");
            return false;
        } else {
            this.mLastBluetoothPowerCycleTime = System.currentTimeMillis();
            LogManager.d(TAG, "Power cycling bluetooth");
            this.cycleBluetooth();
            return true;
        }
    }

    private void cycleBluetooth() {
        try {
            LogManager.d(TAG, "Power cycling bluetooth");
            LogManager.d(TAG, "Turning Bluetooth off.");
            if (mAdapter != null) {
                this.mAdapter.disable();
                this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        LogManager.d(BluetoothMedic.TAG, "Turning Bluetooth back on.");
                        if (BluetoothMedic.this.mAdapter != null) {
                            BluetoothMedic.this.mAdapter.enable();
                        }
                    }
                }, 1000L);
            } else {
                LogManager.w(TAG, "Cannot cycle bluetooth.  Manager is null.");
            }
        } catch (SecurityException ex) {
            LogManager.e(ex, TAG, "Exception : Cannot cycle bluetooth.");

        }
    }

    protected void sendNotification(@NonNull Context context,@NonNull  String message,@NonNull String detail) {
        initializeWithContext(context);
        if(this.mNotificationsEnabled) {
            if (!this.mNotificationChannelCreated) {
                createNotificationChannel(context, "err");
            }
            NotificationCompat.Builder builder =
                    (new NotificationCompat.Builder(context, "err"))
                            .setContentTitle("BluetoothMedic: " + message)
                            .setSmallIcon(mNotificationIcon)
                            .setVibrate(new long[]{200L, 100L, 200L}).setContentText(detail);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntent(new Intent("NoOperation"));

            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
            builder.setContentIntent(resultPendingIntent);
            NotificationManager notificationManager =
                    (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.notify(1, builder.build());
            }
        }
    }

    private void createNotificationChannel(Context context, String channelId) {
        // On Android 8.0 and above posting a notification without a
        // channel is an error. So create a notification channel 'err'
        String channelName = "Errors";
        String description = "Scan errors";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        mNotificationChannelCreated = true;
    }

    private void scheduleRegularTests(Context context) {
        initializeWithContext(context);
        ComponentName serviceComponent = new ComponentName(context, BluetoothTestJob.class);
        android.app.job.JobInfo.Builder builder =
                new android.app.job.JobInfo.Builder(BluetoothTestJob.getJobId(context), serviceComponent);
        builder.setRequiresCharging(false);
        builder.setRequiresDeviceIdle(false);
        builder.setPeriodic(900000L); // 900 secs is 15 minutes -- the minimum time on Android
        builder.setPersisted(true);
        PersistableBundle bundle = new PersistableBundle();
        bundle.putInt("test_type", this.mTestType);
        builder.setExtras(bundle);
        JobScheduler jobScheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            jobScheduler.schedule(builder.build());
        }
    }

    private BluetoothLeAdvertiser getAdvertiserSafely(BluetoothAdapter adapter) {
        try {
            // This can sometimes throw a NullPointerException as reported here:
            // https://github.com/AltBeacon/android-beacon-library/issues/672
            return adapter.getBluetoothLeAdvertiser();
        }
        catch (Exception e) {
            LogManager.w(TAG, "Cannot get bluetoothLeAdvertiser", e);
        }
        return null;
    }
}
