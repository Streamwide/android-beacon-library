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
package com.streamwide.smartms.altbeacon.beacon.logging;

import androidx.annotation.NonNull;

/**
 * Static factory methods for getting different {@link Logger}
 * implementations.
 *
 * @author Andrew Reitz
 * @since 2.2
 */
public final class Loggers {
    /**
     * Empty Logger Singleton.
     */
    private static final Logger EMPTY_LOGGER = new EmptyLogger();

    /**
     * Debug Logger Singleton.
     */
    private static final Logger VERBOSE_ANDROID_LOGGER = new VerboseAndroidLogger();

    /**
     * Info Logger Singleton.
     */
    private static final Logger INFO_ANDROID_LOGGER = new InfoAndroidLogger();

    /**
     * Warning Logger Singleton.
     */
    private static final Logger WARNING_ANDROID_LOGGER = new WarningAndroidLogger();

    /**
     * @return Get a logger that does nothing.
     */
    public static @NonNull
    Logger empty() {
        return EMPTY_LOGGER;
    }

    /**
     * @return Get a logger that logs all messages to default Android logs.
     * @see android.util.Log
     */
    @NonNull
    public static Logger verboseLogger() {
        return VERBOSE_ANDROID_LOGGER;
    }

    /**
     * @return Get a logger that logs messages of info and greater.
     * @see android.util.Log
     */
    @NonNull
    public static Logger infoLogger() {
        return INFO_ANDROID_LOGGER;
    }

    /**
     * @return Get a logger that logs messages of warning and greater.
     * @see android.util.Log
     */
    @NonNull
    public static Logger warningLogger() {
        return WARNING_ANDROID_LOGGER;
    }

    private Loggers() {
        // No instances
    }
}
