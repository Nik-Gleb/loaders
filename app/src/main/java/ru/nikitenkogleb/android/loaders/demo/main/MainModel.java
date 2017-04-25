package ru.nikitenkogleb.android.loaders.demo.main;

import android.content.ContentProviderClient;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.OperationCanceledException;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.nikitenkogleb.android.loaders.OneShotLoader;
import ru.nikitenkogleb.android.loaders.StableCursorLoader;
import ru.nikitenkogleb.android.loaders.StableLoader;
import ru.nikitenkogleb.android.loaders.demo.base.Model;

/**
 * @author Nikitenko Gleb
 * @since 1.0, 25/04/2017
 */
public class MainModel extends Model {

    /** Calc sum a bundle argument. */
    private static final String BUNDLE_CALC_SUM_A = "a";
    /** Calc sum b bundle argument. */
    private static final String BUNDLE_CALC_SUM_B = "b";

    @Nullable
    private ContentProviderClient mContentProviderClient = null;

    /** {@inheritDoc} */
    @Override
    public final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContentProviderClient = getContext().getApplicationContext().getContentResolver()
                .acquireContentProviderClient(ContactsContract.AUTHORITY_URI);
    }

    /** {@inheritDoc} */
    @Override
    public final void onDestroy() {
        assert mContentProviderClient != null;
        //mContentProviderClient.close();
        mContentProviderClient.release();
        mContentProviderClient = null;


        super.onDestroy();
    }





    /*   =====    OneShot-Loader Experiments   =====    */

    /**
     * Sum calculating.
     *
     * @param a first arg
     * @param b second arg
     * @return the sum
     */
    private static long calcSum(int a, int b, @NonNull CancellationSignal signal) {
        for (int i = 0; i < 15; i++) {
            signal.throwIfCanceled();
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                throw new OperationCanceledException(e.getMessage());
            }
        }

        return a + b;
    }

    /**
     * Sum calculating.
     *
     * @param context the application context
     * @param args arguments
     * @return the loader
     */
    @NonNull
    static OneShotLoader<Object> calcSum (@NonNull Context context, @NonNull Bundle args) {
        final int a = args.getInt(BUNDLE_CALC_SUM_A);
        final int b = args.getInt(BUNDLE_CALC_SUM_B);

        return new OneShotLoader<Object>(context, AsyncTask.THREAD_POOL_EXECUTOR) {
            @Override protected final Long loadInBackground
                    (@NonNull CancellationSignal cancellationSignal) {
                return calcSum(a, b, cancellationSignal);
            }
        };
    }

    /**
     * Sum calculating.
     *
     * @param a first arg
     * @param b second arg
     * @return the bundle args
     */
    @NonNull
    static Bundle calcSum (int a, int b) {
        final Bundle result = new Bundle();
        result.putInt(BUNDLE_CALC_SUM_A, a);
        result.putInt(BUNDLE_CALC_SUM_B, b);
        return result;
    }





    /*   =====    Stable-Loader Experiments   =====    */

    /**
     * Get contacts.
     *
     * @param context the application context
     * @param args arguments
     * @return the loader
     */
    @NonNull
    final StableLoader<Object> getContacts (@NonNull Context context, @NonNull Bundle args) {


        final Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        final String[] projection = {
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        };

        final String selection =
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE '?'";
        final String sort = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC";

        final String[] selectionArgs = new String[] {
                args != Bundle.EMPTY ? args.getString("query", "*") : null
        };

        return new StableCursorLoader(context, AsyncTask.THREAD_POOL_EXECUTOR) {
            /** Background loading. */
            @Nullable @Override
            protected final Cursor loadInBackground(@NonNull CancellationSignal cancellationSignal){

                try {
                    return mContentProviderClient != null ?
                            mContentProviderClient.query(uri, projection, selection,
                                    selectionArgs, sort, cancellationSignal) : null;
                } catch (RemoteException exception) {
                    return null;
                }

            }
        };
    }

    /**
     * Sum calculating.
     *
     * @param query search string
     * @return the bundle args
     */
    @NonNull
    static Bundle getContacts (@NonNull String query) {
        final Bundle result = new Bundle();
        result.putString("query", query);
        return result;
    }
}
