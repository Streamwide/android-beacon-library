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

package com.streamwide.smartms.altbeacon.beacon.distance;

/**
 * Interface for a class that can estimate the distance between a mobile
 * device and a beacon based on the measured RSSI and a reference txPower
 * calibration value.
 * <p>
 * Created by dyoung on 8/28/14.
 */
public interface DistanceCalculator {
    public double calculateDistance(int txPower, double rssi);
}
