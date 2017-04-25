/*
 * 	BaseLoader.java
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
import android.os.CancellationSignal;
import android.os.OperationCanceledException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.concurrent.Executor;

import proguard.annotation.Keep;
import proguard.annotation.KeepPublicProtectedClassMembers;

/**
 * Base {@link AsyncTaskLoader}.
 *
 * @author Nikitenko Gleb
 * @since 1.0, 08/04/2017
 */
@Keep@KeepPublicProtectedClassMembers
@SuppressWarnings({"WeakerAccess", "unused"})
abstract class BaseLoader<T> extends AsyncTaskLoader<T> {

    /** The log-cat tag. */
    private static final String TAG = "BaseLoader";

    /** The executor field. */
    private static final Field FIELD_EXECUTOR = getExecutorField();

    /** The cancellation signal. */
    @Nullable
    private CancellationSignal mCancellationSignal;

    /** The loaded data. */
    @Nullable private T mData = null;

    /** Skip nulls, flag */
    private boolean mSkipNulls = false;

    /**
     * Constructs a new {@link OneShotLoader}.
     *
     * @param context the activity-context
     */
    public BaseLoader(@NonNull Context context, boolean skipNulls) {
        super(context);
        mSkipNulls = skipNulls;
    }

    /**
     * Constructs a new {@link OneShotLoader}.
     *
     * @param context the activity-context
     * @param executor the runtime executor
     */
    public BaseLoader(@NonNull Context context, @NonNull Executor executor, boolean skipNulls) {
        super(context);
        mSkipNulls = skipNulls;
        setExecutor(executor);
    }


    /** @param executor the runtime executor */
    private void setExecutor(@Nullable Executor executor) {
        try {
            if (FIELD_EXECUTOR != null) {
                FIELD_EXECUTOR.set(this, executor);
            }
        } catch (IllegalAccessException exception) {
            Log.w(TAG,  exception);
        }
    }

    /** {@inheritDoc} */
    @Nullable
    public final T loadInBackground() {
        synchronized (this) {
            if (isLoadInBackgroundCanceled())
                throw new OperationCanceledException();
            mCancellationSignal = new CancellationSignal();
        }

        try {
            return loadInBackground(mCancellationSignal);
        } finally {
            synchronized (this) {
                mCancellationSignal = null;
            }
        }
    }


    /** {@inheritDoc} */
    @Override
    public final void cancelLoadInBackground() {
        super.cancelLoadInBackground();
        synchronized (this) {
            if (mCancellationSignal != null) {
                mCancellationSignal.cancel();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void deliverResult(@Nullable T data) {
        if (isReset()) {
            releaseResources(data);
        }

        final T oldData = mData;
        mData = data;

        final boolean isStarted = isStarted();
        onDelivered(data, isStarted);

        if (isStarted) {
            super.deliverResult(data);
        }

        if (oldData != null && oldData != data) {
            releaseResources(oldData);
        }

    }

    /** {@inheritDoc} */
    @Override
    protected final void onStartLoading() {
        super.onStartLoading();

        if (needDelivery(mData)) {
            super.deliverResult(mData);
        }

        if (needLoad(mData)) {
            forceLoad();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected final void onStopLoading() {
        super.onStopLoading();
        cancelLoad();
    }

    /** {@inheritDoc} */
    @Override
    public final void onCanceled(@Nullable T data) {
        super.onCanceled(data);
        releaseResources(data);
    }

    /** {@inheritDoc} */
    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        releaseResources(mData);
        mData = null;
    }

    /** @param data resources for release */
    private void releaseResources(@Nullable T data) {
        if (data != null ) {
            release(data);
        }
    }

    /** Every delivery callback. */
    protected abstract void onDelivered(@Nullable T data, boolean isStarted);
    /** Check for need delivery. */
    protected abstract boolean needDelivery(@Nullable T data);
    /** Check for need startLoad. */
    protected abstract boolean needLoad(@Nullable T data);
    /** Release data. */
    protected abstract void release(@NonNull T data);

    /** Background loading. */
    @Nullable
    protected abstract T loadInBackground(@NonNull CancellationSignal cancellationSignal);

    /** @return the super.mExecutor field */
    @Nullable private static Field getExecutorField() {
        try {
            final Field result = AsyncTaskLoader.class.getDeclaredField("mExecutor");
            result.setAccessible(true); return result;
        } catch (NoSuchFieldException exception) {
            Log.w(TAG, exception); return null;
        }
    }
}
