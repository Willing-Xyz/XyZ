package com.willing.android.xyz.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.willing.android.xyz.R;
import com.willing.android.xyz.utils.CategoryUtils;
import com.willing.android.xyz.utils.MusicDbHelper;

/**
 * Created by Willing on 2015/12/11 0011.
 */
public class CategoryAdapter extends SimpleCursorAdapter
{
    private Context mContext;
    private LayoutInflater mInflater;

    private boolean mActionModeStarted;

    public CategoryAdapter(Context context, int layout, Cursor c, String[] from,
                          int[] to)
    {
        super(context, layout, c, from, to, 0);

        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        final Cursor cursor = (Cursor) getItem(position);

        ViewHolder holder = null;
        if (convertView == null)
        {
            convertView = mInflater.inflate(R.layout.catelog_item, null);

            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.tv_category_name);
            holder.num = (TextView) convertView.findViewById(R.id.tv_category_num);
            holder.checkbox = (CheckBox) convertView.findViewById(R.id.cb_checked);
            holder.delete = (ImageButton) convertView.findViewById(R.id.ib_delete_category);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }


        final String categoryName = cursor.getString(cursor.getColumnIndex(MusicDbHelper.CATEGORY_NAME));
        holder.name.setText(categoryName);
        holder.num.setText("" + cursor.getInt(cursor.getColumnIndex(MusicDbHelper.COUNT)));

        holder.delete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CategoryUtils.showDeleteCategoryDialog(mContext, categoryName);
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
        TextView num;
        ImageButton delete;
        CheckBox checkbox;
    }
}
