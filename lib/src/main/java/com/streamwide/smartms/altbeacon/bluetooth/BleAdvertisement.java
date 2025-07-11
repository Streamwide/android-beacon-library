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

package com.streamwide.smartms.altbeacon.bluetooth;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses a byte array representing a BLE advertisement into
 * a number of "Payload Data Units" (PDUs).
 * <p>
 * Created by dyoung on 4/14/15.
 */
public class BleAdvertisement {
    private static final String TAG = "BleAdvertisement";
    private List<Pdu> mPdus;
    private byte[] mBytes;

    public BleAdvertisement(@Nullable byte[] bytes) {
        mBytes = bytes != null ? bytes.clone() : null;
        ArrayList<Pdu> pdus = new ArrayList<Pdu>();
        // Get PDUs from the main advert
        parsePdus(0, bytes != null && bytes.length < 31 ? bytes.length : 31, pdus);
        // Get PDUs from the scan response
        // Android puts the scan response at offset 31
        if (bytes != null && bytes.length > 31) {
            parsePdus(31, bytes.length, pdus);
        }
        mPdus = pdus;
    }

    private void parsePdus(int startIndex, int endIndex, ArrayList<Pdu> pdus) {
        int index = startIndex;
        Pdu pdu = null;
        if (mBytes != null) {
            do {
                pdu = Pdu.parse(mBytes, index);
                if (pdu != null) {
                    index = index + pdu.getDeclaredLength() + 1;
                    pdus.add(pdu);
                }
            }
            while (pdu != null && index < endIndex);
        }
    }

    /**
     * The list of PDUs inside the advertisement
     *
     * @return
     */
    @Nullable
    public List<Pdu> getPdus() {
        return mPdus;
    }
}