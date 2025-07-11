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

package com.streamwide.smartms.altbeacon.util;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CollectionUtils {

    /**
     * private constructor to hide the implicit public one.
     */
    private CollectionUtils() {
        // do nothing...
    }

    @Nullable
    public static <E> ArrayList<E> copyArrayList(@Nullable List<E> sourceList) {
        return sourceList != null ? new ArrayList<>(sourceList) : null;
    }


}
