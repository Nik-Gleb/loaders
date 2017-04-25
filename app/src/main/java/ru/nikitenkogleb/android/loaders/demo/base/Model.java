/*
 * 	Model.java
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

package ru.nikitenkogleb.android.loaders.demo.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * @author Nikitenko Gleb
 * @since 1.0, 04/04/2017
 */
public abstract class Model extends Fragment {

    /** The fragment tag. */
    private static final String TAG = "MODEL";

    /** Construct a new model. */
    public Model() {
        setRetainInstance(true);
    }

    /**
     * Backup current state.
     *
     * @param state the state container
     */
    public final void backup(@NonNull Bundle state) {
        getFragmentManager().putFragment(state, TAG, this);
    }

    /**
     * Constructs a new fragment.
     *
     * @param activity the host activity
     * @param clazz the clazz name
     * @param savedInstanceState the saved state
     *
     * @return the fragment instance
     */
    @NonNull
    public static Model instantiate(@NonNull FragmentActivity activity,
            @NonNull Class<? extends Model> clazz, @Nullable Bundle savedInstanceState) {
        final FragmentManager fragmentManager = activity.getSupportFragmentManager();
        Fragment result = fragmentManager.findFragmentByTag(TAG);
        if (result == null && savedInstanceState != null) {
            result = fragmentManager.getFragment(savedInstanceState, TAG);
        }
        if (result == null) {
            try {
                result = clazz.newInstance();
                fragmentManager.beginTransaction().add(result, TAG).commitNow();
            } catch (java.lang.InstantiationException | IllegalAccessException exception) {
                throw new RuntimeException(exception);
            }

        }
        return (Model) result;
    }

    /**
     * @param fragment current fragment
     * @return the model
     */
    @NonNull
    public static Model get(@NonNull Fragment fragment) {
        final FragmentManager fragmentManager = fragment.getFragmentManager();
        final Fragment result = fragmentManager.findFragmentByTag(TAG);
        if (result != null) {
            return (Model) result;
        } else {
            throw new RuntimeException("Model not found");
        }
    }

}
