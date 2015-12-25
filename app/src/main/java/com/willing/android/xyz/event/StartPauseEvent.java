package com.willing.android.xyz.event;

/**
 * Created by Willing on 2015/12/11 0011.
 */
public class StartPauseEvent
{
    private static StartPauseEvent mInstance = new StartPauseEvent();

    private StartPauseEvent()
    {}

    public static StartPauseEvent getInstance()
    {
        return mInstance;
    }
}
