package com.willing.android.xyz.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.willing.android.xyz.R;
import com.willing.android.xyz.activity.CategoryItemActivity;
import com.willing.android.xyz.adapter.CategoryAdapter;
import com.willing.android.xyz.asynctask.CursorLoadCallback;
import com.willing.android.xyz.asynctask.DbLoader;
import com.willing.android.xyz.utils.CategoryUtils;
import com.willing.android.xyz.utils.MusicDbHelper;

import java.util.ArrayList;

public class CategoryFragment extends BaseFragment
{
	private static final int	LOADER_ID	= 3;
	private ListView mCategoryListView;
	private CategoryAdapter mAdapter;
	private TextView mNewCategory;

    private ActionMode mActionMode;


    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View view =  inflater.inflate(R.layout.fragment_category, container, false);

		initView(view);
		setupListener();

//		getLoaderManager().initLoader(LOADER_ID, null, this);

		return view;
	}


	private void initView(View view)
	{
		mCategoryListView = (ListView)view.findViewById(R.id.lv_playlist);
		mNewCategory = (TextView) view.findViewById(R.id.bt_new_category);


        String[] from = new String[]{
            MusicDbHelper.CATEGORY_NAME,
            MusicDbHelper.COUNT
        };
        int[] to = new int[]{
            R.id.tv_category_name,
            R.id.tv_category_num
        };

        mAdapter = new CategoryAdapter(getActivity(), R.layout.catelog_item, null,
                from, to);

        mCategoryListView.setAdapter(mAdapter);

        getLoaderManager().initLoader(LOADER_ID, null,
                new CursorLoadCallback(getActivity(), mAdapter, DbLoader.TYPE_CATEGORY, null));
	}

	private void setupListener()
	{
		mCategoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				TextView tvPlaylistName = (TextView) view.findViewById(R.id.tv_category_name);
				String name = tvPlaylistName.getText().toString();

				Intent intent = new Intent(CategoryFragment.this.getActivity(), CategoryItemActivity.class);
				intent.putExtra(CategoryItemActivity.CATEGORY_NAME, name);
				startActivity(intent);

                getActivity().overridePendingTransition(0, 0);
			}
		});

		mNewCategory.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
				CategoryUtils.showNewCategoryDialog(getActivity());
            }
        });

		mCategoryListView.setMultiChoiceModeListener(new CategoryMultiChoiceModeListener());
		mCategoryListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
	}

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        if (mActionMode != null)
        {
            finishActionMode();

            mActionMode.finish();
        }
    }

    private class CategoryMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener
	{
		@Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu)
        {
            return false;
        }

		@Override
        public void onDestroyActionMode(ActionMode mode)
        {
            finishActionMode();
        }

		@Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {
            mActionMode = mode;

            mode.getMenuInflater().inflate(R.menu.delete, menu);

			mNewCategory.setVisibility(View.GONE);


            ActionBarActivity activity = (ActionBarActivity)getActivity();
            ActionBar actionbar = activity.getSupportActionBar();
            actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

            mAdapter.setActionModeStarted(true);

            mAdapter.notifyDataSetChanged();

            return true;
        }

		@Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item)
        {
            switch (item.getItemId())
            {
            case R.id.delete:

                Cursor cursor = null;

                SparseBooleanArray checked = mCategoryListView.getCheckedItemPositions();
                ArrayList<String> categorys = new ArrayList<String>();
                boolean selected;
                for (int i = 0; i < checked.size(); ++i)
                {
                    selected = checked.get(checked.keyAt(i));
                    if (selected)
                    {
                        cursor = (Cursor) mAdapter.getItem(checked.keyAt(i));
                        categorys.add(cursor.getString(cursor.getColumnIndex(MusicDbHelper.CATEGORY_NAME)));
                    }
                }

                CategoryUtils.showDeleteCategorysDialog(getActivity(), categorys);

                return true;
            }
            return false;
        }

		@Override
        public void onItemCheckedStateChanged(ActionMode mode, int position,
                long id, boolean checked)
        {
            View view = mCategoryListView.getChildAt(position - mCategoryListView.getFirstVisiblePosition());
            CheckBox checkbox = (CheckBox) view.findViewById(R.id.cb_checked);
            checkbox.setChecked(mCategoryListView.isItemChecked(position));
        }
	}

    private void finishActionMode()
    {
        mAdapter.setActionModeStarted(false);

        mAdapter.notifyDataSetChanged();


        ActionBarActivity activity = (ActionBarActivity)getActivity();
        ActionBar actionbar = activity.getSupportActionBar();
        actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mNewCategory.setVisibility(View.VISIBLE);

        mActionMode = null;
    }
}
