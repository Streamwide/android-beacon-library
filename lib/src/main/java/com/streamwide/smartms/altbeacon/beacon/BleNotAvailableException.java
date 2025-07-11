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
 * @lastModifiedOn Wed, 5 Jun 2024 10:44:41 +0100
 */
package com.streamwide.smartms.altbeacon.beacon;

import androidx.annotation.NonNull;

/**
 * Indicates that Bluetooth Low Energy is not available on this device
 *
 * @author David G. Young
 * @see BeaconManager#checkAvailability
 */
public class BleNotAvailableException extends RuntimeException {

    private static final long serialVersionUID = 2242747823097637729L;

    public BleNotAvailableException(@NonNull String message) {
        super(message);
    }

}
