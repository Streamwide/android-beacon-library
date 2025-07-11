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

import android.bluetooth.BluetoothDevice;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

/**
 * Android API agnostic Bluetooth scan callback wrapper.
 * <p>
 * Since Android bluetooth scan callbacks occur on the main thread it is expected that these
 * callbacks will also occur on the main thread.
 * <p>
 * Created by dyoung on 10/6/14.
 */
@MainThread
public interface CycledLeScanCallback {
    void onLeScan(@NonNull BluetoothDevice device, int rssi, @NonNull byte[] scanRecord, long timestampMs);

    void onCycleEnd();
}
