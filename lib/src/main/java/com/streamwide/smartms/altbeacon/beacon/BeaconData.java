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

import androidx.annotation.Nullable;

/**
 * Server-side data associated with a beacon.  Requires registration of a web service to fetch the
 * data.
 */
public interface BeaconData {
    @Nullable
    public Double getLatitude();

    public void setLatitude(@Nullable Double latitude);

    public void setLongitude(@Nullable Double longitude);

    @Nullable
    public Double getLongitude();

    @Nullable
    public String get(@Nullable String key);

    public void set(@Nullable String key, @Nullable String value);

    public void sync(@Nullable BeaconDataNotifier notifier);

    public boolean isDirty();
}
