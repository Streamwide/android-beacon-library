/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Wed, 5 Jun 2024 10:44:41 +0100
 * @copyright  Copyright (c) 2024 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2024 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Wed, 5 Jun 2024 10:44:40 +0100
 */

package com.streamwide.smartms.altbeacon.beacon.client;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.altbeacon.beacon.Beacon;
import com.streamwide.smartms.altbeacon.beacon.BeaconDataNotifier;

/**
 * This can be configured for the public beacon data store, or a private beacon data store.
 * In the public data store, you can read any value but only write to the values to the beacons you created
 *
 * @author dyoung
 */
public interface BeaconDataFactory {
    /**
     * Asynchronous call
     * When data is available, it is passed back to the beaconDataNotifier interface
     *
     * @param beacon
     */
    public void requestBeaconData(@Nullable Beacon beacon, @NonNull BeaconDataNotifier notifier);
}

