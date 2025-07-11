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

package com.streamwide.smartms.altbeacon.beacon.client;

import androidx.annotation.Nullable;

public class DataProviderException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -2574842662565384114L;

    public DataProviderException() {
        super();
    }

    public DataProviderException(@Nullable String msg) {
        super(msg);
    }

    public DataProviderException(@Nullable String msg, @Nullable Throwable t) {
        super(msg, t);
    }
}
