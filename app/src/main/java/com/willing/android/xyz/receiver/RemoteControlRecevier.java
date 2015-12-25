package com.willing.android.xyz.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.willing.android.xyz.App;
import com.willing.android.xyz.service.MusicPlayService;

/**
 * Created by Willing on 12/26/2015/026.
 */
public class RemoteControlRecevier extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction()))
        {
            KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

            if (event == null)
            {
                return;
            }

            MusicPlayService service;
            if (App.getInstance() != null && App.getInstance().getPlayService() != null)
            {
                service = App.getInstance().getPlayService();
            }
            else
            {
                return;
            }

            switch (event.getKeyCode())
            {
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    service.start();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    service.pause();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    if (service.isPlaying())
                    {
                        service.pause();
                    } else
                    {
                        service.start();
                    }
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    service.next();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    service.pre();
                    break;
                case KeyEvent.KEYCODE_MEDIA_REWIND:
                    service.seekTo(0);
                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    service.pause();
                    service.seekTo(0);
                    break;
            }


        }
        if (isOrderedBroadcast())
        {
            abortBroadcast();
        }
    }
}
