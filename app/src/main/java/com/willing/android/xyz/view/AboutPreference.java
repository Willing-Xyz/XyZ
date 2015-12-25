package com.willing.android.xyz.view;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class AboutPreference extends DialogPreference
{

	public AboutPreference(Context context, AttributeSet set)
	{
		super(context, set);

        setPositiveButtonText(android.R.string.ok);
        setDialogTitle("关于");
        setDialogMessage("作者: Willing Xyz\n" + "邮箱: sxswilling@126.com");
        
        setDialogIcon(null);
	}
	
	

}
