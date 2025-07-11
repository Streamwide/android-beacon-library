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

import android.content.Context;
import android.content.Intent;

import com.streamwide.smartms.altbeacon.beacon.logging.LogManager;
import com.streamwide.smartms.altbeacon.beacon.service.MonitoringData;
import com.streamwide.smartms.altbeacon.beacon.service.MonitoringStatus;
import com.streamwide.smartms.altbeacon.beacon.service.RangingData;

import java.util.Set;

/**
 * Converts internal Intents for ranging/monitoring to notifier callbacks.
 * These may be local broadcast intents from BeaconLocalBroadcastProcessor or
 * global broadcast intents fro BeaconIntentProcessor
 * <p>
 * Internal library class.  Do not use directly from outside the library
 *
 * @hide Created by dyoung on 7/20/17.
 */

/* package private*/
class IntentHandler {
    private static final String TAG = IntentHandler.class.getSimpleName();

    public void convertIntentsToCallbacks(Context context, Intent intent) {
        MonitoringData monitoringData = null;
        RangingData rangingData = null;

        if (intent != null && intent.getExtras() != null) {
            if (intent.getExtras().getBundle("monitoringData") != null) {
                monitoringData = MonitoringData.fromBundle(intent.getExtras().getBundle("monitoringData"));
            }
            if (intent.getExtras().getBundle("rangingData") != null) {
                rangingData = RangingData.fromBundle(intent.getExtras().getBundle("rangingData"));
            }
        }

        if (rangingData != null) {
            LogManager.d(TAG, "got ranging data");
            if (rangingData.getBeacons() == null) {
                LogManager.w(TAG, "Ranging data has a null beacons collection");
            }
            Set<RangeNotifier> notifiers = BeaconManager.getInstanceForApplication(context).getRangingNotifiers();
            java.util.Collection<Beacon> beacons = rangingData.getBeacons();
            if (notifiers != null) {
                for (RangeNotifier notifier : notifiers) {
                    notifier.didRangeBeaconsInRegion(beacons, rangingData.getRegion());
                }
            } else {
                LogManager.d(TAG, "but ranging notifier is null, so we're dropping it.");
            }
            RangeNotifier dataNotifier = BeaconManager.getInstanceForApplication(context).getDataRequestNotifier();
            if (dataNotifier != null) {
                dataNotifier.didRangeBeaconsInRegion(beacons, rangingData.getRegion());
            }
        }

        if (monitoringData != null) {
            LogManager.d(TAG, "got monitoring data");
            Set<MonitorNotifier> notifiers = BeaconManager.getInstanceForApplication(context).getMonitoringNotifiers();
            if (notifiers != null) {
                for (MonitorNotifier notifier : notifiers) {
                    LogManager.d(TAG, "Calling monitoring notifier: %s", notifier);
                    Region region = monitoringData.getRegion();
                    Integer state = monitoringData.isInside() ? MonitorNotifier.INSIDE :
                            MonitorNotifier.OUTSIDE;
                    notifier.didDetermineStateForRegion(state, region);
                    // In case the beacon scanner is running in a separate process, the monitoring
                    // status in this process  will not have been updated yet as a result of this
                    // region state change.  We make a call here to keep it in sync.
                    MonitoringStatus.getInstance().updateLocalState(context, region, state);
                    if (monitoringData.isInside()) {
                        notifier.didEnterRegion(monitoringData.getRegion());
                    } else {
                        notifier.didExitRegion(monitoringData.getRegion());
                    }
                }
            }
        }

    }
}
