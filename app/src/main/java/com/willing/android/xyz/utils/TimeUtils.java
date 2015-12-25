package com.willing.android.xyz.utils;

/**
 * Created by Willing on 2015/12/12 0012.
 */
public class TimeUtils
{
    public static String parseDuration(long duration)
    {
        String str = "";

        long minute = duration / 60;
        long second = duration % 60;

        String strMinute;
        if (minute < 10)
        {
            strMinute = "0" + minute;
        }
        else
        {
            strMinute = "" + minute;
        }

        String strSecond;
        if (second < 10)
        {
            strSecond = "0" + second;
        }
        else
        {
            strSecond = "" + second;
        }

        str += strMinute + ":" + strSecond;

        return str;
    }
}
