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

import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.altbeacon.beacon.Beacon;
import com.streamwide.smartms.altbeacon.beacon.BeaconManager;
import com.streamwide.smartms.altbeacon.beacon.logging.LogManager;

import java.io.Serializable;
import java.lang.reflect.Constructor;

public class RangedBeacon implements Serializable {

    private static final String TAG = "RangedBeacon";
    public static final long DEFAULT_MAX_TRACKING_AGE = 5000; /* 5 Seconds */
    public static long maxTrackingAge = DEFAULT_MAX_TRACKING_AGE; /* 5 Seconds */
    //kept here for backward compatibility
    public static final long DEFAULT_SAMPLE_EXPIRATION_MILLISECONDS = 20000; /* 20 seconds */
    private static long sampleExpirationMilliseconds = DEFAULT_SAMPLE_EXPIRATION_MILLISECONDS;
    private boolean mTracked = true;
    protected long lastTrackedTimeMillis = 0;
    Beacon mBeacon;
    @Nullable
    protected transient RssiFilter mFilter = null;
    private int packetCount = 0;
    private long firstCycleDetectionTimestamp = 0;
    private long lastCycleDetectionTimestamp = 0;

    public RangedBeacon(@NonNull Beacon beacon) {
        updateBeacon(beacon);
    }

    public void updateBeacon(@NonNull Beacon beacon) {
        packetCount += 1;
        mBeacon = beacon;
        if (firstCycleDetectionTimestamp == 0) {
            firstCycleDetectionTimestamp = beacon.getFirstCycleDetectionTimestamp();
        }
        lastCycleDetectionTimestamp = beacon.getLastCycleDetectionTimestamp();
        addMeasurement(mBeacon.getRssi());
    }

    public boolean isTracked() {
        return mTracked;
    }

    public void setTracked(boolean tracked) {
        mTracked = tracked;
    }

    @NonNull
    public Beacon getBeacon() {
        return mBeacon;
    }

    // Done at the end of each cycle before data are sent to the client
    public void commitMeasurements() {
        RssiFilter rssiFilter = getFilter();
        if (rssiFilter != null && !rssiFilter.noMeasurementsAvailable()) {
            double runningAverage = rssiFilter.calculateRssi();
            mBeacon.setRunningAverageRssi(runningAverage);
            mBeacon.setRssiMeasurementCount(rssiFilter.getMeasurementCount());
            LogManager.d(TAG, "calculated new runningAverageRssi: %s", runningAverage);
        } else {
            LogManager.d(TAG, "No measurements available to calculate running average");
        }
        mBeacon.setPacketCount(packetCount);
        mBeacon.setFirstCycleDetectionTimestamp(firstCycleDetectionTimestamp);
        mBeacon.setLastCycleDetectionTimestamp(lastCycleDetectionTimestamp);
        packetCount = 0;
        firstCycleDetectionTimestamp = 0L;
        lastCycleDetectionTimestamp = 0L;
    }

    public void addMeasurement(@NonNull Integer rssi) {
        // Filter out unreasonable values per
        // http://stackoverflow.com/questions/30118991/rssi-returned-by-altbeacon-library-127-messes-up-distance
        if (rssi != 127) {
            mTracked = true;
            lastTrackedTimeMillis = SystemClock.elapsedRealtime();
            if (getFilter() != null) {
                mFilter.addMeasurement(rssi);
            }
        }
    }

    //kept here for backward compatibility
    public static void setSampleExpirationMilliseconds(long milliseconds) {
        sampleExpirationMilliseconds = milliseconds;
        RunningAverageRssiFilter.setSampleExpirationMilliseconds(sampleExpirationMilliseconds);
    }

    public static void setMaxTrackinAge(int maxTrackinAge) {
        RangedBeacon.maxTrackingAge = maxTrackinAge;
    }

    public boolean noMeasurementsAvailable() {
        if (getFilter() != null) {
            return mFilter.noMeasurementsAvailable();
        } else {
            return false;
        }

    }

    public long getTrackingAge() {
        return SystemClock.elapsedRealtime() - lastTrackedTimeMillis;
    }

    public boolean isExpired() {
        return getTrackingAge() > maxTrackingAge;
    }

    private RssiFilter getFilter() {
        if (mFilter == null) {
            //set RSSI filter
            try {
                Constructor cons = BeaconManager.getRssiFilterImplClass().getConstructors()[0];
                mFilter = (RssiFilter) cons.newInstance();
            } catch (Exception e) {
                LogManager.e(TAG, "Could not construct RssiFilterImplClass %s", BeaconManager.getRssiFilterImplClass().getName());
            }
        }
        return mFilter;
    }

}
