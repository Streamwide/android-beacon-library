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

import android.app.ActivityManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * Created by dyoung on 3/10/17.
 * <p>
 * Internal class used to determine current process state in multi-process setups
 *
 * @hide
 */

public class ProcessUtils {
    Context mContext;

    public ProcessUtils(@NonNull Context context) {
        mContext = context;
    }

    @Nullable
    public String getProcessName() {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = manager.getRunningAppProcesses();
        if (processes != null) {
            for (ActivityManager.RunningAppProcessInfo processInfo : processes) {
                if (processInfo.pid == getPid()) {
                    return processInfo.processName;
                }
            }
        }
        return null;
    }

    @NonNull
    public String getPackageName() {
        return mContext.getApplicationContext().getPackageName();
    }

    public int getPid() {
        return android.os.Process.myPid();
    }

    public boolean isMainProcess() {
        return (getPackageName().equals(getProcessName()));
    }
}
