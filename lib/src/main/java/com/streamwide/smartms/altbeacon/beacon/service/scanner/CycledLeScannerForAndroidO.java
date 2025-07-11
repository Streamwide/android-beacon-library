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

package com.streamwide.smartms.altbeacon.beacon.service.scanner;

import android.annotation.TargetApi;
import android.content.Context;

import com.streamwide.smartms.altbeacon.beacon.service.ScanJob;
import com.streamwide.smartms.altbeacon.bluetooth.BluetoothCrashResolver;

/**
 * The scanner used for Android O is effectively the same as used for JellyBeaconMr2.  There is no
 * point in using the low power scanning APIs introduced in Lollipop, because they only work when
 * the app is running, effectively requiring a long running service, something newly disallowed
 * by Android O.  The new strategy for Android O is to use a JobScheduler combined with background
 * scans delivered by Intents.
 *
 * @see ScanJob
 * <p>
 * Created by dyoung on 5/28/17.
 */

@TargetApi(26)
class CycledLeScannerForAndroidO extends CycledLeScannerForLollipop {
    private static final String TAG = CycledLeScannerForAndroidO.class.getSimpleName();

    CycledLeScannerForAndroidO(Context context, long scanPeriod, long betweenScanPeriod, boolean backgroundFlag, CycledLeScanCallback cycledLeScanCallback, BluetoothCrashResolver crashResolver) {
        super(context, scanPeriod, betweenScanPeriod, backgroundFlag, cycledLeScanCallback, crashResolver);
    }
}
