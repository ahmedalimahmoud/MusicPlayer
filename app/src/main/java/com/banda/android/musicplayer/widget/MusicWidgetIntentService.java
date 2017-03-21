package com.banda.android.musicplayer.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;

import com.banda.android.musicplayer.Models.SongInfo;
import com.banda.android.musicplayer.R;

import java.util.ArrayList;
import java.util.List;


public class MusicWidgetIntentService extends IntentService {
    public static final String PLAY = "com.banda.android.musicplayer.PLAY";
    public static final String NEXT = "com.banda.android.musicplayer.NEXT";
    public static final String PREVIOUS = "com.banda.android.musicplayer.PREVIOUS";
    private static int position=1;
    public MusicWidgetIntentService() {
        super("MusicWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                AppWidget.class));
        List<SongInfo> musicModelList = new ArrayList<>();
        Uri songsUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        Cursor data = getContentResolver().query(songsUri, null, selection, null, null);

        if(data!=null&&data.getCount()>0) {
            data.moveToFirst();
            int i = 0;
            while (!data.isAfterLast()) {
                SongInfo songInfoModel = new SongInfo();

                songInfoModel.setPath(data.getString(data.getColumnIndex(MediaStore.Audio.Media.DATA)));
                songInfoModel.setTitle(data.getString(data.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                songInfoModel.setArtist(data.getString(data.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                songInfoModel.setId(i);
                songInfoModel.setAlbum(data.getString(data.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                musicModelList.add(songInfoModel);
                i++;
                data.moveToNext();
            }

            data.close();
            for (int appWidgetId : appWidgetIds) {
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.app_widget);
                views.setTextViewText(R.id.song_title,musicModelList.get(position).getTitle());
                views.setTextViewText(R.id.song_artist,musicModelList.get(position).getArtist());
                Intent intentPlay = new Intent(this, AppWidget.class);
                intentPlay.setAction(PLAY);
                intentPlay.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                intentPlay.setData(Uri.parse(musicModelList.get(position).getPath()));
                Log.v("Now",musicModelList.get(position).getPath());
                PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(this, 0, intentPlay, 0);
                views.setOnClickPendingIntent(R.id.play, pendingIntentPlay);
                Intent intentNext = new Intent(this, AppWidget.class);
                intentNext.setAction(NEXT);
                position++;
                intentNext.setData(Uri.parse(musicModelList.get(position).getPath()));
                Log.v("NEXT",musicModelList.get(++position).getPath());
                intentNext.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                PendingIntent pendingIntentNext = PendingIntent.getBroadcast(this, 0, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
                views.setOnClickPendingIntent(R.id.next, pendingIntentNext);
                Intent intentPrevious = new Intent(this, AppWidget.class);
                intentPrevious.setAction(PREVIOUS);
                intentPrevious.setData(Uri.parse(musicModelList.get(position--).getPath()));
                Log.v("Previous",musicModelList.get(position--).getPath());
                intentPrevious.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                PendingIntent pendingIntentPrevious = PendingIntent.getBroadcast(this, 0, intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);
                views.setOnClickPendingIntent(R.id.previous, pendingIntentPrevious);
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }


        }

    }
}
