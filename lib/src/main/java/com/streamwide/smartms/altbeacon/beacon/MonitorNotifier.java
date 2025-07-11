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

import androidx.annotation.NonNull;

/**
 * This interface is implemented by classes that receive beacon monitoring notifications
 *
 * @author David G. Young
 * @see BeaconManager#setMonitorNotifier(MonitorNotifier notifier)
 * @see BeaconManager#startMonitoringBeaconsInRegion(Region region)
 * @see Region
 */
public interface MonitorNotifier {
    /**
     * Indicates the Android device is inside the Region of beacons
     */
    public static final int INSIDE = 1;
    /**
     * Indicates the Android device is outside the Region of beacons
     */
    public static final int OUTSIDE = 0;

    /**
     * Called when at least one beacon in a <code>Region</code> is visible.
     *
     * @param region a Region that defines the criteria of beacons to look for
     */
    public void didEnterRegion(@NonNull Region region);

    /**
     * Called when no beacons in a <code>Region</code> are visible.
     *
     * @param region a Region that defines the criteria of beacons to look for
     */
    public void didExitRegion(@NonNull Region region);

    /**
     * Called with a state value of MonitorNotifier.INSIDE when at least one beacon in a <code>Region</code> is visible.
     * Called with a state value of MonitorNotifier.OUTSIDE when no beacons in a <code>Region</code> are visible.
     *
     * @param state  either MonitorNotifier.INSIDE or MonitorNotifier.OUTSIDE
     * @param region a Region that defines the criteria of beacons to look for
     */
    public void didDetermineStateForRegion(int state, @NonNull Region region);
}
