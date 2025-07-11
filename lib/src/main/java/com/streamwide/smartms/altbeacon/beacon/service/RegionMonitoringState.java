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

import com.streamwide.smartms.altbeacon.beacon.BeaconManager;
import com.streamwide.smartms.altbeacon.beacon.logging.LogManager;

import java.io.Serializable;

public class RegionMonitoringState implements Serializable {
    private static final String TAG = RegionMonitoringState.class.getSimpleName();
    private boolean inside = false;
    private long lastSeenTime = 0l;
    private final Callback callback;

    public RegionMonitoringState(@NonNull Callback c) {
        callback = c;
    }

    @NonNull
    public Callback getCallback() {
        return callback;
    }

    // returns true if it is newly inside
    public boolean markInside() {
        lastSeenTime = SystemClock.elapsedRealtime();
        if (!inside) {
            inside = true;
            return true;
        }
        return false;
    }

    public void markOutside() {
        inside = false;
        lastSeenTime = 0l;
    }

    public boolean markOutsideIfExpired() {
        if (inside) {
            if (lastSeenTime > 0 && SystemClock.elapsedRealtime() - lastSeenTime > BeaconManager.getRegionExitPeriod()) {
                LogManager.d(TAG, "We are newly outside the region because the lastSeenTime of %s "
                                + "was %s seconds ago, and that is over the expiration duration "
                                + "of %s", lastSeenTime, SystemClock.elapsedRealtime() - lastSeenTime,
                        BeaconManager.getRegionExitPeriod());
                markOutside();
                return true;
            }
        }
        return false;
    }

    public boolean getInside() {
        return inside;
    }
}
