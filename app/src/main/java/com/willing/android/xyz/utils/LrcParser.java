package com.willing.android.xyz.utils;

import android.graphics.Paint;

import com.willing.android.xyz.entity.Lyric;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

/**
 * 解析lrc文件
 * Created by Willing on 2015/11/4 0004.
 */
public class LrcParser
{
    private static final String LRC_ALBUM = "al";
    private static final String LRC_ARTIST = "ar";
    private static final String LRC_BY = "by";
    private static final String LRC_OFFSET = "offset";
    private static final String LRC_SOFT = "re";
    private static final String LRC_VERSION = "ve";
    private static final String LRC_TITLE = "ti";
    private static final String LRC_LENGTH = "length";
    private static final String LRC_AUTHOR = "au";
    


    public static Lyric parse(String path, int viewWidth, Paint paint)
    {
    	if (path == null)
    	{
    		return null;
    	}
        File file = new File(path);

        return parse(file, viewWidth, paint);
    }

    public static Lyric parse(File file, int viewWidth, Paint paint)
    {
    	if (!file.exists())
    	{
    		return null;
    	}

        Lyric lyric = new Lyric();

        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), guessEncoding(file)));

            String str = null;
            while ((str = reader.readLine()) != null)
            {
                parseLine(str, lyric, viewWidth, paint);
            }

        } catch (IOException e)
        {
            return null;
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        Collections.sort(lyric.getLrcs());

        return lyric;
    }

    private static void parseLine(String str, Lyric lyric, int viewWidth, Paint paint)
    {
        str = str.trim();

        if (str.length() == 0)
        {
        	return;
        }
  
        int openBraceIndex = str.indexOf('[');
        int closeBraceIndex = str.indexOf(']');
        
        if (openBraceIndex == -1 || closeBraceIndex == -1 || openBraceIndex >= closeBraceIndex)
        {
        	return;
        }

        // 如果方括号后面的第一个字符为数字，表示该行为歌词行
        if (Character.isDigit(str.charAt(openBraceIndex + 1)))
        {
            ArrayList<Integer> times = new ArrayList<>();
            int dataIndex = 0;
            do
            {
                dataIndex = closeBraceIndex + 1;
                String strTime = str.substring(openBraceIndex + 1, closeBraceIndex).trim();
                int intTime = parseTime(strTime);
                if (intTime == -1)
                {
                    continue;
                }
                times.add(intTime);

                openBraceIndex = str.indexOf('[', openBraceIndex + 1);
                closeBraceIndex = str.indexOf(']', closeBraceIndex + 1);

            } while (openBraceIndex != -1 && closeBraceIndex != -1);


            String lrc = str.substring(dataIndex).trim();

            int startIndex = 0;
            do
            {
                int count = paint.breakText(lrc, startIndex, lrc.length(), true, viewWidth, null);

                for (int i = 0; i < times.size(); ++i)
                {
                    lyric.addLrc(times.get(i), lrc.substring(startIndex, startIndex + count));
                }
                startIndex = startIndex + count;
                if (startIndex >= lrc.length())
                {
                    break;
                }

            } while (true);

        }
        else
        {
            String tag = str.substring(openBraceIndex + 1, closeBraceIndex).trim();
            String val = str.substring(closeBraceIndex + 1).trim();
            switch (tag)
            {
                case LRC_ALBUM:
                    lyric.setAlbum(val);
                    break;
                case LRC_ARTIST:
                    lyric.setArtist(val);
                    break;
                case LRC_AUTHOR:
                    lyric.setAuthor(val);
                    break;
                case LRC_BY:
                    lyric.setBy(val);
                    break;
                case LRC_LENGTH:
                    int length = 0;
                    try
                    {
                        length = Integer.parseInt(val);
                    }
                    catch (NumberFormatException ex)
                    {
                        ex.printStackTrace();
                    }
                    lyric.setLength(length);
                    break;
                case LRC_OFFSET:
                    int offset = 0;
                    try
                    {
                        offset = Integer.parseInt(val);
                    }
                    catch (NumberFormatException ex)
                    {
                        ex.printStackTrace();
                    }
                    lyric.setOffset(offset);
                    break;
                case LRC_SOFT:
                    lyric.setSoft(val);
                    break;
                case LRC_TITLE:
                    lyric.setTitle(val);
                    break;
                case LRC_VERSION:
                    lyric.setVersion(val);
                    break;
                default:
                    break;
            }
        }
    }

    private static int parseTime(String str)
    {
        int time = -1;
        try
        {
            int mm = Integer.parseInt(str.substring(0, 2));
            int ss = Integer.parseInt(str.substring(3, 5));
            int xx = Integer.parseInt(str.substring(6, 8));

            time = mm * 60 * 100 + ss * 100 + xx;
        }
        catch (NumberFormatException ex)
        {
            ex.printStackTrace();
        }

        return time;
    }
    
    private static String guessEncoding(File file)
    {
    	InputStream in = null;
    	
    	try
		{
			in = new FileInputStream(file);
			
			byte[] b = new byte[3];
			in.read(b);
		
			if (b[0] == -17 && b[1] == -69 && b[2] == -65) 
			{
				return "UTF-8";
			}
			else
			{
				return "GBK";
			}
			
		} catch (IOException e)
		{
			e.printStackTrace();
		}
    	finally
    	{
    		if (in != null)
    		{
    			try
				{
					in.close();
				} catch (IOException e)
				{
				 
					e.printStackTrace();
				}
    		}
    	}
    	
    	return "GBK";
    }
}
