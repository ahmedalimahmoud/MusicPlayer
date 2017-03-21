package com.banda.android.musicplayer.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.banda.android.musicplayer.R;

import static com.banda.android.musicplayer.data.MusicContract.MusicEntry.TABLE_NAME;

public class MusicContentProvider extends ContentProvider {
    private static final int MUSIC = 100;
    private static final int MUSIC_WITH_ID = 101;
    private MusicDbHelper mMusicDbHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(MusicContract.AUTHORITY, MusicContract.PATH_MUSIC, MUSIC);
        uriMatcher.addURI(MusicContract.AUTHORITY, MusicContract.PATH_MUSIC + "/#", MUSIC_WITH_ID);

        return uriMatcher;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mMusicDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MUSIC:
                long id = db.insert(TABLE_NAME, null, values);
                if (id > 0) {

                    returnUri = ContentUris.withAppendedId(MusicContract.MusicEntry.CONTENT_URI, id);
                } else {
                    //noinspection ConstantConditions
                    throw new android.database.SQLException(getContext().getString(R.string.db_insert_error) + uri);
                }
                break;

            default:
                //noinspection ConstantConditions
                throw new UnsupportedOperationException(getContext().getString(R.string.db_uri_error) + uri);
        }
        //noinspection ConstantConditions
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }


    @Override
    public boolean onCreate() {
        Context context = getContext();
        mMusicDbHelper = new MusicDbHelper(context);
        return true;
    }


    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mMusicDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match) {
            case MUSIC:
                retCursor = db.query(TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                //noinspection ConstantConditions
                throw new UnsupportedOperationException(getContext().getString(R.string.db_uri_error) + uri);
        }
        //noinspection ConstantConditions
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = sUriMatcher.match(uri);

        switch (match) {
            case MUSIC:
                return "vnd.android.cursor.dir" + "/" + MusicContract.AUTHORITY + "/" + MusicContract.PATH_MUSIC;
            case MUSIC_WITH_ID:
                return "vnd.android.cursor.item" + "/" + MusicContract.AUTHORITY + "/" + MusicContract.PATH_MUSIC;
            default:
                //noinspection ConstantConditions
                throw new UnsupportedOperationException(getContext().getString(R.string.db_uri_error) + uri);
        }
    }


    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mMusicDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int tasksDeleted;
        switch (match) {
            case MUSIC:
                tasksDeleted = db.delete(TABLE_NAME, selection, selectionArgs);
                break;
            case MUSIC_WITH_ID:
                String id = uri.getPathSegments().get(1);
                tasksDeleted = db.delete(TABLE_NAME, "song_id=?", new String[]{id});
                break;
            default:
                //noinspection ConstantConditions
                throw new UnsupportedOperationException(getContext().getString(R.string.db_uri_error) + uri);
        }
        if (tasksDeleted != 0) {
            //noinspection ConstantConditions
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return tasksDeleted;
    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        int tasksUpdated;
        int match = sUriMatcher.match(uri);

        switch (match) {
            case MUSIC_WITH_ID:
                String id = uri.getPathSegments().get(1);
                tasksUpdated = mMusicDbHelper.getWritableDatabase().update(TABLE_NAME, values, "_id=?", new String[]{id});
                break;
            default:
                //noinspection ConstantConditions
                throw new UnsupportedOperationException(getContext().getString(R.string.db_uri_error) + uri);
        }

        if (tasksUpdated != 0) {
            //noinspection ConstantConditions
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return tasksUpdated;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mMusicDbHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case MUSIC:
                // mange large insert
                db.beginTransaction();
                int rowInserted = 0;
                try {
                    for (ContentValues value : values) {
                        @SuppressWarnings("UnusedAssignment") long Data = value.getAsLong(MusicContract.MusicEntry.COLUMN_TITLE);
                        long _id = db.insert(TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                if (rowInserted > 0) {
                    //noinspection ConstantConditions
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowInserted;
            default:
                return super.bulkInsert(uri, values);

        }
    }
}
