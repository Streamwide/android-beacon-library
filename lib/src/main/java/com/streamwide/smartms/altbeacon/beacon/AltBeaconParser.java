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

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A specific beacon parser designed to parse only AltBeacons from raw BLE packets detected by
 * Android.  By default, this is the only <code>BeaconParser</code> that is used by the library.
 * Additional <code>BeaconParser</code> instances can get created and registered with the library.
 * {@link BeaconParser See BeaconParser for more information.}
 */
public class AltBeaconParser extends BeaconParser {
    public static final String TAG = "AltBeaconParser";

    /**
     * Constructs an AltBeacon Parser and sets its layout
     */
    public AltBeaconParser() {
        super();
        // Radius networks and other manufacturers seen in AltBeacons
        // Note: Other manufacturer codes that have been seen in the wild with AltBeacons are:
        // 0x004c, 0x00e0
        // We are not adding these here because there is no indication they are widely used
        // for production purposes.  We need to keep the hardware assist list short in order to
        // save slots.  If you are a manufacturer of AltBeacons and want you company code added to
        // this list, please open an issue on the Github project for this library.  If a beacon
        // manufacturer code not in this list is used for AltBeacons, phones using Andoroid 5.x+
        // detection APIs will not be able to detect the beacon in the background.
        mHardwareAssistManufacturers = new int[]{0x0118};
        this.setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT);
        this.mIdentifier = "altbeacon";
    }

    /**
     * Construct an AltBeacon from a Bluetooth LE packet collected by Android's Bluetooth APIs,
     * including the raw Bluetooth device info
     *
     * @param scanData    The actual packet bytes
     * @param rssi        The measured signal strength of the packet
     * @param device      The Bluetooth device that was detected
     * @param timestampMs The timestamp in milliseconds of the scan execution
     * @return An instance of an <code>Beacon</code>
     */
    @Override
    @NonNull
    public Beacon fromScanData(@Nullable byte[] scanData, int rssi, @Nullable BluetoothDevice device, long timestampMs) {
        return fromScanData(scanData, rssi, device, timestampMs, new AltBeacon());
    }

}
