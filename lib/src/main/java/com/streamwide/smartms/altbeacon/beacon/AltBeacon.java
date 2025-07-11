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

import android.os.Parcel;

import androidx.annotation.NonNull;

import java.util.ArrayList;

/**
 * <p>The <code>AltBeacon</code> class represents a single hardware AltBeacon detected by
 * an Android device.  It is more specific than the <code>Beacon</code> class in that it provides
 * access to the #mfgReserved field.</p>
 *
 * <p>An <code>AltBeacon</code> is identified by a unique three part identifier.  The first
 * identifier Id1 is normally used across an organization, the second identifier Id2 is used to
 * group beacons and the third identifier Id3 is used to uniquely identify a specific beacon (in
 * combination with the other two identifiers.)
 *
 * @author David G. Young
 */
public class AltBeacon extends Beacon {
    private static final String TAG = "AltBeacon";

    /**
     * Required for making object Parcelable.  If you override this class, you must provide an
     * equivalent version of this method.
     */
    public static final Creator<AltBeacon> CREATOR
            = new Creator<AltBeacon>() {
        public AltBeacon createFromParcel(Parcel in) {
            return new AltBeacon(in);
        }

        public AltBeacon[] newArray(int size) {
            return new AltBeacon[size];
        }
    };

    /**
     * Copy constructor from base class
     *
     * @param beacon
     */
    protected AltBeacon(@NonNull Beacon beacon) {
        super(beacon);
    }

    /**
     * @see Builder to make AltBeacon instances
     */
    protected AltBeacon() {
        super();
    }

    /**
     * Required for making object Parcelable
     **/
    protected AltBeacon(@NonNull Parcel in) {
        super(in);
    }

    /**
     * Returns a field with a value from 0-255 that can be used for the purposes specified by the
     * manufacturer.  The manufacturer specifications for the beacon should be checked before using
     * this field, and the manufacturer should be checked against the Beacon#mManufacturer
     * field
     *
     * @return mfgReserved
     */
    public int getMfgReserved() {
        return mDataFields.get(0).intValue();
    }

    /**
     * Required for making object Parcelable
     *
     * @return
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Required for making object Parcelable
     **/
    @Override
    public void writeToParcel(@NonNull Parcel out, int flags) {
        super.writeToParcel(out, flags);
    }


    /**
     * Builder class for AltBeacon objects. Provides a convenient way to set the various fields of a
     * Beacon
     *
     * <p>Example:
     *
     * <pre>
     * Beacon beacon = new Beacon.Builder()
     *         .setId1(&quot;2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6&quot;)
     *         .setId2("1")
     *         .setId3("2")
     *         .setMfgReserved(3);
     *         .build();
     * </pre>
     */
    public static class Builder extends Beacon.Builder {
        @Override
        @NonNull
        public Beacon build() {
            return new AltBeacon(super.build());
        }

        @NonNull
        public Builder setMfgReserved(int mfgReserved) {
            if (mBeacon.mDataFields.size() != 0) {
                mBeacon.mDataFields = new ArrayList<Long>();
            }
            mBeacon.mDataFields.add((long) mfgReserved);
            return this;
        }
    }

}
