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

/**
 * Created by dyoung on 1/10/15.
 */
public class DetectionTracker {
    private static final DetectionTracker INSTANCE = new DetectionTracker();

    private long mLastDetectionTime = 0l;

    private DetectionTracker() {

    }

    @NonNull
    public static DetectionTracker getInstance() {
        return INSTANCE;
    }

    public long getLastDetectionTime() {
        return mLastDetectionTime;
    }

    public void recordDetection() {
        mLastDetectionTime = SystemClock.elapsedRealtime();
    }
}
