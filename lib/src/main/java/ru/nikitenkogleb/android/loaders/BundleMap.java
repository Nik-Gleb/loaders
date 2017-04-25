/*
 * 	BundleMap.java
 * 	app
 *
 *  	The MIT License (MIT)
 *
 *  	Copyright (c) 2017, Gleb Nikitenko
 *
 * 	Permission is hereby granted, free of charge, to any person obtaining a copy
 *  	of this software and associated documentation files (the "Software"), to deal
 *  	in the Software without restriction, including without limitation the rights
 *  	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  	copies of the Software, and to permit persons to whom the Software is
 *  	furnished to do so, subject to the following conditions:
 *
 * 	The above copyright notice and this permission notice shall be included in all
 * 	copies or substantial portions of the Software.
 *
 *  	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  	SOFTWARE.
 *
 */

package ru.nikitenkogleb.android.loaders;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import proguard.annotation.Keep;
import proguard.annotation.KeepPublicProtectedClassMembers;


/**
 * The parcelable map of bundles.
 *
 * @author Nikitenko Gleb
 * @since 1.0, 23/06/2016
 */
@Keep@KeepPublicProtectedClassMembers
@SuppressWarnings("WeakerAccess, unused")
public class BundleMap extends SparseArray<Bundle> implements Parcelable {

    /** The Parcelable Creator. */
    @SuppressWarnings("unused")
    public static final Creator CREATOR = new Creator();

    /** Constructs a new empty BundleMap */
    public BundleMap() {super(0);}

    /**
     * Constructs a new BundleMap with Parcel in.
     * @param source parcel in
     */
    public BundleMap(Parcel source) {
        final int size = source.readInt();
        for (int i = 0; i < size; i++)
            put(source.readInt(), source.readBundle(getClass().getClassLoader()));
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable's
     * marshalled representation.
     *
     * @return a bitmask indicating the set of special object types marshalled
     * by the Parcelable.
     */
    @Override
    public final int describeContents() {return 0;}

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public final void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(size());
        for (int i = 0; i < size(); i++) {
            dest.writeInt(keyAt(i));
            dest.writeBundle(valueAt(i));
        }
    }

    /**
     * Parcel Creator.
     *
     * @author Gleb Nikitenko
     * @since 1.0, 10/06/16
     */

    @SuppressWarnings("WeakerAccess, unused")
    @Keep@KeepPublicProtectedClassMembers
    public static final class Creator implements Parcelable.Creator<BundleMap> {
        /** {@inheritDoc} */
        @Override
        public final BundleMap createFromParcel(Parcel source) {return new BundleMap(source);}
        /** {@inheritDoc} */
        @Override
        public final BundleMap[] newArray(int size) {return new BundleMap[size];}
    }

}
