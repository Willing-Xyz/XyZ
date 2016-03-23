package com.willing.android.xyz.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.willing.android.xyz.App;
import com.willing.android.xyz.entity.Music;
import com.willing.android.xyz.utils.MusicDbHelper;
import com.willing.android.xyz.utils.ParseMusicFile;

import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashSet;

public class ScanMusicService extends IntentService
{
	private static final String TAG = "ScanMusicService";
    private static final boolean DEBUG = true;
	
	// 支持的文件扩展名
	private static String[] exts = {"mp3"};
	
	private static App mApp;

	private HashSet<Music> mMusics;
	
	public ScanMusicService(String name)
	{
		super(name);
	}
	
	public ScanMusicService()
	{
		super("ScanMusicService-Thread");
	}

	


	@Override
	public void onCreate()
	{
		super.onCreate();

		mApp = App.getInstance();
		
		mMusics = new HashSet<Music>();
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		mMusics.clear();
		
		scanFile(Environment.getExternalStorageDirectory());
		
		MusicDbHelper.clearMusic(this);
		
		MusicDbHelper.insertMusicBulk(this, mMusics);

		stopSelf();
	}
	

	private void scanFile(File file)
	{
        if (DEBUG)
        {
            Log.i(TAG, "scanFile " + file.getPath());
        }
		if (file.isDirectory())
		{
			File[] files = file.listFiles(new FileFilter()
			{
				@Override
				public boolean accept(File pathname)
				{
					if ((mApp != null && mApp.isContainInFilterPath(pathname)) || pathname.getName().startsWith("."))
					{
						return false;
					}
					if (pathname.isDirectory())
					{
						return true;
					}
					else
					{
						for (int i = 0; i < exts.length; ++i)
						{
							if (pathname.getName().toLowerCase().endsWith(exts[i]))
							{
								return true;
							}
						}
					}
					return false;
				}
			});
			if (files == null || files.length == 0)
			{
				return;
			}
			for (int i = 0; i < files.length; ++i)
			{
				scanFile(files[i]);
			}
		}
		else
		{
			handleFile(file);
		}
	}
	
	private void handleFile(File file)
	{
        if (DEBUG)
        {
            Log.i(TAG, "handleFile " + file.getPath());
        }
		Music music;
		try
		{
			music = ParseMusicFile.parse(file);
		} 
		catch (CannotReadException | IOException | TagException
				| InvalidAudioFrameException e)
		{
			return;
		}

		if (music.getDuration() < mApp.getScanMinDuration())
		{
			return;
		}
		
		mMusics.add(music);
	}
	
}
