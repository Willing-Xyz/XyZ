package com.willing.android.xyz.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AlertDialog;
import android.util.SparseBooleanArray;
import android.widget.Toast;

import com.willing.android.xyz.R;
import com.willing.android.xyz.entity.Music;

import java.util.ArrayList;

/**
 * Created by Willing on 12/26/2015/026.
 */
public class MusicUtils
{
    public static void showDeleteMusicsDialog(final Context context, final SparseBooleanArray checked,
                                              final CursorAdapter adapter,
                                              boolean isFromCategory, String categoryName)
    {
        Cursor cursor = null;

        ArrayList<Integer> musics = new ArrayList<Integer>();
        boolean selected;
        for (int i = 0; i < checked.size(); ++i)
        {
            selected = checked.get(checked.keyAt(i));
            if (selected)
            {
                cursor = (Cursor) adapter.getItem(checked.keyAt(i));
                musics.add(cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)));
            }


        }

        showDeleteMusicsDialog(context, musics, isFromCategory, categoryName);
    }


    public static void showDeleteMusicDialog(Context context, int music, boolean isFromCategory, String categoryName)
    {
        ArrayList<Integer> musics = new ArrayList<>(1);

        musics.add(music);

        showDeleteMusicsDialog(context, musics, isFromCategory, categoryName);
    }

    private static void showDeleteMusicsDialog(final Context context,
                                               final ArrayList<Integer> musics, final boolean isFromCategory, final String categoryName)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        String title = context.getString(R.string.ensure_delete_song);
        builder.setTitle(title);

        builder.setPositiveButton(R.string.ok_dialog, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (MusicDbHelper.deleteMusics(context, musics))
                {
                    if (isFromCategory)
                    {
                        MusicDbHelper.deleteMusicsFromCategory(context, musics, categoryName);
                    }
                    Toast.makeText(context, R.string.delete_successed, Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(context, R.string.delete_failed, Toast.LENGTH_SHORT).show();
                }

            }
        });
        builder.setNegativeButton(R.string.cancel_dialog, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        if (isFromCategory)
        {
            builder.setNeutralButton(R.string.delete_from_catelog, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    if (MusicDbHelper.deleteMusicsFromCategory(context, musics, categoryName))
                    {
                        Toast.makeText(context, R.string.delete_successed, Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(context, R.string.delete_failed, Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }


        builder.setCancelable(true);
        builder.create().show();
    }


    public static void showMusicInfoDialog(Context context, Music music)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("详细信息");

        builder.setMessage("标题: \t" + music.getName()
                + "\n歌手: \t" + music.getSinger()
                + "\n专辑: \t" + music.getAlbum()
                + "\n时长: \t" + TimeUtils.parseDuration(music.getDuration())
                + "\n路径: \t" + music.getPath()
        );

        builder.setCancelable(true);
        builder.create().show();
    }
}
