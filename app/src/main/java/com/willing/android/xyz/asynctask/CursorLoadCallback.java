package com.willing.android.xyz.asynctask;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;

/**
 * Created by Willing on 2015/12/10 0010.
 */
public class CursorLoadCallback implements LoaderManager.LoaderCallbacks<Cursor>
{
    private String mName;
    private Context mContext;
    private CursorAdapter mListAdapter;
    private int mType;

    public CursorLoadCallback(Context context, CursorAdapter adapter, int type, String name)
    {
        mContext = context;
        mListAdapter = adapter;
        mType = type;
        mName = name;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id,
                                         Bundle args)
    {
        return new DbLoader(mContext, mType, mName);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        if (mListAdapter != null)
        {
            mListAdapter.changeCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader loader)
    {
        if (mListAdapter != null)
        {
            mListAdapter.changeCursor(null);
        }
    }

}
