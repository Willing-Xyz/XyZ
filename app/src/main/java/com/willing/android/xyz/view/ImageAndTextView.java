package com.willing.android.xyz.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.willing.android.xyz.R;

public class ImageAndTextView extends LinearLayout
{
	private static final String namespace = "http://schemas.android.com/apk/res/android";


	public ImageAndTextView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		
		int imageRes = attrs.getAttributeResourceValue(namespace, "src", R.mipmap.ic_launcher);
		int textRes = attrs.getAttributeResourceValue(namespace, "text", R.string.unknown);
		
		View view = View.inflate(getContext(), R.layout.image_and_text_layout, this);
		ImageButton image = (ImageButton) view.findViewById(R.id.image);
		TextView text = (TextView) view.findViewById(R.id.text);
		
		image.setImageResource(imageRes);
		text.setText(textRes);
	}

	public ImageAndTextView(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}
	
}
