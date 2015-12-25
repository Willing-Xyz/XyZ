package com.willing.android.xyz;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.willing.android.xyz.activity.SettingsActivity;
import com.willing.android.xyz.receiver.RemoteControlRecevier;
import com.willing.android.xyz.service.MusicPlayService;
import com.willing.android.xyz.service.ScanMusicService;
import com.willing.android.xyz.utils.BitmapLru;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Willing on 2015/12/9 0009.
 */
public class App extends Application
{
    private static App mApp;

    // 保存当前active的Activity
    private List<Activity> mActivityList;

    private MusicPlayService	mMusicPlayService;
    private ServiceConnection mServiceConn;

    private static BitmapLru mBitmapLru = new BitmapLru((int) (Runtime.getRuntime().maxMemory() / 10));

    //region 扫描歌曲的设置

    private int mScanMinDuration;    // 最小时长为多少
    private HashSet<File> mFilterPath;  // 不被扫描的路径

    //endregion

    private BroadcastReceiver mRemoteControllReceiver;


    public static App getInstance()
    {
        return mApp;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        mApp = this;

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        mScanMinDuration = Integer.parseInt(preference.getString(SettingsActivity.MIN_DURATION, "30"));

        mActivityList = new LinkedList<Activity>();

        mFilterPath = new HashSet<File>();
        mFilterPath.add(new File("/sys"));
        mFilterPath.add(new File("/proc"));
        mFilterPath.add(new File("/data"));
        mFilterPath.add(new File("/dev"));
        mScanMinDuration = 30;

        startMusicService();

        mRemoteControllReceiver = new RemoteControlRecevier();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_BUTTON);
        filter.addAction(Intent.CATEGORY_DEFAULT);
        registerReceiver(mRemoteControllReceiver, filter);
    }

    @Override
    public void onTerminate()
    {
        super.onTerminate();

        unregisterReceiver(mRemoteControllReceiver);
    }

    private void startMusicService()
    {
        // 启动一个服务，用来扫描音乐
        Intent intent = new Intent(this, ScanMusicService.class);
        startService(intent);

        // 绑定播放服务，用来播放音乐
        Intent playIntent = new Intent(this, MusicPlayService.class);
        mServiceConn = new ServiceConnection()
        {

            @Override
            public void onServiceDisconnected(ComponentName name)
            {
                mMusicPlayService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service)
            {
                mMusicPlayService = ((MusicPlayService.MusicPlayBinder)service).getService();
            }
        };

        bindService(playIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    public MusicPlayService getPlayService()
    {
        return mMusicPlayService;
    }

    // 添加移除当前active的Activity

    public void addActivity(Activity a)
    {
        mActivityList.add(a);
    }
    public void removeActivity(Activity a)
    {
        mActivityList.remove(a);
    }

    public static BitmapLru getBitmapLru()
    {
        return mBitmapLru;
    }

    @Override
    public void onTrimMemory(int level)
    {
        super.onTrimMemory(level);

        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW)
        {
            getBitmapLru().evictAll();
        }
    }




    //region 扫描歌曲的设置

    public int getScanMinDuration()
    {
        return mScanMinDuration;
    }

    public void setScanMinDuration(int scanMinDuration)
    {
        mScanMinDuration = scanMinDuration;

        // 启动一个服务，用来扫描音乐
        Intent intent = new Intent(this, ScanMusicService.class);
        startService(intent);

    }

    public HashSet<File> getFilterPath()
    {
        return mFilterPath;
    }

    public void setFilterPath(HashSet<File> filterPath)
    {
        mFilterPath = filterPath;
    }

    public void addFilterPath(File path)
    {
        mFilterPath.add(path);
    }

    // 对特定的目录不进行扫描
    public boolean isContainInFilterPath(File path)
    {
        Iterator<File> ite = mFilterPath.iterator();
        File file = null;
        while (ite.hasNext())
        {
            file = ite.next();
            try
            {
                if (path.getCanonicalPath().startsWith(file.getCanonicalPath()))
                {
                    return true;
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return false;
    }
    //endregion

    public void quitApp()
    {
        for (Iterator<Activity> ite = mActivityList.iterator(); ite.hasNext(); )
        {
            Activity activity = ite.next();

            if (activity != null)
            {
                activity.finish();
            }
        }

        getPlayService().stopForeground(true);
        getPlayService().pause();
        unbindService(mServiceConn);
    }


}
