package com.willing.android.xyz.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.willing.android.xyz.entity.Music;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by Willing on 2015/12/9 0009.
 */
public class MusicDbHelper extends SQLiteOpenHelper
{
    private static final String MUSIC_DBNAME = "music_dbName";

    private static final String CATEGORY_TABLE_NAME = "category";
    private static final String CATEGORYS_TABLE_NAME = "categorys";
    private static final String MUSIC_TABLE_NAME = "music";

    private static final int VERSION = 1;

    public static final String NAME = "name";
    public static final String SINGER = "singer";
    public static final String ALBUM = "album";
    public static final String DURATION = "duration";
    public static final String PATH = "path";

    public static final String COUNT = "count";

    public static final String CATEGORY_NAME = "name";

    public static final String CATEGORY_ID = "category_id";
    public static final String MUSIC_ID = "music_id";



    private static final boolean DEBUG = true;
    private static final String TAG = "MusicDbHelper";

    private static final Uri URI_ALL = Uri.parse("content://com.willing.android.xyz/");
    // 歌曲本身相关
    private static final Uri URI_MUSIC = Uri.parse("content://com.willing.android.xyz/music/");
    // 包括播放列表和item
    private static final Uri URI_CATEGORYALL = Uri.parse("content://com.willing.android.xyz/category/");
    // 播放列表本身，不包括item
    private static final Uri URI_CATEGORY = Uri.parse("content://com.willing.android.xyz/category/category/");


    public MusicDbHelper(Context context)
    {
        super(context, MUSIC_DBNAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String	SQL_CREATE_MUSIC_TABLE	=
                "CREATE TABLE " +
                        MUSIC_TABLE_NAME + "( " +
                        BaseColumns._ID + " INTEGER PRIMARY KEY , " +
                        NAME + " TEXT , " +
                        SINGER + " TEXT , " +
                        ALBUM + " TEXT , " +
                        DURATION + " INTEGER , " +
                        PATH + " TEXT " +  "UNIQUE" + ");";

        String SQL_CREATE_CATEGORY_TABLE =
                "CREATE TABLE " +
                        CATEGORY_TABLE_NAME + " ( " +
                        BaseColumns._ID + " INTEGER PRIMARY KEY , " +
                        CATEGORY_NAME + " TEXT UNIQUE);";


        String SQL_CREATE_CATEGORYS_TABLE =
                "CREATE TABLE " +
                        CATEGORYS_TABLE_NAME + " ( " +
                        BaseColumns._ID + " INTEGER PRIMARY KEY , " +
                        CATEGORY_ID + " INTEGER , " +
                        MUSIC_ID + " INTEGER);";

        db.execSQL(SQL_CREATE_MUSIC_TABLE);
        db.execSQL(SQL_CREATE_CATEGORY_TABLE);
        db.execSQL(SQL_CREATE_CATEGORYS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
    }

    // 清除数据库中的歌曲信息
    public static void clearMusic(Context context)
    {
        SQLiteDatabase db = new MusicDbHelper(context).getWritableDatabase();

        db.beginTransaction();

        db.delete(MusicDbHelper.MUSIC_TABLE_NAME, null, null);

        db.setTransactionSuccessful();
        db.endTransaction();

        context.getContentResolver().notifyChange(URI_MUSIC, null);

        if (DEBUG)
        {
            Log.i(TAG, "clear Music From Db");
        }

        db.close();
    }

    // 查询所有歌曲
    public static Cursor queryAllSong(Context context)
    {
        MusicDbHelper helper = new MusicDbHelper(context);

        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.query(MusicDbHelper.MUSIC_TABLE_NAME,
                new String[]{BaseColumns._ID, NAME, SINGER, ALBUM, DURATION, PATH},
                null, null, null, null, NAME);

        cursor.setNotificationUri(context.getContentResolver(), URI_MUSIC);

        return cursor;
    }


    public static long insertMusic(Context context, Music music)
    {
        ContentValues values = new ContentValues(5);
        values.put(MusicDbHelper.NAME, music.getName());
        values.put(MusicDbHelper.SINGER, music.getSinger());
        values.put(MusicDbHelper.ALBUM, music.getAlbum());
        values.put(MusicDbHelper.DURATION, music.getDuration());
        values.put(MusicDbHelper.PATH, music.getPath());

        SQLiteDatabase db = new MusicDbHelper(context).getWritableDatabase();

        long num = db.insert(MusicDbHelper.MUSIC_TABLE_NAME, null, values);

        if (num > 0)
        {
            context.getContentResolver().notifyChange(URI_MUSIC, null);
        }

        db.close();

        return num;
    }

    // 批量插入
    public static long insertMusicBulk(Context context, Collection<Music> musics)
    {
        SQLiteDatabase db = new MusicDbHelper(context).getWritableDatabase();

        db.beginTransaction();

        ContentValues values = new ContentValues(5);
        Music music;
        long totalNum = 0;
        for (Iterator<Music> ite = musics.iterator(); ite.hasNext(); )
        {
            music = ite.next();

            values.put(MusicDbHelper.NAME, music.getName());
            values.put(MusicDbHelper.SINGER, music.getSinger());
            values.put(MusicDbHelper.ALBUM, music.getAlbum());
            values.put(MusicDbHelper.DURATION, music.getDuration());
            values.put(MusicDbHelper.PATH, music.getPath());

            totalNum += db.insert(MusicDbHelper.MUSIC_TABLE_NAME, null, values);

            if (DEBUG)
            {
                Log.i(TAG, "insert music " + music.getName() + " : " + music.getPath());
            }
        }

        db.setTransactionSuccessful();
        db.endTransaction();

        if (totalNum > 0)
        {
            context.getContentResolver().notifyChange(URI_MUSIC, null);
        }
        db.close();

        return totalNum;
    }

    // 返回歌手和其所拥有的歌曲
    public static Cursor querySingerAndCount(Context context)
    {
        SQLiteDatabase db = new MusicDbHelper(context).getReadableDatabase();

        String sql = "select " +
                BaseColumns._ID + " , " +
                SINGER + " , count(*) as " + COUNT +
                " from " + MUSIC_TABLE_NAME +
                " group by " + SINGER + " order by " + SINGER + ";";

        Cursor cursor = db.rawQuery(sql, null);

        cursor.setNotificationUri(context.getContentResolver(), URI_MUSIC);

        return cursor;
    }

    public static boolean containCategory(Context context, String name)
    {
        SQLiteDatabase db = new MusicDbHelper(context).getReadableDatabase();

        Cursor cursor = db.query(CATEGORY_TABLE_NAME, new String[]{BaseColumns._ID}, CATEGORY_NAME + "=?",
                new String[]{name}, null, null, null);

        boolean contained = false;
        if (cursor != null && cursor.getCount() > 0)
        {
            contained = true;
        }
        cursor.close();
        db.close();

        return contained;
    }

    public static long insertCategory(Context context, String name)
    {
        SQLiteDatabase db = new MusicDbHelper(context).getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CATEGORY_NAME, name);

        long num = db.insert(CATEGORY_TABLE_NAME, null, values);

        db.close();

        context.getContentResolver().notifyChange(URI_CATEGORY, null);

        return num;
    }

    public static Cursor queryCategoryAndCount(Context context)
    {
        SQLiteDatabase db = new MusicDbHelper(context).getReadableDatabase();

        // 交集
        String interSql = "select " +
                CATEGORY_TABLE_NAME + "." + BaseColumns._ID + " , " +
                CATEGORY_NAME + " , count(*) as " + COUNT +
                " from " + CATEGORY_TABLE_NAME +
                " inner join " + CATEGORYS_TABLE_NAME
                + " on " + CATEGORY_TABLE_NAME + "." + BaseColumns._ID + "=" + CATEGORYS_TABLE_NAME + "." + CATEGORY_ID +
                " group by " + CATEGORY_TABLE_NAME + "." + BaseColumns._ID + ";";

        Cursor interCursor = db.rawQuery(interSql, null);

        // 当Category中没有歌曲时，显示为0首
        String nullSql = "select " +
                CATEGORY_TABLE_NAME + "." + BaseColumns._ID + " , " +
                CATEGORY_NAME +
                " from " + CATEGORY_TABLE_NAME +
                " where " + CATEGORY_TABLE_NAME + "." + BaseColumns._ID + " not in " +
                " (select " + CATEGORY_ID +
                " from " + CATEGORYS_TABLE_NAME + ");"
                ;

        Cursor nullCursor = db.rawQuery(nullSql, null);

        int count = interCursor.getCount() + nullCursor.getCount();
        MatrixCursor maxtrixCursor = new MatrixCursor(new String[]{BaseColumns._ID, CATEGORY_NAME, COUNT},
                count);

        int i = 1;
        for (interCursor.moveToFirst(); !interCursor.isAfterLast(); interCursor.moveToNext())
        {
            maxtrixCursor.addRow(new Object[]{i++,
                    interCursor.getString(interCursor.getColumnIndex(CATEGORY_NAME)),
                    interCursor.getInt(interCursor.getColumnIndex(COUNT))});

            if (DEBUG)
            {
                Log.i(TAG, "inter " + interCursor.getString(interCursor.getColumnIndex(CATEGORY_NAME)));
            }
        }

        for (nullCursor.moveToFirst(); !nullCursor.isAfterLast(); nullCursor.moveToNext())
        {
            maxtrixCursor.addRow(new Object[]{i++, nullCursor.getString(nullCursor.getColumnIndex(CATEGORY_NAME)), 0});

            if (DEBUG)
            {
                Log.i(TAG, "null " + nullCursor.getString(interCursor.getColumnIndex(CATEGORY_NAME)));
            }
        }

        db.close();
        interCursor.close();
        nullCursor.close();

        maxtrixCursor.setNotificationUri(context.getContentResolver(), URI_ALL);

        return maxtrixCursor;
    }

    // 查询特定Category的歌曲
    public static Cursor queryCategoryItem(Context context, String name)
    {
        SQLiteDatabase db = new MusicDbHelper(context).getReadableDatabase();

        int categoryId = queryCategoryId(db, name);
        if (categoryId == -1)
        {
            return null;
        }

        String categorySql = "select " + MUSIC_ID +
                " from " + CATEGORYS_TABLE_NAME +
                " where " + CATEGORY_ID + "=" + categoryId;

        String sql = "select " +
                BaseColumns._ID + ", "
                + NAME + "," + SINGER + "," + ALBUM + "," + PATH + "," + DURATION +
                " from " + MUSIC_TABLE_NAME + " inner join " + "(" + categorySql + ")" +
                " on " + BaseColumns._ID + "=" + MUSIC_ID + " order by " + NAME;

        Cursor cursor = db.rawQuery(sql, null);

        cursor.setNotificationUri(context.getContentResolver(), URI_ALL);

        return cursor;
    }

    public static Cursor querySingerItem(Context context, String name)
    {
        SQLiteDatabase db = new MusicDbHelper(context).getReadableDatabase();

        Cursor cursor = db.query(MUSIC_TABLE_NAME, new String[]{BaseColumns._ID,
                NAME, SINGER, ALBUM, DURATION, PATH}, SINGER + "=?", new String[]{name},
                null, null, NAME);

        cursor.setNotificationUri(context.getContentResolver(), URI_MUSIC);

        return cursor;
    }

    public static Music convertCursorToMusic(Cursor cursor)
    {
        Music music = new Music();
        music.setAlbum(cursor.getString(cursor.getColumnIndex(MusicDbHelper.ALBUM)));
        music.setSinger(cursor.getString(cursor.getColumnIndex(MusicDbHelper.SINGER)));
        music.setPath(cursor.getString(cursor.getColumnIndex(MusicDbHelper.PATH)));
        music.setName(cursor.getString(cursor.getColumnIndex(MusicDbHelper.NAME)));
        music.setDuration(cursor.getInt(cursor.getColumnIndex(MusicDbHelper.DURATION)));

        return music;
    }

    public static boolean deleteCategory(Context context, String categoryName)
    {
        SQLiteDatabase db = new MusicDbHelper(context).getWritableDatabase();

        int categoryId = queryCategoryId(db, categoryName);
        if (categoryId == -1)
        {
            return false;
        }

        int x = db.delete(CATEGORY_TABLE_NAME, BaseColumns._ID + "=?", new String[]{categoryId + ""});
        db.delete(CATEGORYS_TABLE_NAME, CATEGORY_ID + "=?", new String[]{categoryId + ""});

        db.close();

        if (x > 0)
        {
            context.getContentResolver().notifyChange(URI_CATEGORY, null);
        }
        return x > 0;
    }

    private static int queryCategoryId(SQLiteDatabase db, String categoryName)
    {
        Cursor categoryIdCur = db.query(CATEGORY_TABLE_NAME, new String[]{BaseColumns._ID}, CATEGORY_NAME + "=?", new String[]{categoryName}, null, null, null);

        int categoryId = -1;
        if (categoryIdCur != null && categoryIdCur.getCount() > 0)
        {
            categoryIdCur.moveToFirst();
            categoryId = categoryIdCur.getInt(categoryIdCur.getColumnIndex(BaseColumns._ID));
        }

        return categoryId;
    }

    public static String[] queryCategory(Context context)
    {
        SQLiteDatabase db = new MusicDbHelper(context).getReadableDatabase();

        Cursor cursor = db.query(CATEGORY_TABLE_NAME, new String[]{CATEGORY_NAME}, null, null, null, null, null);

        String[] categorys = new String[cursor.getCount()];

        cursor.moveToFirst();
        for (int i = 0; !cursor.isAfterLast(); cursor.moveToNext(), ++i)
        {
            categorys[i] = cursor.getString(cursor.getColumnIndex(CATEGORY_NAME));
        }

        cursor.close();
        db.close();

        return categorys;
    }

    public static boolean insertMusicsToCategory(Context context, String categoryName, ArrayList<Integer> musics)
    {
        SQLiteDatabase db = new MusicDbHelper(context).getWritableDatabase();

        int categoryId = queryCategoryId(db, categoryName);
        if (categoryId == -1)
        {
            return false;
        }

        long num = 0;
        ContentValues value;
        for (int i = 0; i < musics.size(); ++i)
        {
            value = new ContentValues(2);
            value.put(CATEGORY_ID, categoryId);
            value.put(MUSIC_ID, musics.get(i));

            Cursor existed = db.query(CATEGORYS_TABLE_NAME, new String[]{BaseColumns._ID},
                    CATEGORY_ID + "=? AND " + MUSIC_ID + "=?", new String[]{categoryId + "",
                            musics.get(i) + ""}, null, null, null);

            if (existed.getCount() <= 0)
            {
                num += db.insert(CATEGORYS_TABLE_NAME, null, value);
            }
        }

        if (num > 0)
        {
            context.getContentResolver().notifyChange(URI_CATEGORYALL, null);
        }
        return num > 0;
    }

    public static boolean deleteMusics(Context context, ArrayList<Integer> musics)
    {
        SQLiteDatabase db = new MusicDbHelper(context).getWritableDatabase();

        int x = 0;
        boolean fileDeleted = false;
        File file;
        for (int i = 0; i < musics.size(); ++i)
        {
            Cursor cursor = db.query(MUSIC_TABLE_NAME, new String[]{PATH}, BaseColumns._ID + "=?",
                    new String[]{musics.get(i) + ""}, null, null, null);
            if (cursor != null && cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                file = new File(cursor.getString(cursor.getColumnIndex(PATH)));
                fileDeleted |= file.delete();
            }
            x += db.delete(MUSIC_TABLE_NAME, BaseColumns._ID + "=?", new String[]{musics.get(i) + ""});
        }

        if (x > 0 && fileDeleted)
        {
            context.getContentResolver().notifyChange(URI_ALL, null);
        }
        return x > 0 && fileDeleted;
    }

    public static boolean deleteMusicsFromCategory(Context context, ArrayList<Integer> musics, String categoryName)
    {
        SQLiteDatabase db = new MusicDbHelper(context).getWritableDatabase();

        int categoryId = queryCategoryId(db, categoryName);
        if (categoryId == -1)
        {
            return false;
        }

        int x = 0;
        for (int i = 0; i < musics.size(); ++i)
        {
            x += db.delete(CATEGORYS_TABLE_NAME, CATEGORY_ID + "=? AND " + MUSIC_ID + "=?",
                    new String[]{"" + categoryId, "" + musics.get(i)});
        }

        if (x > 0)
        {
            context.getContentResolver().notifyChange(URI_CATEGORYALL, null);
        }
        return x > 0;
    }


    public static boolean deleteCategorys(Context context, ArrayList<String> categorys)
    {
        SQLiteDatabase db = new MusicDbHelper(context).getWritableDatabase();

        int categoryId = -1;
        int x = 0;
        for (int i = 0; i < categorys.size(); ++i)
        {
            categoryId = queryCategoryId(db, categorys.get(i));
            if (categoryId == -1)
            {
                return false;
            }

            x += db.delete(CATEGORY_TABLE_NAME, BaseColumns._ID + "=?", new String[]{categoryId + ""});
            db.delete(CATEGORYS_TABLE_NAME, CATEGORY_ID + "=?", new String[]{categoryId + ""});
        }

        db.close();

        if (x > 0)
        {
            context.getContentResolver().notifyChange(URI_CATEGORY, null);
        }
        return x > 0;
    }
}


