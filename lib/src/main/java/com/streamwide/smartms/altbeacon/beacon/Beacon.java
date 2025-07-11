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

import com.streamwide.smartms.altbeacon.beacon.client.BeaconDataFactory;
import com.streamwide.smartms.altbeacon.beacon.client.NullBeaconDataFactory;
import com.streamwide.smartms.altbeacon.beacon.distance.DistanceCalculator;
import com.streamwide.smartms.altbeacon.beacon.logging.LogManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The <code>Beacon</code> class represents a single hardware Beacon detected by
 * an Android device.
 *
 * <pre>A Beacon is identified by a unique multi-part identifier, with the first of the ordered
 * identifiers being more significant for the purposes of grouping beacons.
 *
 * A Beacon sends a Bluetooth Low Energy (BLE) advertisement that contains these
 * three identifiers, along with the calibrated tx power (in RSSI) of the
 * Beacon's Bluetooth transmitter.
 *
 * This class may only be instantiated from a BLE packet, and an RSSI measurement for
 * the packet.  The class parses out the identifier, along with the calibrated
 * tx power.  It then uses the measured RSSI and calibrated tx power to do a rough
 * distance measurement (the mDistance field)
 *
 * @author David G. Young
 * @see     Region#matchesBeacon(Beacon Beacon)
 */
public class Beacon implements Parcelable, Serializable {
    private static final String TAG = "Beacon";

    private static final List<Long> UNMODIFIABLE_LIST_OF_LONG =
            Collections.unmodifiableList(new ArrayList<Long>());
    private static final List<Identifier> UNMODIFIABLE_LIST_OF_IDENTIFIER =
            Collections.unmodifiableList(new ArrayList<Identifier>());

    /**
     * Determines whether a the bluetoothAddress (mac address) must be the same for two Beacons
     * to be configured equal.
     */
    protected static boolean sHardwareEqualityEnforced = false;

    @Nullable
    protected static DistanceCalculator sDistanceCalculator = null;

    /**
     * The a list of the multi-part identifiers of the beacon.  Together, these identifiers signify
     * a unique beacon.  The identifiers are ordered by significance for the purpose of grouping
     * beacons
     */
    @NonNull
    protected List<Identifier> mIdentifiers;

    /**
     * A list of generic non-identifying data fields included in the beacon advertisement.  Data
     * fields are limited to the size of a Java long, or six bytes.
     */
    @NonNull
    protected List<Long> mDataFields;
    /**
     * A list of generic non-identifying data fields included in a secondary beacon advertisement
     * and merged into this beacon.  Data fields are limited to the size of a Java long, or six
     * bytes.
     */
    @NonNull
    protected List<Long> mExtraDataFields;

    /**
     * A double that is an estimate of how far the Beacon is away in meters.   Note that this number
     * fluctuates quite a bit with RSSI, so despite the name, it is not super accurate.
     */
    @Nullable
    protected Double mDistance;
    /**
     * The measured signal strength of the Bluetooth packet that led do this Beacon detection.
     */
    protected int mRssi;
    /**
     * The calibrated measured Tx power of the Beacon in RSSI
     * This value is baked into an Beacon when it is manufactured, and
     * it is transmitted with each packet to aid in the mDistance estimate
     */
    protected int mTxPower;

    /**
     * The Bluetooth mac address
     */
    @NonNull
    protected String mBluetoothAddress;

    /**
     * The number of rssi samples available, if known
     */
    private int mRssiMeasurementCount = 0;

    /**
     * The number of packets detected in the last cycle
     */
    private int mPacketCount = 0;

    /**
     * If multiple RSSI samples were available, this is the running average
     */
    @Nullable
    protected Double mRunningAverageRssi = null;

    /**
     * Used to attach data to individual Beacons, either locally or in the cloud
     */
    @NonNull
    protected static BeaconDataFactory beaconDataFactory = new NullBeaconDataFactory();

    /**
     * The two byte value indicating the type of beacon that this is, which is used for figuring
     * out the byte layout of the beacon advertisement
     */
    protected int mBeaconTypeCode;

    /**
     * A two byte code indicating the beacon manufacturer.  A list of registered manufacturer codes
     * may be found here:
     * https://www.bluetooth.org/en-us/specification/assigned-numbers/company-identifiers
     * <p>
     * If the beacon is a GATT-based beacon, this field will be set to -1
     */
    protected int mManufacturer;

    /**
     * A 32 bit service uuid for the beacon
     * <p>
     * This is valid only for GATT-based beacons.   If the beacon is a manufacturer data-based
     * beacon, this field will be -1
     */

    protected int mServiceUuid = -1;

    /**
     * The Bluetooth device name.  This is a field transmitted by the remote beacon device separate
     * from the advertisement data
     */
    @NonNull
    protected String mBluetoothName;

    /**
     * The identifier of the beaconParser used to create this beacon.  Useful for figuring out
     * beacon types.
     */
    @NonNull
    protected String mParserIdentifier;

    /**
     * An indicator marking this beacon as a potential multi frame beacon.
     * <p>
     * This will be set to true if the beacon was parsed by a BeaconParser which has extra
     * data parsers defined.
     */
    protected boolean mMultiFrameBeacon = false;

    /**
     * The timestamp of the first packet detected in milliseconds.
     */
    protected long mFirstCycleDetectionTimestamp = 0L;

    /**
     * The timestamp of the last packet detected in milliseconds.
     */
    protected long mLastCycleDetectionTimestamp = 0L;

    /**
     * Required for making object Parcelable.  If you override this class, you must provide an
     * equivalent version of this method.
     */
    @Deprecated
    public static final Creator<Beacon> CREATOR
            = new Creator<Beacon>() {
        public Beacon createFromParcel(Parcel in) {
            return new Beacon(in);
        }

        public Beacon[] newArray(int size) {
            return new Beacon[size];
        }
    };

    /**
     * Sets the DistanceCalculator to use with this beacon
     *
     * @param dc
     */
    public static void setDistanceCalculator(@Nullable DistanceCalculator dc) {
        sDistanceCalculator = dc;
    }

    /**
     * Gets the DistanceCalculator to use with this beacon
     */
    @Nullable
    public static DistanceCalculator getDistanceCalculator() {
        return sDistanceCalculator;
    }

    /**
     * Configures whether a the bluetoothAddress (mac address) must be the same for two Beacons
     * to be configured equal.  This setting applies to all beacon instances in the same process.
     * Defaults to false for backward compatibility.
     *
     * @param e
     */
    public static void setHardwareEqualityEnforced(boolean e) {
        sHardwareEqualityEnforced = e;
    }

    public static boolean getHardwareEqualityEnforced() {
        return sHardwareEqualityEnforced;
    }

    /**
     * Required for making Beacon parcelable
     *
     * @param in parcel
     */
    @Deprecated
    protected Beacon(@NonNull Parcel in) {
        int size = in.readInt();

        this.mIdentifiers = new ArrayList<Identifier>(size);
        for (int i = 0; i < size; i++) {
            mIdentifiers.add(Identifier.parse(in.readString()));
        }
        mDistance = in.readDouble();
        mRssi = in.readInt();
        mTxPower = in.readInt();
        mBluetoothAddress = in.readString();
        mBeaconTypeCode = in.readInt();
        mServiceUuid = in.readInt();
        int dataSize = in.readInt();
        this.mDataFields = new ArrayList<Long>(dataSize);
        for (int i = 0; i < dataSize; i++) {
            mDataFields.add(in.readLong());
        }
        int extraDataSize = in.readInt();
        this.mExtraDataFields = new ArrayList<Long>(extraDataSize);
        for (int i = 0; i < extraDataSize; i++) {
            mExtraDataFields.add(in.readLong());
        }
        mManufacturer = in.readInt();
        mBluetoothName = in.readString();
        mParserIdentifier = in.readString();
        mMultiFrameBeacon = in.readByte() != 0;
        mRunningAverageRssi = in.readDouble();
        mRssiMeasurementCount = in.readInt();
        mPacketCount = in.readInt();
        mFirstCycleDetectionTimestamp = in.readLong();
        mLastCycleDetectionTimestamp = in.readLong();
    }

    /**
     * Copy constructor
     *
     * @param otherBeacon
     */
    protected Beacon(@NonNull Beacon otherBeacon) {
        super();
        mIdentifiers = new ArrayList<>(otherBeacon.mIdentifiers);
        mDataFields = new ArrayList<>(otherBeacon.mDataFields);
        mExtraDataFields = new ArrayList<>(otherBeacon.mExtraDataFields);
        this.mDistance = otherBeacon.mDistance;
        this.mRunningAverageRssi = otherBeacon.mRunningAverageRssi;
        this.mPacketCount = otherBeacon.mPacketCount;
        this.mRssiMeasurementCount = otherBeacon.mRssiMeasurementCount;
        this.mRssi = otherBeacon.mRssi;
        this.mTxPower = otherBeacon.mTxPower;
        this.mBluetoothAddress = otherBeacon.mBluetoothAddress;
        this.mBeaconTypeCode = otherBeacon.getBeaconTypeCode();
        this.mServiceUuid = otherBeacon.getServiceUuid();
        this.mBluetoothName = otherBeacon.mBluetoothName;
        this.mParserIdentifier = otherBeacon.mParserIdentifier;
        this.mMultiFrameBeacon = otherBeacon.mMultiFrameBeacon;
        this.mManufacturer = otherBeacon.mManufacturer;
        this.mFirstCycleDetectionTimestamp = otherBeacon.mFirstCycleDetectionTimestamp;
        this.mLastCycleDetectionTimestamp = otherBeacon.mLastCycleDetectionTimestamp;
    }

    /**
     * Basic constructor that simply allocates fields
     */
    protected Beacon() {
        mIdentifiers = new ArrayList<Identifier>(1);
        mDataFields = new ArrayList<Long>(1);
        mExtraDataFields = new ArrayList<Long>(1);
    }


    /**
     * Sets the measurement count that went into the rssi sample
     *
     * @param rssiMeasurementCount
     */
    public void setRssiMeasurementCount(int rssiMeasurementCount) {
        mRssiMeasurementCount = rssiMeasurementCount;
    }

    /**
     * Returns the number of packet detections in the last ranging cycle
     */
    public int getPacketCount() {
        return mPacketCount;
    }

    /**
     * Sets the packet detections in the last ranging cycle
     *
     * @param packetCount
     */
    public void setPacketCount(int packetCount) {
        mPacketCount = packetCount;
    }

    /**
     * Returns the timestamp of the first packet detected
     */
    public long getFirstCycleDetectionTimestamp() {
        return mFirstCycleDetectionTimestamp;
    }

    /**
     * Sets the timestamp of the first packet detected
     *
     * @param firstCycleDetectionTimestamp
     */
    public void setFirstCycleDetectionTimestamp(long firstCycleDetectionTimestamp) {
        mFirstCycleDetectionTimestamp = firstCycleDetectionTimestamp;
    }

    /**
     * Returns the timestamp of the last packet detected
     */
    public long getLastCycleDetectionTimestamp() {
        return mLastCycleDetectionTimestamp;
    }

    /**
     * Sets the timestamp of the last packet detected
     *
     * @param lastCycleDetectionTimestamp
     */
    public void setLastCycleDetectionTimestamp(long lastCycleDetectionTimestamp) {
        mLastCycleDetectionTimestamp = lastCycleDetectionTimestamp;
    }

    /**
     * Returns the number of packet detections that went in to the runningAverageRssi, if known.
     * If not known or inapplicable for the rssi filter used, this is zero.
     */
    public int getMeasurementCount() {
        return mRssiMeasurementCount;
    }

    /**
     * Sets the running average rssi for use in distance calculations
     *
     * @param rssi the running average rssi
     */
    public void setRunningAverageRssi(double rssi) {
        mRunningAverageRssi = rssi;
        mDistance = null; // force calculation of accuracy and proximity next time they are requested
    }

    /**
     * @deprecated To be removed in a future release. Use
     * {@link Beacon#getRunningAverageRssi()}
     * instead.
     */
    @Deprecated
    public double getRunningAverageRssi(double rssi) {
        return mRunningAverageRssi = rssi;
    }

    /**
     * Returns the running average rssi
     *
     * @return double
     */
    public double getRunningAverageRssi() {
        if (mRunningAverageRssi != null) {
            return mRunningAverageRssi;
        }
        return mRssi;
    }

    /**
     * Sets the most recently measured rssi for use in distance calculations if a running average is
     * not available
     *
     * @param rssi
     */
    public void setRssi(int rssi) {
        mRssi = rssi;
    }

    /**
     * @see #mManufacturer
     */
    public int getManufacturer() {
        return mManufacturer;
    }

    /**
     * @see #mServiceUuid
     */
    public int getServiceUuid() {
        return mServiceUuid;
    }

    /**
     * Returns the specified identifier - 0 indexed
     * Note:  to read id1, call getIdentifier(0);
     *
     * @param i - index identfier
     * @return identifier
     */
    @NonNull
    public Identifier getIdentifier(int i) {
        return mIdentifiers.get(i);
    }


    /**
     * Convenience method to get the first identifier
     *
     * @return
     */
    @NonNull
    public Identifier getId1() {
        return mIdentifiers.get(0);
    }

    /**
     * Convenience method to get the second identifier
     *
     * @return
     */
    @NonNull
    public Identifier getId2() {
        return mIdentifiers.get(1);
    }

    /**
     * Convenience method to get the third identifier
     *
     * @return
     */
    @NonNull
    public Identifier getId3() {
        return mIdentifiers.get(2);
    }

    /**
     * Returns the list of data fields transmitted with the advertisement
     *
     * @return dataFields
     */
    @NonNull
    public List<Long> getDataFields() {
        if (mDataFields.getClass().isInstance(UNMODIFIABLE_LIST_OF_LONG)) {
            return mDataFields;
        } else {
            return Collections.unmodifiableList(mDataFields);
        }
    }

    /**
     * Returns the list of data fields transmitted with the advertisement
     *
     * @return dataFields
     */
    @NonNull
    public List<Long> getExtraDataFields() {
        if (mExtraDataFields.getClass().isInstance(UNMODIFIABLE_LIST_OF_LONG)) {
            return mExtraDataFields;
        } else {
            return Collections.unmodifiableList(mExtraDataFields);
        }
    }

    /**
     * Sets extra data fields
     *
     * @param fields
     */
    public void setExtraDataFields(@NonNull List<Long> fields) {
        mExtraDataFields = fields;
    }

    /**
     * Returns the list of identifiers transmitted with the advertisement
     *
     * @return identifier
     */
    @NonNull
    public List<Identifier> getIdentifiers() {
        if (mIdentifiers.getClass().isInstance(UNMODIFIABLE_LIST_OF_IDENTIFIER)) {
            return mIdentifiers;
        } else {
            return Collections.unmodifiableList(mIdentifiers);
        }
    }


    /**
     * Provides a calculated estimate of the distance to the beacon based on a running average of
     * the RSSI and the transmitted power calibration value included in the beacon advertisement.
     * This value is specific to the type of Android device receiving the transmission.
     *
     * @return distance
     * @see #mDistance
     */
    public double getDistance() {
        if (mDistance == null) {
            double bestRssiAvailable = mRssi;
            if (mRunningAverageRssi != null) {
                bestRssiAvailable = mRunningAverageRssi;
            } else {
                LogManager.d(TAG, "Not using running average RSSI because it is null");
            }
            mDistance = calculateDistance(mTxPower, bestRssiAvailable);
        }
        return mDistance;
    }

    /**
     * @return mRssi
     * @see #mRssi
     */
    public int getRssi() {
        return mRssi;
    }

    /**
     * @return txPowwer
     * @see #mTxPower
     */
    public int getTxPower() {
        return mTxPower;
    }

    /**
     * @return beaconTypeCode
     * @see #mBeaconTypeCode
     */
    public int getBeaconTypeCode() {
        return mBeaconTypeCode;
    }

    /**
     * @return mBluetoothAddress
     * @see #mBluetoothAddress
     */
    @NonNull
    public String getBluetoothAddress() {
        return mBluetoothAddress;
    }

    /**
     * @return mBluetoothName
     * @see #mBluetoothName
     */
    @NonNull
    public String getBluetoothName() {
        return mBluetoothName;
    }

    /**
     * @return mParserIdentifier
     * @see #mParserIdentifier
     */
    @NonNull
    public String getParserIdentifier() {
        return mParserIdentifier;
    }

    /**
     * @return mMultiFrameBeacon
     * @see #mMultiFrameBeacon
     */
    public boolean isMultiFrameBeacon() {
        return mMultiFrameBeacon;
    }

    /**
     * Calculate a hashCode for this beacon
     *
     * @return
     */
    @Override
    public int hashCode() {
        StringBuilder sb = toStringBuilder();
        if (sHardwareEqualityEnforced) {
            sb.append(mBluetoothAddress);
        }
        return sb.toString().hashCode();
    }

    /**
     * Two detected beacons are considered equal if they share the same three identifiers, regardless of their mDistance or RSSI.
     */
    @Override
    public boolean equals(Object that) {
        if (that == null) {
            return false;
        }
        if (this.getClass() != that.getClass()) {
            return false;
        }
        Beacon thatBeacon = (Beacon) that;
        if (!this.mIdentifiers.equals(thatBeacon.mIdentifiers)) {
            return false;
        }
        return sHardwareEqualityEnforced ?
                this.getBluetoothAddress().equals(thatBeacon.getBluetoothAddress()) :
                true;
    }

    /**
     * Requests server-side data for this beacon.  Requires that a BeaconDataFactory be set up with
     * a backend service.
     *
     * @param notifier interface providing a callback when data are available
     */
    public void requestData(@NonNull BeaconDataNotifier notifier) {
        beaconDataFactory.requestBeaconData(this, notifier);
    }

    /**
     * Formats a beacon as a string showing only its unique identifiers
     *
     * @return
     */
    @Override
    public String toString() {
        return toStringBuilder().toString();
    }

    private StringBuilder toStringBuilder() {
        final StringBuilder sb = new StringBuilder();
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
        if (mParserIdentifier != null) {
            sb.append(" type " + mParserIdentifier);
        }
        return sb;
    }

    /**
     * Required for making object Parcelable
     */
    @Deprecated
    public int describeContents() {
        return 0;
    }

    /**
     * Required for making object Parcelable.  If you override this class, you must override this
     * method if you add any additional fields.
     */
    @Deprecated
    public void writeToParcel(@NonNull Parcel out, int flags) {
        out.writeInt(mIdentifiers.size());
        for (Identifier identifier : mIdentifiers) {
            out.writeString(identifier == null ? null : identifier.toString());
        }
        out.writeDouble(getDistance());
        out.writeInt(mRssi);
        out.writeInt(mTxPower);
        out.writeString(mBluetoothAddress);
        out.writeInt(mBeaconTypeCode);
        out.writeInt(mServiceUuid);
        out.writeInt(mDataFields.size());
        for (Long dataField : mDataFields) {
            out.writeLong(dataField);
        }
        out.writeInt(mExtraDataFields.size());
        for (Long dataField : mExtraDataFields) {
            out.writeLong(dataField);
        }
        out.writeInt(mManufacturer);
        out.writeString(mBluetoothName);
        out.writeString(mParserIdentifier);
        out.writeByte((byte) (mMultiFrameBeacon ? 1 : 0));
        out.writeValue(mRunningAverageRssi);
        out.writeInt(mRssiMeasurementCount);
        out.writeInt(mPacketCount);
        out.writeLong(mFirstCycleDetectionTimestamp);
        out.writeLong(mLastCycleDetectionTimestamp);
    }

    /**
     * Indicates whether this beacon is an "Extra data beacon," meaning one that has no identifiers
     * but has data fields.
     *
     * @return
     */
    public boolean isExtraBeaconData() {
        return mIdentifiers.size() == 0 && mDataFields.size() != 0;
    }

    /**
     * Estimate the distance to the beacon using the DistanceCalculator set on this class.  If no
     * DistanceCalculator has been set, return -1 as the distance.
     *
     * @param txPower
     * @param bestRssiAvailable
     * @return
     * @see DistanceCalculator
     */
    @NonNull
    protected static Double calculateDistance(int txPower, double bestRssiAvailable) {
        if (Beacon.getDistanceCalculator() != null) {
            return Beacon.getDistanceCalculator().calculateDistance(txPower, bestRssiAvailable);
        } else {
            LogManager.e(TAG, "Distance calculator not set.  Distance will bet set to -1");
            return -1.0;
        }
    }

    /**
     * Builder class for Beacon objects. Provides a convenient way to set the various fields of a
     * Beacon
     *
     * <p>Example:
     *
     * <pre>
     * Beacon beacon = new Beacon.Builder()
     *         .setId1(&quot;2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6&quot;)
     *         .setId2("1")
     *         .setId3("2")
     *         .build();
     * </pre>
     */
    public static class Builder {
        protected final Beacon mBeacon;
        private Identifier mId1, mId2, mId3;

        /**
         * Creates a builder instance
         */
        public Builder() {
            mBeacon = new Beacon();
        }

        /**
         * Builds an instance of this beacon based on parameters set in the Builder
         *
         * @return beacon
         */
        @NonNull
        public Beacon build() {
            if (mId1 != null) {
                mBeacon.mIdentifiers.add(mId1);
                if (mId2 != null) {
                    mBeacon.mIdentifiers.add(mId2);
                    if (mId3 != null) {
                        mBeacon.mIdentifiers.add(mId3);
                    }
                }
            }
            return mBeacon;
        }

        /**
         * @param beacon the beacon whose fields we should copy to this beacon builder
         * @return
         */
        @NonNull
        public Builder copyBeaconFields(@NonNull Beacon beacon) {
            setIdentifiers(beacon.getIdentifiers());
            setBeaconTypeCode(beacon.getBeaconTypeCode());
            setDataFields(beacon.getDataFields());
            setBluetoothAddress(beacon.getBluetoothAddress());
            setBluetoothName(beacon.getBluetoothName());
            setExtraDataFields(beacon.getExtraDataFields());
            setManufacturer(beacon.getManufacturer());
            setTxPower(beacon.getTxPower());
            setRssi(beacon.getRssi());
            setServiceUuid(beacon.getServiceUuid());
            setMultiFrameBeacon(beacon.isMultiFrameBeacon());
            return this;
        }

        /**
         * @param identifiers identifiers to set
         * @return builder
         * @see Beacon#mIdentifiers
         */
        @NonNull
        public Builder setIdentifiers(@NonNull List<Identifier> identifiers) {
            mId1 = null;
            mId2 = null;
            mId3 = null;
            mBeacon.mIdentifiers = identifiers;
            return this;
        }

        /**
         * Convenience method allowing the first beacon identifier to be set as a String.  It will
         * be parsed into an Identifier object
         *
         * @param id1String string to parse into an identifier
         * @return builder
         */
        @NonNull
        public Builder setId1(@NonNull String id1String) {
            mId1 = Identifier.parse(id1String);
            return this;
        }

        /**
         * Convenience method allowing the second beacon identifier to be set as a String.  It will
         * be parsed into an Identifier object
         *
         * @param id2String string to parse into an identifier
         * @return builder
         */
        @NonNull
        public Builder setId2(@NonNull String id2String) {
            mId2 = Identifier.parse(id2String);
            return this;
        }

        /**
         * Convenience method allowing the third beacon identifier to be set as a String.  It will
         * be parsed into an Identifier object
         *
         * @param id3String string to parse into an identifier
         * @return builder
         */
        @NonNull
        public Builder setId3(@NonNull String id3String) {
            mId3 = Identifier.parse(id3String);
            return this;
        }

        /**
         * @param rssi
         * @return builder
         * @see Beacon#mRssi
         */
        @NonNull
        public Builder setRssi(int rssi) {
            mBeacon.mRssi = rssi;
            return this;
        }

        /**
         * @param rssi
         * @return builder
         * @see Beacon#mRssi
         */
        @NonNull
        public Builder setRunningAverageRssi(double rssi) {
            mBeacon.mRunningAverageRssi = rssi;
            return this;
        }

        /**
         * @param txPower
         * @return builder
         * @see Beacon#mTxPower
         */
        @NonNull
        public Builder setTxPower(int txPower) {
            mBeacon.mTxPower = txPower;
            return this;
        }

        /**
         * @param beaconTypeCode
         * @return builder
         * @see Beacon#mBeaconTypeCode
         */
        @NonNull
        public Builder setBeaconTypeCode(int beaconTypeCode) {
            mBeacon.mBeaconTypeCode = beaconTypeCode;
            return this;
        }

        /**
         * @param serviceUuid
         * @return builder
         * @see Beacon#mServiceUuid
         */
        @NonNull
        public Builder setServiceUuid(int serviceUuid) {
            mBeacon.mServiceUuid = serviceUuid;
            return this;
        }

        /**
         * @param bluetoothAddress
         * @return builder
         * @see Beacon#mBluetoothAddress
         */
        @NonNull
        public Builder setBluetoothAddress(@NonNull String bluetoothAddress) {
            mBeacon.mBluetoothAddress = bluetoothAddress;
            return this;
        }

        /**
         * @param dataFields
         * @return builder
         * @see Beacon#mDataFields
         */
        @NonNull
        public Builder setDataFields(@NonNull List<Long> dataFields) {
            mBeacon.mDataFields = dataFields;
            return this;
        }

        /**
         * @param extraDataFields
         * @return builder
         * @see Beacon#mDataFields
         */
        @NonNull
        public Builder setExtraDataFields(@NonNull List<Long> extraDataFields) {
            mBeacon.mExtraDataFields = extraDataFields;
            return this;
        }

        /**
         * @param manufacturer
         * @return builder
         * @see Beacon#mManufacturer
         */
        @NonNull
        public Builder setManufacturer(int manufacturer) {
            mBeacon.mManufacturer = manufacturer;
            return this;
        }

        /**
         * @param name
         * @return builder
         * @see Beacon#mBluetoothName
         */
        @NonNull
        public Builder setBluetoothName(@NonNull String name) {
            mBeacon.mBluetoothName = name;
            return this;
        }

        /**
         * @param id
         * @return builder
         * @see Beacon#mParserIdentifier
         */
        @NonNull
        public Builder setParserIdentifier(@NonNull String id) {
            mBeacon.mParserIdentifier = id;
            return this;
        }


        /**
         * @param multiFrameBeacon
         * @return builder
         * @see Beacon#mMultiFrameBeacon
         */
        @NonNull
        public Builder setMultiFrameBeacon(boolean multiFrameBeacon) {
            mBeacon.mMultiFrameBeacon = multiFrameBeacon;
            return this;
        }
    }

}
