package com.willing.android.xyz.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 歌词的表示
 * Created by Willing on 2015/11/4 0004.
 */
public class Lyric
{
    private String mAlbum; // 所属专辑
    private String mArtist; // 创作该歌词的作者
    private String mBy;    // 该歌词文件由谁制作
    private int mOffset;   // 整个歌词文件的偏移值
    private String mSoft;  // 制作该歌词的软件
    private String mVersion; // 软件版本
    private String mTitle; // 歌词标题
    private int mLength; // 歌曲长度
    private String mAuthor; // don't know.


    private ArrayList<LrcLine> mLines;


    public Lyric()
    {
        mLines = new ArrayList<>();
    }

    public List<LrcLine> getLrcs()
    {
        return mLines;
    }

    public void addLrc(int time, String line)
    {
        mLines.add(new LrcLine(time, line));
    }

    public String getAlbum()
    {
        return mAlbum;
    }

    public void setAlbum(String album)
    {
        mAlbum = album;
    }

    public String getArtist()
    {
        return mArtist;
    }

    public void setArtist(String artist)
    {
        mArtist = artist;
    }

    public String getBy()
    {
        return mBy;
    }

    public void setBy(String by)
    {
        mBy = by;
    }

    public int getOffset()
    {
        return mOffset;
    }

    public void setOffset(int offset)
    {
        mOffset = offset;
    }

    public String getSoft()
    {
        return mSoft;
    }

    public void setSoft(String soft)
    {
        mSoft = soft;
    }

    public String getTitle()
    {
        return mTitle;
    }

    public void setTitle(String title)
    {
        mTitle = title;
    }

    public String getVersion()
    {
        return mVersion;
    }

    public void setVersion(String version)
    {
        mVersion = version;
    }

    public int getLength()
    {
        return mLength;
    }

    public void setLength(int length)
    {
        mLength = length;
    }

    public String getAuthor()
    {
        return mAuthor;
    }

    public void setAuthor(String author)
    {
        mAuthor = author;
    }
}

