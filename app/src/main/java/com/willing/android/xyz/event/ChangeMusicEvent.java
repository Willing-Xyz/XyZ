package com.willing.android.xyz.event;

/**
 * Created by Willing on 2015/12/11 0011.
 */
public class ChangeMusicEvent
{
    private static ChangeMusicEvent mInstance = new ChangeMusicEvent();

    private ChangeMusicEvent()
    {}

    public static ChangeMusicEvent getInstance()
    {
        return mInstance;
    }
}
