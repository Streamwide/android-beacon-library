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

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.NonNull;

/**
 * Converts internal intents to notifier callbacks
 * <p>
 * This is used with the BeaconService and supports scanning in a separate process.
 * It is not used with the ScanJob, as an IntentService will not be able to be started in some cases
 * where the app is in the background on Android O.
 *
 * @hide
 * @see BeaconLocalBroadcastProcessor for the equivalent use with ScanJob.
 * <p>
 * This IntentService may be running in a different process from the BeaconService, which justifies
 * its continued existence for multi-process service cases.
 * <p>
 * Internal library class.  Do not use directly from outside the library
 */
public class BeaconIntentProcessor extends IntentService {
    private static final String TAG = "BeaconIntentProcessor";

    public BeaconIntentProcessor() {
        super("BeaconIntentProcessor");
    }

    @Override
    protected void onHandleIntent(@NonNull Intent intent) {
        new IntentHandler().convertIntentsToCallbacks(this.getApplicationContext(), intent);
    }
}
