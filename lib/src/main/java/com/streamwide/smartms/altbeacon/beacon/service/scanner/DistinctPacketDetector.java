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

package com.streamwide.smartms.altbeacon.beacon.service.scanner;


import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by dyoung on 4/8/17.
 * <p>
 * This class tracks whether multiple distinct BLE packets have been seen, with the purpose of
 * determining if the Android device supports detecting multiple distinct packets in a single scan.
 * Some older devices are not capable of this (e.g. Nexus 4, Moto G1), so detecting multiple packets
 * requires stopping and restarting scanning on these devices.  This allows detecting if that is
 * necessary.
 * <p>
 * <strong>This class is not thread safe.</strong>
 */
public class DistinctPacketDetector {
    // Sanity limit for the number of packets to track, so we don't use too much memory
    private static final int MAX_PACKETS_TO_TRACK = 1000;

    @NonNull
    private final Set<ByteBuffer> mDistinctPacketsDetected = new HashSet<>();

    public void clearDetections() {
        mDistinctPacketsDetected.clear();
    }

    public boolean isPacketDistinct(@NonNull String originMacAddress, @NonNull byte[] scanRecord) {
        byte[] macBytes = originMacAddress.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(macBytes.length + scanRecord.length);
        buffer.put(macBytes);
        buffer.put(scanRecord);
        buffer.rewind(); // rewind puts position back to beginning so .equals and .hashCode work

        if (mDistinctPacketsDetected.size() == MAX_PACKETS_TO_TRACK) {
            return mDistinctPacketsDetected.contains(buffer);
        } else {
            return mDistinctPacketsDetected.add(buffer);
        }
    }

}
