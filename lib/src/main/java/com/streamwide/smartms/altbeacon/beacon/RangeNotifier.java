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
 * @lastModifiedOn Wed, 5 Jun 2024 10:44:41 +0100
 */
package com.streamwide.smartms.altbeacon.beacon;

import androidx.annotation.NonNull;

import java.util.Collection;

/**
 * This interface is implemented by classes that receive beacon ranging notifications
 *
 * @author David G. Young
 * @see BeaconManager#setRangeNotifier(RangeNotifier notifier)
 * @see BeaconManager#startRangingBeaconsInRegion(Region region)
 * @see Region
 * @see Beacon
 */
public interface RangeNotifier {
    /**
     * Called once per second to give an estimate of the mDistance to visible beacons
     *
     * @param beacons a collection of <code>Beacon<code> objects that have been seen in the past second
     * @param region  the <code>Region</code> object that defines the criteria for the ranged beacons
     */
    public void didRangeBeaconsInRegion(@NonNull Collection<Beacon> beacons, @NonNull Region region);
}
