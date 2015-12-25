package com.willing.android.xyz.event;

/**
 * Created by Willing on 2015/12/12 0012.
 */
public class PlayModeChangeEvent
{
    private static PlayModeChangeEvent mInstance = new PlayModeChangeEvent();

    private PlayModeChangeEvent()
    {}

    public static PlayModeChangeEvent getInstance()
    {
        return mInstance;
    }
}
