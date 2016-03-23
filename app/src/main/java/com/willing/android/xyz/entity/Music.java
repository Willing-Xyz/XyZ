package com.willing.android.xyz.entity;


public class Music implements Comparable<Music>
{
	private String mName;
	private String mAlbum;
	private String mSinger;
	private String mPath;
	private int mDuration;
	
	
	public Music()
	{
		mName = "未知";
		mAlbum = "未知";
		mSinger = "未知";
		mPath = "";
		mDuration = 0;
	}
	
	public Music(String path)
	{
		mPath = path;
	}
	public Music(String name, String album, String artists, String path,
			int duration)
	{
		mName = getString(name);
		mAlbum = getString(album);
		mSinger = getString(artists);
		mPath = path;
		mDuration = duration;
	}
	
	
	public String getName()
	{
		return mName;
	}
	public void setName(String name)
	{
		mName = getString(name);
	}
	public String getAlbum()
	{
		return mAlbum;
	}
	public void setAlbum(String album)
	{
		mAlbum = getString(album);
	}
	public String getSinger()
	{
		return mSinger;
	}
	public void setSinger(String artists)
	{
		mSinger = getString(artists);
	}
	public String getPath()
	{
		return mPath;
	}
	public void setPath(String path)
	{
		mPath = path;
	}
	public int getDuration()
	{
		return mDuration;
	}
	public void setDuration(int duration)
	{
		mDuration = duration;
	}
	
	public String getString(String str)
	{
		if (str.trim() == "")
		{
			return "未知";
		}
		return str;
	}

	@Override
	public boolean equals(Object o)
	{
		
		if (o == null)
		{
			return false;
		}
		if (o == this)
		{
			return true;
		}
		if (o instanceof Music)
		{
			return ((Music) o).getPath().equals(this.getPath());
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return getPath().hashCode();
	}


	@Override
	public int compareTo(Music another) {

		return mPath.compareTo(another.getPath());
	}
}
