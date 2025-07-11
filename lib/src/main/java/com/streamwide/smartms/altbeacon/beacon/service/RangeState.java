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

import androidx.annotation.NonNull;

import com.streamwide.smartms.altbeacon.beacon.Beacon;
import com.streamwide.smartms.altbeacon.beacon.logging.LogManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RangeState implements Serializable {
    private static final String TAG = "RangeState";
    private Callback mCallback;
    private final Object lockObj = new Object();
    private Map<Beacon, RangedBeacon> mRangedBeacons = new HashMap<Beacon, RangedBeacon>();
    private static boolean sUseTrackingCache = false;

    public RangeState(@NonNull Callback c) {
        mCallback = c;
    }

    @NonNull
    public Callback getCallback() {
        return mCallback;
    }

    public void addBeacon(@NonNull Beacon beacon) {
        RangedBeacon rangedBeacon = mRangedBeacons.get(beacon);
        if (rangedBeacon != null) {
            if (LogManager.isVerboseLoggingEnabled()) {
                LogManager.d(TAG, "adding %s to existing range for: %s", beacon, rangedBeacon);
            }
            rangedBeacon.updateBeacon(beacon);
        } else {
            if (LogManager.isVerboseLoggingEnabled()) {
                LogManager.d(TAG, "adding %s to new rangedBeacon", beacon);
            }
            mRangedBeacons.put(beacon, new RangedBeacon(beacon));
        }
    }

    // returns a list of beacons that are tracked, and then removes any from the list that should not
    // be there for the next cycle
    @NonNull
    public synchronized Collection<Beacon> finalizeBeacons() {
        Map<Beacon, RangedBeacon> newRangedBeacons = new HashMap<Beacon, RangedBeacon>();
        ArrayList<Beacon> finalizedBeacons = new ArrayList<Beacon>();

        synchronized (lockObj) {
            for (Beacon beacon : mRangedBeacons.keySet()) {
                RangedBeacon rangedBeacon = mRangedBeacons.get(beacon);
                if (rangedBeacon != null) {
                    if (rangedBeacon.isTracked()) {
                        rangedBeacon.commitMeasurements(); // calculates accuracy
                        if (!rangedBeacon.noMeasurementsAvailable()) {
                            finalizedBeacons.add(rangedBeacon.getBeacon());
                        }
                    }
                    // If we still have useful measurements, keep it around but mark it as not
                    // tracked anymore so we don't pass it on as visible unless it is seen again
                    if (!rangedBeacon.noMeasurementsAvailable() == true) {
                        //if TrackingCache is enabled, allow beacon to not receive
                        //measurements for a certain amount of time
                        if (!sUseTrackingCache || rangedBeacon.isExpired())
                            rangedBeacon.setTracked(false);
                        newRangedBeacons.put(beacon, rangedBeacon);
                    } else {
                        LogManager.d(TAG, "Dumping beacon from RangeState because it has no recent measurements.");
                    }
                }
            }
            mRangedBeacons = newRangedBeacons;
        }

        return finalizedBeacons;
    }

    public static void setUseTrackingCache(boolean useTrackingCache) {
        RangeState.sUseTrackingCache = useTrackingCache;
    }

    public static boolean getUseTrackingCache() {
        return sUseTrackingCache;
    }

}
