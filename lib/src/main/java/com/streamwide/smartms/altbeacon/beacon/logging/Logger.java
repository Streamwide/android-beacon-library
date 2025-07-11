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
 * Logging interface for logging messages in the android-beacon-library. To set a custom logger
 * implement this interface and set it with {@link LogManager#setLogger(Logger)}.
 *
 * @author Andrew Reitz
 * @see LogManager
 * @since 2.2
 */
public interface Logger {
    /**
     * Send a verbose log message.
     *
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param message The message you would like logged. This message may contain string formatting
     *                which will be replaced with values from args.
     * @param args    Arguments for string formatting.
     * @see android.util.Log#v(String, String)
     * @see java.util.Formatter
     * @see String#format(String, Object...)
     */
    void v(@Nullable String tag, @Nullable String message, @Nullable Object... args);

    /**
     * Send a verbose log message.
     *
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param message The message you would like logged. This message may contain string formatting
     *                which will be replaced with values from args.
     * @param t       An exception to log.
     * @param args    Arguments for string formatting.
     * @see android.util.Log#v(String, String, Throwable)
     * @see java.util.Formatter
     * @see String#format(String, Object...)
     */
    void v(@Nullable Throwable t, @Nullable String tag, @Nullable String message, @Nullable Object... args);

    /**
     * Send a debug log message.
     *
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param message The message you would like logged. This message may contain string formatting
     *                which will be replaced with values from args.
     * @param args    Arguments for string formatting.
     * @see android.util.Log#d(String, String)
     * @see java.util.Formatter
     * @see String#format(String, Object...)
     */
    void d(@Nullable String tag, @Nullable String message, @Nullable Object... args);

    /**
     * Send a debug log message.
     *
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param message The message you would like logged. This message may contain string formatting
     *                which will be replaced with values from args.
     * @param t       An exception to log.
     * @param args    Arguments for string formatting.
     * @see android.util.Log#d(String, String, Throwable)
     * @see java.util.Formatter
     * @see String#format(String, Object...)
     */
    void d(@Nullable Throwable t, @Nullable String tag, @Nullable String message, @Nullable Object... args);

    /**
     * Send a info log message.
     *
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param message The message you would like logged. This message may contain string formatting
     *                which will be replaced with values from args.
     * @param args    Arguments for string formatting.
     * @see android.util.Log#i(String, String)
     * @see java.util.Formatter
     * @see String#format(String, Object...)
     */
    void i(@Nullable String tag, @Nullable String message, @Nullable Object... args);

    /**
     * Send a info log message.
     *
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param message The message you would like logged. This message may contain string formatting
     *                which will be replaced with values from args.
     * @param t       An exception to log.
     * @param args    Arguments for string formatting.
     * @see android.util.Log#i(String, String, Throwable)
     * @see java.util.Formatter
     * @see String#format(String, Object...)
     */
    void i(@Nullable Throwable t, @Nullable String tag, @Nullable String message, @Nullable Object... args);

    /**
     * Send a warning log message.
     *
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param message The message you would like logged. This message may contain string formatting
     *                which will be replaced with values from args.
     * @param args    Arguments for string formatting.
     * @see android.util.Log#w(String, String)
     * @see java.util.Formatter
     * @see String#format(String, Object...)
     */
    void w(@Nullable String tag, @Nullable String message, @Nullable Object... args);

    /**
     * Send a warning log message.
     *
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param message The message you would like logged. This message may contain string formatting
     *                which will be replaced with values from args.
     * @param t       An exception to log.
     * @param args    Arguments for string formatting.
     * @see android.util.Log#w(String, String, Throwable)
     * @see java.util.Formatter
     * @see String#format(String, Object...)
     */
    void w(@Nullable Throwable t, @Nullable String tag, @Nullable String message, @Nullable Object... args);

    /**
     * Send a error log message.
     *
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param message The message you would like logged. This message may contain string formatting
     *                which will be replaced with values from args.
     * @param args    Arguments for string formatting.
     * @see android.util.Log#e(String, String)
     * @see java.util.Formatter
     * @see String#format(String, Object...)
     */
    void e(@Nullable String tag, @Nullable String message, @Nullable Object... args);

    /**
     * Send a error log message.
     *
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param message The message you would like logged. This message may contain string formatting
     *                which will be replaced with values from args.
     * @param t       An exception to log.
     * @param args    Arguments for string formatting.
     * @see android.util.Log#e(String, String, Throwable)
     * @see java.util.Formatter
     * @see String#format(String, Object...)
     */
    void e(@Nullable Throwable t, @Nullable String tag, @Nullable String message, @Nullable Object... args);
}
