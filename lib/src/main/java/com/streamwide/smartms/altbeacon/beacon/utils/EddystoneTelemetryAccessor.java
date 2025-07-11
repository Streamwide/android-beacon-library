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

package com.streamwide.smartms.altbeacon.beacon.utils;

import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.altbeacon.beacon.Beacon;
import com.streamwide.smartms.altbeacon.beacon.BeaconParser;
import com.streamwide.smartms.altbeacon.beacon.logger.Logger;

/**
 * Utility class for working beacons that include Eddystone-TLM (telemetry) information
 * Created by dyoung on 12/21/15.
 */
public class EddystoneTelemetryAccessor {
    private static final String TAG = "EddystoneTLMAccessor";

    /**
     * Extracts the raw Eddystone telemetry bytes from the extra data fields of an associated beacon.
     * This is useful for passing the telemetry to Google's backend services.
     *
     * @param beacon
     * @return the bytes of the telemetry frame
     */
    @Nullable
    public byte[] getTelemetryBytes(@NonNull Beacon beacon) {
        if (beacon.getExtraDataFields().size() >= 5) {
            Beacon telemetryBeacon = new Beacon.Builder()
                    .setDataFields(beacon.getExtraDataFields())
                    .build();
            BeaconParser telemetryParser = new BeaconParser()
                    .setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT);
            byte[] telemetryBytes = telemetryParser.getBeaconAdvertisementData(telemetryBeacon);
            Logger.debug(TAG, "Rehydrated telemetry bytes are :" + byteArrayToString(telemetryBytes));
            return telemetryBytes;
        } else {
            return null;
        }
    }

    /**
     * Extracts the raw Eddystone telemetry bytes from the extra data fields of an associated beacon
     * and base64 encodes them.  This is useful for passing the telemetry to Google's backend
     * services.
     *
     * @param beacon
     * @return base64 encoded telemetry bytes
     */
    public @Nullable String getBase64EncodedTelemetry(@NonNull Beacon beacon) {
        byte[] bytes = getTelemetryBytes(beacon);
        if (bytes != null) {
            String base64EncodedTelemetry = Base64.encodeToString(bytes, Base64.DEFAULT);
            // 12-21 00:17:18.844 20180-20180/? D/EddystoneTLMAccessor: Rehydrated telemetry bytes are :20 00 00 00 88 29 18 4d 00 00 18 4d 00 00
            // 12-21 00:17:18.844 20180-20180/? D/EddystoneTLMAccessor: Base64 telemetry bytes are :IAAAAIgpGE0AABhNAAA=
            Logger.debug(TAG, "Base64 telemetry bytes are :" + base64EncodedTelemetry);
            return base64EncodedTelemetry;
        } else {
            return null;
        }
    }

    private String byteArrayToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(String.format("%02x", bytes[i]));
            sb.append(" ");
        }
        return sb.toString().trim();
    }
}
