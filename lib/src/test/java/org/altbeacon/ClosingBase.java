/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Tue, 30 Mar 2021 10:28:01 +0100
 * @copyright  Copyright (c) 2021 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2021 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Tue, 30 Mar 2021 10:28:01 +0100
 */
package org.altbeacon;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Test base class that keeps track of Closeable objects
 * and cleans them up.
 */
public class ClosingBase {
    private final List<Closeable> toClose = new ArrayList<>();

    protected <T extends Closeable> T willClose(final T t) {
        toClose.add(t);
        return t;
    }

    @BeforeEach
    public void setup() throws IOException {
        toClose.clear();
    }

    @AfterEach
    public void cleanup() {
        for (final Closeable c : toClose) {
            try {
                c.close();
            } catch (final IOException ignored) {
                // ignore
            }
        }
    }
}