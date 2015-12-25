package com.willing.android.xyz.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.willing.android.xyz.R;
import com.willing.android.xyz.activity.SingerItemActivity;
import com.willing.android.xyz.asynctask.CursorLoadCallback;
import com.willing.android.xyz.asynctask.DbLoader;
import com.willing.android.xyz.utils.MusicDbHelper;

import java.util.List;
import java.util.Map;

public class SingerFragment extends BaseFragment
{
	private static final int	LOADER_ID	= 1;

	private ListView mSingerListView;
    private CursorAdapter mListViewAdapter;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_singer, container, false);

		initView(view);
		setupListener();

		return view;
	}

	private void initView(View view)
	{
		mSingerListView = (ListView) view.findViewById(R.id.lv_singer);

        String[] from = new String[]{
                MusicDbHelper.SINGER,
                MusicDbHelper.COUNT
        };
        int[] to = new int[]{
                R.id.tv_singer_name,
                R.id.tv_singer_num
        };
        mListViewAdapter = new SimpleCursorAdapter(getActivity(), R.layout.singer_item, null, from, to, 0);
        mSingerListView.setAdapter(mListViewAdapter);

        getLoaderManager().initLoader(LOADER_ID, null,
                new CursorLoadCallback(getActivity(), mListViewAdapter, DbLoader.TYPE_SINGER, null));
	}

	private void setListViewAdapter(List<Map<String, String>> data)
	{
//		String[] from = new String[]{
//			"artist",
//			"count"
//		};
//		int[] to = new int[]{
//			R.id.tv_singer_name,
//			R.id.tv_singer_num
//		};
//
//		if (data == null)
//		{
//			data = new ArrayList<Map<String, String>>();
//		}
//
//		SimpleAdapter adapter = new SingerAdapter(getActivity(), data, R.layout.singer_item, from, to);
//
//		mSingerListView.setAdapter(adapter);
	}

	private void setupListener()
	{
		mSingerListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				Intent intent = new Intent(SingerFragment.this.getActivity(), SingerItemActivity.class);

				TextView tmp = (TextView) view.findViewById(R.id.tv_singer_name);
				String name = tmp.getText().toString();
				intent.putExtra(SingerItemActivity.SINGER_NAME, name);

				startActivity(intent);

				getActivity().overridePendingTransition(0, 0);
			}
		});
	}
//



}
