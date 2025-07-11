/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Thu, 16 May 2024 10:22:50 +0100
 * @copyright  Copyright (c) 2024 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2024 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Thu, 16 May 2024 10:08:33 +0100
 */

package com.streamwide.smartms.altbeacon.beacon.distance;


import android.content.Context;

import androidx.annotation.NonNull;

import com.streamwide.smartms.altbeacon.R;
import com.streamwide.smartms.altbeacon.beacon.logging.LogManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Obtains a <code>DistanceCalculator</code> appropriate for a specific Android model.  Each model
 * may have a different Bluetooth chipset, radio and antenna and sees a different signal level
 * at the same distance, therefore requiring a different equation coefficients for each model.
 * <p>
 * This class uses a configuration table to look for a matching Android device model for which
 * coefficients are known.  If an exact match cannot be found, this class will attempt to find the
 * closest match possible based on the assumption that an unknown model made by Samsung, for example
 * might have a different signal response as a known device model also made by Samsung.
 * <p>
 * If no match can be found at all, the device model marked as the default will be used for the
 * calculation.
 * <p>
 * The configuration table is stored in model_distance_calculations.json
 * <p>
 * For information on how to get new Android device models added to this table, please
 * see <a href='http://altbeacon.github.io/android-beacon-library/distance-calculations.html'
 * Optimizing Distance Calculations</a>
 * <p>
 * Created by dyoung on 8/28/14.
 */
public class ModelSpecificDistanceCalculator implements DistanceCalculator {
    Map<AndroidModel, DistanceCalculator> mModelMap;
    private static final String TAG = "ModelSpecificDistanceCalculator";
    private AndroidModel mDefaultModel;
    private DistanceCalculator mDistanceCalculator;
    private AndroidModel mModel;
    private AndroidModel mRequestedModel;
    private Context mContext;
    private final ReentrantLock mLock = new ReentrantLock();

    /**
     * Obtains the best possible <code>DistanceCalculator</code> for the Android device calling
     * the constructor
     */
    public ModelSpecificDistanceCalculator(@NonNull Context context) {
        this(context, AndroidModel.forThisDevice());
    }

    /**
     * Obtains the best possible <code>DistanceCalculator</code> for the Android device passed
     * as an argument
     */
    public ModelSpecificDistanceCalculator(@NonNull Context context, @NonNull AndroidModel model) {
        mRequestedModel = model;
        mContext = context;
        loadModelMap();
        mDistanceCalculator = findCalculatorForModelWithLock(model);
    }

    /**
     * @return the Android device model used for distance calculations
     */
    @NonNull
    public AndroidModel getModel() {
        return mModel;
    }

    /**
     * @return the Android device model requested to be used for distance calculations
     */
    @NonNull
    public AndroidModel getRequestedModel() {
        return mRequestedModel;
    }

    @Override
    public double calculateDistance(int txPower, double rssi) {
        if (mDistanceCalculator == null) {
            LogManager.w(TAG, "distance calculator has not been set");
            return -1.0;
        }
        return mDistanceCalculator.calculateDistance(txPower, rssi);
    }

    DistanceCalculator findCalculatorForModelWithLock(AndroidModel model) {
        mLock.lock();
        try {
            return findCalculatorForModel(model);
        } finally {
            mLock.unlock();
        }
    }

    private DistanceCalculator findCalculatorForModel(AndroidModel model) {
        LogManager.d(TAG, "Finding best distance calculator for %s, %s, %s, %s",
                model.getVersion(), model.getBuildNumber(), model.getModel(),
                model.getManufacturer());

        if (mModelMap == null) {
            LogManager.d(TAG, "Cannot get distance calculator because modelMap was never initialized");
            return null;
        }

        int highestScore = 0;
        AndroidModel bestMatchingModel = null;
        for (AndroidModel candidateModel : mModelMap.keySet()) {
            if (candidateModel.matchScore(model) > highestScore) {
                highestScore = candidateModel.matchScore(model);
                bestMatchingModel = candidateModel;
            }
        }
        if (bestMatchingModel != null) {
            LogManager.d(TAG, "found a match with score %s", highestScore);
            LogManager.d(TAG, "Finding best distance calculator for %s, %s, %s, %s",
                    bestMatchingModel.getVersion(), bestMatchingModel.getBuildNumber(),
                    bestMatchingModel.getModel(), bestMatchingModel.getManufacturer());
            mModel = bestMatchingModel;
        } else {
            mModel = mDefaultModel;
            LogManager.w(TAG, "Cannot find match for this device.  Using default");
        }
        return mModelMap.get(mModel);
    }

    private void loadModelMap() {
        loadDefaultModelMap();
        mDistanceCalculator = findCalculatorForModelWithLock(mRequestedModel);
    }

    private void buildModelMap(String jsonString) throws JSONException {
        HashMap<AndroidModel, DistanceCalculator> map = new HashMap<AndroidModel, DistanceCalculator>();
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray array = jsonObject.getJSONArray("models");
        for (int i = 0; i < array.length(); i++) {
            JSONObject modelObject = array.getJSONObject(i);
            boolean defaultFlag = false;
            if (modelObject.has("default")) {
                defaultFlag = modelObject.getBoolean("default");
            }
            Double coefficient1 = modelObject.getDouble("coefficient1");
            Double coefficient2 = modelObject.getDouble("coefficient2");
            Double coefficient3 = modelObject.getDouble("coefficient3");
            String version = modelObject.getString("version");
            String buildNumber = modelObject.getString("build_number");
            String model = modelObject.getString("model");
            String manufacturer = modelObject.getString("manufacturer");

            CurveFittedDistanceCalculator distanceCalculator =
                    new CurveFittedDistanceCalculator(coefficient1, coefficient2, coefficient3);

            AndroidModel androidModel = new AndroidModel(version, buildNumber, model, manufacturer);

            map.put(androidModel, distanceCalculator);
            if (defaultFlag) {
                mDefaultModel = androidModel;
            }
        }
        mModelMap = map;
    }

    private void loadDefaultModelMap() {
        try {
            buildModelMap(stringFromFilePath());
        } catch (Exception e) {
            mModelMap = new HashMap<AndroidModel, DistanceCalculator>();
            LogManager.e(e, TAG, "Cannot build model distance calculations");
        }
    }

    private String stringFromFilePath() throws IOException {
        InputStream stream = null;
        BufferedReader bufferedReader = null;
        StringBuilder inputStringBuilder = new StringBuilder();
        try {
            stream = mContext.getResources().openRawResource(R.raw.model_distance_calculations);
            if (stream == null) {
                throw new RuntimeException("Cannot load resource at raw directory");
            }
            bufferedReader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            String line = bufferedReader.readLine();
            while (line != null) {
                inputStringBuilder.append(line);
                inputStringBuilder.append('\n');
                line = bufferedReader.readLine();
            }
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (stream != null) {
                stream.close();
            }
        }
        return inputStringBuilder.toString();
    }

}
