/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Thu, 16 May 2024 09:56:21 +0100
 * @copyright  Copyright (c) 2024 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2024 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Thu, 16 May 2024 09:38:17 +0100
 */
package com.streamwide.smartms.altbeacon.beacon.service;


import static com.streamwide.smartms.altbeacon.util.BeaconServiceUtil.startForegroundIfConfigured;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.streamwide.smartms.altbeacon.beacon.Beacon;
import com.streamwide.smartms.altbeacon.beacon.BeaconLocalBroadcastProcessor;
import com.streamwide.smartms.altbeacon.beacon.BeaconManager;
import com.streamwide.smartms.altbeacon.beacon.BeaconParser;
import com.streamwide.smartms.altbeacon.beacon.Region;
import com.streamwide.smartms.altbeacon.beacon.distance.DistanceCalculator;
import com.streamwide.smartms.altbeacon.beacon.distance.ModelSpecificDistanceCalculator;
import com.streamwide.smartms.altbeacon.beacon.logging.LogManager;
import com.streamwide.smartms.altbeacon.beacon.service.scanner.CycledLeScanCallback;
import com.streamwide.smartms.altbeacon.beacon.utils.ProcessUtils;
import com.streamwide.smartms.altbeacon.bluetooth.BluetoothCrashResolver;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * @author dyoung
 */

public class BeaconService extends Service {
    public static final String TAG = "BeaconService";
    private final Handler handler = new Handler();
    private BluetoothCrashResolver bluetoothCrashResolver;
    private ScanHelper mScanHelper;
    private BeaconLocalBroadcastProcessor mBeaconNotificationProcessor;

    /*
     * The scan period is how long we wait between restarting the BLE advertisement scans
     * Each time we restart we only see the unique advertisements once (e.g. unique beacons)
     * So if we want updates, we have to restart.  For updates at 1Hz, ideally we
     * would restart scanning that often to get the same update rate.  The trouble is that when you
     * restart scanning, it is not instantaneous, and you lose any beacon packets that were in the
     * air during the restart.  So the more frequently you restart, the more packets you lose.  The
     * frequency is therefore a tradeoff.  Testing with 14 beacons, transmitting once per second,
     * here are the counts I got for various values of the SCAN_PERIOD:
     *
     * Scan period     Avg beacons      % missed
     *    1s               6                 57
     *    2s               10                29
     *    3s               12                14
     *    5s               14                0
     *
     * Also, because beacons transmit once per second, the scan period should not be an even multiple
     * of seconds, because then it may always miss a beacon that is synchronized with when it is stopping
     * scanning.
     *
     */

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class BeaconBinder extends Binder {
        @NonNull
        public BeaconService getService() {
            LogManager.i(TAG, "getService of BeaconBinder called");
            // Return this instance of LocalService so clients can call public methods
            return BeaconService.this;
        }
    }

    /**
     * Command to the service to display a message
     */
    public static final int MSG_START_RANGING = 2;
    public static final int MSG_STOP_RANGING = 3;
    public static final int MSG_START_MONITORING = 4;
    public static final int MSG_STOP_MONITORING = 5;
    public static final int MSG_SET_SCAN_PERIODS = 6;
    public static final int MSG_SYNC_SETTINGS = 7;

    static class IncomingHandler extends Handler {
        private final WeakReference<BeaconService> mService;

        IncomingHandler(BeaconService service) {
            /*
             * Explicitly state this uses the main thread. Without this we defer to where the
             * service instance is initialized/created; which is usually the main thread anyways.
             * But by being explicit we document our code design expectations for where things run.
             */
            super(Looper.getMainLooper());
            mService = new WeakReference<BeaconService>(service);
        }

        @MainThread
        @Override
        public void handleMessage(Message msg) {
            BeaconService service = mService.get();
            if (service != null) {
                StartRMData startRMData = StartRMData.fromBundle(msg.getData());
                if (startRMData != null) {
                    switch (msg.what) {
                        case MSG_START_RANGING:
                            LogManager.i(TAG, "start ranging received");
                            service.startRangingBeaconsInRegion(startRMData.getRegionData(), new com.streamwide.smartms.altbeacon.beacon.service.Callback(startRMData.getCallbackPackageName()));
                            service.setScanPeriods(startRMData.getScanPeriod(), startRMData.getBetweenScanPeriod(), startRMData.getBackgroundFlag());
                            break;
                        case MSG_STOP_RANGING:
                            LogManager.i(TAG, "stop ranging received");
                            service.stopRangingBeaconsInRegion(startRMData.getRegionData());
                            service.setScanPeriods(startRMData.getScanPeriod(), startRMData.getBetweenScanPeriod(), startRMData.getBackgroundFlag());
                            break;
                        case MSG_START_MONITORING:
                            LogManager.i(TAG, "start monitoring received");
                            service.startMonitoringBeaconsInRegion(startRMData.getRegionData(), new com.streamwide.smartms.altbeacon.beacon.service.Callback(startRMData.getCallbackPackageName()));
                            service.setScanPeriods(startRMData.getScanPeriod(), startRMData.getBetweenScanPeriod(), startRMData.getBackgroundFlag());
                            break;
                        case MSG_STOP_MONITORING:
                            LogManager.i(TAG, "stop monitoring received");
                            service.stopMonitoringBeaconsInRegion(startRMData.getRegionData());
                            service.setScanPeriods(startRMData.getScanPeriod(), startRMData.getBetweenScanPeriod(), startRMData.getBackgroundFlag());
                            break;
                        case MSG_SET_SCAN_PERIODS:
                            LogManager.i(TAG, "set scan intervals received");
                            service.setScanPeriods(startRMData.getScanPeriod(), startRMData.getBetweenScanPeriod(), startRMData.getBackgroundFlag());
                            break;
                        default:
                            super.handleMessage(msg);
                    }
                } else if (msg.what == MSG_SYNC_SETTINGS) {
                    LogManager.i(TAG, "Received settings update from other process");
                    SettingsData settingsData = SettingsData.fromBundle(msg.getData());
                    if (settingsData != null) {
                        settingsData.apply(service);
                    } else {
                        LogManager.w(TAG, "Settings data missing");
                    }
                } else {
                    LogManager.i(TAG, "Received unknown message from other process : " + msg.what);
                }

            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler(this));

    @MainThread
    @Override
    public void onCreate() {

        mScanHelper = new ScanHelper(this);
        if (mScanHelper.getCycledScanner() == null) {
            mScanHelper.createCycledLeScanner(false, bluetoothCrashResolver);
        }
        mScanHelper.setMonitoringStatus(MonitoringStatus.getInstance());
        mScanHelper.setRangedRegionState(new HashMap<Region, RangeState>());
        mScanHelper.setBeaconParsers(new HashSet<BeaconParser>());
        mScanHelper.setExtraDataBeaconTracker(new ExtraDataBeaconTracker());

        BeaconManager beaconManager = BeaconManager.getInstanceForApplication(getApplicationContext());
        beaconManager.setScannerInSameProcess(true);
        if (beaconManager.isMainProcess()) {
            LogManager.i(TAG, "beaconService version %s is starting up on the main process");
            // if we are on the main process, we use local broadcast notifications to deliver results.
            ensureNotificationProcessorSetup();
        } else {
            LogManager.i(TAG, "beaconService version %s is starting up on a separate process");
            ProcessUtils processUtils = new ProcessUtils(this);
            LogManager.i(TAG, "beaconService PID is " + processUtils.getPid() + " with process name " + processUtils.getProcessName());
        }
        //FIXME : fix added for jira ticket SMARTMS-4128 to enable the long scan forcing
//        String longScanForcingEnabled = getManifestMetadataValue("longScanForcingEnabled");
//        if (longScanForcingEnabled != null && longScanForcingEnabled.equals("true")) {
        LogManager.i(TAG, "FIXME#BEACON longScanForcingEnabled to keep scans going on Android N for > 30 minutes");
        if (mScanHelper.getCycledScanner() != null) {
            mScanHelper.getCycledScanner().setLongScanForcingEnabled(true);
        }
//        }

        mScanHelper.reloadParsers();

        DistanceCalculator defaultDistanceCalculator = new ModelSpecificDistanceCalculator(this);
        Beacon.setDistanceCalculator(defaultDistanceCalculator);

        // Look for simulated scan data
        try {
            Class klass = Class.forName("org.altbeacon.beacon.SimulatedScanData");
            java.lang.reflect.Field f = klass.getField("beacons");
            mScanHelper.setSimulatedScanData((List<Beacon>) f.get(null));
        } catch (ClassNotFoundException e) {
            LogManager.d(TAG, "No org.altbeacon.beacon.SimulatedScanData class exists.");
        } catch (Exception e) {
            LogManager.e(e, TAG, "Cannot get simulated Scan data.  Make sure your org.altbeacon.beacon.SimulatedScanData class defines a field with the signature 'public static List<Beacon> beacons'");
        }
        startForegroundIfConfigured(this);
    }


    private void ensureNotificationProcessorSetup() {
        if (mBeaconNotificationProcessor == null) {
            mBeaconNotificationProcessor = new BeaconLocalBroadcastProcessor(this);
            mBeaconNotificationProcessor.register();
        }
    }


    private String getManifestMetadataValue(String key) {
        String value = null;
        try {
            PackageItemInfo info = this.getPackageManager().getServiceInfo(new ComponentName(this, BeaconService.class), PackageManager.GET_META_DATA);
            if (info != null && info.metaData != null) {
                return info.metaData.get(key).toString();
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
        return null;
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        LogManager.d(TAG,
                intent == null ?
                        "starting with null intent"
                        :
                        "starting with intent " + intent.toString()
        );
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @NonNull
    @Override
    public IBinder onBind(@NonNull Intent intent) {
        LogManager.i(TAG, "binding");
        return mMessenger.getBinder();
    }

    // called when the last bound client calls unbind
    @Override
    public boolean onUnbind(@NonNull Intent intent) {
        LogManager.i(TAG, "unbinding so destroying self");
        this.stopForeground(true);
        this.stopSelf();
        return false;
    }

    @MainThread
    @Override
    public void onDestroy() {
        LogManager.e(TAG, "onDestroy()");
        if (mBeaconNotificationProcessor != null) {
            mBeaconNotificationProcessor.unregister();
        }
        if (bluetoothCrashResolver != null) {
            bluetoothCrashResolver.stop();
        }
        LogManager.i(TAG, "onDestroy called.  stopping scanning");
        handler.removeCallbacksAndMessages(null);

        if (mScanHelper.getCycledScanner() != null) {
            mScanHelper.getCycledScanner().stop();
            mScanHelper.getCycledScanner().destroy();
        }
        mScanHelper.getMonitoringStatus().stopStatusPreservation(this);
        mScanHelper.terminateThreads();
    }

    @Override
    public void onTaskRemoved(@NonNull Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        LogManager.d(TAG, "task removed");
    }

    /**
     * methods for clients
     */
    @MainThread
    public void startRangingBeaconsInRegion(@NonNull Region region, @NonNull Callback callback) {
        synchronized (mScanHelper.getRangedRegionState()) {
            if (mScanHelper.getRangedRegionState().containsKey(region)) {
                LogManager.i(TAG, "Already ranging that region -- will replace existing region.");
                mScanHelper.getRangedRegionState().remove(region); // need to remove it, otherwise the old object will be retained because they are .equal //FIXME That is not true
            }
            mScanHelper.getRangedRegionState().put(region, new RangeState(callback));
            LogManager.d(TAG, "Currently ranging %s regions.", mScanHelper.getRangedRegionState().size());
        }
        if (mScanHelper.getCycledScanner() != null) {
            mScanHelper.getCycledScanner().start();
        }
    }

    @MainThread
    public void stopRangingBeaconsInRegion(@NonNull Region region) {
        int rangedRegionCount;
        synchronized (mScanHelper.getRangedRegionState()) {
            mScanHelper.getRangedRegionState().remove(region);
            rangedRegionCount = mScanHelper.getRangedRegionState().size();
            LogManager.d(TAG, "Currently ranging %s regions.", mScanHelper.getRangedRegionState().size());
        }

        if (rangedRegionCount == 0 && mScanHelper.getMonitoringStatus().regionsCount(this) == 0) {
            if (mScanHelper.getCycledScanner() != null) {
                mScanHelper.getCycledScanner().stop();
            }
        }
    }

    @MainThread
    public void startMonitoringBeaconsInRegion(@NonNull Region region, @NonNull Callback callback) {
        LogManager.d(TAG, "startMonitoring called");
        mScanHelper.getMonitoringStatus().addRegion(this, region, callback);
        LogManager.d(TAG, "Currently monitoring %s regions.", mScanHelper.getMonitoringStatus().regionsCount(this));
        if (mScanHelper.getCycledScanner() != null) {
            mScanHelper.getCycledScanner().start();
        }
    }

    @MainThread
    public void stopMonitoringBeaconsInRegion(@NonNull Region region) {
        LogManager.d(TAG, "stopMonitoring called");
        mScanHelper.getMonitoringStatus().removeRegion(this, region);
        LogManager.d(TAG, "Currently monitoring %s regions.", mScanHelper.getMonitoringStatus().regionsCount(this));
        if (mScanHelper.getMonitoringStatus().regionsCount(this) == 0 && mScanHelper.getRangedRegionState().size() == 0) {
            if (mScanHelper.getCycledScanner() != null) {
                mScanHelper.getCycledScanner().stop();
            }
        }
    }

    @MainThread
    public void setScanPeriods(long scanPeriod, long betweenScanPeriod, boolean backgroundFlag) {
        if (mScanHelper.getCycledScanner() != null) {
            mScanHelper.getCycledScanner().setScanPeriods(scanPeriod, betweenScanPeriod, backgroundFlag);
        }
    }

    public void reloadParsers() {
        mScanHelper.reloadParsers();
    }

    @NonNull
    @RestrictTo(RestrictTo.Scope.TESTS)
    protected CycledLeScanCallback getCycledLeScanCallback() {
        return mScanHelper.getCycledLeScanCallback();
    }
}
