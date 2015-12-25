package com.willing.android.xyz.activity;

import android.content.Intent;
import android.os.Bundle;

import com.willing.android.xyz.R;
import com.willing.android.xyz.fragment.SettingsFragment;


public class SettingsActivity extends BaseActivity
{

	public static final String MIN_DURATION = "minDuration";
	

	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(
				R.color.actionbar));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
		
 
	}
	
	@Override
	public Intent getParentActivityIntent()
	{
		return getIntent();
	}
}
