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

package com.streamwide.smartms.altbeacon.beacon.service;

import androidx.annotation.NonNull;

import com.streamwide.smartms.altbeacon.beacon.logging.LogManager;

/**
 * This filter calculates its rssi on base of an auto regressive moving average (ARMA)
 * It needs only the current value to do this; the general formula is  n(t) = n(t-1) - c * (n(t-1) - n(t))
 * where c is a coefficient, that denotes the smoothness - the lower the value, the smoother the average
 * Note: a smoother average needs longer to "settle down"
 * Note: For signals, that change rather frequently (say, 1Hz or faster) and tend to vary more a recommended
 * value would be 0,1 (that means the actual value is changed by 10% of the difference between the
 * actual measurement and the actual average)
 * For signals at lower rates (10Hz) a value of 0.25 to 0.5 would be appropriate
 */
public class ArmaRssiFilter implements RssiFilter {

    private static double DEFAULT_ARMA_SPEED = 0.1;     //How likely is it that the RSSI value changes?
    //Note: the more unlikely, the higher can that value be
    //      also, the lower the (expected) sending frequency,
    //      the higher should that value be

    private static final String TAG = "ArmaRssiFilter";
    //initially set to min value
    private int armaMeasurement;
    private double armaSpeed = 0.1;
    private boolean isInitialized = false;

    public ArmaRssiFilter() {
        this.armaSpeed = DEFAULT_ARMA_SPEED;
    }

    public void addMeasurement(@NonNull Integer rssi) {
        LogManager.d(TAG, "adding rssi: %s", rssi);
        //use first measurement as initialization
        if (!isInitialized) {
            armaMeasurement = rssi;
            isInitialized = true;
        }
        ;
        armaMeasurement = (int) (armaMeasurement - armaSpeed * (armaMeasurement - rssi));
        LogManager.d(TAG, "armaMeasurement: %s", armaMeasurement);
    }

    @Override
    public int getMeasurementCount() {
        return 0;
    }

    public boolean noMeasurementsAvailable() {
        return false;
    }

    public double calculateRssi() {
        return armaMeasurement;

    }

    public static void setDEFAULT_ARMA_SPEED(double default_arma_speed) {
        DEFAULT_ARMA_SPEED = default_arma_speed;
    }

}
