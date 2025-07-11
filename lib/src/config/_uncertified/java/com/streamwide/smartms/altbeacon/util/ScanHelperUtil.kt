/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Wed, 30 Oct 2024 10:18:37 +0100
 * @copyright  Copyright (c) 2024 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2024 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Wed, 30 Oct 2024 10:18:36 +0100
 */

package com.streamwide.smartms.altbeacon.util

import android.app.PendingIntent
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import com.streamwide.smartms.altbeacon.beacon.BeaconParser
import com.streamwide.smartms.altbeacon.beacon.logging.LogManager
import com.streamwide.smartms.altbeacon.beacon.service.scanner.ScanFilterUtils

internal object ScanHelperUtil {

    private const val TAG = "ScanHelperUtil"

    @JvmStatic
    fun startAndroidOBackgroundScan(
        context: Context,
        beaconParsers: Set<BeaconParser>?,
        scanCallBackIntent: () -> PendingIntent
    ) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

            LogManager.e(
                TAG,
                "Failed to start background scan on Android O"
            )
            return
        }
        val settings =
            ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()

        val filters = ScanFilterUtils().createScanFiltersForBeaconParsers(
            beaconParsers?.toList() ?: listOf()
        )
        try {
            val bluetoothAdapter =
                (context.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?)?.adapter

            if (bluetoothAdapter == null) {
                LogManager.w(TAG, "Failed to construct a BluetoothAdapter")
            } else if (!bluetoothAdapter.isEnabled) {
                LogManager.w(
                    TAG,
                    "Failed to start background scan on Android O: BluetoothAdapter is not enabled"
                )
            } else {
                val scanner = bluetoothAdapter.bluetoothLeScanner
                if (scanner != null) {
                    val result: Int = scanner.startScan(filters, settings, scanCallBackIntent())
                    if (result != 0) {
                        LogManager.e(
                            TAG,
                            "Failed to start background scan on Android O."
                        )
                    } else {
                        LogManager.d(TAG, "Started passive beacon scan")
                    }
                } else {
                    LogManager.e(
                        TAG,
                        "Failed to start background scan on Android O: scanner is null"
                    )
                }
            }
        } catch (e: SecurityException) {
            LogManager.e(TAG, "SecurityException making Android O background scanner")
        } catch (e: NullPointerException) {
            // Needed to stop a crash caused by internal NPE thrown by Android.  See issue #636
            LogManager.e(
                TAG,
                "NullPointerException starting Android O background scanner",
                e
            )
        } catch (e: RuntimeException) {
            // Needed to stop a crash caused by internal Android throw.  See issue #701
            LogManager.e(
                TAG,
                "Unexpected runtime exception starting Android O background scanner",
                e
            )
        }
    }

    @JvmStatic
    fun stopAndroidOBackgroundScan(
        context: Context,
        scanCallBackIntent: () -> PendingIntent
    ) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

            LogManager.e(
                TAG,
                "Failed to stopping background scan on Android O"
            )
            return
        }

        try {

            val bluetoothAdapter =
                (context.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?)?.adapter

            if (bluetoothAdapter == null) {
                LogManager.w(TAG, "Failed to construct a BluetoothAdapter")
            } else if (!bluetoothAdapter.isEnabled) {
                LogManager.w(TAG, "BluetoothAdapter is not enabled")
            } else {
                val scanner = bluetoothAdapter.bluetoothLeScanner
                scanner?.stopScan(scanCallBackIntent())
            }
        } catch (e: SecurityException) {
            LogManager.e(TAG, "SecurityException stopping Android O background scanner")
        } catch (e: java.lang.NullPointerException) {
            // Needed to stop a crash caused by internal NPE thrown by Android.  See issue #636
            LogManager.e(
                TAG,
                "NullPointerException stopping Android O background scanner",
                e
            )
        } catch (e: java.lang.RuntimeException) {
            // Needed to stop a crash caused by internal Android throw.  See issue #701
            LogManager.e(
                TAG,
                "Unexpected runtime exception stopping Android O background scanner",
                e
            )
        }
    }
}