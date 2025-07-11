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

package com.streamwide.smartms.altbeacon.beacon.service;

import androidx.annotation.NonNull;

/**
 * Interface that can be implemented to overwrite measurement and filtering
 * of RSSI values
 */
public interface RssiFilter {

    public void addMeasurement(@NonNull Integer rssi);

    public boolean noMeasurementsAvailable();

    public double calculateRssi();

    public int getMeasurementCount();

}
