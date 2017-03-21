package com.banda.android.musicplayer.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class MusicContract {
    public static final String AUTHORITY = "com.banda.android.musicplayer";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_MUSIC = "music";

    public static final class MusicEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MUSIC).build();
        public static final String TABLE_NAME = "music";
        public static final String SONG_ID = "song_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_ALBUM = "album";
        public static final String COLUMN_ARTIST = "artist";
        public static final String COLUMN_PATH = "path";
        public static final String COLUMN_COVER = "cover";

    }

}
