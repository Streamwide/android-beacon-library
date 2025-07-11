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

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.SystemClock;

import androidx.annotation.NonNull;

import com.streamwide.smartms.altbeacon.beacon.logging.LogManager;
import com.streamwide.smartms.altbeacon.bluetooth.BluetoothCrashResolver;

public class CycledLeScannerForJellyBeanMr2 extends CycledLeScanner {
    private static final String TAG = "CycledLeScannerForJellyBeanMr2";
    private BluetoothAdapter.LeScanCallback leScanCallback;

    public CycledLeScannerForJellyBeanMr2(@NonNull Context context, long scanPeriod, long betweenScanPeriod, boolean backgroundFlag, @NonNull CycledLeScanCallback cycledLeScanCallback, @NonNull BluetoothCrashResolver crashResolver) {
        super(context, scanPeriod, betweenScanPeriod, backgroundFlag, cycledLeScanCallback, crashResolver);
    }

    @Override
    protected void stopScan() {
        postStopLeScan();
    }

    @Override
    protected boolean deferScanIfNeeded() {
        long millisecondsUntilStart = mNextScanCycleStartTime - SystemClock.elapsedRealtime();
        if (millisecondsUntilStart > 0) {
            LogManager.d(TAG, "Waiting to start next Bluetooth scan for another %s milliseconds",
                    millisecondsUntilStart);
            // Don't actually wait until the next scan time -- only wait up to 1 second.  This
            // allows us to start scanning sooner if a consumer enters the foreground and expects
            // results more quickly.
            if (mBackgroundFlag) {
                setWakeUpAlarm();
            }
            mHandler.postDelayed(() -> scanLeDevice(true), millisecondsUntilStart > 1000 ? 1000 : millisecondsUntilStart);
            return true;
        }
        return false;
    }

    @Override
    protected void startScan() {
        postStartLeScan();
    }

    @Override
    protected void finishScan() {
        postStopLeScan();
        mScanningPaused = true;
    }

    private void postStartLeScan() {
        final BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();
        if (bluetoothAdapter == null) {
            return;
        }
        final BluetoothAdapter.LeScanCallback leScanCallback = getLeScanCallback();
        mScanHandler.removeCallbacksAndMessages(null);
        mScanHandler.post(() -> {
            try {
                //noinspection deprecation
                bluetoothAdapter.startLeScan(leScanCallback);
            } catch (SecurityException e) {
                LogManager.e(e, TAG, "SecurityException in startLeScan()");
            } catch (Exception e) {
                LogManager.e(e, TAG, "Internal Android exception in startLeScan()");
            }
        });
    }

    private void postStopLeScan() {
        final BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();
        if (bluetoothAdapter == null) {
            return;
        }
        final BluetoothAdapter.LeScanCallback leScanCallback = getLeScanCallback();
        mScanHandler.removeCallbacksAndMessages(null);
        mScanHandler.post(() -> {
            try {
                //noinspection deprecation
                bluetoothAdapter.stopLeScan(leScanCallback);
            } catch (SecurityException e) {
                LogManager.e(e, TAG, "SecurityException in stopLeScan()");
            } catch (Exception e) {
                LogManager.e(e, TAG, "Internal Android exception in stopLeScan()");
            }
        });
    }

    @NonNull
    protected BluetoothAdapter.LeScanCallback getLeScanCallback() {
        if (leScanCallback == null) {
            leScanCallback =
                    (device, rssi, scanRecord) -> {
                        LogManager.d(TAG, "got record");
                        mCycledLeScanCallback.onLeScan(device, rssi, scanRecord, System.currentTimeMillis());
                        if (mBluetoothCrashResolver != null) {
                            mBluetoothCrashResolver.notifyScannedDevice(device, getLeScanCallback());
                        }
                    };
        }
        return leScanCallback;
    }
}
