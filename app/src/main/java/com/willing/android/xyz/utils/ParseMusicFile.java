package com.willing.android.xyz.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.willing.android.xyz.entity.Music;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.datatype.Artwork;

import java.io.File;
import java.io.IOException;


public class ParseMusicFile
{
    // 解析音乐文件中的信息，如果抛出异常，则表示返回的Music内部的信息是无效的。
    public static Music parse(File file) throws CannotReadException, IOException, TagException, InvalidAudioFrameException
    {
        Music music = new Music();

        AudioFile audioFile = null;
        try
        {
            audioFile = AudioFileIO.read(file);
        } catch (ReadOnlyFileException e)
        {
            // continue;
        }

        Tag tag = audioFile.getTag();
        if (tag != null)
        {
            try
            {
                music.setName(tag.getFirst(FieldKey.TITLE));
                music.setAlbum(tag.getFirst(FieldKey.ALBUM));
                music.setSinger(tag.getFirst(FieldKey.ARTIST));
            } catch (KeyNotFoundException ex)
            {
                // continue;
            }
        }

        // 获取歌曲的时长
        AudioHeader header = audioFile.getAudioHeader();
        int length = header.getTrackLength();
        music.setDuration(length);

        music.setPath(file.getCanonicalPath());



        return music;
    }

    public static Bitmap parseAlbumImage(String path, int size)
    {
        AudioFile audioFile = null;
        try
        {
            audioFile = AudioFileIO.read(new File(path));
        } catch (ReadOnlyFileException e)
        {
            // continue;
        } catch (Exception e)
        {
            e.printStackTrace();

            return null;
        }

        Tag tag = audioFile.getTag();

        Artwork art = null;
        if (tag != null)
        {
            art = tag.getFirstArtwork();
        }
        byte[] bytes = null;
        if (art != null)
        {
            bytes = art.getBinaryData();
        }
        Bitmap bitmap = null;
        if (bytes != null)
        {
            // 获取原图片尺寸
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inJustDecodeBounds = true;

            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opt);

            int srcHeight = opt.outHeight;

            // 获取适合View的大小
            opt.inJustDecodeBounds = false;
            if (srcHeight > size)
            {
                opt.inSampleSize = srcHeight / size;
            }


            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opt);

//            Log.i("test", "ImageButton width: " + width + ";; src Width: " + srcWidth
//            + ", out width: " + bitmap.getWidth()
//                    + ", opt widht: " + opt.outWidth);
        }

        return bitmap;
    }
}
