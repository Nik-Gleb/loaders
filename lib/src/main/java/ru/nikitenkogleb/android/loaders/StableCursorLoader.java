/*
 * 	StableCursorLoader.java
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
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;

import proguard.annotation.Keep;
import proguard.annotation.KeepPublicProtectedClassMembers;

/**
 * Stable cursor loader.
 *
 * @author Nikitenko Gleb
 * @since 1.0, 10/04/2017
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@Keep@KeepPublicProtectedClassMembers
public abstract class StableCursorLoader extends StableLoader<Object> {

    /**
     * Constructs a new {@link StableCursorLoader}.
     *
     * @param context the activity-context
     */
    public StableCursorLoader(@NonNull Context context) {
        super(context);
    }

    /**
     * Constructs a new {@link StableCursorLoader}.
     *
     * @param context  the activity-context
     * @param executor the runtime executor
     */
    public StableCursorLoader(@NonNull Context context, @NonNull Executor executor) {
        super(context, executor);
    }

    /** {@inheritDoc} */
    @Override
    protected final Object onLoadInBackground() {
        final Object object = super.onLoadInBackground();
        if (object != null && object instanceof Cursor) {
            final Cursor result = (Cursor) object;
            result.registerContentObserver(contentObserver);
            return result;
        }
        return object;
    }

    /** {@inheritDoc} */
    @Override
    protected final void release(@NonNull Object data) {
        super.release(data);
        if (data instanceof Cursor) {
            ((Cursor)data).close();
        }
    }
}
