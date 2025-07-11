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

import android.util.SparseArray;

import androidx.annotation.NonNull;

import java.util.ListIterator;
import java.util.NoSuchElementException;

public final class SparseArrayIterator<E> implements ListIterator<E> {

    private final SparseArray<E> array;
    private int cursor;
    private boolean cursorNowhere;

    /**
     * @param array to iterate over.
     * @return A ListIterator on the elements of the SparseArray. The elements
     * are iterated in the same order as they occur in the SparseArray.
     * {@link #nextIndex()} and {@link #previousIndex()} return a
     * SparseArray key, not an index! To get the index, call
     * {@link SparseArray#indexOfKey(int)}.
     */
    @NonNull
    public static <E> ListIterator<E> iterate(@NonNull SparseArray<E> array) {
        return iterateAt(array, -1);
    }

    /**
     * @param array to iterate over.
     * @param key   to start the iteration at. {@link SparseArray#indexOfKey(int)}
     *              < 0 results in the same call as {@link #iterate(SparseArray)}.
     * @return A ListIterator on the elements of the SparseArray. The elements
     * are iterated in the same order as they occur in the SparseArray.
     * {@link #nextIndex()} and {@link #previousIndex()} return a
     * SparseArray key, not an index! To get the index, call
     * {@link SparseArray#indexOfKey(int)}.
     */
    @NonNull
    public static <E> ListIterator<E> iterateAtKey(@NonNull SparseArray<E> array, int key) {
        return iterateAt(array, array.indexOfKey(key));
    }

    /**
     * @param array    to iterate over.
     * @param location to start the iteration at. Value < 0 results in the same call
     *                 as {@link #iterate(SparseArray)}. Value >
     *                 {@link SparseArray#size()} set to that size.
     * @return A ListIterator on the elements of the SparseArray. The elements
     * are iterated in the same order as they occur in the SparseArray.
     * {@link #nextIndex()} and {@link #previousIndex()} return a
     * SparseArray key, not an index! To get the index, call
     * {@link SparseArray#indexOfKey(int)}.
     */
    @NonNull
    public static <E> ListIterator<E> iterateAt(@NonNull SparseArray<E> array, int location) {
        return new SparseArrayIterator<E>(array, location);
    }

    private SparseArrayIterator(SparseArray<E> array, int location) {
        this.array = array;
        if (location < 0) {
            cursor = -1;
            cursorNowhere = true;
        } else if (location < array.size()) {
            cursor = location;
            cursorNowhere = false;
        } else {
            cursor = array.size() - 1;
            cursorNowhere = true;
        }
    }

    @Override
    public boolean hasNext() {
        return cursor < array.size() - 1;
    }

    @Override
    public boolean hasPrevious() {
        return cursorNowhere && cursor >= 0 || cursor > 0;
    }

    @Override
    public int nextIndex() {
        if (hasNext()) {
            return array.keyAt(cursor + 1);
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public int previousIndex() {
        if (hasPrevious()) {
            if (cursorNowhere) {
                return array.keyAt(cursor);
            } else {
                return array.keyAt(cursor - 1);
            }
        } else {
            throw new NoSuchElementException();
        }
    }

    @NonNull
    @Override
    public E next() {
        if (hasNext()) {
            if (cursorNowhere) {
                cursorNowhere = false;
            }
            cursor++;
            return array.valueAt(cursor);
        } else {
            throw new NoSuchElementException();
        }
    }

    @NonNull
    @Override
    public E previous() {
        if (hasPrevious()) {
            if (cursorNowhere) {
                cursorNowhere = false;
            } else {
                cursor--;
            }
            return array.valueAt(cursor);
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public void add(@NonNull E object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove() {
        if (!cursorNowhere) {
            array.remove(array.keyAt(cursor));
            cursorNowhere = true;
            cursor--;
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public void set(@NonNull E object) {
        if (!cursorNowhere) {
            array.setValueAt(cursor, object);
        } else {
            throw new IllegalStateException();
        }
    }
}