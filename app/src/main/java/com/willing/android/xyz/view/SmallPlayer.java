package com.willing.android.xyz.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.willing.android.xyz.App;
import com.willing.android.xyz.R;
import com.willing.android.xyz.activity.PlayingActivity;
import com.willing.android.xyz.entity.Music;
import com.willing.android.xyz.service.MusicPlayService;
import com.willing.android.xyz.utils.ParseMusicFile;

/**
 * Created by Willing on 2015/12/11 0011.
 */
public class SmallPlayer extends LinearLayout
{
    private ImageButton mAlbumImage;
    private TextView mMusicName;
    private TextView mSinger;
    private ImageButton mStartPause;
    private ImageButton mNext;

    private Context mContext;
    private View mRootview;

    private int mAlbumImageHeight;

    public SmallPlayer(Context context)
    {
        super(context);

        init(context);
    }

    public SmallPlayer(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        init(context);
    }

    public SmallPlayer(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    public void init(Context context)
    {
        mContext = context;
        LayoutInflater inflater = LayoutInflater.from(context);

        mRootview = inflater.inflate(R.layout.small_play, this, true);

        mAlbumImage = (ImageButton) mRootview.findViewById(R.id.ib_small_pic);
        mMusicName = (TextView) mRootview.findViewById(R.id.tv_name);
        mSinger = (TextView) mRootview.findViewById(R.id.tv_singer);
        mStartPause = (ImageButton) mRootview.findViewById(R.id.ib_pause);
        mNext = (ImageButton) mRootview.findViewById(R.id.ib_next);

        setupListener();
    }

    private void setupListener()
    {
        mAlbumImage.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(mContext, PlayingActivity.class);
                mContext.startActivity(intent);
            }
        });

        mStartPause.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MusicPlayService service = App.getInstance().getPlayService();
                if (service.isPlaying())
                {
                    service.pause();
                } else
                {
                    service.start();
                }
            }
        });

        mNext.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MusicPlayService service = App.getInstance().getPlayService();
                service.next();
            }
        });

        mRootview.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {

                mRootview.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                mAlbumImageHeight = mRootview.getHeight();

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mAlbumImageHeight, mAlbumImageHeight);
                mAlbumImage.setLayoutParams(params);

                updateAlbum();

            }
        });
    }

    public void updateState(Music music, boolean isPlaying)
    {

        mMusicName.setText(music.getName());
        mSinger.setText(music.getSinger());

        setPlaying(isPlaying);


        if (mAlbumImageHeight != 0)
        {
            updateAlbum();
        }
    }

    private void updateAlbum()
    {
        if (App.getInstance() != null && App.getInstance().getPlayService() != null)
        {
            Music music = App.getInstance().getPlayService().getPlayingMusic();

            if (music != null)
            {
                Bitmap bitmap = ParseMusicFile.parseAlbumImage(music.getPath(), mAlbumImageHeight);

                if (bitmap != null)
                {
                    mAlbumImage.setImageBitmap(bitmap);

                    return;
                }
            }
        }
        mAlbumImage.setImageResource(R.drawable.album_default);
    }

    public void setPlaying(boolean isPlaying)
    {
        if (isPlaying)
        {
            mStartPause.setImageResource(R.drawable.pause);
        }
        else
        {
            mStartPause.setImageResource(R.drawable.start);
        }
    }
}
