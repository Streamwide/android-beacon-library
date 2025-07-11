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

package com.streamwide.smartms.altbeacon.beacon.startup;

import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.altbeacon.beacon.BeaconManager;
import com.streamwide.smartms.altbeacon.beacon.logging.LogManager;
import com.streamwide.smartms.lib.template.receiver.BaseReceiver;
import com.streamwide.smartms.lib.template.serialization.ValidatingIntent;

public class StartupBroadcastReceiver extends BaseReceiver {

    private static final String TAG = "StartupBroadcastReceiver";

    @Override
    public void onDataReceived(@NonNull Context context, @NonNull String action,
                               @NonNull ValidatingIntent validatingIntent) {
        LogManager.d(TAG, "onReceive called in startup broadcast receiver");

        if (action == null) {
            LogManager.w(TAG, "Intent action is null. Ignoring broadcast receiver.");
            return;
        }

        if (action.equals(Intent.ACTION_BOOT_COMPLETED) ||
                action.equals(Intent.ACTION_POWER_CONNECTED) ||
                action.equals(Intent.ACTION_POWER_DISCONNECTED)) {

            BeaconManager beaconManager = BeaconManager.getInstanceForApplication(context.getApplicationContext());
            beaconManager.processBluetoothLeScanResults(context, validatingIntent);
        }
    }

    @Nullable
    @Override
    protected Class<?>[] acceptedClasses() {
        return new Class[]{Boolean.class, String.class, ScanResult.class};
    }
}
