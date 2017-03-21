package com.banda.android.musicplayer.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class MusicDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "musicDb.db";

    private static final int VERSION = 3;

    public MusicDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String CREATE_TABLE = "CREATE TABLE " + MusicContract.MusicEntry.TABLE_NAME + " ( " +
                MusicContract.MusicEntry._ID + " INTEGER PRIMARY KEY, " +
                MusicContract.MusicEntry.SONG_ID + " INTEGER , " +
                MusicContract.MusicEntry.COLUMN_TITLE + " TEXT , " +
                MusicContract.MusicEntry.COLUMN_ALBUM + " TEXT , " +
                MusicContract.MusicEntry.COLUMN_ARTIST + " TEXT , " +
                MusicContract.MusicEntry.COLUMN_PATH + " TEXT , " +
                MusicContract.MusicEntry.COLUMN_COVER + " TEXT  ); ";
        Log.v("Table_Create", CREATE_TABLE);
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MusicContract.MusicEntry.TABLE_NAME);
        onCreate(db);
    }

}
