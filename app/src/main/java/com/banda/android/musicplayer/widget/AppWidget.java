package com.banda.android.musicplayer.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;

import com.banda.android.musicplayer.R;

import java.io.IOException;

public class AppWidget extends AppWidgetProvider {
    private static final MediaPlayer mediaPlayer=new MediaPlayer();
    private static boolean play=true;
    private static boolean isplaying=false;


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        context.startService(new Intent(context, MusicWidgetIntentService.class));
    }
    @Override
    public void onReceive(@NonNull final Context context, @NonNull Intent intent) {
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,
                        AppWidget.class));
                for (int appWidgetId : appWidgetIds) {
                    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
                    views.setImageViewResource(R.id.play,R.drawable.ic_play_circle_filled_48px);
                    play=true;
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
            }
        });

        if (intent.getAction().equals(MusicWidgetIntentService.PLAY)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,
                    AppWidget.class));
            for (int appWidgetId : appWidgetIds) {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
                if(play)
                {
                    if(!isplaying) {
                        playSong(intent.getData().toString());
                        isplaying=!isplaying;
                    }
                    else if(isplaying)
                    {
                        mediaPlayer.start();
                    }
                    views.setImageViewResource(R.id.play,R.drawable.ic_pause_circle_filled_48px);
                    play=!play;
                }
                else if(!play)
                {
                    mediaPlayer.pause();
                    views.setImageViewResource(R.id.play,R.drawable.ic_play_circle_filled_48px);
                    play=!play;

                }
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }
        else if (intent.getAction().equals(MusicWidgetIntentService.NEXT))
        {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,
                    AppWidget.class));
            for (int appWidgetId : appWidgetIds) {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
                views.setImageViewResource(R.id.play,R.drawable.ic_pause_circle_filled_48px);
                play=false;
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
            playSong(intent.getData().toString());
        }
        else if (intent.getAction().equals(MusicWidgetIntentService.PREVIOUS))
        {
            Log.v("Previous",intent.getData().toString());
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,
                    AppWidget.class));
            for (int appWidgetId : appWidgetIds) {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
                views.setImageViewResource(R.id.play,R.drawable.ic_pause_circle_filled_48px);
                play=false;
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
            playSong(intent.getData().toString());
        }
        super.onReceive(context, intent);


    }
    private void playSong(String path)
    {
        try {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.start();
        }catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }

}

