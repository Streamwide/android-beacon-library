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

import android.util.Log;

import androidx.annotation.Nullable;

import com.streamwide.smartms.altbeacon.beacon.logger.Logger;

/**
 * Android logger that only logs out warning and above to the {@link Log}.
 *
 * @author Andrew Reitz
 * @since 2.2
 */
final class InfoAndroidLogger extends AbstractAndroidLogger {
    @Override
    public void v(@Nullable String tag, @Nullable String message, @Nullable Object... args) {
    }

    @Override
    public void v(@Nullable Throwable t, @Nullable String tag, @Nullable String message, @Nullable Object... args) {
    }

    @Override
    public void d(@Nullable String tag, @Nullable String message, @Nullable Object... args) {
    }

    @Override
    public void d(@Nullable Throwable t, @Nullable String tag, @Nullable String message, @Nullable Object... args) {
    }

    @Override
    public void i(@Nullable String tag, @Nullable String message, @Nullable Object... args) {
        Logger.info(tag, formatString(message, args));
    }

    @Override
    public void i(@Nullable Throwable t, @Nullable String tag, @Nullable String message, @Nullable Object... args) {
        Logger.error(tag, formatString(message, args), t);
    }

    @Override
    public void w(@Nullable String tag, @Nullable String message, @Nullable Object... args) {
        Log.w(tag, formatString(message, args));
    }

    @Override
    public void w(@Nullable Throwable t, @Nullable String tag, @Nullable String message, @Nullable Object... args) {
        Log.w(tag, formatString(message, args), t);
    }

    @Override
    public void e(@Nullable String tag, @Nullable String message, @Nullable Object... args) {
        Log.e(tag, formatString(message, args));
    }

    @Override
    public void e(@Nullable Throwable t, @Nullable String tag, @Nullable String message, @Nullable Object... args) {
        Log.e(tag, formatString(message, args), t);
    }
}
