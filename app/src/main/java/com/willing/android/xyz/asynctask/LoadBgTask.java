package com.willing.android.xyz.asynctask;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;

import com.willing.android.xyz.App;
import com.willing.android.xyz.R;
import com.willing.android.xyz.activity.PlayingActivity;
import com.willing.android.xyz.entity.Music;
import com.willing.android.xyz.utils.BitmapBlur;
import com.willing.android.xyz.utils.BitmapLru;
import com.willing.android.xyz.utils.ParseMusicFile;

import java.lang.ref.WeakReference;

/**
 * Created by Willing on 2015/12/13 0013.
 */
public class LoadBgTask extends AsyncTask<String, Void, Bitmap>
{
    private WeakReference<PlayingActivity> mActivity;

    private static BitmapLru mBitmapLru;

    public LoadBgTask(PlayingActivity activity)
    {
        mActivity = new WeakReference<>(activity);
    }

    @Override
    protected Bitmap doInBackground(String... params)
    {
        Music music = App.getInstance().getPlayService().getPlayingMusic();
        if (music != null)
        {
            String key = music.getSinger() + BitmapLru.SEPERATOR + music.getAlbum();

            Bitmap tmp = App.getBitmapLru().get(key);
            if (tmp != null)
            {
                return tmp;
            }
        }
        PlayingActivity activity = mActivity.get();
        if (activity == null)
        {
            return null;
        }
        int height = activity.getResources().getDisplayMetrics().heightPixels;
        int width = activity.getResources().getDisplayMetrics().widthPixels;
        activity = null;
        if (isCancelled())
        {
            return null;
        }
        Bitmap bitmap = ParseMusicFile.parseAlbumImage(params[0], height);
        if (bitmap != null)
        {
            if (height > bitmap.getHeight() || width > bitmap.getWidth())
            {
                if (isCancelled())
                {
                    return null;
                }
                // 说明原图片小于屏幕大小, 需要放大图片
                float scaleHeight = (float) height / bitmap.getHeight();
                float scaleWidth = (float) width / bitmap.getWidth();

                float maxScale = Math.max(scaleHeight, scaleWidth);

                int dstHeight = (int) (maxScale * bitmap.getHeight()) + 1;
                int dstWidth = (int) (maxScale * bitmap.getWidth()) + 1;
                Bitmap scaleBitmap = bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, false);
                if (scaleBitmap != bitmap)
                {
                    bitmap.recycle();
                }
                bitmap = scaleBitmap;
            }

            int x = 0;
            int y = 0;
            if (bitmap.getHeight() > height)
            {
                y = (bitmap.getHeight() - height) / 2;
            }
            if (bitmap.getWidth() > width)
            {
                x = (bitmap.getWidth() - width) / 2;
            }

            if (isCancelled())
            {
                return null;
            }
            Bitmap rightBitmap = bitmap.createBitmap(bitmap, x, y, width, height);
            if (bitmap != rightBitmap)
            {
                bitmap.recycle();
            }

            if (isCancelled())
            {
                return null;
            }
            Bitmap blurBitmap = BitmapBlur.blur(rightBitmap, 50);
            if (rightBitmap != blurBitmap)
            {
                rightBitmap.recycle();
            }

            return blurBitmap;
        }
        return null;

    }

    @Override
    protected void onPostExecute(Bitmap bitmap)
    {
        if (isCancelled())
        {
            return;
        }
        PlayingActivity activity = mActivity.get();
        if (activity != null)
        {
            if (bitmap != null)
            {
                Music music = App.getInstance().getPlayService().getPlayingMusic();
                if (music != null)
                {
                    String key = music.getSinger() + BitmapLru.SEPERATOR + music.getAlbum();

                    App.getBitmapLru().put(key, bitmap);
                }
                activity.getRootView().setBackgroundDrawable(
                        new BitmapDrawable(mActivity.get().getResources(), bitmap));
            }
            else
            {
                activity.getRootView().setBackgroundResource(R.drawable.album_default);
            }
        }
    }
}
