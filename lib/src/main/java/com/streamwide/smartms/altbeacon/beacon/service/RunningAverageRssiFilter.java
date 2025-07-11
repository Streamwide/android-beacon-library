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

import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.streamwide.smartms.altbeacon.beacon.logging.LogManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Calculate a RSSI value on base of an arbitrary list of measured RSSI values
 * The list is clipped by a certain length at start and end and the average
 * is calculate by simple arithmetic average
 */
public class RunningAverageRssiFilter implements RssiFilter {

    private static final String TAG = "RunningAverageRssiFilter";
    public static final long DEFAULT_SAMPLE_EXPIRATION_MILLISECONDS = 20000; /* 20 seconds */
    private static long sampleExpirationMilliseconds = DEFAULT_SAMPLE_EXPIRATION_MILLISECONDS;
    private ArrayList<Measurement> mMeasurements = new ArrayList<Measurement>();

    @Override
    public void addMeasurement(@NonNull Integer rssi) {
        Measurement measurement = new Measurement();
        measurement.rssi = rssi;
        measurement.timestamp = SystemClock.elapsedRealtime();
        mMeasurements.add(measurement);
    }

    @Override
    public boolean noMeasurementsAvailable() {
        return mMeasurements.size() == 0;
    }


    @Override
    public int getMeasurementCount() {
        return mMeasurements.size();
    }

    @Override
    public double calculateRssi() {
        refreshMeasurements();
        int size = mMeasurements.size();
        int startIndex = 0;
        int endIndex = size - 1;
        if (size > 2) {
            startIndex = size / 10 + 1;
            endIndex = size - size / 10 - 2;
        }

        double sum = 0;
        for (int i = startIndex; i <= endIndex; i++) {
            sum += mMeasurements.get(i).rssi;
        }
        double runningAverage = sum / (endIndex - startIndex + 1);

        LogManager.d(TAG, "Running average mRssi based on %s measurements: %s",
                size, runningAverage);
        return runningAverage;
    }

    private synchronized void refreshMeasurements() {
        ArrayList<Measurement> newMeasurements = new ArrayList<Measurement>();
        Iterator<Measurement> iterator = mMeasurements.iterator();
        while (iterator.hasNext()) {
            Measurement measurement = iterator.next();
            if (SystemClock.elapsedRealtime() - measurement.timestamp < sampleExpirationMilliseconds) {
                newMeasurements.add(measurement);
            }
        }
        mMeasurements = newMeasurements;
        Collections.sort(mMeasurements);
    }

    protected class Measurement implements Comparable<Measurement> {
        Integer rssi;
        long timestamp;

        @Override
        public int compareTo(Measurement arg0) {
            return rssi.compareTo(arg0.rssi);
        }
    }

    public static void setSampleExpirationMilliseconds(long newSampleExpirationMilliseconds) {
        sampleExpirationMilliseconds = newSampleExpirationMilliseconds;
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    static long getSampleExpirationMilliseconds() {
        return sampleExpirationMilliseconds;
    }
}
