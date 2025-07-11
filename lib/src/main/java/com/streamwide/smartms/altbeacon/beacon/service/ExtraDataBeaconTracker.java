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
import androidx.annotation.Nullable;

import com.streamwide.smartms.altbeacon.beacon.Beacon;
import com.streamwide.smartms.altbeacon.beacon.utils.SerializableSparseArray;
import com.streamwide.smartms.altbeacon.beacon.utils.SparseArrayIterator;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Keeps track of beacons that have ever been seen and
 * merges them together depending on configured beacon parsers
 * Created by dyoung on 5/5/15.
 */
public class ExtraDataBeaconTracker implements Serializable {
    private static final String TAG = "BeaconTracker";

    /**
     * This is a lookup table to find tracked beacons by the calculated beacon key
     */
    @NonNull
    private final HashMap<String, SerializableSparseArray<Beacon>> mBeaconsByKey = new HashMap<>();

    private final boolean matchBeaconsByServiceUUID;

    public ExtraDataBeaconTracker() {
        this(true);
    }

    public ExtraDataBeaconTracker(boolean matchBeaconsByServiceUUID) {
        this.matchBeaconsByServiceUUID = matchBeaconsByServiceUUID;
    }

    /**
     * Tracks a beacon. For Gatt-based beacons, returns a merged copy of fields from multiple
     * frames. Returns null when passed a Gatt-based beacon that has is only extra beacon data.
     */
    @Nullable
    public synchronized Beacon track(@NonNull Beacon beacon) {
        Beacon trackedBeacon = null;
        if (beacon.isMultiFrameBeacon() || beacon.getServiceUuid() != -1) {
            trackedBeacon = trackGattBeacon(beacon);
        } else {
            trackedBeacon = beacon;
        }
        return trackedBeacon;
    }

    /**
     * The following code is for dealing with merging data fields in beacons
     */
    @Nullable
    private Beacon trackGattBeacon(@NonNull Beacon beacon) {
        if (beacon.isExtraBeaconData()) {
            updateTrackedBeacons(beacon);
            return null;
        }

        String key = getBeaconKey(beacon);
        SerializableSparseArray<Beacon> matchingTrackedBeacons = mBeaconsByKey.get(key);
        if (null == matchingTrackedBeacons) {
            matchingTrackedBeacons = new SerializableSparseArray<>();
        } else {
            Beacon trackedBeacon = SparseArrayIterator.iterate(matchingTrackedBeacons).next();
            beacon.setExtraDataFields(trackedBeacon.getExtraDataFields());
        }
        matchingTrackedBeacons.put(beacon.hashCode(), beacon);
        mBeaconsByKey.put(key, matchingTrackedBeacons);

        return beacon;
    }

    private void updateTrackedBeacons(@NonNull Beacon beacon) {
        SerializableSparseArray<Beacon> matchingTrackedBeacons = mBeaconsByKey.get(getBeaconKey(beacon));
        if (null != matchingTrackedBeacons) {
            for (int i = 0, nSize = matchingTrackedBeacons.size(); i < nSize; i++) {
                Beacon matchingTrackedBeacon = matchingTrackedBeacons.valueAt(i);
                matchingTrackedBeacon.setRssi(beacon.getRssi());
                matchingTrackedBeacon.setExtraDataFields(beacon.getDataFields());
            }
        }
    }

    private String getBeaconKey(@NonNull Beacon beacon) {
        if (matchBeaconsByServiceUUID) {
            return beacon.getBluetoothAddress() + beacon.getServiceUuid();
        } else {
            return beacon.getBluetoothAddress();
        }
    }
}
