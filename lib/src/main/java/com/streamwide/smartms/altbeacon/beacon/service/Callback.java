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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.streamwide.smartms.altbeacon.beacon.BeaconLocalBroadcastProcessor;
import com.streamwide.smartms.altbeacon.beacon.BeaconManager;
import com.streamwide.smartms.altbeacon.beacon.logging.LogManager;

import java.io.IOException;
import java.io.Serializable;

public class Callback implements Serializable {
    private static final String TAG = "Callback";

    //TODO: Remove this constructor in favor of an empty one, as the package name is no longer needed
    public Callback(@Nullable String intentPackageName) {
    }

    /**
     * Tries making the callback, first via messenger, then via intent
     *
     * @param context
     * @param dataName
     * @param data
     * @return false if it callback cannot be made
     */
    public boolean call(@NonNull Context context, @NonNull String dataName, @NonNull Bundle data) {
        boolean useLocalBroadcast = BeaconManager.getInstanceForApplication(context).isMainProcess();
        boolean success = false;

        if (useLocalBroadcast) {
            String action = null;
            if (dataName != null && dataName.equals("rangingData")) {
                action = BeaconLocalBroadcastProcessor.RANGE_NOTIFICATION;
            } else {
                action = BeaconLocalBroadcastProcessor.MONITOR_NOTIFICATION;
            }
            Intent intent = new Intent(action);
            intent.putExtra(dataName, data);
            LogManager.d(TAG, "attempting callback via local broadcast intent: %s", action);
            success = LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        } else {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(context.getPackageName(), "org.altbeacon.beacon.BeaconIntentProcessor"));
            intent.putExtra(dataName, data);
            LogManager.d(TAG, "attempting callback via global broadcast intent: %s", intent.getComponent());
            try {
                context.startService(intent);
                success = true;
            } catch (Exception e) {
                LogManager.e(
                        TAG,
                        "Failed attempting to start service: " + intent.getComponent().flattenToString(),
                        e
                );
            }
        }
        return success;
    }

    @SuppressWarnings("unused")
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }
}
