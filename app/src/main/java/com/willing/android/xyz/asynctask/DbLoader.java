package com.willing.android.xyz.asynctask;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;

import com.willing.android.xyz.utils.MusicDbHelper;

/**
 * Created by Willing on 2015/12/10 0010.
 */
public class DbLoader extends AsyncTaskLoader<Cursor>
{
    public static final int TYPE_ALL_SONG = 1;
    public static final int TYPE_SINGER = 2;
    public static final int TYPE_CATEGORY = 3;
    public static final int TYPE_CATEGORY_ITEM = 4;
    public static final int TYPE_SINGER_ITEM = 5;

    private String mName;

    private Cursor	mCursor;
    private LoadContentObserver	mObserver;

    private int mType;

    public DbLoader(Context context, int type)
    {
        super(context);

        mType = type;
        mObserver = new LoadContentObserver();
    }

    public DbLoader(Context context, int type, String name)
    {
        this(context, type);

        mName = name;
    }

    @Override
    public Cursor loadInBackground()
    {
        switch (mType)
        {
            case TYPE_ALL_SONG:
                mCursor = MusicDbHelper.queryAllSong(getContext());
                break;
            case TYPE_SINGER:
                mCursor = MusicDbHelper.querySingerAndCount(getContext());
                break;
            case TYPE_CATEGORY:
                mCursor = MusicDbHelper.queryCategoryAndCount(getContext());
                break;
            case TYPE_CATEGORY_ITEM:
                mCursor = MusicDbHelper.queryCategoryItem(getContext(), mName);
                break;
            case TYPE_SINGER_ITEM:
                mCursor = MusicDbHelper.querySingerItem(getContext(), mName);
                break;
        }
        if (mCursor != null)
        {
            mCursor.getCount();
            mCursor.registerContentObserver(mObserver);
        }

        return mCursor;
    }

    @Override
    public void deliverResult(Cursor cur)
    {
        if (isReset())
        {
            releaseResources(cur);
            return;
        }

        Cursor oldCur = mCursor;
        mCursor = cur;

        if (isStarted())
        {
            super.deliverResult(cur);
        }

        if (oldCur != null && oldCur != cur)
        {
            releaseResources(oldCur);
        }
    }

    @Override
    protected void onStartLoading()
    {
        if (mCursor != null)
        {
            deliverResult(mCursor);
        }

        if (takeContentChanged() || mCursor == null)
        {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading()
    {
        cancelLoad();
    }

    @Override
    protected void onReset()
    {
        onStopLoading();


        if (mCursor != null)
        {
            mCursor.unregisterContentObserver(mObserver);
            releaseResources(mCursor);
            mCursor = null;
        }
        if (mObserver != null)
        {
            mObserver = null;
        }
    }

    @Override
    public void onCanceled(Cursor cur)
    {
        super.onCanceled(cur);

        // The load has been canceled, so we should release the resources
        // associated with 'data'.
        releaseResources(cur);
    }

    private void releaseResources(Cursor cur)
    {
        if (cur != null)
        {
            cur.close();
            cur = null;
        }

    }


    private class LoadContentObserver extends ContentObserver
    {

        public LoadContentObserver()
        {
            super(new Handler(getContext().getMainLooper()));
        }

        @Override
        public void onChange(boolean selfChange, Uri uri)
        {
            onContentChanged();
        }
    }
}
