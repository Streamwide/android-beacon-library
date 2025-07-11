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

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.streamwide.smartms.altbeacon.beacon.Beacon;
import com.streamwide.smartms.altbeacon.beacon.Region;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Internal class used to transfer ranging data between the BeaconService and the client
 *
 * @hide
 */
public class RangingData {
    private static final String TAG = "RangingData";
    private final Collection<Beacon> mBeacons;
    private final Object lockObj = new Object();
    private final Region mRegion;
    private static final String REGION_KEY = "region";
    private static final String BEACONS_KEY = "beacons";

    public RangingData(@NonNull Collection<Beacon> beacons, @NonNull Region region) {
        synchronized (lockObj) {
            this.mBeacons = beacons;
        }
        this.mRegion = region;
    }

    @NonNull
    public Collection<Beacon> getBeacons() {
        return mBeacons;
    }

    @NonNull
    public Region getRegion() {
        return mRegion;
    }

    @NonNull
    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(REGION_KEY, mRegion);
        ArrayList<Serializable> serializableBeacons = new ArrayList<Serializable>();
        for (Beacon beacon : mBeacons) {
            serializableBeacons.add(beacon);
        }
        bundle.putSerializable(BEACONS_KEY, serializableBeacons);

        return bundle;
    }

    @NonNull
    public static RangingData fromBundle(@NonNull Bundle bundle) {
        bundle.setClassLoader(Thread.currentThread().getContextClassLoader());
        Region region = null;
        Collection<Beacon> beacons = null;
        if (bundle.get(BEACONS_KEY) != null) {
            beacons = (Collection<Beacon>) bundle.getSerializable(BEACONS_KEY);
        }
        if (bundle.get(REGION_KEY) != null) {
            region = (Region) bundle.getSerializable(REGION_KEY);
        }

        return new RangingData(beacons, region);
    }

}
