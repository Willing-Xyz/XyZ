package com.willing.android.xyz.activity;

import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.willing.android.xyz.App;
import com.willing.android.xyz.R;

/**
 * Created by Willing on 2015/12/9 0009.
 */
public class BaseActivity extends AppCompatActivity
{
    private static App mApp;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.actionbar)));

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mApp = App.getInstance();

        mApp.addActivity(this);
    }

    @Override
    protected void onDestroy()
    {
        mApp.removeActivity(this);

        super.onDestroy();
    }
}

