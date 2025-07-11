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

package com.streamwide.smartms.altbeacon.beacon.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.altbeacon.beacon.Beacon;
import com.streamwide.smartms.altbeacon.beacon.MonitorNotifier;
import com.streamwide.smartms.altbeacon.beacon.Region;
import com.streamwide.smartms.altbeacon.beacon.io.IoFileConfiguration;
import com.streamwide.smartms.altbeacon.beacon.logging.LogManager;
import com.streamwide.smartms.lib.template.serialization.ValidatorClassNameMatcher;

import java.io.File;
import java.io.IOException;
import java.io.InvalidClassException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MonitoringStatus {

    private static MonitoringStatus sInstance;

    private static final int MAX_REGIONS_FOR_STATUS_PRESERVATION = 50;
    private static final int MAX_STATUS_PRESERVATION_FILE_AGE_TO_RESTORE_SECS = 60 * 15;
    private static final String TAG = MonitoringStatus.class.getSimpleName();
    public static final String STATUS_PRESERVATION_FILE_NAME =
            "org.altbeacon.beacon.service.monitoring_status_state";

    private Map<Region, RegionMonitoringState> mRegionsStatesMap;

    private boolean mStatePreservationIsOn = true;

    @NonNull
    public static MonitoringStatus getInstance() {

        synchronized (MonitoringStatus.class) {
            if (sInstance == null) {
                sInstance = new MonitoringStatus();
            }
        }

        return sInstance;
    }

    private MonitoringStatus() {

    }

    public synchronized void addRegion(@NonNull Context context, @NonNull Region region, @NonNull Callback callback) {
        addLocalRegion(context, region, callback);
        saveMonitoringStatusIfOn(context);
    }

    public synchronized void removeRegion(@NonNull Context context, @NonNull Region region) {
        removeLocalRegion(context, region);
        saveMonitoringStatusIfOn(context);
    }

    @NonNull
    public synchronized Set<Region> regions(@NonNull Context context) {
        return getRegionsStateMap(context).keySet();
    }

    public synchronized int regionsCount(@NonNull Context context) {
        return regions(context).size();
    }

    @Nullable
    public synchronized RegionMonitoringState stateOf(@NonNull Context context, @NonNull Region region) {
        return getRegionsStateMap(context).get(region);
    }

    public synchronized void updateNewlyOutside(@NonNull Context context) {
        Iterator<Region> monitoredRegionIterator = regions(context).iterator();
        boolean needsMonitoringStateSaving = false;
        while (monitoredRegionIterator.hasNext()) {
            Region region = monitoredRegionIterator.next();
            RegionMonitoringState state = stateOf(context, region);
            if (state.markOutsideIfExpired()) {
                needsMonitoringStateSaving = true;
                LogManager.d(TAG, "found a monitor that expired: %s", region);
                state.getCallback().call(context, "monitoringData", new MonitoringData(state.getInside(), region).toBundle());
            }
        }
        if (needsMonitoringStateSaving) {
            saveMonitoringStatusIfOn(context);
        } else {
            updateMonitoringStatusTime(context, System.currentTimeMillis());
        }
    }

    public synchronized void updateNewlyInsideInRegionsContaining(@NonNull Context context, @NonNull Beacon beacon) {
        List<Region> matchingRegions = regionsMatchingTo(context, beacon);
        boolean needsMonitoringStateSaving = false;
        for (Region region : matchingRegions) {
            RegionMonitoringState state = getRegionsStateMap(context).get(region);
            if (state != null && state.markInside()) {
                needsMonitoringStateSaving = true;
                state.getCallback().call(context, "monitoringData",
                        new MonitoringData(state.getInside(), region).toBundle());
            }
        }
        if (needsMonitoringStateSaving) {
            saveMonitoringStatusIfOn(context);
        } else {
            updateMonitoringStatusTime(context, System.currentTimeMillis());
        }
    }

    @NonNull
    private Map<Region, RegionMonitoringState> getRegionsStateMap(Context context) {
        if (mRegionsStatesMap == null) {
            restoreOrInitializeMonitoringStatus(context);
        }
        return mRegionsStatesMap;
    }

    private void restoreOrInitializeMonitoringStatus(Context context) {
        long millisSinceLastMonitor = System.currentTimeMillis() - getLastMonitoringStatusUpdateTime(context);
        mRegionsStatesMap = new ConcurrentHashMap<Region, RegionMonitoringState>();
        if (!mStatePreservationIsOn) {
            LogManager.d(TAG, "Not restoring monitoring state because persistence is disabled");
        } else if (millisSinceLastMonitor > MAX_STATUS_PRESERVATION_FILE_AGE_TO_RESTORE_SECS * 1000) {
            LogManager.d(TAG, "Not restoring monitoring state because it was recorded too many milliseconds ago: " + millisSinceLastMonitor);
        } else {
            restoreMonitoringStatus(context);
            LogManager.d(TAG, "Done restoring monitoring status");
        }
    }

    private List<Region> regionsMatchingTo(Context context, Beacon beacon) {
        List<Region> matched = new ArrayList<Region>();
        for (Region region : regions(context)) {
            if (region.matchesBeacon(beacon)) {
                matched.add(region);
            } else {
                LogManager.d(TAG, "This region (%s) does not match beacon: %s", region, beacon);
            }
        }
        return matched;
    }

    protected void saveMonitoringStatusIfOn(@NonNull Context context) {
        if (!mStatePreservationIsOn) return;
        LogManager.d(TAG, "saveMonitoringStatusIfOn()");
        if (getRegionsStateMap(context).size() > MAX_REGIONS_FOR_STATUS_PRESERVATION) {
            LogManager.w(TAG, "Too many regions being monitored.  Will not persist region state");
            String tempFile = IoFileConfiguration.getIoFileStrategy().getRootDirectoryPath(context) + File.separator + STATUS_PRESERVATION_FILE_NAME;
            File file = new File(tempFile);
            if (!file.delete()) {
                LogManager.e(TAG, "Cannot delete existing file.");
            }
        } else {
            try {
                Map<Region, RegionMonitoringState> map = getRegionsStateMap(context);
                // Must convert ConcurrentHashMap to HashMap becasue attempting to serialize
                // ConcurrentHashMap throws a java.io.NotSerializableException
                HashMap<Region, RegionMonitoringState> serializableMap = new HashMap<Region, RegionMonitoringState>();
                for (Region region : map.keySet()) {
                    serializableMap.put(region, map.get(region));
                }
                String tempFile = IoFileConfiguration.getIoFileStrategy().getRootDirectoryPath(context) + File.separator + STATUS_PRESERVATION_FILE_NAME;
                IoFileConfiguration.getIoFileStrategy().writeObject(context, serializableMap, tempFile);

            } catch (IOException e) {
                LogManager.e(TAG, "Error while saving monitored region states to file ", e);
            } catch (GeneralSecurityException e) {
                LogManager.e(TAG, "GeneralSecurityException", e);
            }
        }
    }

    protected void updateMonitoringStatusTime(@NonNull Context context, long time) {
        String tempFile = IoFileConfiguration.getIoFileStrategy().getRootDirectoryPath(context) + File.separator + STATUS_PRESERVATION_FILE_NAME;
        File file = new File(tempFile);
        if (!file.setLastModified(time)) {
            LogManager.w(TAG, "Unable to modify the last-modified time of the file or directory");
        }
    }

    protected long getLastMonitoringStatusUpdateTime(@NonNull Context context) {
        String tempFile = IoFileConfiguration.getIoFileStrategy().getRootDirectoryPath(context) + File.separator + STATUS_PRESERVATION_FILE_NAME;
        File file = new File(tempFile);
        return file.lastModified();
    }

    protected void restoreMonitoringStatus(@NonNull Context context) {
        try {
            String tempFile = IoFileConfiguration.getIoFileStrategy().getRootDirectoryPath(context) + File.separator + STATUS_PRESERVATION_FILE_NAME;

            ValidatorClassNameMatcher validatorClassNameMatcher = new ValidatorClassNameMatcher
                    .Builder()
                    .accept("*")
                    .build();
            Map<Region, RegionMonitoringState> obj = (Map<Region, RegionMonitoringState>) IoFileConfiguration.getIoFileStrategy().readObject(context, tempFile, validatorClassNameMatcher);

            LogManager.d(TAG, "Restored region monitoring state for " + obj.size() + " regions.");
            for (Region region : obj.keySet()) {
                LogManager.d(TAG, "Region  " + region + " uniqueId: " + region.getUniqueId() + " state: " + obj.get(region));
            }

            // RegionMonitoringState objects only get serialized to the status preservation file when they are first inside,
            // therefore, their {@link RegionMonitoringState#lastSeenTime will be when they were first "inside".
            // Mark all beacons that were inside again so they don't trigger as a new exit - enter.
            for (RegionMonitoringState regionMonitoringState : obj.values()) {
                if (regionMonitoringState.getInside()) {
                    regionMonitoringState.markInside();
                }
            }

            mRegionsStatesMap.putAll(obj);

        } catch (IOException | ClassCastException e) {
            if (e instanceof InvalidClassException) {
                LogManager.d(TAG, "Serialized Monitoring State has wrong class. Just ignoring saved state...");
            } else LogManager.e(TAG, "Deserialization exception, message: %s", e.getMessage());
        }
    }

    /**
     * Client applications should not call directly.  Call BeaconManager#setRegionStatePeristenceEnabled
     */
    public synchronized void stopStatusPreservation(@NonNull Context context) {
        String tempFile = IoFileConfiguration.getIoFileStrategy().getRootDirectoryPath(context) + File.separator + STATUS_PRESERVATION_FILE_NAME;
        File file = new File(tempFile);
        if (!file.delete()) {
            LogManager.e(TAG, "Cannot delete existing file.");
        }

        this.mStatePreservationIsOn = false;
    }

    /**
     * Client applications should not call directly.  Call BeaconManager#setRegionStatePeristenceEnabled
     */
    public synchronized void startStatusPreservation(@NonNull Context context) {
        if (!this.mStatePreservationIsOn) {
            this.mStatePreservationIsOn = true;
            saveMonitoringStatusIfOn(context);
        }
    }

    public boolean isStatePreservationOn() {
        return mStatePreservationIsOn;
    }

    public synchronized void clear(@NonNull Context context) {
        String tempFile = IoFileConfiguration.getIoFileStrategy().getRootDirectoryPath(context) + File.separator + STATUS_PRESERVATION_FILE_NAME;
        File file = new File(tempFile);
        if (!file.delete()) {
            LogManager.e(TAG, "Cannot delete existing file.");
        }
        getRegionsStateMap(context).clear();
    }

    public void updateLocalState(@NonNull Context context, @NonNull Region region, @Nullable Integer state) {
        RegionMonitoringState internalState = getRegionsStateMap(context).get(region);
        if (internalState == null) {
            internalState = addLocalRegion(context, region);
        }
        if (state != null) {
            if (state == MonitorNotifier.OUTSIDE) {
                internalState.markOutside();

            }
            if (state == MonitorNotifier.INSIDE) {
                internalState.markInside();
            }
        }
    }

    public void removeLocalRegion(@NonNull Context context, @NonNull Region region) {
        getRegionsStateMap(context).remove(region);
    }

    @NonNull
    public RegionMonitoringState addLocalRegion(@NonNull Context context, @NonNull Region region) {
        Callback dummyCallback = new Callback(null);
        return addLocalRegion(context, region, dummyCallback);
    }

    private RegionMonitoringState addLocalRegion(Context context, Region region, Callback callback) {
        if (getRegionsStateMap(context).containsKey(region)) {
            // if the region definition hasn't changed, becasue if it has, we need to clear state
            // otherwise a region with the same uniqueId can never be changed
            for (Region existingRegion : getRegionsStateMap(context).keySet()) {
                if (existingRegion.equals(region)) {
                    if (existingRegion.hasSameIdentifiers(region)) {
                        return getRegionsStateMap(context).get(existingRegion);
                    } else {
                        LogManager.d(TAG, "Replacing region with unique identifier " + region.getUniqueId());
                        LogManager.d(TAG, "Old definition: " + existingRegion);
                        LogManager.d(TAG, "New definition: " + region);
                        LogManager.d(TAG, "clearing state");
                        getRegionsStateMap(context).remove(region);
                        break;
                    }
                }
            }
        }
        RegionMonitoringState monitoringState = new RegionMonitoringState(callback);
        getRegionsStateMap(context).put(region, monitoringState);
        return monitoringState;
    }
}
