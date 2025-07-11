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

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.WorkerThread;

import com.streamwide.smartms.altbeacon.beacon.Beacon;
import com.streamwide.smartms.altbeacon.beacon.BeaconManager;
import com.streamwide.smartms.altbeacon.beacon.BeaconParser;
import com.streamwide.smartms.altbeacon.beacon.Region;
import com.streamwide.smartms.altbeacon.beacon.logging.LogManager;
import com.streamwide.smartms.altbeacon.beacon.service.scanner.CycledLeScanCallback;
import com.streamwide.smartms.altbeacon.beacon.service.scanner.CycledLeScanner;
import com.streamwide.smartms.altbeacon.beacon.service.scanner.DistinctPacketDetector;
import com.streamwide.smartms.altbeacon.beacon.service.scanner.NonBeaconLeScanCallback;
import com.streamwide.smartms.altbeacon.beacon.startup.StartupBroadcastReceiver;
import com.streamwide.smartms.altbeacon.bluetooth.BluetoothCrashResolver;
import com.streamwide.smartms.altbeacon.util.ScanHelperUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by dyoung on 6/16/17.
 * <p>
 * This is an internal utility class and should not be called directly by library users.
 * <p>
 * This encapsulates shared data and methods used by both ScanJob and BeaconService
 * that deal with the specifics of beacon scanning.
 *
 * @hide
 */

class ScanHelper {
    protected static final String TAG = ScanHelper.class.getSimpleName();
    @Nullable
    private ExecutorService mExecutor;
    private BeaconManager mBeaconManager;
    @Nullable
    protected CycledLeScanner mCycledScanner;
    protected MonitoringStatus mMonitoringStatus;
    private final Map<Region, RangeState> mRangedRegionState = new HashMap<>();
    protected DistinctPacketDetector mDistinctPacketDetector = new DistinctPacketDetector();

    @NonNull
    private ExtraDataBeaconTracker mExtraDataBeaconTracker = new ExtraDataBeaconTracker();

    protected Set<BeaconParser> mBeaconParsers = new HashSet<>();
    private List<Beacon> mSimulatedScanData = null;
    @NonNull
    protected Context mContext;

    ScanHelper(Context context) {
        mContext = context;
        mBeaconManager = BeaconManager.getInstanceForApplication(context);
    }

    private ExecutorService getExecutor() {
        if (mExecutor == null) {
            mExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
        }
        return mExecutor;
    }

    void terminateThreads() {
        if (mExecutor != null) {
            mExecutor.shutdown();
            try {
                if (!mExecutor.awaitTermination(10, TimeUnit.MILLISECONDS)) {
                    LogManager.e(TAG, "Can't stop beacon parsing thread.");
                }
            } catch (InterruptedException e) {
                LogManager.e(TAG, "Interrupted waiting to stop beacon parsing thread.", e);
                Thread.currentThread().interrupt();
            }
            mExecutor = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        terminateThreads();
    }

    @Nullable
    CycledLeScanner getCycledScanner() {
        return mCycledScanner;
    }

    MonitoringStatus getMonitoringStatus() {
        return mMonitoringStatus;
    }

    void setMonitoringStatus(MonitoringStatus monitoringStatus) {
        mMonitoringStatus = monitoringStatus;
    }

    Map<Region, RangeState> getRangedRegionState() {
        return mRangedRegionState;
    }

    void setRangedRegionState(Map<Region, RangeState> rangedRegionState) {
        synchronized (mRangedRegionState) {
            mRangedRegionState.clear();
            mRangedRegionState.putAll(rangedRegionState);
        }
    }

    void setExtraDataBeaconTracker(@NonNull ExtraDataBeaconTracker extraDataBeaconTracker) {
        mExtraDataBeaconTracker = extraDataBeaconTracker;
    }

    void setBeaconParsers(Set<BeaconParser> beaconParsers) {
        mBeaconParsers = beaconParsers;
    }

    void setSimulatedScanData(List<Beacon> simulatedScanData) {
        mSimulatedScanData = simulatedScanData;
    }


    void createCycledLeScanner(boolean backgroundMode, BluetoothCrashResolver crashResolver) {
        mCycledScanner = CycledLeScanner.createScanner(mContext, BeaconManager.DEFAULT_FOREGROUND_SCAN_PERIOD,
                BeaconManager.DEFAULT_FOREGROUND_BETWEEN_SCAN_PERIOD, backgroundMode,
                mCycledLeScanCallback, crashResolver);
    }

    void processScanResult(BluetoothDevice device, int rssi, byte[] scanRecord, long timestampMs) {
        NonBeaconLeScanCallback nonBeaconLeScanCallback = mBeaconManager.getNonBeaconLeScanCallback();

        try {
            new ScanProcessor(this, nonBeaconLeScanCallback).executeOnExecutor(getExecutor(),
                    new ScanData(device, rssi, scanRecord, timestampMs));
        } catch (RejectedExecutionException e) {
            LogManager.w(TAG, "Ignoring scan result because we cannot keep up.");
        } catch (OutOfMemoryError e) {
            LogManager.w(TAG, "Ignoring scan result because we cannot start a thread to keep up.");
        }
    }

    void reloadParsers() {
        HashSet<BeaconParser> newBeaconParsers = new HashSet<>();
        //flatMap all beacon parsers
        boolean matchBeaconsByServiceUUID = true;
        newBeaconParsers.addAll(mBeaconManager.getBeaconParsers());
        for (BeaconParser beaconParser : mBeaconManager.getBeaconParsers()) {
            if (beaconParser.getExtraDataParsers().size() > 0) {
                matchBeaconsByServiceUUID = false;
                newBeaconParsers.addAll(beaconParser.getExtraDataParsers());
            }
        }
        mBeaconParsers = newBeaconParsers;
        //initialize the extra data beacon tracker
        mExtraDataBeaconTracker = new ExtraDataBeaconTracker(matchBeaconsByServiceUUID);
    }

    void startAndroidOBackgroundScan(Set<BeaconParser> beaconParsers) {
        ScanHelperUtil.startAndroidOBackgroundScan(mContext, beaconParsers, this::getScanCallbackIntent);
    }

    void stopAndroidOBackgroundScan() {
        ScanHelperUtil.stopAndroidOBackgroundScan(mContext, this::getScanCallbackIntent);
    }

    // Low power scan results in the background will be delivered via Intent
    PendingIntent getScanCallbackIntent() {
        Intent intent = new Intent(mContext, StartupBroadcastReceiver.class);
        intent.putExtra("o-scan", true);
        return PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private final CycledLeScanCallback mCycledLeScanCallback = new CycledLeScanCallback() {
        @Override
        @MainThread
        public void onLeScan(@NonNull BluetoothDevice device, int rssi, @NonNull byte[] scanRecord, long timestampMs) {
            processScanResult(device, rssi, scanRecord, timestampMs);
        }

        @Override
        @MainThread
        @SuppressLint("WrongThread")
        public void onCycleEnd() {
            if (BeaconManager.getBeaconSimulator() != null) {
                LogManager.d(TAG, "Beacon simulator enabled");
                // if simulatedScanData is provided, it will be seen every scan cycle.  *in addition* to anything actually seen in the air
                // it will not be used if we are not in debug mode
                if (BeaconManager.getBeaconSimulator().getBeacons() != null) {
                    if (0 != (mContext.getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE)) {
                        LogManager.d(TAG, "Beacon simulator returns " + BeaconManager.getBeaconSimulator().getBeacons().size() + " beacons.");
                        for (Beacon beacon : BeaconManager.getBeaconSimulator().getBeacons()) {
                            // This is an expensive call and we do not want to block the main thread.
                            // But here we are in debug/test mode so we allow it on the main thread.
                            //noinspection WrongThread
                            processBeaconFromScan(beacon);
                        }
                    } else {
                        LogManager.w(TAG, "Beacon simulations provided, but ignored because we are not running in debug mode.  Please remove beacon simulations for production.");
                    }
                } else {
                    LogManager.w(TAG, "getBeacons is returning null. No simulated beacons to report.");
                }
            } else {
                if (LogManager.isVerboseLoggingEnabled()) {
                    LogManager.d(TAG, "Beacon simulator not enabled");
                }
            }
            mDistinctPacketDetector.clearDetections();
            mMonitoringStatus.updateNewlyOutside(mContext);
            processRangeData();
        }
    };

    @RestrictTo(RestrictTo.Scope.TESTS)
    CycledLeScanCallback getCycledLeScanCallback() {
        return mCycledLeScanCallback;
    }

    protected void processRangeData() {
        synchronized (mRangedRegionState) {
            for (Region region : mRangedRegionState.keySet()) {
                RangeState rangeState = mRangedRegionState.get(region);
                LogManager.d(TAG, "Calling ranging callback");
                rangeState.getCallback().call(mContext, "rangingData", new RangingData(rangeState.finalizeBeacons(), region).toBundle());
            }
        }
    }

    /**
     * Helper for processing BLE beacons. This has been extracted from {@link ScanProcessor} to
     * support simulated scan data for test and debug environments.
     * <p>
     * Processing beacons is a frequent and expensive operation. It should not be run on the main
     * thread to avoid UI contention.
     */
    @WorkerThread
    protected void processBeaconFromScan(@NonNull Beacon beacon) {
        if (Stats.getInstance().isEnabled()) {
            Stats.getInstance().log(beacon);
        }
        if (LogManager.isVerboseLoggingEnabled()) {
            LogManager.d(TAG,
                    "beacon detected : %s", beacon.toString());
        }

        beacon = mExtraDataBeaconTracker.track(beacon);
        // If this is a Gatt beacon that should be ignored, it will be set to null as a result of
        // the above
        if (beacon == null) {
            if (LogManager.isVerboseLoggingEnabled()) {
                LogManager.d(TAG,
                        "not processing detections for GATT extra data beacon");
            }
        } else {

            mMonitoringStatus.updateNewlyInsideInRegionsContaining(mContext, beacon);

            List<Region> matchedRegions;
            Iterator<Region> matchedRegionIterator;
            LogManager.d(TAG, "looking for ranging region matches for this beacon");
            synchronized (mRangedRegionState) {
                matchedRegions = matchingRegions(beacon, mRangedRegionState.keySet());
                matchedRegionIterator = matchedRegions.iterator();
                while (matchedRegionIterator.hasNext()) {
                    Region region = matchedRegionIterator.next();
                    LogManager.d(TAG, "matches ranging region: %s", region);
                    RangeState rangeState = mRangedRegionState.get(region);
                    if (rangeState != null) {
                        rangeState.addBeacon(beacon);
                    }
                }
            }
        }
    }

    /**
     * <strong>This class is not thread safe.</strong>
     */
    private class ScanData {
        ScanData(@NonNull BluetoothDevice device, int rssi, @NonNull byte[] scanRecord, long timestampMs) {
            this.device = device;
            this.rssi = rssi;
            this.scanRecord = scanRecord;
            this.timestampMs = timestampMs;
        }

        final int rssi;

        @NonNull
        BluetoothDevice device;

        @NonNull
        byte[] scanRecord;

        long timestampMs;
    }

    private static class ScanProcessor extends AsyncTask<ScanData, Void, Void> {

        final DetectionTracker mDetectionTracker = DetectionTracker.getInstance();
        private final WeakReference<ScanHelper> mReference;

        private final NonBeaconLeScanCallback mNonBeaconLeScanCallback;

        ScanProcessor(@NonNull ScanHelper reference, NonBeaconLeScanCallback nonBeaconLeScanCallback) {
            mNonBeaconLeScanCallback = nonBeaconLeScanCallback;
            this.mReference = new WeakReference<>(reference);
        }

        @NonNull
        protected ScanHelper getReference() {
            return mReference.get();
        }

        @WorkerThread
        @Override
        protected Void doInBackground(ScanData... params) {

            if (null == getReference()) {
                return null;
            }
            ScanData scanData = params[0];
            Beacon beacon = null;

            for (BeaconParser parser : getReference().mBeaconParsers) {
                beacon = parser.fromScanData(scanData.scanRecord, scanData.rssi, scanData.device, scanData.timestampMs);

                if (beacon != null) {
                    break;
                }
            }
            if (beacon != null) {
                if (LogManager.isVerboseLoggingEnabled()) {
                    LogManager.d(TAG, "Beacon packet detected for: " + beacon + " with rssi " + beacon.getRssi());
                }
                mDetectionTracker.recordDetection();
                if (getReference().mCycledScanner != null && !getReference().mCycledScanner.getDistinctPacketsDetectedPerScan()) {
                    if (!getReference().mDistinctPacketDetector.isPacketDistinct(scanData.device.getAddress(),
                            scanData.scanRecord)) {
                        LogManager.i(TAG, "Non-distinct packets detected in a single scan.  Restarting scans unecessary.");
                        getReference().mCycledScanner.setDistinctPacketsDetectedPerScan(true);
                    }
                }
                getReference().processBeaconFromScan(beacon);
            } else {
                if (mNonBeaconLeScanCallback != null) {
                    mNonBeaconLeScanCallback.onNonBeaconLeScan(scanData.device, scanData.rssi, scanData.scanRecord);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //Nothing to do ...
        }

        @Override
        protected void onPreExecute() {
            //Nothing to do ...
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            //Nothing to do ...
        }
    }

    private List<Region> matchingRegions(Beacon beacon, Collection<Region> regions) {
        List<Region> matched = new ArrayList<>();
        for (Region region : regions) {
            // Need to check if region is null in case it was removed from the collection by
            // another thread during iteration
            if (region != null) {
                if (region.matchesBeacon(beacon)) {
                    matched.add(region);
                } else {
                    LogManager.d(TAG, "This region (%s) does not match beacon: %s", region, beacon);
                }
            }
        }
        return matched;
    }

}
