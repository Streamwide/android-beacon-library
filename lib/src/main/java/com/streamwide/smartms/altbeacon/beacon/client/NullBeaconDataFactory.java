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

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.altbeacon.beacon.Beacon;
import com.streamwide.smartms.altbeacon.beacon.BeaconDataNotifier;

public class NullBeaconDataFactory implements BeaconDataFactory {

    @Override
    public void requestBeaconData(@Nullable Beacon beacon, final @NonNull BeaconDataNotifier notifier) {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                notifier.beaconDataUpdate(null, null, new DataProviderException("You need to configure a beacon data service to use this feature."));
            }
        });
    }
}

