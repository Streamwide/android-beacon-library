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
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class represents a criteria of fields used to match beacons.
 * <p>
 * The uniqueId field is used to distinguish this Region in the system.  When you set up
 * monitoring or ranging based on a Region and later want to stop monitoring or ranging,
 * you must do so by passing a Region object that has the same uniqueId field value.  If it
 * doesn't match, you can't cancel the operation.  There is no other purpose to this field.
 * <p>
 * The region can be constructed from a multi-part identifier.  The first identifier is the most
 * significant, the second the second most significant, etc.
 * <p>
 * When constructing a range, any or all of these identifiers may be set to null,
 * which indicates that they are a wildcard and will match any value.
 *
 * @author dyoung
 */
public class Region implements Parcelable, Serializable {
    private static final String TAG = "Region";
    private static final Pattern MAC_PATTERN = Pattern.compile("^[0-9A-Fa-f]{2}\\:[0-9A-Fa-f]{2}\\:[0-9A-Fa-f]{2}\\:[0-9A-Fa-f]{2}\\:[0-9A-Fa-f]{2}\\:[0-9A-Fa-f]{2}$");
    /**
     * Required to make class Parcelable
     */
    public static final Creator<Region> CREATOR
            = new Creator<Region>() {
        public Region createFromParcel(Parcel in) {
            return new Region(in);
        }

        public Region[] newArray(int size) {
            return new Region[size];
        }
    };
    protected final List<Identifier> mIdentifiers;
    protected final String mBluetoothAddress;
    protected final String mUniqueId;

    /**
     * Constructs a new Region object to be used for Ranging or Monitoring
     *
     * @param uniqueId - A unique identifier used to later cancel Ranging and Monitoring, or change the region being Ranged/Monitored
     * @param id1      - most significant identifier (can be null)
     * @param id2      - second most significant identifier (can be null)
     * @param id3      - third most significant identifier (can be null)
     */
    public Region(@NonNull String uniqueId, @Nullable Identifier id1, @Nullable Identifier id2, @Nullable Identifier id3) {
        this.mIdentifiers = new ArrayList<Identifier>(3);
        this.mIdentifiers.add(id1);
        this.mIdentifiers.add(id2);
        this.mIdentifiers.add(id3);
        this.mUniqueId = uniqueId;
        this.mBluetoothAddress = null;
        if (uniqueId == null) {
            throw new NullPointerException("uniqueId may not be null");
        }
    }

    /**
     * Constructs a new Region object to be used for Ranging or Monitoring
     *
     * @param uniqueId    - A unique identifier used to later cancel Ranging and Monitoring, or change the region being Ranged/Monitored
     * @param identifiers - list of identifiers for this region
     */
    public Region(@Nullable String uniqueId, @Nullable List<Identifier> identifiers) {
        this(uniqueId, identifiers, null);
    }

    /**
     * Constructs a new Region object to be used for Ranging or Monitoring
     *
     * @param uniqueId         - A unique identifier used to later cancel Ranging and Monitoring, or change the region being Ranged/Monitored
     * @param identifiers      - list of identifiers for this region
     * @param bluetoothAddress - mac address
     */
    public Region(@Nullable String uniqueId, @Nullable List<Identifier> identifiers, @Nullable String bluetoothAddress) {
        validateMac(bluetoothAddress);
        this.mIdentifiers = new ArrayList<Identifier>(identifiers);
        this.mUniqueId = uniqueId;
        this.mBluetoothAddress = bluetoothAddress;
        if (uniqueId == null) {
            throw new NullPointerException("uniqueId may not be null");
        }
    }

    /**
     * Constructs a new Region object to be used for Ranging or Monitoring
     *
     * @param uniqueId         - A unique identifier used to later cancel Ranging and Monitoring, or change the region being Ranged/Monitored
     * @param bluetoothAddress - mac address used to match beacons
     */
    public Region(@Nullable String uniqueId, @Nullable String bluetoothAddress) {
        validateMac(bluetoothAddress);
        this.mBluetoothAddress = bluetoothAddress;
        this.mUniqueId = uniqueId;
        this.mIdentifiers = new ArrayList<Identifier>();
        if (uniqueId == null) {
            throw new NullPointerException("uniqueId may not be null");
        }
    }

    /**
     * Convenience method to get the first identifier
     *
     * @return
     */
    @Nullable
    public Identifier getId1() {
        return getIdentifier(0);
    }

    /**
     * Convenience method to get the second identifier
     *
     * @return
     */
    @Nullable
    public Identifier getId2() {
        return getIdentifier(1);
    }

    /**
     * Convenience method to get the third identifier
     *
     * @return
     */
    @Nullable
    public Identifier getId3() {
        return getIdentifier(2);
    }

    /**
     * Returns the 0-indexed identifier
     * Note:  IMPORTANT:  to get id1, you would call getIdentifier(0);
     *
     * @param i
     * @return
     */
    @Nullable
    public Identifier getIdentifier(int i) {
        return mIdentifiers.size() > i ? mIdentifiers.get(i) : null;
    }

    /**
     * Returns the identifier used to start or stop ranging/monitoring this region when calling
     * the <code>BeaconManager</code> methods.
     *
     * @return
     */
    @Nullable
    public String getUniqueId() {
        return mUniqueId;
    }

    /**
     * Returns the mac address used to filter for beacons
     */
    @Nullable
    public String getBluetoothAddress() {
        return mBluetoothAddress;
    }

    /**
     * Checks to see if an Beacon object is included in the matching criteria of this Region
     *
     * @param beacon the beacon to check to see if it is in the Region
     * @return true if is covered
     */
    public boolean matchesBeacon(@NonNull Beacon beacon) {
        // All identifiers must match, or the corresponding region identifier must be null.
        for (int i = mIdentifiers.size(); --i >= 0; ) {
            final Identifier identifier = mIdentifiers.get(i);
            Identifier beaconIdentifier = null;
            if (i < beacon.mIdentifiers.size()) {
                beaconIdentifier = beacon.getIdentifier(i);
            }
            if ((beaconIdentifier == null && identifier != null) ||
                    (beaconIdentifier != null && identifier != null && !identifier.equals(beaconIdentifier))) {
                return false;
            }
        }
        if (mBluetoothAddress != null && !mBluetoothAddress.equalsIgnoreCase(beacon.mBluetoothAddress)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return this.mUniqueId.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this.getClass() == other.getClass()) {
            return ((Region) other).mUniqueId.equals(this.mUniqueId);
        }
        return false;
    }

    public boolean hasSameIdentifiers(@NonNull Region region) {
        if (region.mIdentifiers.size() == this.mIdentifiers.size()) {
            for (int i = 0; i < region.mIdentifiers.size(); i++) {

                if (region.getIdentifier(i) == null && this.getIdentifier(i) != null) {
                    return false;
                } else if (region.getIdentifier(i) != null && this.getIdentifier(i) == null) {
                    return false;
                } else if (!(region.getIdentifier(i) == null && this.getIdentifier(i) == null)) {
                    if (!region.getIdentifier(i).equals(this.getIdentifier(i))) {
                        return false;
                    }
                }
            }
        } else {
            return false;
        }
        return true;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (Identifier identifier : mIdentifiers) {
            if (i > 1) {
                sb.append(" ");
            }
            sb.append("id");
            sb.append(i);
            sb.append(": ");
            sb.append(identifier == null ? "null" : identifier.toString());
            i++;
        }
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(@NonNull Parcel out, int flags) {
        out.writeString(mUniqueId);
        out.writeString(mBluetoothAddress);
        out.writeInt(mIdentifiers.size());

        for (Identifier identifier : mIdentifiers) {
            if (identifier != null) {
                out.writeString(identifier.toString());
            } else {
                out.writeString(null);
            }
        }
    }


    protected Region(@NonNull Parcel in) {
        mUniqueId = in.readString();
        mBluetoothAddress = in.readString();
        int size = in.readInt();
        mIdentifiers = new ArrayList<Identifier>(size);
        for (int i = 0; i < size; i++) {
            String identifierString = in.readString();
            if (identifierString == null) {
                mIdentifiers.add(null);
            } else {
                Identifier identifier = Identifier.parse(identifierString);
                mIdentifiers.add(identifier);
            }
        }
    }

    private void validateMac(String mac) throws IllegalArgumentException {
        if (mac != null) {
            if (!MAC_PATTERN.matcher(mac).matches()) {
                throw new IllegalArgumentException("Invalid mac address: '" + mac + "' Must be 6 hex bytes separated by colons.");
            }
        }
    }

    /**
     * Returns a clone of this instance.
     *
     * @return a new instance of this class with the same uniqueId and identifiers
     * @deprecated instances of this class are immutable and therefore don't have to be cloned when
     * used in concurrent code.
     */
    @Override
    @Deprecated
    @NonNull
    public Region clone() {
        return new Region(mUniqueId, mIdentifiers, mBluetoothAddress);
    }

}
