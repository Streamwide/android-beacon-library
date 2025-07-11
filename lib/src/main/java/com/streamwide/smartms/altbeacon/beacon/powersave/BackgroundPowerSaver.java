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

package com.streamwide.smartms.altbeacon.beacon.powersave;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.streamwide.smartms.altbeacon.beacon.BeaconManager;
import com.streamwide.smartms.altbeacon.beacon.logging.LogManager;

/**
 * Simply creating an instance of this class and holding a reference to it in your Application can
 * improve battery life by 60% by slowing down scans when your app is in the background.
 */
public class BackgroundPowerSaver implements Application.ActivityLifecycleCallbacks {
    @NonNull
    private static final String TAG = "BackgroundPowerSaver";

    @NonNull
    private final BeaconManager beaconManager;

    private int activeActivityCount = 0;

    /**
     * Constructs a new BackgroundPowerSaver
     *
     * @deprecated the {@code countActiveActivityStrategy} flag is no longer used. Use
     * {@link #BackgroundPowerSaver(Context)}
     */
    @Deprecated
    public BackgroundPowerSaver(@NonNull Context context, boolean countActiveActivityStrategy) {
        this(context);
    }

    /**
     * Constructs a new BackgroundPowerSaver using the default background determination strategy
     *
     * @param context
     */
    public BackgroundPowerSaver(@NonNull Context context) {
        beaconManager = BeaconManager.getInstanceForApplication(context);
        ((Application) context.getApplicationContext()).registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @NonNull Bundle bundle) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        activeActivityCount++;
        if (activeActivityCount < 1) {
            LogManager.d(TAG, "reset active activity count on resume.  It was %s", activeActivityCount);
            activeActivityCount = 1;
        }
        beaconManager.setBackgroundMode(false);
        LogManager.d(TAG, "activity resumed: %s active activities: %s", activity, activeActivityCount);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        activeActivityCount--;
        LogManager.d(
                TAG,
                "activity paused: %s active activities: %s",
                activity,
                activeActivityCount
        );
        if (activeActivityCount < 1) {
            LogManager.d(TAG, "setting background mode");
            beaconManager.setBackgroundMode(true);
        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }
}
