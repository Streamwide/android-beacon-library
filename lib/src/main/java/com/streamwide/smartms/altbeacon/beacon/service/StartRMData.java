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
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.streamwide.smartms.altbeacon.beacon.Region;

import java.io.Serializable;

/**
 * Internal class used to transfer ranging and monitoring data between the BeaconService and client
 *
 * @hide
 */
public class StartRMData implements Serializable, Parcelable {
    private static final String SCAN_PERIOD_KEY = "scanPeriod";
    private static final String BETWEEN_SCAN_PERIOD_KEY = "betweenScanPeriod";
    private static final String BACKGROUND_FLAG_KEY = "backgroundFlag";
    private static final String CALLBACK_PACKAGE_NAME_KEY = "callbackPackageName";
    private static final String REGION_KEY = "region";

    private Region mRegion;
    private long mScanPeriod;
    private long mBetweenScanPeriod;
    private boolean mBackgroundFlag;
    private String mCallbackPackageName;

    private StartRMData() {
    }

    public StartRMData(@NonNull Region region, @NonNull String callbackPackageName) {
        this.mRegion = region;
        this.mCallbackPackageName = callbackPackageName;
    }

    public StartRMData(long scanPeriod, long betweenScanPeriod, boolean backgroundFlag) {
        this.mScanPeriod = scanPeriod;
        this.mBetweenScanPeriod = betweenScanPeriod;
        this.mBackgroundFlag = backgroundFlag;
    }

    public StartRMData(@NonNull Region region, @NonNull String callbackPackageName, long scanPeriod, long betweenScanPeriod, boolean backgroundFlag) {
        this.mScanPeriod = scanPeriod;
        this.mBetweenScanPeriod = betweenScanPeriod;
        this.mRegion = region;
        this.mCallbackPackageName = callbackPackageName;
        this.mBackgroundFlag = backgroundFlag;
    }


    public long getScanPeriod() {
        return mScanPeriod;
    }

    public long getBetweenScanPeriod() {
        return mBetweenScanPeriod;
    }

    @NonNull
    public Region getRegionData() {
        return mRegion;
    }

    @NonNull
    public String getCallbackPackageName() {
        return mCallbackPackageName;
    }

    public boolean getBackgroundFlag() {
        return mBackgroundFlag;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(@NonNull Parcel out, int flags) {
        out.writeParcelable(mRegion, flags);
        out.writeString(mCallbackPackageName);
        out.writeLong(mScanPeriod);
        out.writeLong(mBetweenScanPeriod);
        out.writeByte((byte) (mBackgroundFlag ? 1 : 0));
    }

    public static final Creator<StartRMData> CREATOR
            = new Creator<StartRMData>() {
        public StartRMData createFromParcel(Parcel in) {
            return new StartRMData(in);
        }

        public StartRMData[] newArray(int size) {
            return new StartRMData[size];
        }
    };

    protected StartRMData(@NonNull Parcel in) {
        mRegion = in.readParcelable(Thread.currentThread().getContextClassLoader());
        mCallbackPackageName = in.readString();
        mScanPeriod = in.readLong();
        mBetweenScanPeriod = in.readLong();
        mBackgroundFlag = in.readByte() != 0;
    }

    @NonNull
    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putLong(SCAN_PERIOD_KEY, this.mScanPeriod);
        bundle.putLong(BETWEEN_SCAN_PERIOD_KEY, this.mBetweenScanPeriod);
        bundle.putBoolean(BACKGROUND_FLAG_KEY, this.mBackgroundFlag);
        bundle.putString(CALLBACK_PACKAGE_NAME_KEY, this.mCallbackPackageName);
        if (mRegion != null) {
            bundle.putSerializable(REGION_KEY, mRegion);
        }
        return bundle;
    }

    @NonNull
    public static StartRMData fromBundle(@NonNull Bundle bundle) {
        bundle.setClassLoader(Thread.currentThread().getContextClassLoader());
        boolean valid = false;
        StartRMData data = new StartRMData();
        if (bundle.containsKey(REGION_KEY)) {
            data.mRegion = (Region) bundle.getSerializable(REGION_KEY);
            valid = true;
        }
        if (bundle.containsKey(SCAN_PERIOD_KEY)) {
            data.mScanPeriod = (Long) bundle.get(SCAN_PERIOD_KEY);
            valid = true;
        }
        if (bundle.containsKey(BETWEEN_SCAN_PERIOD_KEY)) {
            data.mBetweenScanPeriod = (Long) bundle.get(BETWEEN_SCAN_PERIOD_KEY);
        }
        if (bundle.containsKey(BACKGROUND_FLAG_KEY)) {
            data.mBackgroundFlag = (Boolean) bundle.get(BACKGROUND_FLAG_KEY);
        }
        if (bundle.containsKey(CALLBACK_PACKAGE_NAME_KEY)) {
            data.mCallbackPackageName = (String) bundle.get(CALLBACK_PACKAGE_NAME_KEY);
        }
        if (valid) {
            return data;
        } else {
            return null;
        }
    }

}
