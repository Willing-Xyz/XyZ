package com.willing.android.xyz.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.willing.android.xyz.App;
import com.willing.android.xyz.R;
import com.willing.android.xyz.asynctask.LoadBgTask;
import com.willing.android.xyz.entity.Music;
import com.willing.android.xyz.event.ChangeMusicEvent;
import com.willing.android.xyz.event.PlayModeChangeEvent;
import com.willing.android.xyz.event.StartPauseEvent;
import com.willing.android.xyz.service.MusicPlayService;
import com.willing.android.xyz.utils.TimeUtils;
import com.willing.android.xyz.view.LrcView;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Willing on 2015/12/11 0011.
 */
public class PlayingActivity extends BaseActivity
{
    private ImageButton mPlayMode;
    private ImageButton mPause;
    private ImageButton mPlayList;
    private ImageButton mNext;
    private ImageButton mPre;

    private SeekBar mSeekBar;
    private TextView mCurTime;
    private TextView mTotalTime;

    private LrcView mLrcView;

    private View mRootView;

    private Handler mUpdateHandler;
    private Runnable mUpdateRunnable;

    private LoadBgTask mLoadBgTask;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_playing);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.transparent_black)));

        initView();
        setupListener();

        mUpdateHandler = new Handler(getMainLooper());
        mUpdateRunnable = new UpdateRunnable(mUpdateHandler);
    }

    private void initView()
    {
        mPlayMode = (ImageButton) findViewById(R.id.ib_play_mode);
        mPause = (ImageButton) findViewById(R.id.ib_pause);
        mPlayList = (ImageButton) findViewById(R.id.ib_play_list);
        mPre = (ImageButton) findViewById(R.id.ib_pre);
        mNext = (ImageButton) findViewById(R.id.ib_next);

        mSeekBar = (SeekBar) findViewById(R.id.sb_play_progress);
        mCurTime = (TextView) findViewById(R.id.tv_cur_time);
        mTotalTime = (TextView) findViewById(R.id.tv_total_time);

        mLrcView = (LrcView) findViewById(R.id.lrcView);
        mRootView = mLrcView.getRootView();
        mRootView.setBackgroundResource(R.drawable.album_default);
    }

    private void setupListener()
    {
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                if (fromUser)
                {
                    App.getInstance().getPlayService().seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
            }
        });

        mPause.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MusicPlayService service = App.getInstance().getPlayService();
                if (service.isPlaying())
                {
                    service.pause();
                } else
                {
                    service.start();
                }
            }
        });

        mNext.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MusicPlayService service = App.getInstance().getPlayService();
                service.next();
            }
        });

        mPre.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MusicPlayService service = App.getInstance().getPlayService();
                service.pre();
            }
        });

        mPlayMode.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MusicPlayService service = App.getInstance().getPlayService();
                service.nextPlayMode();
            }
        });

        mPlayList.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showPlayListWindow();
            }
        });

    }

    private void showPlayListWindow()
    {
        final MusicPlayService service = App.getInstance().getPlayService();

        if (service == null)
        {
            return;
        }

        View view = getLayoutInflater().inflate(R.layout.popupwindow_play_list, null);

        ListView listView = (ListView) view.findViewById(R.id.play_list);
        TextView clearTextView = (TextView) view.findViewById(R.id.tv_clear_play_list);
        View closeView = view.findViewById(R.id.close_playlist);

        // 设置播放列表
        ArrayList<Music> infoList = service.getPlayList();
        List<String> data = new LinkedList<String>();

        int count = infoList.size();
        for (int i = 0; i < count; ++i)
        {
            Music info = infoList.get(i);
            String songSinger = info.getName() + " - " + info.getSinger();

            data.add(songSinger);
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.playlist_item, data);
        listView.setAdapter(adapter);

        // 显示播放列表
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int height = windowManager.getDefaultDisplay().getHeight() / 5 * 3;
        final PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, height, true);

        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.playList_bg)));
        popupWindow.showAtLocation(mPlayList, Gravity.NO_GRAVITY, 0, height);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
            {
                popupWindow.dismiss();

                service.setIndex(position);
                service.playNewMusic();
            }
        });
        clearTextView.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                service.getPlayList().clear();

                adapter.clear();
                adapter.notifyDataSetChanged();
            }
        });
        closeView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                popupWindow.dismiss();
            }
        });

    }

    @Override
    protected void onStart()
    {
        super.onStart();

        EventBus.getDefault().registerSticky(this);

        mUpdateHandler.postDelayed(mUpdateRunnable, 1000);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        EventBus.getDefault().unregister(this);

        mUpdateHandler.removeCallbacks(mUpdateRunnable);
    }

    @Nullable
    @Override
    public Intent getSupportParentActivityIntent()
    {
        return getIntent();
    }

    public void onEventMainThread(ChangeMusicEvent event)
    {
        //
        Music music = App.getInstance().getPlayService().getPlayingMusic();


        if (music != null)
        {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle(music.getName());
            actionBar.setSubtitle(music.getSinger());

            mPause.setImageResource(R.drawable.pause);

            mTotalTime.setText(TimeUtils.parseDuration(music.getDuration()));

            mLrcView.setPath(musicToLrcPath(music.getPath()));
            mLrcView.setMusicName(music.getName());

            mSeekBar.setMax(music.getDuration());

            if (mLoadBgTask != null)
            {
                mLoadBgTask.cancel(false);
            }
            mLoadBgTask = new LoadBgTask(this);
            mLoadBgTask.execute(music.getPath());
        }
    }

    public View getRootView()
    {
        return mRootView;
    }

    public void onEventMainThread(StartPauseEvent event)
    {
        boolean isPlaying = App.getInstance().getPlayService().isPlaying();
        if (isPlaying)
        {
            mPause.setImageResource(R.drawable.pause);
        } else
        {
            mPause.setImageResource(R.drawable.start);
        }


    }

    public void onEventMainThread(PlayModeChangeEvent event)
    {
        int mode = App.getInstance().getPlayService().getPlayMode();

        mPlayMode.setImageResource(MusicPlayService.PLAY_MODE_RES[mode]);
    }

    public static String musicToLrcPath(String musicPath)
    {
        if (musicPath == null)
        {
            return null;
        }
        int index = musicPath.lastIndexOf('.');

        if (index == -1)
        {
            return null;
        }

        // 相同目录下的lrc
        String path = musicPath.substring(0, index) + ".lrc";
        File file = new File (path);
        if (file.exists())
        {
            return path;
        }

        return null;

    }


    private class UpdateRunnable implements Runnable
    {
        private Handler mHandler;

        public UpdateRunnable(Handler handler)
        {
            mHandler = handler;
        }
        @Override
        public void run()
        {
            mHandler.postDelayed(this, 1000);

            MusicPlayService service = App.getInstance().getPlayService();

            if (service.isPlaying())
            {
                int curTime = App.getInstance().getPlayService().getCurPos() / 1000;
                mCurTime.setText(TimeUtils.parseDuration(curTime));

                mSeekBar.setProgress(curTime);

                mLrcView.setCurTime(App.getInstance().getPlayService().getCurPos() / 10);
            }
        }
    }

}
