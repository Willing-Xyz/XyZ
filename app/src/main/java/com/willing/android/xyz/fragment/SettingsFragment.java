package com.willing.android.xyz.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.willing.android.xyz.App;
import com.willing.android.xyz.R;
import com.willing.android.xyz.activity.SettingsActivity;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
	private SharedPreferences	mPreferences;
	private String	mMinDurationSummary;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());


		EditTextPreference minDuration = (EditTextPreference) findPreference(SettingsActivity.MIN_DURATION);
		String strMinDuration = mPreferences.getString(SettingsActivity.MIN_DURATION, "30");
		int intMinDuration = 30;
		try
		{
			intMinDuration = Integer.parseInt(strMinDuration);
		}
		catch (NumberFormatException ex)
		{

		}
		minDuration.setText(String.valueOf(intMinDuration));
		mMinDurationSummary = "过滤掉" + intMinDuration + "秒以下的歌曲";

	}

	@Override
	public void onResume()
	{
		super.onResume();

		EditTextPreference minDuration = (EditTextPreference) findPreference(SettingsActivity.MIN_DURATION);
		minDuration.setSummary(mMinDurationSummary);

		mPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause()
	{
		super.onPause();

		mPreferences.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key)
	{
		if (key.equals(SettingsActivity.MIN_DURATION))
		{
			EditTextPreference minDuration = (EditTextPreference) findPreference(SettingsActivity.MIN_DURATION);
			String strMinDuration = mPreferences.getString(SettingsActivity.MIN_DURATION, "30");
			int intMinDuration = 30;
			try
			{
				intMinDuration = Integer.parseInt(strMinDuration);
			}
			catch (NumberFormatException ex)
			{
				Toast.makeText(getActivity(), R.string.mustbe_input_digit, Toast.LENGTH_SHORT).show();
			}
			mMinDurationSummary = "过滤掉" + intMinDuration + "秒以下的歌曲";
			minDuration.setSummary(mMinDurationSummary);

			App.getInstance().setScanMinDuration(intMinDuration);
		}

	}
}
