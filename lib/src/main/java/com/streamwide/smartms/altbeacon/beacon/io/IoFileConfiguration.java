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

package com.streamwide.smartms.altbeacon.beacon.io;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.lib.template.file.IOFileStrategy;


public class IoFileConfiguration {

    private IOFileStrategy mIoFileStrategy = new DefaultIoFileStrategy();

    IoFileConfiguration() {
        //explicit constructor with package visibility
    }

    private static class SingletonHolder {

        private static final IoFileConfiguration instance = new IoFileConfiguration();

        public static IoFileConfiguration getInstance() {
            return instance;
        }
    }

    public static void resetIoFileConfiguration() {
        synchronized (IoFileConfiguration.class) {
            SingletonHolder.getInstance().mIoFileStrategy = new DefaultIoFileStrategy();
        }
    }

    public static void setIoFileConfiguration(@Nullable IOFileStrategy ioFileStrategy) {
        if (ioFileStrategy == null)
            throw new IllegalArgumentException("io File Strategy MUST not be null!");

        synchronized (IoFileConfiguration.class) {
            SingletonHolder.getInstance().mIoFileStrategy = ioFileStrategy;
        }
    }

    public static @NonNull IOFileStrategy getIoFileStrategy() {
        synchronized (IoFileConfiguration.class) {
            return SingletonHolder.getInstance().mIoFileStrategy;
        }
    }

}
