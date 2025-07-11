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
package com.streamwide.smartms.altbeacon.beacon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.streamwide.smartms.altbeacon.beacon.logging.LogManager;

/**
 * Converts internal intents to notifier callbacks
 * <p>
 * This is used with ScanJob and supports delivering intents even under Android O background
 * restrictions preventing starting a new IntentService.
 * <p>
 * It is not used with the BeaconService, if running in a separate process, as local broadcast
 * intents cannot be deliverd across different processes which the BeaconService supports.
 *
 * @hide
 * @see BeaconIntentProcessor for the equivalent use with BeaconService in a separate process.
 * *
 * Internal library class.  Do not use directly from outside the library
 */
public class BeaconLocalBroadcastProcessor {
    private static final String TAG = "BeaconLocalBroadcastProcessor";

    public static final String RANGE_NOTIFICATION = "org.altbeacon.beacon.range_notification";
    public static final String MONITOR_NOTIFICATION = "org.altbeacon.beacon.monitor_notification";

    @NonNull
    private Context mContext;

    private BeaconLocalBroadcastProcessor() {

    }

    public BeaconLocalBroadcastProcessor(@NonNull Context context) {
        mContext = context;

    }

    static int registerCallCount = 0;
    int registerCallCountForInstnace = 0;

    public void register() {
        registerCallCount += 1;
        registerCallCountForInstnace += 1;
        LogManager.d(TAG, "Register calls: global=" + registerCallCount + " instance=" + registerCallCountForInstnace);
        unregister();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mLocalBroadcastReceiver,
                new IntentFilter(RANGE_NOTIFICATION));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mLocalBroadcastReceiver,
                new IntentFilter(MONITOR_NOTIFICATION));
    }

    public void unregister() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mLocalBroadcastReceiver);
    }


    private BroadcastReceiver mLocalBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            new IntentHandler().convertIntentsToCallbacks(context, intent);
        }
    };
}