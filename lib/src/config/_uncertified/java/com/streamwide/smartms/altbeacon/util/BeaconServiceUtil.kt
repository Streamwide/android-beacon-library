/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Wed, 30 Oct 2024 10:18:38 +0100
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

import android.content.pm.ServiceInfo
import android.os.Build
import com.streamwide.smartms.altbeacon.beacon.BeaconManager
import com.streamwide.smartms.altbeacon.beacon.service.BeaconService

internal object BeaconServiceUtil {

    /*
     * This starts the scanning service as a foreground service if it is so configured in the
     * manifest
     */
    @JvmStatic
    fun BeaconService.startForegroundIfConfigured() {
        val beaconManager = BeaconManager.getInstanceForApplication(applicationContext)
        val notification = beaconManager.foregroundServiceNotification
        val notificationId = beaconManager.foregroundServiceNotificationId

        if (notification != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                this.startForeground(
                    notificationId,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                )
            } else {
                this.startForeground(notificationId, notification)
            }
        }
    }
}