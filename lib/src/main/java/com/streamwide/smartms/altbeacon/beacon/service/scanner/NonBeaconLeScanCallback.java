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

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

/**
 * Allows an implementation to see non-Beacon BLE devices as they are scanned.
 * <p/>
 * To use:
 * <pre><code>
 * public class BeaconReferenceApplication extends Application implements ..., NonBeaconLeScanCallback {
 *     public void onCreate() {
 *         super.onCreate();
 *         BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
 *         ...
 *         beaconManager.setNonBeaconLeScanCallback(this);
 *         ...
 *     }
 *
 *     {@literal @}Override
 *     public void onNonBeaconLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
 *          ...
 *     }
 *  }
 * </code></pre>
 */
@WorkerThread
public interface NonBeaconLeScanCallback {
    /**
     * NOTE: This method is NOT called on the main UI thread.
     *
     * @param device     Identifies the remote device
     * @param rssi       The RSSI value for the remote device as reported by the
     *                   Bluetooth hardware. 0 if no RSSI value is available.
     * @param scanRecord The content of the advertisement record offered by
     *                   the remote device.
     */
    void onNonBeaconLeScan(@NonNull BluetoothDevice device, int rssi, @NonNull byte[] scanRecord);
}
