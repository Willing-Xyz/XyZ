package com.willing.android.xyz.adapter;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.willing.android.xyz.R;
import com.willing.android.xyz.entity.Music;
import com.willing.android.xyz.utils.CategoryUtils;
import com.willing.android.xyz.utils.MusicDbHelper;
import com.willing.android.xyz.utils.MusicUtils;

/**
 * 
 * @author Willing
 *
 */
public class SongAdapter extends SimpleCursorAdapter
{
    private final boolean mIsFromCategory;
    private final String mCategoryName;

    private Context mContext;
	private LayoutInflater mInflater;
	
	private boolean mActionModeStarted;

	private static int layout = R.layout.song_item;
	private static String[] from =
			{
					MusicDbHelper.NAME,
					MusicDbHelper.SINGER,
					MusicDbHelper.ALBUM
			};
	private static int[] to =
			{
					R.id.tv_name,
					R.id.tv_singer,
					R.id.tv_album
			};

    public SongAdapter(Context context)
    {
        this(context, false, null);
    }

	public SongAdapter(Context context, boolean isCategory, String categoryName)
	{
		super(context, layout, null, from, to, 0);
		
		mContext = context;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mIsFromCategory = isCategory;
        mCategoryName = categoryName;
	}
	
	public void setActionModeStarted(boolean started)
	{
		mActionModeStarted = started;
	}
	
	public boolean isActionModeStarted()
	{
		return mActionModeStarted;
	}
	
	
	@Override
	public View getView(final int position, View convertView, ViewGroup viewGroup)
	{
		ViewHolder holder = null;
		if (convertView == null)
		{
			convertView = mInflater.inflate(R.layout.song_item, null);
			
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.tv_name);
			holder.singer = (TextView) convertView.findViewById(R.id.tv_singer);
			holder.album = (TextView) convertView.findViewById(R.id.tv_album);
			holder.options = (ImageButton) convertView.findViewById(R.id.ib_options);
			holder.checkbox = (CheckBox) convertView.findViewById(R.id.cb_checked);
			holder.optionsPanel = convertView.findViewById(R.id.options_panel);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}

		final Cursor cursor = (Cursor) getItem(position);
        final String musicName = cursor.getString(cursor.getColumnIndex(MusicDbHelper.NAME));
		holder.name.setText(musicName);
		holder.singer.setText(cursor.getString(cursor.getColumnIndex(MusicDbHelper.SINGER)));
		holder.album.setText(cursor.getString(cursor.getColumnIndex(MusicDbHelper.ALBUM)));
		
		holder.optionsPanel.setVisibility(View.GONE);

        final int musicId = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
        final Music music = MusicDbHelper.convertCursorToMusic(cursor);

		final ViewHolder finalHolder = holder;
		holder.options.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (finalHolder.optionsPanel.getVisibility() == View.GONE)
                {
                    finalHolder.optionsPanel.setVisibility(View.VISIBLE);

                    View addToCatelog = finalHolder.optionsPanel.findViewById(R.id.add_to_catelog);
                    View delete = finalHolder.optionsPanel.findViewById(R.id.delete_song);
                    View info = finalHolder.optionsPanel.findViewById(R.id.song_info);


                    addToCatelog.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            CategoryUtils.showAddToCategoryDialog(mContext, musicId, mIsFromCategory, mCategoryName);
                        }
                    });

                    delete.setOnClickListener(new View.OnClickListener()
                    {

                        @Override
                        public void onClick(View v)
                        {
                            MusicUtils.showDeleteMusicDialog(mContext, musicId, mIsFromCategory, mCategoryName);
                        }
                    });

                    info.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {

                            MusicUtils.showMusicInfoDialog(mContext, music);
                        }
                    });

                } else
                {
                    finalHolder.optionsPanel.setVisibility(View.GONE);
                }

            }
        });

		
		if (isActionModeStarted())
		{
			if (holder.checkbox.getVisibility() == View.GONE)
			{
				holder.checkbox.setVisibility(View.VISIBLE);
			}
			ListView listView = (ListView) viewGroup;
			holder.checkbox.setChecked(listView.isItemChecked(position));
		}
		else
		{
			if (holder.checkbox.getVisibility() == View.VISIBLE)
			{
				holder.checkbox.setVisibility(View.GONE);
			}
		}

		return convertView;
	}






    private static class ViewHolder
	{
		TextView name;
		TextView singer;
		TextView album;
		ImageButton options;
		CheckBox checkbox;
		View optionsPanel;
	}
 
}
