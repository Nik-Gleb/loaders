/*
 * 	MainActivity.java
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

package ru.nikitenkogleb.android.loaders.demo.main;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import proguard.annotation.Keep;
import proguard.annotation.KeepPublicProtectedClassMembers;
import ru.nikitenkogleb.android.loaders.LoaderManager;
import ru.nikitenkogleb.android.loaders.demo.R;
import ru.nikitenkogleb.android.loaders.demo.base.Model;


/**
 * @author Nikitenko Gleb
 * @since 1.0, 09/02/2017
 */
@Keep
@KeepPublicProtectedClassMembers
@SuppressWarnings("unused")
public final class MainActivity extends AppCompatActivity {

    static { android.support.v4.app.LoaderManager.enableDebugLogging(true);}

    /** The log-cat tag. */
    private static final String TAG = "MainActivity";


    /** The calc sum loader id. */
    private static final int IDL_CONTACTS = 0;

    /** The calc sum loader id. */
    private static final int IDL_CALC_SUM = 1;


    /** The model. */
    private MainModel mModel = null;
    /** The loader manager. */
    private LoaderManager mLoaderManager = null;

    private GridLayout mGridLayout = null;
    private ProgressBar mProgressBar = null;

    /** The "A" edit text widget. */
    private EditText mAEditText = null;
    /** The "B" edit text widget. */
    private EditText mBEditText = null;


    /** {@inheritDoc} */
    @Override
    protected final void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        (mAEditText = (EditText)findViewById(R.id.a_editText)).setText("0");
        (mBEditText = (EditText)findViewById(R.id.b_editText)).setText("0");

        mGridLayout = (GridLayout) findViewById(R.id.grid_layout);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (savedInstanceState != null) {
            mGridLayout.setVisibility(savedInstanceState.getInt("grid"));
            mProgressBar.setVisibility(savedInstanceState.getInt("progress"));
        } else {
            mGridLayout.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.INVISIBLE);
        }

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            /** {@inheritDoc} */
            @Override
            public final void onClick(View v) {
                MainActivity.this.onClick(v);
            }
        });


        mModel = (MainModel) Model.instantiate(this, MainModel.class, savedInstanceState);
        mLoaderManager = createLoaderManager(getSupportLoaderManager(), savedInstanceState);

    }

    /** {@inheritDoc} */
    @Override
    protected final void onSaveInstanceState(Bundle outState) {

        mLoaderManager.backup(outState);
        mModel.backup(outState);

        outState.putInt("grid", mGridLayout.getVisibility());
        outState.putInt("progress", mProgressBar.getVisibility());

        super.onSaveInstanceState(outState);
    }

    /** {@inheritDoc} */
    @Override
    protected final void onDestroy() {

        mLoaderManager.close();
        mLoaderManager = null;

        mBEditText = null;
        mAEditText = null;

        super.onDestroy();
    }

    /** {@inheritDoc} */
    @Override
    protected final void onStart() {
        mLoaderManager.start();
        super.onStart();

    }
    /** {@inheritDoc} */
    protected final void onStop() {
        super.onStop();
        mLoaderManager.stop();
    }

    /**
     * Calls when user clicked some widget.
     *
     * @param view the clicked widget
     */
    private void onClick(@NonNull View view) {

        mGridLayout.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);

        final int a = Integer.parseInt(mAEditText.getText().toString());
        final int b = Integer.parseInt(mBEditText.getText().toString());

        mLoaderManager.startLoad(IDL_CALC_SUM, MainModel.calcSum(a, b), false);

        //onContacts();

    }

    /**
     * Callback by sum calculated.
     *
     * @param sum the sum
     */
    private void onSumCalculated(long sum) {
        mGridLayout.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);

        Toast.makeText(this, "Calculated sum: " + sum, Toast.LENGTH_SHORT).show();
    }


    /** Contacts start/stop loading */
    private void onContacts() {
        if (getSupportLoaderManager().getLoader(IDL_CONTACTS) == null)
            mLoaderManager.startLoad(IDL_CONTACTS, MainModel.getContacts("Gleb%"), true);
        else
            mLoaderManager.stopLoad(IDL_CONTACTS);
    }

    /**
     * Contacts cursor
     *
     * @param cursor cursor
     */
    private void onContactsLoaded(@NonNull Cursor cursor) {
        DatabaseUtils.dumpCursor(cursor);
    }



    /*=======================================================================================
     *       T H E    L O A D E R - M A N A G E R   D E C L A R A T I O N S
     *=======================================================================================/

    /**
     * @param loaderManager the framework loader manager
     * @param savedInstanceState saved state instance
     *
     * @return created loader manager
     */
    @NonNull
    private LoaderManager createLoaderManager
            (@NonNull android.support.v4.app.LoaderManager loaderManager,
                    @Nullable Bundle savedInstanceState) {


        return new LoaderManager(loaderManager, savedInstanceState) {

            /** {@inheritDoc} */
            @Nullable @Override
            protected final Loader<Object> onCreateLoader(int id, @NonNull Bundle args) {
                switch (id) {

                    // Static access (clean function)
                    case IDL_CALC_SUM : return MainModel.calcSum(getApplicationContext(), args);

                    // Dynamic access (context-dependent, BUT !!! Retain-based, no leaks)
                    case IDL_CONTACTS : return mModel.getContacts(getApplicationContext(), args);

                    default: return super.onCreateLoader(id, args);
                }
            }

            /** {@inheritDoc} */
            @Override
            protected final void onLoadFinished(int id, @Nullable Object data) {
                switch (id) {
                    case IDL_CALC_SUM : {
                        if (data != null) {
                            onSumCalculated((Long) data);
                        }
                    }
                    case IDL_CONTACTS : {
                        if (data != null && data instanceof Cursor) {
                            onContactsLoaded((Cursor) data);
                        }
                    }
                    default: super.onLoadFinished(id, data);
                }
            }

        };
    }

}
