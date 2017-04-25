/*
 * 	StableLoader.java
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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.Executor;

import proguard.annotation.Keep;
import proguard.annotation.KeepPublicProtectedClassMembers;

/**
 * The stable loader/
 *
 * @author Nikitenko Gleb
 * @since 1.0, 10/04/2017
 */
@SuppressWarnings("WeakerAccess")
@Keep@KeepPublicProtectedClassMembers
public abstract class StableLoader<T> extends BaseLoader<T> {

    /** The content observer. */
    protected final ForceLoadContentObserver contentObserver = new ForceLoadContentObserver();

    /**
     * Constructs a new {@link OneShotLoader}.
     *
     * @param context the activity-context
     */
    public StableLoader(@NonNull Context context) {
        super(context, true);
    }

    /**
     * Constructs a new {@link OneShotLoader}.
     *
     * @param context  the activity-context
     * @param executor the runtime executor
     */
    public StableLoader(@NonNull Context context, @NonNull Executor executor) {
        super(context, executor, true);
    }

    /** {@inheritDoc} */
    @Override
    protected void onDelivered(@Nullable T data, boolean isStarted) {
        /*if (!isStarted && data != null) {
            data.clearDiffs();
        }*/
    }

    /** {@inheritDoc} */
    @Override
    protected final boolean needDelivery(@Nullable T data) {
        return data != null;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean needLoad(@Nullable T data) {
        return takeContentChanged() || data == null;
    }

    /** {@inheritDoc} */
    @Override
    protected void release(@NonNull T data) {}
}
