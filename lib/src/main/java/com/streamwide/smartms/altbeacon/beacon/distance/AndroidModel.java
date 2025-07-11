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

package com.streamwide.smartms.altbeacon.beacon.distance;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.altbeacon.beacon.logging.LogManager;

/**
 * Represents a specific Android device model based on the available device build information
 * <p>
 * Created by dyoung on 8/28/14.
 */
public class AndroidModel {
    private static final String TAG = "AndroidModel";
    String mVersion;
    String mBuildNumber;
    String mModel;
    String mManufacturer;


    public AndroidModel(@Nullable String version, @Nullable String buildNumber,
                        @Nullable String model,
                        @Nullable String manufacturer) {
        mVersion = version;
        mBuildNumber = buildNumber;
        mModel = model;
        mManufacturer = manufacturer;

    }

    @NonNull
    public static AndroidModel forThisDevice() {
        return new AndroidModel(
                Build.VERSION.RELEASE,
                Build.ID,
                Build.MODEL,
                Build.MANUFACTURER);
    }

    public @NonNull String getVersion() {
        return mVersion;
    }

    public void setVersion(@NonNull String mVersion) {
        this.mVersion = mVersion;
    }

    public @NonNull String getBuildNumber() {
        return mBuildNumber;
    }

    public @NonNull String getModel() {
        return mModel;
    }


    public @NonNull String getManufacturer() {
        return mManufacturer;
    }

    public void setBuildNumber(@NonNull String mBuildNumber) {
        this.mBuildNumber = mBuildNumber;
    }

    public void setModel(@NonNull String mModel) {
        this.mModel = mModel;
    }

    public void setManufacturer(@NonNull String mManufacturer) {
        this.mManufacturer = mManufacturer;
    }

    /**
     * Calculates a qualitative match score between two different Android device models for the
     * purposes of how likely they are to have similar Bluetooth signal level responses
     *
     * @param otherModel
     * @return match quality, higher numbers are a better match
     */
    public int matchScore(@NonNull AndroidModel otherModel) {
        int score = 0;
        if (this.mManufacturer.equalsIgnoreCase(otherModel.mManufacturer)) {
            score = 1;
        }
        if (score == 1 && this.mModel.equals(otherModel.mModel)) {
            score = 2;
        }
        if (score == 2 && this.mBuildNumber.equals(otherModel.mBuildNumber)) {
            score = 3;
        }
        if (score == 3 && this.mVersion.equals(otherModel.mVersion)) {
            score = 4;
        }
        LogManager.d(TAG, "Score is %s for %s compared to %s", score, toString(), otherModel);
        return score;
    }

    @Override
    public String toString() {
        return "" + mManufacturer + ";" + mModel + ";" + mBuildNumber + ";" + mVersion;
    }
}
