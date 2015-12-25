package com.willing.android.xyz.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.v4.widget.CursorAdapter;
import android.util.SparseBooleanArray;
import android.widget.EditText;
import android.widget.Toast;

import com.willing.android.xyz.R;

import java.util.ArrayList;

/**
 * Created by Willing on 2015/12/12 0012.
 */
public class CategoryUtils
{
    public static void showNewCategoryDialog(final Context context)
    {
        AlertDialog.Builder build = new AlertDialog.Builder(context);
        build.setTitle(R.string.new_playlist_dialog);
        final EditText edit = new EditText(context);
        build.setView(edit);
        build.setPositiveButton(R.string.ok_dialog, new DialogInterface.OnClickListener()
        {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String name = edit.getText().toString().trim();
                if ("".equals(name))
                {
                    Toast.makeText(context, R.string.playlist_name_is_empty, Toast.LENGTH_LONG).show();
                    return;
                }

                if (!MusicDbHelper.containCategory(context, name))
                {
                    MusicDbHelper.insertCategory(context, name);
                }
                else
                {
                    Toast.makeText(context, R.string.new_playlist_same, Toast.LENGTH_SHORT).show();
                }
            }
        });
        build.setNegativeButton(R.string.cancel_dialog, new DialogInterface.OnClickListener()
        {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }

        });
        build.setCancelable(true);

        build.create().show();
    }

    public static void showAddToCategoryDialog(final Context context, int music, boolean isFromCategory, String categoryName)
    {
        ArrayList<Integer> musics = new ArrayList<>(1);
        musics.add(music);

        showAddToCategoryDialog(context, musics, isFromCategory, categoryName);
    }

    public static void showAddToCategorysDialog(Context context, SparseBooleanArray checked,
                                                CursorAdapter listAdapter, boolean isFromCategory, String categoryName)
    {
        Cursor cursor = null;

        ArrayList<Integer> musics = new ArrayList<Integer>();
        boolean selected;
        for (int i = 0; i < checked.size(); ++i)
        {
            selected = checked.get(checked.keyAt(i));
            if (selected)
            {
                cursor = (Cursor) listAdapter.getItem(checked.keyAt(i));
                musics.add(cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)));
            }

        }

        showAddToCategoryDialog(context, musics, isFromCategory, categoryName);
    }

    private static void showAddToCategoryDialog(final Context context, final ArrayList<Integer> musics,
                                                boolean isFromCategory, String categoryName)
    {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
        builder.setTitle(R.string.add_to_catelog);

        String[] items = MusicDbHelper.queryCategory(context);


        if (isFromCategory)
        {
            if (items != null && items.length > 0)
            {
                String[] tmp = new String[items.length - 1];
                for (int i = 0, j = 0; i < items.length; ++i)
                {
                    if (!items[i].equals(categoryName))
                    {
                        tmp[j++] = items[i];
                    }
                }
                items = tmp;
            }
        }

        if (items == null || items.length == 0)
        {
            items = new String[1];
            items[0] = context.getResources().getString(R.string.new_playlist);

            builder.setItems(items, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    CategoryUtils.showNewCategoryDialog(context);
                }
            });
        }
        else
        {
            final String[] finalItems = items;
            builder.setItems(items, new DialogInterface.OnClickListener()
            {

                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    if (MusicDbHelper.insertMusicsToCategory(context, finalItems[which], musics))
                    {
                        Toast.makeText(context, R.string.add_successed, Toast.LENGTH_SHORT).show();
                    } else
                    {
                        Toast.makeText(context, R.string.add_failed, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        builder.setCancelable(true);
        builder.create().show();
    }

    public static void showDeleteCategoryDialog(final Context context, final String categoryName)
    {
        android.app.AlertDialog.Builder build = new android.app.AlertDialog.Builder(context);
        String title = context.getString(R.string.ensure_delete_category, categoryName);

        build.setTitle(title);
        build.setPositiveButton(R.string.ok_dialog, new DialogInterface.OnClickListener()
        {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String info;
                if (MusicDbHelper.deleteCategory(context, categoryName))
                {
                    info = context.getString(R.string.delete_successed);
                }
                else
                {
                    info = context.getString(R.string.delete_failed);
                }
                Toast.makeText(context, info, Toast.LENGTH_SHORT).show();
            }
        });
        build.setNegativeButton(R.string.cancel_dialog, new DialogInterface.OnClickListener()
        {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }

        });
        build.setCancelable(true);

        build.create().show();
    }

    public static void showDeleteCategorysDialog(final Context context, final ArrayList<String> categorys)
    {
        android.app.AlertDialog.Builder build = new android.app.AlertDialog.Builder(context);
        String title = context.getString(R.string.ensure_delete);

        build.setTitle(title);
        build.setPositiveButton(R.string.ok_dialog, new DialogInterface.OnClickListener()
        {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String info;
                if (MusicDbHelper.deleteCategorys(context, categorys))
                {
                    info = context.getString(R.string.delete_successed);
                }
                else
                {
                    info = context.getString(R.string.delete_failed);
                }
                Toast.makeText(context, info, Toast.LENGTH_SHORT).show();
            }
        });
        build.setNegativeButton(R.string.cancel_dialog, new DialogInterface.OnClickListener()
        {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }

        });
        build.setCancelable(true);

        build.create().show();
    }
}
