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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.altbeacon.beacon.logging.LogManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

/**
 * Provides a mechanism for transmitting as a beacon.   Requires Android 5.0
 */
public class BeaconTransmitter {
    public static final int SUPPORTED = 0;
    public static final int NOT_SUPPORTED_MIN_SDK = 1;
    public static final int NOT_SUPPORTED_BLE = 2;
    // isMultipleAdvertisementSupported returning false no longer indicates that transmission is not
    // possible
    @Deprecated
    public static final int NOT_SUPPORTED_MULTIPLE_ADVERTISEMENTS = 3;
    public static final int NOT_SUPPORTED_CANNOT_GET_ADVERTISER = 4;
    public static final int NOT_SUPPORTED_CANNOT_GET_ADVERTISER_MULTIPLE_ADVERTISEMENTS = 5;
    private static final String TAG = "BeaconTransmitter";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private int mAdvertiseMode = AdvertiseSettings.ADVERTISE_MODE_LOW_POWER;
    private int mAdvertiseTxPowerLevel = AdvertiseSettings.ADVERTISE_TX_POWER_HIGH;
    private Beacon mBeacon;
    private BeaconParser mBeaconParser;
    @Nullable
    protected AdvertiseCallback mAdvertisingClientCallback;
    protected boolean mStarted;
    private AdvertiseCallback mAdvertiseCallback;
    private boolean mConnectable = false;

    /**
     * Creates a new beacon transmitter capable of transmitting beacons with the format
     * specified in the BeaconParser and with the data fields specified in the Beacon object
     *
     * @param context
     * @param parser  specifies the format of the beacon transmission
     */
    public BeaconTransmitter(@NonNull Context context, @NonNull BeaconParser parser) {
        mBeaconParser = parser;
        BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            mBluetoothAdapter = bluetoothManager.getAdapter();
            mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
            LogManager.d(TAG, "new BeaconTransmitter constructed.  mbluetoothLeAdvertiser is %s",
                    mBluetoothLeAdvertiser);
        } else {
            LogManager.e(TAG, "Failed to get BluetoothManager");
        }
    }

    /**
     * Tells if transmission has started
     *
     * @return
     */
    public boolean isStarted() {
        return mStarted;
    }

    /**
     * Sets the beacon whose fields will be transmitted
     *
     * @param beacon
     */
    public void setBeacon(@NonNull Beacon beacon) {
        mBeacon = beacon;
    }

    /**
     * Sets the beaconParsser used for formatting the transmission
     *
     * @param beaconParser
     */
    public void setBeaconParser(@NonNull BeaconParser beaconParser) {
        mBeaconParser = beaconParser;
    }

    /**
     * @return advertiseMode
     * @see #setAdvertiseMode(int)
     */
    public int getAdvertiseMode() {
        return mAdvertiseMode;
    }

    /**
     * AdvertiseSettings.ADVERTISE_MODE_BALANCED 3 Hz
     * AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY 10 Hz
     * AdvertiseSettings.ADVERTISE_MODE_LOW_POWER 1 Hz
     *
     * @param mAdvertiseMode
     */
    public void setAdvertiseMode(int mAdvertiseMode) {
        this.mAdvertiseMode = mAdvertiseMode;
    }

    /**
     * @return txPowerLevel
     * @see #setAdvertiseTxPowerLevel(int mAdvertiseTxPowerLevel)
     */
    public int getAdvertiseTxPowerLevel() {
        return mAdvertiseTxPowerLevel;
    }

    /**
     * AdvertiseSettings.ADVERTISE_TX_POWER_HIGH -56 dBm @ 1 meter with Nexus 5
     * AdvertiseSettings.ADVERTISE_TX_POWER_LOW -75 dBm @ 1 meter with Nexus 5
     * AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM -66 dBm @ 1 meter with Nexus 5
     * AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW not detected with Nexus 5
     *
     * @param mAdvertiseTxPowerLevel
     */
    public void setAdvertiseTxPowerLevel(int mAdvertiseTxPowerLevel) {
        this.mAdvertiseTxPowerLevel = mAdvertiseTxPowerLevel;
    }

    /**
     * Whether the advertisement should indicate the device is connectable.
     *
     * @param connectable
     */
    public void setConnectable(boolean connectable) {
        this.mConnectable = connectable;
    }

    /**
     * @return connectable
     * @see #setConnectable(boolean)
     */
    public boolean isConnectable() {
        return mConnectable;
    }

    /**
     * Checks to see if this device supports beacon advertising
     *
     * @return SUPPORTED if yes, otherwise:
     * NOT_SUPPORTED_MIN_SDK
     * NOT_SUPPORTED_BLE
     * NOT_SUPPORTED_MULTIPLE_ADVERTISEMENTS
     * NOT_SUPPORTED_CANNOT_GET_ADVERTISER
     */
    public static int checkTransmissionSupported(@NonNull Context context) {
        int returnCode = SUPPORTED;

        if (!context.getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            returnCode = NOT_SUPPORTED_BLE;
        } else {
            try {
                // Check to see if the getBluetoothLeAdvertiser is available.  If not, this will throw an exception indicating we are not running Android L
                if (((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().getBluetoothLeAdvertiser() == null) {
                    if (!((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().isMultipleAdvertisementSupported()) {
                        returnCode = NOT_SUPPORTED_CANNOT_GET_ADVERTISER_MULTIPLE_ADVERTISEMENTS;
                    } else {
                        returnCode = NOT_SUPPORTED_CANNOT_GET_ADVERTISER;
                    }
                }
            } catch (Exception e) {
                returnCode = NOT_SUPPORTED_CANNOT_GET_ADVERTISER;
            }
        }

        return returnCode;
    }

    private AdvertiseCallback getAdvertiseCallback() {
        if (mAdvertiseCallback == null) {
            mAdvertiseCallback = new AdvertiseCallback() {
                @Override
                public void onStartFailure(int errorCode) {
                    LogManager.e(TAG, "Advertisement start failed, code: %s", errorCode);
                    if (mAdvertisingClientCallback != null) {
                        mAdvertisingClientCallback.onStartFailure(errorCode);
                    }

                }

                @Override
                public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    LogManager.i(TAG, "Advertisement start succeeded.");
                    mStarted = true;
                    if (mAdvertisingClientCallback != null) {
                        mAdvertisingClientCallback.onStartSuccess(settingsInEffect);
                    }

                }
            };


        }
        return mAdvertiseCallback;
    }

    /**
     * Parse UUID from bytes. The {@code uuidBytes} can represent a 16-bit, 32-bit or 128-bit UUID,
     * but the returned UUID is always in 128-bit format.
     * Note UUID is little endian in Bluetooth.
     *
     * @param uuidBytes Byte representation of uuid.
     * @return {@link ParcelUuid} parsed from bytes.
     * @throws IllegalArgumentException If the {@code uuidBytes} cannot be parsed.
     *                                  <p>
     *                                  Copied from java/android/bluetooth/BluetoothUuid.java
     *                                  Copyright (C) 2009 The Android Open Source Project
     *                                  Licensed under the Apache License, Version 2.0
     */
    private static ParcelUuid parseUuidFrom(byte[] uuidBytes) {
        /** Length of bytes for 16 bit UUID */
        final int UUID_BYTES_16_BIT = 2;
        /** Length of bytes for 32 bit UUID */
        final int UUID_BYTES_32_BIT = 4;
        /** Length of bytes for 128 bit UUID */
        final int UUID_BYTES_128_BIT = 16;
        final ParcelUuid BASE_UUID =
                ParcelUuid.fromString("00000000-0000-1000-8000-00805F9B34FB");
        if (uuidBytes == null) {
            throw new IllegalArgumentException("uuidBytes cannot be null");
        }
        int length = uuidBytes.length;
        if (length != UUID_BYTES_16_BIT && length != UUID_BYTES_32_BIT &&
                length != UUID_BYTES_128_BIT) {
            throw new IllegalArgumentException("uuidBytes length invalid - " + length);
        }
        // Construct a 128 bit UUID.
        if (length == UUID_BYTES_128_BIT) {
            ByteBuffer buf = ByteBuffer.wrap(uuidBytes).order(ByteOrder.LITTLE_ENDIAN);
            long msb = buf.getLong(8);
            long lsb = buf.getLong(0);
            return new ParcelUuid(new UUID(msb, lsb));
        }
        // For 16 bit and 32 bit UUID we need to convert them to 128 bit value.
        // 128_bit_value = uuid * 2^96 + BASE_UUID
        long shortUuid;
        if (length == UUID_BYTES_16_BIT) {
            shortUuid = uuidBytes[0] & 0xFF;
            shortUuid += (uuidBytes[1] & 0xFF) << 8;
        } else {
            shortUuid = uuidBytes[0] & 0xFF;
            shortUuid += (uuidBytes[1] & 0xFF) << 8;
            shortUuid += (uuidBytes[2] & 0xFF) << 16;
            shortUuid += (uuidBytes[3] & 0xFF) << 24;
        }
        long msb = BASE_UUID.getUuid().getMostSignificantBits() + (shortUuid << 32);
        long lsb = BASE_UUID.getUuid().getLeastSignificantBits();
        return new ParcelUuid(new UUID(msb, lsb));
    }

}
