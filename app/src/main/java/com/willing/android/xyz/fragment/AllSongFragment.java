package com.willing.android.xyz.fragment;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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

import com.willing.android.xyz.App;
import com.willing.android.xyz.R;
import com.willing.android.xyz.adapter.SongAdapter;
import com.willing.android.xyz.asynctask.CursorLoadCallback;
import com.willing.android.xyz.asynctask.DbLoader;
import com.willing.android.xyz.entity.Music;
import com.willing.android.xyz.service.MusicPlayService;
import com.willing.android.xyz.utils.CategoryUtils;
import com.willing.android.xyz.utils.MusicDbHelper;
import com.willing.android.xyz.utils.MusicUtils;

public class AllSongFragment extends BaseFragment
{
	
	private ListView mAllSongListView;
	private SongAdapter mListAdapter;
	private TextView mPlayAllSong;

    private ActionMode mActionMode;

	private static final int	LOADER_ID	= 1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_allsong, container,
				false);

		initView(view);
		setupListener();

		return view;
	}

	private void initView(View view)
	{
		mAllSongListView = (ListView) view.findViewById(R.id.lv_allsong);
		mPlayAllSong = (TextView) view.findViewById(R.id.tv_play_allsong);

		mListAdapter = new SongAdapter(getActivity());

		mAllSongListView.setAdapter(mListAdapter);

        getLoaderManager().initLoader(LOADER_ID, null,
                new CursorLoadCallback(getActivity(), mListAdapter, DbLoader.TYPE_ALL_SONG, null));
	}

	private void setupListener()
	{

		mAllSongListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{

				Cursor cursor = (Cursor)mListAdapter.getItem(position);

				Music music = MusicDbHelper.convertCursorToMusic(cursor);

                MusicPlayService service = App.getInstance().getPlayService();
                if (service != null)
                {
                   service.addToPlayList(music, true);
                }
			}
		});

		mPlayAllSong.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				PlayAllThread thread = new PlayAllThread();

                thread.start();
			}
		});


		mAllSongListView.setMultiChoiceModeListener(new AllSongMultiChoiceModeListener());
		mAllSongListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
	}

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        if (getActivity().isChangingConfigurations())
        {
            finishActionMode();

            if (mActionMode != null)
            {
                mActionMode.finish();
            }
        }
    }

    private static class PlayAllThread extends Thread
	{

        @Override
		public void run()
		{
			MusicPlayService service = App.getInstance().getPlayService();
			if (service != null)
			{
                Context context = App.getInstance();
                if (context != null)
                {
                    Cursor cursor = MusicDbHelper.queryAllSong(context);
                    service.addToPlayList(cursor, true);
                }
			}
		}
    }
    private class AllSongMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener
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

            mode.getMenuInflater().inflate(R.menu.song, menu);

            mPlayAllSong.setVisibility(View.GONE);

            AppCompatActivity activity = (AppCompatActivity) getActivity();
            ActionBar actionbar = activity.getSupportActionBar();
            actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionbar.show();
            mListAdapter.setActionModeStarted(true);

            mListAdapter.notifyDataSetChanged();

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item)
        {
            switch (item.getItemId())
            {
            case R.id.delete:
                MusicUtils.showDeleteMusicsDialog(getActivity(), mAllSongListView.getCheckedItemPositions(), mListAdapter,
                        false, null);

                return true;
            case R.id.add_to_catelog:
                CategoryUtils.showAddToCategorysDialog(getActivity(), mAllSongListView.getCheckedItemPositions(),
                        mListAdapter, false, null);

                return true;
            }
            return false;
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position,
                long id, boolean checked)
        {
            View view = mAllSongListView.getChildAt(position - mAllSongListView.getFirstVisiblePosition());
            CheckBox checkbox = (CheckBox) view.findViewById(R.id.cb_checked);
            checkbox.setChecked(mAllSongListView.isItemChecked(position));
        }

    }

    private void finishActionMode()
    {
        mListAdapter.setActionModeStarted(false);

        mListAdapter.notifyDataSetChanged();

        AppCompatActivity activity = (AppCompatActivity)getActivity();
        ActionBar actionbar = activity.getSupportActionBar();
        actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mPlayAllSong.setVisibility(View.VISIBLE);

        mActionMode = null;
    }
}
