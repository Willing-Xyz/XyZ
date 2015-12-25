package com.willing.android.xyz.entity;

/**
 * Created by Willing on 2015/11/5 0005.
 */
public class LrcLine implements Comparable<LrcLine>
{
    private int mTime; // 该行歌词显示的时间点。可能有多个time和line，因为如果一行显示不下，会拆分为多行
    private String mLine;

    public LrcLine(int time, String line)
    {
        mTime = time;
        mLine = line;
    }

    public int getTime()
    {
        return mTime;
    }

    public void setTime(int time)
    {
        mTime = time;
    }

    public String getLine()
    {
        return mLine;
    }

    public void setLine(String line)
    {
        mLine = line;
    }

    @Override
    public int compareTo(LrcLine another)
    {
        if (another == null)
        {
            throw new NullPointerException();
        }
        if (another instanceof LrcLine)
        {
            if (mTime > another.mTime)
            {
                return 1;
            }
            else if (mTime < another.mTime)
            {
                return -1;
            }
            else
            {
                return 0;
            }
        }
        else
        {
            throw new ClassCastException();
        }
    }
}
