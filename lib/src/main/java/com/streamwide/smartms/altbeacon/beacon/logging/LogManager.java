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

import androidx.annotation.Nullable;

/**
 * Manager for logging in the Altbeacon library. The default is a
 * {@link Loggers#warningLogger()} ()}.
 *
 * @author Andrew Reitz
 * @since 2.2
 */
public final class LogManager {
    private static Logger sLogger = Loggers.infoLogger();
    private static boolean sVerboseLoggingEnabled = false;

    /**
     * Set the logger that the Altbeacon library will use to send it's log messages to.
     *
     * @param logger The logger implementation that logs will be sent to for logging.
     * @throws NullPointerException if logger is null.
     * @see Logger
     * @see Loggers
     */
    public static void setLogger(@Nullable Logger logger) {
        if (logger == null) {
            throw new NullPointerException("Logger may not be null.");
        }

        sLogger = logger;
    }

    /**
     * Gets the currently set logger
     *
     * @return logger
     * @see Logger
     */
    @Nullable
    public static Logger getLogger() {
        return sLogger;
    }

    /**
     * Indicates whether verbose logging is enabled.   If not, expensive calculations to create
     * log strings should be avoided.
     *
     * @return
     */
    @Nullable
    public static boolean isVerboseLoggingEnabled() {
        return sVerboseLoggingEnabled;
    }

    /**
     * Sets whether verbose logging is enabled.  If not, expensive calculations to create
     * log strings should be avoided.
     *
     * @param enabled
     */
    public static void setVerboseLoggingEnabled(boolean enabled) {
        sVerboseLoggingEnabled = enabled;
    }

    /**
     * Send a verbose log message to the logger.
     *
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param message The message you would like logged. This message may contain string formatting
     *                which will be replaced with values from args.
     * @param args    Arguments for string formatting.
     */
    public static void v(@Nullable String tag, @Nullable String message, @Nullable Object... args) {
        sLogger.v(tag, message, args);
    }

    /**
     * Send a verbose log message to the logger.
     *
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param message The message you would like logged. This message may contain string formatting
     *                which will be replaced with values from args.
     * @param t       An exception to log.
     * @param args    Arguments for string formatting.
     */
    public static void v(@Nullable Throwable t, @Nullable String tag, @Nullable String message, @Nullable Object... args) {
        sLogger.v(t, tag, message, args);
    }

    /**
     * Send a debug log message to the logger.
     *
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param message The message you would like logged. This message may contain string formatting
     *                which will be replaced with values from args.
     * @param args    Arguments for string formatting.
     */
    public static void d(@Nullable String tag, @Nullable String message, @Nullable Object... args) {
        sLogger.d(tag, message, args);
    }

    /**
     * Send a debug log message to the logger.
     *
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param message The message you would like logged. This message may contain string formatting
     *                which will be replaced with values from args.
     * @param t       An exception to log.
     * @param args    Arguments for string formatting.
     */
    public static void d(@Nullable Throwable t, @Nullable String tag, @Nullable String message, @Nullable Object... args) {
        sLogger.d(t, tag, message, args);
    }

    /**
     * Send a info log message to the logger.
     *
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param message The message you would like logged. This message may contain string formatting
     *                which will be replaced with values from args.
     * @param args    Arguments for string formatting.
     */
    public static void i(@Nullable String tag, @Nullable String message, @Nullable Object... args) {
        sLogger.i(tag, message, args);
    }

    /**
     * Send a info log message to the logger.
     *
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param message The message you would like logged. This message may contain string formatting
     *                which will be replaced with values from args.
     * @param t       An exception to log.
     * @param args    Arguments for string formatting.
     */
    public static void i(@Nullable Throwable t, @Nullable String tag, @Nullable String message, @Nullable Object... args) {
        sLogger.i(t, tag, message, args);
    }

    /**
     * Send a warning log message to the logger.
     *
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param message The message you would like logged. This message may contain string formatting
     *                which will be replaced with values from args.
     * @param args    Arguments for string formatting.
     */
    public static void w(@Nullable String tag, @Nullable String message, @Nullable Object... args) {
        sLogger.w(tag, message, args);
    }

    /**
     * Send a warning log message to the logger.
     *
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param message The message you would like logged. This message may contain string formatting
     *                which will be replaced with values from args.
     * @param t       An exception to log.
     * @param args    Arguments for string formatting.
     */
    public static void w(@Nullable Throwable t, @Nullable String tag, @Nullable String message, @Nullable Object... args) {
        sLogger.w(t, tag, message, args);
    }

    /**
     * Send a error log message to the logger.
     *
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param message The message you would like logged. This message may contain string formatting
     *                which will be replaced with values from args.
     * @param args    Arguments for string formatting.
     */
    public static void e(@Nullable String tag, @Nullable String message, @Nullable Object... args) {
        sLogger.e(tag, message, args);
    }

    /**
     * Send a error log message to the logger.
     *
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param message The message you would like logged. This message may contain string formatting
     *                which will be replaced with values from args.
     * @param t       An exception to log.
     * @param args    Arguments for string formatting.
     */
    public static void e(@Nullable Throwable t, @Nullable String tag, @Nullable String message, @Nullable Object... args) {
        sLogger.e(t, tag, message, args);
    }

    private LogManager() {
        // no instances
    }
}
