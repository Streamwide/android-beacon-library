/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Thu, 16 May 2024 10:21:47 +0100
 * @copyright  Copyright (c) 2024 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2024 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Thu, 16 May 2024 10:08:33 +0100
 */

package com.streamwide.smartms.altbeacon.beacon.logger;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.altbeacon.BuildConfig;


/**
 * Default logger delegate implementation which logs in LogCat with {@link Log}.
 * Log tag is set to <b>UploadService</b> for all the logs.
 */
public class DefaultLoggerDelegate implements Logger.LoggerDelegate {

    private static final String TAG = "Beacon-LoggerDelegate";

    @Override
    public void error(@Nullable String tag, @NonNull String message) {
        Log.e(TAG, tag + " - " + message);
    }

    @Override
    public void error(@Nullable String tag, @NonNull String message, @Nullable Throwable exception) {
        Log.e(TAG, tag + " - " + message, exception);
    }

    @Override
    public void debug(@Nullable String tag, @NonNull String message) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, tag + " - " + message);
        }
    }

    @Override
    public void info(@Nullable String tag, @NonNull String message) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, tag + " - " + message);
        }
    }
}
