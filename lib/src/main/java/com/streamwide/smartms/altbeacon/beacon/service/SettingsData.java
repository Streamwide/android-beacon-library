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
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.altbeacon.beacon.Beacon;
import com.streamwide.smartms.altbeacon.beacon.BeaconManager;
import com.streamwide.smartms.altbeacon.beacon.BeaconParser;
import com.streamwide.smartms.altbeacon.beacon.logging.LogManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dyoung on 3/10/17.
 * <p>
 * Internal class used to transfer settings between the BeaconService and the client
 *
 * @hide
 */
public class SettingsData implements Serializable {
    private static final String TAG = SettingsData.class.getSimpleName();
    private static final String SETTINGS_DATA_KEY = "SettingsData";
    ArrayList<BeaconParser> mBeaconParsers;
    Boolean mRegionStatePersistenceEnabled;
    Boolean mAndroidLScanningDisabled;
    Long mRegionExitPeriod;
    Boolean mUseTrackingCache;
    Boolean mHardwareEqualityEnforced;

    // The following configuration settings are not implemented here, so they cannot be set when
    // the scanning service is running in another process
    //        BeaconManager.setDistanceModelUpdateUrl(...)
    //        BeaconManager.setRssiFilterImplClass(...)
    //        BeaconManager.setBeaconSimulator(...)
    //        beaconManager.setNonBeaconLeScanCallback(...)

    @NonNull
    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(SETTINGS_DATA_KEY, this);
        return bundle;
    }

    @Nullable
    public static SettingsData fromBundle(@NonNull Bundle bundle) {
        bundle.setClassLoader(Thread.currentThread().getContextClassLoader());
        SettingsData settingsData = null;
        if (bundle.get(SETTINGS_DATA_KEY) != null) {
            settingsData = (SettingsData) bundle.getSerializable(SETTINGS_DATA_KEY);
        }
        return settingsData;
    }

    public void apply(@NonNull BeaconService scanService) {
        LogManager.d(TAG, "Applying settings changes to scanner in other process");
        BeaconManager beaconManager = BeaconManager.getInstanceForApplication(scanService);
        List<BeaconParser> beaconParsers = beaconManager.getBeaconParsers();
        boolean beaconParsersChanged = false;
        if (beaconParsers.size() == mBeaconParsers.size()) {
            for (int i = 0; i < beaconParsers.size(); i++) {
                if (!beaconParsers.get(i).equals(mBeaconParsers.get(i))) {
                    LogManager.d(TAG, "Beacon parsers have changed to: " + mBeaconParsers.get(i).getLayout());
                    beaconParsersChanged = true;
                    break;
                }
            }
        } else {
            beaconParsersChanged = true;
            LogManager.d(TAG, "Beacon parsers have been added or removed.");
        }
        if (beaconParsersChanged) {
            LogManager.d(TAG, "Updating beacon parsers");
            beaconManager.getBeaconParsers().clear();
            beaconManager.getBeaconParsers().addAll(mBeaconParsers);
            scanService.reloadParsers();
        } else {
            LogManager.d(TAG, "Beacon parsers unchanged.");
        }
        MonitoringStatus monitoringStatus = MonitoringStatus.getInstance();
        if (monitoringStatus.isStatePreservationOn() &&
                !mRegionStatePersistenceEnabled) {
            monitoringStatus.stopStatusPreservation(scanService);
        } else if (!monitoringStatus.isStatePreservationOn() &&
                mRegionStatePersistenceEnabled) {
            monitoringStatus.startStatusPreservation(scanService);
        }
        beaconManager.setAndroidLScanningDisabled(mAndroidLScanningDisabled);
        BeaconManager.setRegionExitPeriod(mRegionExitPeriod);
        RangeState.setUseTrackingCache(mUseTrackingCache);
        Beacon.setHardwareEqualityEnforced(mHardwareEqualityEnforced);
    }

    @NonNull
    public SettingsData collect(@NonNull Context context) {
        BeaconManager beaconManager = BeaconManager.getInstanceForApplication(context);
        mBeaconParsers = new ArrayList<>(beaconManager.getBeaconParsers());
        mRegionStatePersistenceEnabled = beaconManager.isRegionStatePersistenceEnabled();
        mAndroidLScanningDisabled = beaconManager.isAndroidLScanningDisabled();
        mRegionExitPeriod = BeaconManager.getRegionExitPeriod();
        mUseTrackingCache = RangeState.getUseTrackingCache();
        mHardwareEqualityEnforced = Beacon.getHardwareEqualityEnforced();
        return this;
    }

}
