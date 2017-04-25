/*
 * 	LoaderManager.java
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import proguard.annotation.Keep;
import proguard.annotation.KeepPublicProtectedClassMembers;

/**
 * The loader manager.
 *
 * @author Nikitenko Gleb
 * @since 1.0, 30/03/2017
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@Keep@KeepPublicProtectedClassMembers
public class LoaderManager {

    /** "Loader's" - state key. */
    private static final String STATE_LOADERS = "loaders";
    /** "Stable Loader's" - state key. */
    private static final String STATE_STABLES = "stables";

    /** The loader callbacks. */
    private final android.support.v4.app.LoaderManager.LoaderCallbacks<Object> mCallbacks =
            new android.support.v4.app.LoaderManager.LoaderCallbacks<Object>() {

                /** {@inheritDoc} */
                @Override @Nullable
                public final Loader<Object> onCreateLoader(int id, @NonNull Bundle args) {
                    checkStarted("onCreateLoader");
                    return LoaderManager.this.onCreateLoader(id, args);
                }

                /** {@inheritDoc} */
                @Override
                public final void onLoadFinished(@NonNull Loader<Object> loader,
                        @Nullable Object data) {
                        checkStarted("onLoadFinished");
                        final int loaderId = loader.getId();

                        if (!mStableIds.contains(loaderId)) {
                            mLoaderManager.destroyLoader(loaderId);
                            mLoaders.remove(loaderId);
                        }

                        LoaderManager.this.onLoadFinished(loaderId, data);
                }

                /** {@inheritDoc} */
                @Override
                public final void onLoaderReset(@NonNull Loader<Object> loader) {
                    LoaderManager.this.onLoadFinished(loader.getId(), null);
                }
            };


    /** Pending loaders. */
    private final HashSet<Integer> mPendingLoaders = new HashSet<>();

    /** The loader manager. */
    private final android.support.v4.app.LoaderManager mLoaderManager;
    /** Current loaders. */
    private final BundleMap mLoaders;
    /** Stable loader ids. */
    private final ArrayList<Integer> mStableIds;

    /** Is loader-manager was stopped. */
    private boolean mStopped = true;
    /** Is loader-map is saved. */
    private boolean mSaved = false;

    /** Is loader-manager was closed. */
    private boolean mClosed;

    /**
     * Constructs a new {@link LoaderManager} with saved state.
     *
     * @param loaderManager the frameworks loader manager
     * @param state the saved state
     */
    public LoaderManager(@NonNull android.support.v4.app.LoaderManager loaderManager,
            @Nullable Bundle state) {
        mLoaderManager = loaderManager;
        if (state != null) {

            final ArrayList<Integer> stableIds = state.getIntegerArrayList(STATE_STABLES);
            mStableIds = stableIds != null ? stableIds : new ArrayList<>();

            final BundleMap loaders = state.getParcelable(STATE_LOADERS);
            if (loaders != null) {
                mLoaders = loaders;
                // Retain loader's callback
                final int count = mLoaders.size();
                for (int i = 0; i < count; i++) {
                    final int loaderId = mLoaders.keyAt(i);
                    if (mLoaderManager.getLoader(loaderId) != null) {
                        if (mLoaderManager.initLoader(loaderId, null, mCallbacks) == null) {
                            mLoaders.removeAt(i);
                            mStableIds.remove(loaderId);
                        }
                    } else {
                        mPendingLoaders.add(loaderId);
                    }
                }
            } else {
                mLoaders = new BundleMap();
            }
        } else {
            mLoaders = new BundleMap();
            mStableIds = new ArrayList<>();
        }
    }

    /**
     * Backup current state.
     *
     * @param state the state container
     */
    public final void backup(@NonNull Bundle state) {
        checkStarted("backup");
        state.putParcelable(STATE_LOADERS, mLoaders);
        state.putIntegerArrayList(STATE_STABLES, mStableIds);
        mSaved = true;
    }

    /** Switch to "STARTED" mode. */
    public final void start() {
        checkStopped("start");
        mStopped = false;
        mSaved = false;

        if (!mPendingLoaders.isEmpty())
            for (final Iterator<Integer> iterator = mPendingLoaders.iterator(); iterator.hasNext();) {
                final Integer id = iterator.next();
                if (mLoaderManager.initLoader(id, mLoaders.get(id), mCallbacks) == null) {
                    mLoaders.remove(id);
                    mStableIds.remove(id);
                }
                iterator.remove();
            }
    }

    /** Switch to "STOPPED" mode. */
    public final void stop() {
        checkStarted("stop");
        mStopped = true;
    }

    /** Release resources */
    public final void close() {
        checkStopped("close");
        if (!mSaved) {
            mLoaders.clear();
            mStableIds.clear();
        }
        mClosed = true;
    }

    /**
     * Start the loader.
     *
     * @param id the loader id
     * @param args the loader args
     */
    public final void startLoad(int id, @NonNull Bundle args, boolean stable) {
        checkStarted("startLoad");
        if (mLoaderManager.getLoader(id) == null) {
            if (mLoaderManager.initLoader(id, args, mCallbacks) != null) {
                mLoaders.put(id, args);
                if (stable) mStableIds.add(id);
            }
        } else {
            if (mLoaderManager.restartLoader(id, args, mCallbacks) == null) {
                mLoaders.remove(id);
                mStableIds.remove(id);
            }
        }
    }

    /**
     * Stop the loader.
     *
     * @param id the loader id.
     */
    public final void stopLoad(int id) {
        checkStarted("stopLoad");
        if (mStableIds.contains(id)) {
            mLoaderManager.destroyLoader(id);
            mLoaders.remove(id);
            mStableIds.remove(id);
        } else {
            throw new IllegalStateException("Loader " + id + " missing");
        }
    }

    /**
     * @param id the loader id
     * @return true if loader exist, otherwise - false
     */
    protected boolean hasLoader(int id) {
        return mLoaderManager.getLoader(id) != null;
    }

    /** Throws when manager is stopped. */
    private void checkStarted(@NonNull String method) {
        checkCreated(method);
        if (mStopped) {
            throw new IllegalStateException("Mustn't " + method + " in stopped state");
        }
    }

    /** Throws when manager is started. */
    private void checkStopped(@NonNull String method) {
        checkCreated(method);
        if (!mStopped) {
            throw new IllegalStateException("Mustn't " + method + " in started state");
        }
    }

    /** Throws when manager is closed. */
    private void checkCreated(@NonNull String method) {
        if (mClosed) {
            throw new IllegalStateException("Mustn't " + method + " in closed state");
        }
    }

    /**
     * The loaders resolver.
     *
     * @param id the loader id
     * @param args the args
     *
     * @return the loader instance
     */
    @Nullable
    protected Loader<Object> onCreateLoader(int id, @NonNull Bundle args) {
        return null;
    }

    /**
     * Load finished resolver.
     *
     * @param id the loader id
     * @param data the loader data
     */
    protected void onLoadFinished(int id, @Nullable Object data) {}

    /** {@inheritDoc} */
    protected final void finalize() throws Throwable {
        try {
            if (!mClosed) {
                close();
                throw new RuntimeException (
                        "\nA resource was acquired at attached stack trace but never released." +
                                "\nSee java.io.Closeable for info on avoiding resource leaks."
                );
            }
        } finally {
            super.finalize();
        }
    }
}
