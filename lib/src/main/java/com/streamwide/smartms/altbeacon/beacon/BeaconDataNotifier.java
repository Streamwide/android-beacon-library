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

import com.streamwide.smartms.altbeacon.beacon.client.DataProviderException;

/**
 * Notifies when server-side beacon data are available from a web service.
 */
public interface BeaconDataNotifier {
    /**
     * This method is called after a request to get or sync beacon data
     * If fetching data was successful, the data is returned and the exception is null.
     * If fetching of the data is not successful, an exception is provided.
     *
     * @param beacon
     * @param data
     * @param exception
     */
    public void beaconDataUpdate(@Nullable Beacon beacon, @Nullable BeaconData data, @Nullable DataProviderException exception);
}
