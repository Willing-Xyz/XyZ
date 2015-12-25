package com.willing.android.xyz.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;

import com.willing.android.xyz.App;
import com.willing.android.xyz.R;
import com.willing.android.xyz.adapter.ViewPagerAndTabAdapter;
import com.willing.android.xyz.event.ChangeMusicEvent;
import com.willing.android.xyz.event.StartPauseEvent;
import com.willing.android.xyz.fragment.AllSongFragment;
import com.willing.android.xyz.fragment.CategoryFragment;
import com.willing.android.xyz.fragment.SingerFragment;
import com.willing.android.xyz.view.SmallPlayer;

import java.lang.reflect.Field;

import de.greenrobot.event.EventBus;

/**
 * Created by Willing on 2015/12/9 0009.
 */
public class MainActivity extends BaseActivity
{
    //    private static final String STATE_CURRENT_TAB_INDEX = "state_current_tab_index";

    //    private ImageButton mSmallPic;
    //    private ImageButton mPause;
    //    private ImageButton mNext;
    //    private TextView mTitle;
    //    private TextView mSinger;

    private ViewPager mViewPager;
    private SmallPlayer mSmallPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        initActionBar();
        setupTabAndViewPager();


    }

    private void initView()
    {
        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        mSmallPlayer = (SmallPlayer) findViewById(R.id.smallPlayer);
    }


    private void initActionBar()
    {
        forceShowActionBarOverflowMenu();

        ActionBar actionBar = getSupportActionBar();

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setStackedBackgroundDrawable(getResources().getDrawable(R.color.actionbar_tab));
    }

    /**
     * 结合ActionBar的Tab和ViewPager
     */
    @SuppressWarnings("deprecation")
    private void setupTabAndViewPager()
    {
        ActionBar actionBar = getSupportActionBar();

        ViewPagerAndTabAdapter adapter = new ViewPagerAndTabAdapter(getSupportFragmentManager(), mViewPager, actionBar);

        adapter.addFragment(new CategoryFragment());
        adapter.addFragment(new SingerFragment());
        adapter.addFragment(new AllSongFragment());

        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(adapter);
        mViewPager.setOnPageChangeListener(adapter);

        actionBar.addTab(actionBar.newTab().setText(getString(R.string.tab_category)).setTabListener(adapter), 0, true);
        actionBar.addTab(actionBar.newTab().setText(getString(R.string.tab_singer)).setTabListener(adapter), 1);
        actionBar.addTab(actionBar.newTab().setText(getString(R.string.tab_allsong)).setTabListener(adapter), 2);
    }


    /**
     * 即使有Menu硬件按钮，也强制显示overflowmenu
     */
    private void forceShowActionBarOverflowMenu()
    {
        try
        {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null)
            {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    @Override
    protected void onStart()
    {
        super.onStart();

        EventBus.getDefault().registerSticky(this);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        int id = item.getItemId();
        switch (id)
        {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);

                startActivity(intent);

                return true;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }



    public void onEventMainThread(ChangeMusicEvent event)
    {
        mSmallPlayer.updateState(App.getInstance().getPlayService().getPlayingMusic(), true);
    }

    public void onEventMainThread(StartPauseEvent event)
    {
        mSmallPlayer.setPlaying(App.getInstance().getPlayService().isPlaying());
    }
}
