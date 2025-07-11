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

package com.streamwide.smartms.altbeacon.beacon.simulator;

import androidx.annotation.Nullable;

import com.streamwide.smartms.altbeacon.beacon.Beacon;

import java.util.List;

/**
 * Created by dyoung on 4/18/14.
 */
public class StaticBeaconSimulator implements BeaconSimulator {

    @Nullable
    public List<Beacon> beacons = null;

    @Override
    @Nullable
    public List<Beacon> getBeacons() {
        return beacons;
    }

    public void setBeacons(@Nullable List<Beacon> beacons) {
        this.beacons = beacons;
    }
}
