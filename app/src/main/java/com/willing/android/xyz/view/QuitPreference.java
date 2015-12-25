package com.willing.android.xyz.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.willing.android.xyz.App;
import com.willing.android.xyz.R;


public class QuitPreference extends Preference
{

	public QuitPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	public View getView(View convertView, ViewGroup parent)
	{
		View view = LayoutInflater.from(getContext()).inflate(R.layout.preference_quit_app, null);

		return view;
	}
	
	@Override
	protected void onClick()
	{
		super.onClick();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setPositiveButton(R.string.ok_dialog, new OnClickListener()
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{

				App.getInstance().quitApp();
			}
		})
		.setNegativeButton(R.string.cancel_dialog, new OnClickListener()
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				 
				
			}
		})
		.setTitle(R.string.sure_quit_app);
		
		builder.create().show();
		
	}
}
