package com.willing.android.xyz.utils;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.LruCache;

/**
 * 没有重载create方法，因为如果重载它的话，多线程比较麻烦
 * Created by Willing on 2015/12/13 0013.
 */
public class BitmapLru extends LruCache<String, Bitmap>
{

    public static final char SEPERATOR = '-';

    public BitmapLru(int maxSize)
    {
        super(maxSize);
    }

    @Override
    protected synchronized void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue)
    {
        if (oldValue != newValue)
        {
            oldValue.recycle();
        }
    }

    @Override
    protected synchronized int sizeOf(String key, Bitmap value)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            return value.getAllocationByteCount();
        }
        else
        {
            return value.getByteCount();
        }
    }


}
