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

import com.streamwide.smartms.altbeacon.beacon.Region;

public class MonitoringData {
    @SuppressWarnings("unused")
    private static final String TAG = "MonitoringData";
    private final boolean mInside;
    private final Region mRegion;
    private static final String REGION_KEY = "region";
    private static final String INSIDE_KEY = "inside";

    public MonitoringData(boolean inside, @NonNull Region region) {
        this.mInside = inside;
        this.mRegion = region;
    }

    public boolean isInside() {
        return mInside;
    }

    @NonNull
    public Region getRegion() {
        return mRegion;
    }

    @NonNull
    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(REGION_KEY, mRegion);
        bundle.putBoolean(INSIDE_KEY, mInside);

        return bundle;
    }

    @NonNull
    public static MonitoringData fromBundle(@NonNull Bundle bundle) {
        bundle.setClassLoader(Thread.currentThread().getContextClassLoader());
        Region region = null;
        if (bundle.get(REGION_KEY) != null) {
            region = (Region) bundle.getSerializable(REGION_KEY);
        }
        Boolean inside = bundle.getBoolean(INSIDE_KEY);
        return new MonitoringData(inside, region);
    }

}
