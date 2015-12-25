package com.willing.android.xyz.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

import com.willing.android.xyz.R;
import com.willing.android.xyz.activity.MainActivity;
import com.willing.android.xyz.entity.Music;
import com.willing.android.xyz.event.ChangeMusicEvent;
import com.willing.android.xyz.event.PlayModeChangeEvent;
import com.willing.android.xyz.event.StartPauseEvent;
import com.willing.android.xyz.utils.MusicDbHelper;

import java.util.ArrayList;
import java.util.Random;

import de.greenrobot.event.EventBus;

public class MusicPlayService extends Service implements OnAudioFocusChangeListener
{
    private static final int ID_FOREGROUND_NOTIFACTION = 1;

    public static final int PLAY_MODE_ORDER = 0;
    public static final int PLAY_MODE_CIRCLE = 1;
    public static final int PLAY_MODE_SINGLE = 2;
    public static final int PLAY_MODE_RANDOM = 3;

    public static final int[] PLAY_MODE_RES = {R.drawable.play_mode_order, R.drawable.play_mode_circle,
            R.drawable.play_mode_single, R.drawable.play_mode_random};

    private int mPlayMode;

    private ArrayList<Music> mPlayList = new ArrayList<Music>(); // 播放列表
    private int mIndex = 0;   // 当前播放的音乐在mPlayList中的位置

    private NotificationManager mNotificationManager;

    public class MusicPlayBinder extends Binder
    {
        public MusicPlayService getService()
        {
            return MusicPlayService.this;
        }
    }

    private final MusicPlayBinder mBinder = new MusicPlayBinder();
    private MediaPlayer	mPlayer;
    private boolean isError;
    private AudioManager	mAudioManager;
    private Builder	mNotificationBuilder;

    // 在失去焦点之前是否正在播放
    private boolean	mBeforeLossIsPlaying;
    // 失去焦点之前的音量
    private int	mBeforeLossVolume;
    // 是否在失去焦点时，降低了音量
    private boolean	mResotreVolume;

    @Override
    public void onCreate()
    {
        super.onCreate();

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        setupForeground();

        initMediaPlayer();

    }

    /**
     * 设置该Service为前台
     */
    private void setupForeground()
    {
        // 设置为前台服务
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        mNotificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("")
                .setContentText("")
                .setContentIntent(pendingIntent);

        startForeground(ID_FOREGROUND_NOTIFACTION, mNotificationBuilder.build());
    }


    private void initMediaPlayer()
    {
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        setMediaPlayerListener();

    }

    private void setMediaPlayerListener()
    {
        mPlayer.setOnCompletionListener(new OnCompletionListener()
        {

            @Override
            public void onCompletion(MediaPlayer mp)
            {
                int playMode = getPlayMode();

                switch (playMode)
                {
                    case PLAY_MODE_CIRCLE:
                        mIndex = (mIndex + 1) % mPlayList.size();

                        break;
                    case PLAY_MODE_RANDOM:
                    case PLAY_MODE_ORDER:
                        if (mIndex < mPlayList.size())
                        {
                            mIndex++;
                        }
                        break;

                    case PLAY_MODE_SINGLE:

                        break;
                    default:
                        break;
                }

                if (mIndex == mPlayList.size() && playMode == PLAY_MODE_ORDER)
                {
                    mAudioManager.abandonAudioFocus(MusicPlayService.this);
                } else
                {
                    MusicPlayService.this.playNewMusic();
                }

            }
        });

        mPlayer.setOnPreparedListener(new OnPreparedListener()
        {

            @Override
            public void onPrepared(MediaPlayer mp)
            {
                if (mPlayer != null)
                {
                    if (isError || mPlayer == null)
                    {
                        return;
                    }
                    if (!requestFocus())
                    {
                        return;
                    }
                    if (mIndex >= mPlayList.size() || mIndex < 0)
                    {
                        return;
                    }
                    try
                    {
                        mPlayer.start();
                    }
                    catch (IllegalStateException e)
                    {
                        playNewMusic();
                    }
                    isError = false;
                }
            }
        });

        mPlayer.setOnErrorListener(new OnErrorListener()
        {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra)
            {
                isError = true;

                return true;
            }
        });

    }


    public void playNewMusic()
    {
        init();

        Music info = getPlayingMusic();
        if (info != null)
        {
            // 修改Notification
            mNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher)  // 专辑封面
                    .setContentTitle(info.getName() + " - " + info.getSinger())
                    .setContentText(info.getAlbum());


            mNotificationManager.notify(ID_FOREGROUND_NOTIFACTION, mNotificationBuilder.build());
        }

        EventBus.getDefault().postSticky(ChangeMusicEvent.getInstance());
    }

    private void init()
    {
        if (!requestFocus())
        {
            return;
        }

        Music info = getPlayingMusic();
        if (info != null)
        {
            mPlayer.reset();

            try
            {
                mPlayer.setDataSource(info.getPath());
                mPlayer.prepareAsync();
            }
            catch (Exception e)
            {
                isError = true;
            }
        }
    }

    public void start()
    {
        if (isError || mPlayer == null)
        {
            return;
        }
        if (!requestFocus())
        {
            return;
        }
        if (mIndex >= mPlayList.size() || mIndex < 0)
        {
            return;
        }
        try
        {
            mPlayer.start();

            EventBus.getDefault().postSticky(StartPauseEvent.getInstance());
        }
        catch (IllegalStateException e)
        {
            playNewMusic();
        }
    }

    public void pause()
    {
        if (isError || mPlayer == null)
        {
            return;
        }

        if (isPlaying())
        {
            mPlayer.pause();

            EventBus.getDefault().postSticky(StartPauseEvent.getInstance());
        }
    }

    public boolean isPlaying()
    {
        if (mPlayer == null)
        {
            return false;
        }
        boolean flag = false;
        try
        {
            flag = mPlayer.isPlaying();
        }
        catch (IllegalStateException ex)
        {
            flag = false;
        }
        return flag;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        mAudioManager.abandonAudioFocus(this);

        if (mPlayer != null)
        {
            release();
        }

        stopForeground(true);
    }

    public void seekTo(int pos)
    {
        if (isError || mPlayer == null)
        {
            return;
        }
        try
        {
            mPlayer.seekTo(pos);
        }
        catch (IllegalStateException ex)
        {
            init();

            mPlayer.seekTo(pos);
        }
    }

    public int getCurPos()
    {
        if (mPlayer == null)
        {
            return 0;
        }
        return mPlayer.getCurrentPosition();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    @SuppressWarnings("deprecation")
    private boolean requestFocus()
    {
        boolean flag =  mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) ==
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED;

        if (flag)
        {
//            mAudioManager.registerMediaButtonEventReceiver(new ComponentName(getApplicationContext(),
//                    RemoteControlRecevier.class));
        }

        return flag;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAudioFocusChange(int focusChange)
    {
        switch (focusChange)
        {
            case AudioManager.AUDIOFOCUS_GAIN:

                if (mResotreVolume)
                {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mBeforeLossVolume, 0);
                    mResotreVolume = false;
                }

                if (mBeforeLossIsPlaying)
                {
                    start();
                }


                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                pause();

                // 该方法表示注销掉监听器，即除非主动请求，否则不会获得焦点
                mAudioManager.abandonAudioFocus(this);

//                mAudioManager.unregisterMediaButtonEventReceiver(new ComponentName(getApplicationContext(),
//                        RemoteControlRecevier.class));

                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (isPlaying())
                {
                    pause();
                    mBeforeLossIsPlaying = true;
                }
                else
                {
                    mBeforeLossIsPlaying = false;
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // 降低音量
                mBeforeLossVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                mResotreVolume = true;

                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) / 3, 0);

                break;

        }
    }

    private void release()
    {
        mPlayer.release();
        mPlayer = null;
    }


    //播放列表

    public ArrayList<Music> getPlayList()
    {
        return mPlayList;
    }

    public void addToPlayList(Music music, boolean play)
    {
        int index = 0;
        if (!mPlayList.contains(music))
        {
            mPlayList.add(music);
            index = mPlayList.size() - 1;
        }
        else
        {
            index = mPlayList.indexOf(music);
        }

        if (play)
        {
            mIndex = index;
            playNewMusic();
        }
    }

    public void addToPlayList(Cursor musics, boolean clean)
    {
        if (musics == null || musics.getCount() == 0)
        {
            return;
        }
        if (clean)
        {
            mPlayList.clear();
        }

        for (musics.moveToFirst(); !musics.isAfterLast(); musics.moveToNext())
        {
            Music music = MusicDbHelper.convertCursorToMusic(musics);

            addToPlayList(music, false);
        }

        if (mPlayMode == PLAY_MODE_RANDOM)
        {
            randomPlaylist();
        }

        mIndex = 0;
        playNewMusic();
    }

    public Music getPlayingMusic()
    {
        Music info = null;
        try
        {
            info = mPlayList.get(mIndex);
        }
        catch (Exception ex)
        {
            info = null;
        }
        return info;
    }

    public void next()
    {
        if (mPlayList.size() == 0)
        {
            return;
        }
        mIndex = (mIndex + 1) % mPlayList.size();
        playNewMusic();
    }

    public void pre()
    {
        if (mIndex == 0)
        {
            mIndex = mPlayList.size() - 1;
        }
        else
        {
            mIndex--;
        }
        playNewMusic();
    }

    public void setIndex(int index)
    {
        if (index < 0 || index >= mPlayList.size())
        {
            return;
        }
        else
        {
            mIndex = index;
        }
    }

    //#end

    //#start PlayMode


    public int getPlayMode()
    {
        return mPlayMode;
    }

    public int nextPlayMode()
    {
        setPlayMode((mPlayMode + 1) % PLAY_MODE_RES.length);

        return mPlayMode;
    }

    public void setPlayMode(int mode)
    {
        if (mode != mPlayMode && mode == PLAY_MODE_RANDOM)
        {
            randomPlaylist();
        }
        mPlayMode = mode;

        EventBus.getDefault().postSticky(PlayModeChangeEvent.getInstance());
    }

    private void randomPlaylist()
    {

        Random random = new Random();

        int x = 0;
        int y = 0;
        Music music;
        int size = mPlayList.size();
        for (int i = 0; i < size; ++i)
        {
            x = random.nextInt(size);
            y = random.nextInt(size);

            music = mPlayList.get(x);
            mPlayList.set(x, mPlayList.get(y));
            mPlayList.set(y, music);
        }
    }



}
