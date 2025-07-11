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

import android.content.Context;

import androidx.annotation.NonNull;

import com.streamwide.smartms.altbeacon.beacon.BeaconManager;
import com.streamwide.smartms.altbeacon.beacon.BeaconParser;
import com.streamwide.smartms.altbeacon.beacon.Region;
import com.streamwide.smartms.altbeacon.beacon.io.IoFileConfiguration;
import com.streamwide.smartms.altbeacon.beacon.logger.Logger;
import com.streamwide.smartms.altbeacon.beacon.logging.LogManager;
import com.streamwide.smartms.lib.template.serialization.ValidatorClassNameMatcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Stores the full state of scanning for the libary, including all settings so it can be ressurrected easily
 * for running from a scheduled job
 * <p>
 * Created by dyoung on 3/26/17.
 *
 * @hide
 */

public class ScanState implements Serializable {
    private static final String TAG = ScanState.class.getSimpleName();
    private static final String STATUS_PRESERVATION_FILE_NAME = "android-beacon-library-scan-state";
    private static final String TEMP_STATUS_PRESERVATION_FILE_NAME = "android-beacon-library-scan-state-temp";
    public static int MIN_SCAN_JOB_INTERVAL_MILLIS = 300000; //  5 minutes

    private Map<Region, RangeState> mRangedRegionState = new HashMap<Region, RangeState>();
    private transient MonitoringStatus mMonitoringStatus;
    private Set<BeaconParser> mBeaconParsers = new HashSet<BeaconParser>();
    private ExtraDataBeaconTracker mExtraBeaconDataTracker = new ExtraDataBeaconTracker();
    private long mForegroundBetweenScanPeriod;
    private long mBackgroundBetweenScanPeriod;
    private long mForegroundScanPeriod;
    private long mBackgroundScanPeriod;
    private boolean mBackgroundMode;
    private long mLastScanStartTimeMillis = 0l;
    private transient Context mContext;

    @NonNull
    public Boolean getBackgroundMode() {
        return mBackgroundMode;
    }

    public void setBackgroundMode(@NonNull Boolean backgroundMode) {
        mBackgroundMode = backgroundMode;
    }

    @NonNull
    public Long getBackgroundBetweenScanPeriod() {
        return mBackgroundBetweenScanPeriod;
    }

    public void setBackgroundBetweenScanPeriod(@NonNull Long backgroundBetweenScanPeriod) {
        mBackgroundBetweenScanPeriod = backgroundBetweenScanPeriod;
    }

    @NonNull
    public Long getBackgroundScanPeriod() {
        return mBackgroundScanPeriod;
    }

    public void setBackgroundScanPeriod(@NonNull Long backgroundScanPeriod) {
        mBackgroundScanPeriod = backgroundScanPeriod;
    }

    @NonNull
    public Long getForegroundBetweenScanPeriod() {
        return mForegroundBetweenScanPeriod;
    }

    public void setForegroundBetweenScanPeriod(@NonNull Long foregroundBetweenScanPeriod) {
        mForegroundBetweenScanPeriod = foregroundBetweenScanPeriod;
    }

    @NonNull
    public Long getForegroundScanPeriod() {
        return mForegroundScanPeriod;
    }

    public void setForegroundScanPeriod(@NonNull Long foregroundScanPeriod) {
        mForegroundScanPeriod = foregroundScanPeriod;
    }

    public ScanState(@NonNull Context context) {
        mContext = context;
    }

    @NonNull
    public MonitoringStatus getMonitoringStatus() {
        return mMonitoringStatus;
    }

    public void setMonitoringStatus(@NonNull MonitoringStatus monitoringStatus) {
        mMonitoringStatus = monitoringStatus;
    }

    @NonNull
    public Map<Region, RangeState> getRangedRegionState() {
        return mRangedRegionState;
    }

    public void setRangedRegionState(@NonNull Map<Region, RangeState> rangedRegionState) {
        mRangedRegionState = rangedRegionState;
    }

    @NonNull
    public ExtraDataBeaconTracker getExtraBeaconDataTracker() {
        return mExtraBeaconDataTracker;
    }

    public void setExtraBeaconDataTracker(@NonNull ExtraDataBeaconTracker extraDataBeaconTracker) {
        mExtraBeaconDataTracker = extraDataBeaconTracker;
    }

    @NonNull
    public Set<BeaconParser> getBeaconParsers() {
        return mBeaconParsers;
    }

    public void setBeaconParsers(@NonNull Set<BeaconParser> beaconParsers) {
        mBeaconParsers = beaconParsers;
    }

    public long getLastScanStartTimeMillis() {
        return mLastScanStartTimeMillis;
    }

    public void setLastScanStartTimeMillis(long time) {
        mLastScanStartTimeMillis = time;
    }

    @NonNull
    public static ScanState restore(@NonNull Context context) {
        ScanState scanState = null;
        synchronized (ScanState.class) {
            try {
                String tempFilePath = IoFileConfiguration.getIoFileStrategy().getRootDirectoryPath(context) + File.separator + STATUS_PRESERVATION_FILE_NAME;

                ValidatorClassNameMatcher validatorClassNameMatcher = new ValidatorClassNameMatcher
                        .Builder()
                        .accept("*")
                        .build();
                scanState = (ScanState) IoFileConfiguration.getIoFileStrategy().readObject(context, tempFilePath, validatorClassNameMatcher);
                if (scanState != null) {
                    scanState.mContext = context;
                }
            } catch (FileNotFoundException fnfe) {
                LogManager.w(TAG, "Serialized ScanState does not exist.  This may be normal on first run.");
            } catch (IOException | ClassCastException e) {
                if (e instanceof InvalidClassException) {
                    LogManager.d(TAG, "Serialized ScanState has wrong class. Just ignoring saved state...");
                } else {
                    LogManager.e(TAG, "Deserialization exception");
                    Logger.error(TAG, "error: ", e);
                }

            }
            if (scanState == null) {
                scanState = new ScanState(context);

            }
            if (scanState.mExtraBeaconDataTracker == null) {
                scanState.mExtraBeaconDataTracker = new ExtraDataBeaconTracker();
            }
            scanState.mMonitoringStatus = MonitoringStatus.getInstance();
            LogManager.d(TAG, "Scan state restore regions: monitored=" + scanState.getMonitoringStatus().regions(context).size() + " ranged=" + scanState.getRangedRegionState().keySet().size());
            return scanState;
        }
    }

    public void save() {
        synchronized (ScanState.class) {
            // TODO: need to limit how big this object is somehow.
            // Impose limits on ranged and monitored regions?
            String tempFilePath = IoFileConfiguration.getIoFileStrategy().getRootDirectoryPath(mContext) + File.separator + TEMP_STATUS_PRESERVATION_FILE_NAME;
            String tempStatusFilePath = IoFileConfiguration.getIoFileStrategy().getRootDirectoryPath(mContext) + File.separator + STATUS_PRESERVATION_FILE_NAME;
            try {

                IoFileConfiguration.getIoFileStrategy().writeObject(mContext, this, tempFilePath);
            } catch (IOException | GeneralSecurityException e) {
                LogManager.e(TAG, "Error while saving scan status to file: ", e.getMessage());
            }

            File file = new File(tempStatusFilePath);
            File tempFile = new File(tempFilePath);
            LogManager.d(TAG, "Temp file is " + tempFile.getAbsolutePath());
            LogManager.d(TAG, "Perm file is " + file.getAbsolutePath());

            if (!file.delete()) {
                LogManager.e(TAG, "Error while saving scan status to file: Cannot delete existing file.");
            }
            if (!tempFile.renameTo(file)) {
                LogManager.e(TAG, "Error while saving scan status to file: Cannot rename temp file.");
            }

            mMonitoringStatus.saveMonitoringStatusIfOn(mContext);
        }
    }

    public int getScanJobIntervalMillis() {
        long cyclePeriodMillis;
        if (getBackgroundMode()) {
            cyclePeriodMillis = getBackgroundScanPeriod() + getBackgroundBetweenScanPeriod();
        } else {
            cyclePeriodMillis = getForegroundScanPeriod() + getForegroundBetweenScanPeriod();
        }
        int scanJobIntervalMillis = MIN_SCAN_JOB_INTERVAL_MILLIS;
        if (cyclePeriodMillis > MIN_SCAN_JOB_INTERVAL_MILLIS) {
            scanJobIntervalMillis = (int) cyclePeriodMillis;
        }
        return scanJobIntervalMillis;
    }

    public int getScanJobRuntimeMillis() {
        long scanPeriodMillis;
        LogManager.d(TAG, "ScanState says background mode for ScanJob is " + getBackgroundMode());
        if (getBackgroundMode()) {
            scanPeriodMillis = getBackgroundScanPeriod();
        } else {
            scanPeriodMillis = getForegroundScanPeriod();
        }
        if (!getBackgroundMode()) {
            // if we are in the foreground, we keep the scan job going for the minimum interval
            if (scanPeriodMillis < MIN_SCAN_JOB_INTERVAL_MILLIS) {
                return MIN_SCAN_JOB_INTERVAL_MILLIS;
            }
        }
        return (int) scanPeriodMillis;
    }


    public void applyChanges(@NonNull BeaconManager beaconManager) {
        mBeaconParsers = new HashSet<>(beaconManager.getBeaconParsers());
        mForegroundScanPeriod = beaconManager.getForegroundScanPeriod();
        mForegroundBetweenScanPeriod = beaconManager.getForegroundBetweenScanPeriod();
        mBackgroundScanPeriod = beaconManager.getBackgroundScanPeriod();
        mBackgroundBetweenScanPeriod = beaconManager.getBackgroundBetweenScanPeriod();
        mBackgroundMode = beaconManager.getBackgroundMode();

        ArrayList<Region> existingMonitoredRegions = new ArrayList<>(mMonitoringStatus.regions(mContext));
        ArrayList<Region> existingRangedRegions = new ArrayList<>(mRangedRegionState.keySet());
        ArrayList<Region> newMonitoredRegions = new ArrayList<>(beaconManager.getMonitoredRegions());
        ArrayList<Region> newRangedRegions = new ArrayList<>(beaconManager.getRangedRegions());
        LogManager.d(TAG, "ranged regions: old=" + existingRangedRegions.size() + " new=" + newRangedRegions.size());
        LogManager.d(TAG, "monitored regions: old=" + existingMonitoredRegions.size() + " new=" + newMonitoredRegions.size());

        for (Region newRangedRegion : newRangedRegions) {
            if (!existingRangedRegions.contains(newRangedRegion)) {
                LogManager.d(TAG, "Starting ranging region: " + newRangedRegion);
                mRangedRegionState.put(newRangedRegion, new RangeState(new Callback(mContext.getPackageName())));
            }
        }
        for (Region existingRangedRegion : existingRangedRegions) {
            if (!newRangedRegions.contains(existingRangedRegion)) {
                LogManager.d(TAG, "Stopping ranging region: " + existingRangedRegion);
                mRangedRegionState.remove(existingRangedRegion);
            }
        }
        LogManager.d(TAG, "Updated state with " + newRangedRegions.size() + " ranging regions and " + newMonitoredRegions.size() + " monitoring regions.");

        this.save();
    }

    @Override
    public String toString() {
        return "ScanState{" +
                "mRangedRegionState=" + mRangedRegionState +
                ", mMonitoringStatus=" + mMonitoringStatus +
                ", mBeaconParsers=" + mBeaconParsers +
                ", mExtraBeaconDataTracker=" + mExtraBeaconDataTracker +
                ", mForegroundBetweenScanPeriod=" + mForegroundBetweenScanPeriod +
                ", mBackgroundBetweenScanPeriod=" + mBackgroundBetweenScanPeriod +
                ", mForegroundScanPeriod=" + mForegroundScanPeriod +
                ", mBackgroundScanPeriod=" + mBackgroundScanPeriod +
                ", mBackgroundMode=" + mBackgroundMode +
                ", mLastScanStartTimeMillis=" + mLastScanStartTimeMillis +
                '}';
    }
}