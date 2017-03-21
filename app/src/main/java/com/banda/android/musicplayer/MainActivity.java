package com.banda.android.musicplayer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.banda.android.musicplayer.Models.AlbumInfo;
import com.banda.android.musicplayer.Models.SongInfo;
import com.banda.android.musicplayer.data.MusicContract;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<List<SongInfo>>
        , MusicAdapter.MusicAdapterOnClickHandler, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String SEARCH_QUERY_URL_EXTRA = "query";
    private static final String SEARCH_SONG_EXTRA = "search";
    private static final String FAVOURITE_SONG_EXTRA = "favourite";
    private static final String ALBUM_SONG_EXTRA = "album_songs";
    private static String sort;
    public static int now;
    private static boolean run = false;

    private static final int LOADER = 22;
    private static final int SEARCH_LOADER = 23;
    private static final int FAVOURITE_LOADER = 24;
    private static final int ALBUM_SONGS_LOADER = 26;
    private Cursor cursor;
    private MusicAdapter adapter;
    private ProgressDialog dialog;

    @SuppressWarnings({"WeakerAccess", "CanBeFinal"})
    @BindView(R.id.list)
    RecyclerView list;
    @SuppressWarnings({"WeakerAccess", "CanBeFinal"})
    @BindView(R.id.error)
    TextView mEmptyView;
    @SuppressWarnings({"WeakerAccess", "CanBeFinal"})
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @SuppressWarnings({"WeakerAccess", "CanBeFinal"})
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @SuppressWarnings({"WeakerAccess", "CanBeFinal"})
    @BindView(R.id.nav_view)
    NavigationView navigationView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //noinspection deprecation
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        dialog = new ProgressDialog(this);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("album")) {
                AlbumInfo albumInfo = extras.getParcelable("album");
                runAlbumSongs(albumInfo);
            }
            if (extras.containsKey(getResources().getString(R.string.favourite))) {
                runFavourite();
            }

        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                setupPreference();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);

            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    list.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                    setupPreference();
                } else {

                    list.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.permission_error), Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    private void runAlbumSongs(AlbumInfo albumInfo) {
        run = false;
        ((CollapsingToolbarLayout) findViewById(R.id.toolbar_layout)).setTitle(albumInfo.getAlbumName());
        ImageView cover = (ImageView) findViewById(R.id.collapsingImageView);
        if (albumInfo.getAlbumCover() != null) {
            Picasso.
                    with(this).
                    load("file://" + Uri.parse(albumInfo.getAlbumCover())).noFade().error(R.drawable.album)
                    .into(cover);

        } else {
            Picasso.with(this).load(R.drawable.album).placeholder(R.drawable.loading).into(cover);
        }
        Bundle queryBundle = new Bundle();
        queryBundle.putParcelable(ALBUM_SONG_EXTRA, albumInfo);
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> githubSearchLoader = loaderManager.getLoader(ALBUM_SONGS_LOADER);
        if (githubSearchLoader == null) {
            loaderManager.initLoader(ALBUM_SONGS_LOADER, queryBundle, this);
        } else {
            loaderManager.restartLoader(ALBUM_SONGS_LOADER, queryBundle, this);
        }
    }

    private void setupPreference() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        loadOrder(sharedPreferences);
        loadNowPlaying(sharedPreferences);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

    }

    private void loadNowPlaying(SharedPreferences sharedPreferences) {
        now = sharedPreferences.getInt(getResources().getString(R.string.pref_now_playing_key), getResources().getInteger(R.integer.now_playing));
    }

    private void loadOrder(SharedPreferences sharedPreferences) {
        sort = sharedPreferences.getString(getResources().getString(R.string.pref_order_key), getResources().getString(R.string.pref_order_title_value));
        runAllMusic();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_order_key))) {
            loadOrder(sharedPreferences);
        } else if (key.equals(getString(R.string.pref_now_playing_key))) {
            loadNowPlaying(sharedPreferences);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    private void runAllMusic() {
        run = false;
        ((CollapsingToolbarLayout) findViewById(R.id.toolbar_layout)).setTitle(getResources().getString(R.string.app_name));
        ((ImageView) findViewById(R.id.collapsingImageView)).setImageResource(R.drawable.image1);

        Bundle queryBundle = new Bundle();
        queryBundle.putString(SEARCH_QUERY_URL_EXTRA, "all");
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> githubSearchLoader = loaderManager.getLoader(LOADER);
        if (githubSearchLoader == null) {
            loaderManager.initLoader(LOADER, queryBundle, this);
        } else {
            loaderManager.restartLoader(LOADER, queryBundle, this);
        }

    }

    public void searchMusic(String songTitle) {
        run = false;
        ((CollapsingToolbarLayout) findViewById(R.id.toolbar_layout)).setTitle(getResources().getString(R.string.dialog_add));
        ((ImageView) findViewById(R.id.collapsingImageView)).setImageResource(R.drawable.image2);
        Bundle queryBundle = new Bundle();
        queryBundle.putString(SEARCH_SONG_EXTRA, songTitle);
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> githubSearchLoader = loaderManager.getLoader(SEARCH_LOADER);
        if (githubSearchLoader == null) {
            loaderManager.initLoader(SEARCH_LOADER, queryBundle, this);
        } else {
            loaderManager.restartLoader(SEARCH_LOADER, queryBundle, this);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void runFavourite() {
        run = true;
        ((CollapsingToolbarLayout) findViewById(R.id.toolbar_layout)).setTitle(getResources().getString(R.string.favourite));
        ((ImageView) findViewById(R.id.collapsingImageView)).setImageResource(R.drawable.image4);

        Bundle queryBundle = new Bundle();
        queryBundle.putString(FAVOURITE_SONG_EXTRA, getResources().getString(R.string.favourite));
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> githubSearchLoader = loaderManager.getLoader(FAVOURITE_LOADER);
        if (githubSearchLoader == null) {
            loaderManager.initLoader(FAVOURITE_LOADER, queryBundle, this);
        } else {
            loaderManager.restartLoader(FAVOURITE_LOADER, queryBundle, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void sortMusic(String sort_key, List<SongInfo> songInfoList) {
        switch (sort_key) {
            case "title":
                Collections.sort(songInfoList, SongInfo.SongTitleComparator);
                break;
            case "artist":
                Collections.sort(songInfoList, SongInfo.SongArtistComparator);
                break;
            case "album":
                Collections.sort(songInfoList, SongInfo.SongAlbumComparator);
                break;
            default:
                break;

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.search) {
            new SearchDialog().show(getFragmentManager(), "SearchDialogFragment");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.now) {
            if (now != getResources().getInteger(R.integer.now_playing)) {
                Intent intent = new Intent(this, DetailsActivity.class);
                intent.putExtra(DetailsActivity.EXTRA_SONGS, now);
                startActivity(intent);
            } else {
                Toast.makeText(this, getResources().getString(R.string.empty_now), Toast.LENGTH_LONG).show();
            }

        } else if (id == R.id.all) {
            runAllMusic();
        } else if (id == R.id.favourite) {
            runFavourite();

        } else if (id == R.id.album) {
            Intent intent = new Intent(this, AlbumActivity.class);
            startActivity(intent);
        } else if (id == R.id.backup) {
            Intent intent = new Intent(this, BackUpActivity.class);
            startActivity(intent);
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public Loader<List<SongInfo>> onCreateLoader(int id, final Bundle args) {

        if (id == LOADER) {

            return new AsyncTaskLoader<List<SongInfo>>(this) {
                List<SongInfo> data;

                @Override
                protected void onStartLoading() {

                    if (args == null) {
                        return;
                    }
                    if (data != null) {
                        deliverResult(data);
                    } else {
                        dialog.setMessage(getResources().getString(R.string.music_progress));
                        dialog.setCancelable(true);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();
                        forceLoad();
                    }
                }

                @Override
                public List<SongInfo> loadInBackground() {
                    String searchQueryUrlString = args.getString(SEARCH_QUERY_URL_EXTRA);
                    if (searchQueryUrlString == null || TextUtils.isEmpty(searchQueryUrlString)) {
                        return null;
                    }
                    try {


                        Uri songsUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
                        cursor = getContentResolver().query(songsUri, null, selection, null, null);

                        List<SongInfo> musicModelList = new ArrayList<>();
                        if (cursor != null) {
                            int i = 0;
                            cursor.moveToFirst();
                            while (!cursor.isAfterLast()) {
                                SongInfo songInfoModel = new SongInfo();

                                songInfoModel.setPath(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                                songInfoModel.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                                songInfoModel.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                                songInfoModel.setId(i);
                                songInfoModel.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                                String albumId = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                                Uri smusicUri = android.provider.MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
                                Cursor music = getContentResolver().query(smusicUri, null
                                        , MediaStore.Audio.Albums._ID + "=?",
                                        new String[]{String.valueOf(albumId)}, null);
                                assert music != null;
                                music.moveToFirst();

                                while (!music.isAfterLast()) {
                                    songInfoModel.setCover(music.getString(music.getColumnIndex(android.provider.MediaStore.Audio.Albums.ALBUM_ART)));
                                    music.moveToNext();
                                }
                                music.close();
                                musicModelList.add(songInfoModel);
                                i++;
                                cursor.moveToNext();
                            }
                            cursor.close();
                        }
                        sortMusic(sort, musicModelList);

                        return musicModelList;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return null;
                }

                @Override
                public void deliverResult(List<SongInfo> data) {
                    this.data = data;
                    super.deliverResult(data);
                }
            };
        } else if (id == SEARCH_LOADER) {
            return new AsyncTaskLoader<List<SongInfo>>(this) {
                List<SongInfo> data;

                @Override
                protected void onStartLoading() {


                    if (args == null) {
                        return;
                    }
                    if (data != null) {
                        deliverResult(data);
                    } else {
                        dialog.setMessage(getResources().getString(R.string.search_progress));
                        dialog.setCancelable(true);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();
                        forceLoad();
                    }
                }

                @Override
                public List<SongInfo> loadInBackground() {
                    String searchString = args.getString(SEARCH_SONG_EXTRA);
                    if (searchString == null || TextUtils.isEmpty(searchString)) {
                        return null;
                    }
                    try {

                        Uri songsUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
                        cursor = getContentResolver().query(songsUri, null, selection, null, null);

                        List<SongInfo> musicModelList = new ArrayList<>();
                        if (cursor != null) {
                            int i = 0;
                            cursor.moveToFirst();
                            while (!cursor.isAfterLast()) {
                                SongInfo songInfoModel = new SongInfo();
                                if (cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)).toLowerCase().contains(searchString.toLowerCase())) {
                                    songInfoModel.setPath(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                                    songInfoModel.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                                    songInfoModel.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                                    songInfoModel.setId(i);
                                    songInfoModel.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                                    String albumId = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                                    Uri smusicUri = android.provider.MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
                                    Cursor music = getContentResolver().query(smusicUri, null
                                            , MediaStore.Audio.Albums._ID + "=?",
                                            new String[]{String.valueOf(albumId)}, null);
                                    assert music != null;
                                    music.moveToFirst();

                                    while (!music.isAfterLast()) {
                                        songInfoModel.setCover(music.getString(music.getColumnIndex(android.provider.MediaStore.Audio.Albums.ALBUM_ART)));
                                        music.moveToNext();
                                    }
                                    music.close();
                                    musicModelList.add(songInfoModel);
                                    i++;
                                }
                                cursor.moveToNext();
                            }
                        }
                        sortMusic(sort, musicModelList);

                        return musicModelList;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return null;
                }

                @Override
                public void deliverResult(List<SongInfo> data) {
                    this.data = data;
                    super.deliverResult(data);
                }
            };
        } else if (id == FAVOURITE_LOADER) {
            return new AsyncTaskLoader<List<SongInfo>>(this) {
                List<SongInfo> data;

                @Override
                protected void onStartLoading() {
                    if (args == null) {
                        return;
                    }
                    if (data != null) {
                        deliverResult(data);
                    } else {
                        dialog.setMessage(getResources().getString(R.string.favourite_progress));
                        dialog.setCancelable(true);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();
                        forceLoad();
                    }
                }

                @Override
                public List<SongInfo> loadInBackground() {
                    String searchQueryUrlString = args.getString(FAVOURITE_SONG_EXTRA);
                    if (searchQueryUrlString == null || TextUtils.isEmpty(searchQueryUrlString)) {
                        return null;
                    }
                    try {

                        List<SongInfo> musicModelList = new ArrayList<>();
                        Cursor res = getContentResolver().query(MusicContract.MusicEntry.CONTENT_URI,
                                null,
                                null,
                                null,
                                null);
                        assert res != null;
                        res.moveToFirst();
                        while (!res.isAfterLast()) {
                            SongInfo songInfo = new SongInfo();
                            songInfo.setId(res.getInt(res.getColumnIndex(MusicContract.MusicEntry.SONG_ID)));
                            songInfo.setTitle(res.getString(res.getColumnIndex(MusicContract.MusicEntry.COLUMN_TITLE)));
                            songInfo.setAlbum(res.getString(res.getColumnIndex(MusicContract.MusicEntry.COLUMN_ALBUM)));
                            songInfo.setArtist(res.getString(res.getColumnIndex(MusicContract.MusicEntry.COLUMN_ARTIST)));
                            songInfo.setPath(res.getString(res.getColumnIndex(MusicContract.MusicEntry.COLUMN_PATH)));
                            songInfo.setCover(res.getString(res.getColumnIndex(MusicContract.MusicEntry.COLUMN_COVER)));
                            musicModelList.add(songInfo);
                            res.moveToNext();
                        }
                        res.close();
                        sortMusic(sort, musicModelList);
                        return musicModelList;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return null;
                }

                @Override
                public void deliverResult(List<SongInfo> data) {
                    this.data = data;
                    super.deliverResult(data);
                }
            };
        } else if (id == ALBUM_SONGS_LOADER) {
            return new AsyncTaskLoader<List<SongInfo>>(this) {
                List<SongInfo> data;

                @Override
                protected void onStartLoading() {

                    if (args == null) {
                        return;
                    }
                    if (data != null) {
                        deliverResult(data);
                    } else {
                        forceLoad();
                    }
                }

                @Override
                public List<SongInfo> loadInBackground() {
                    AlbumInfo albumInfo = args.getParcelable(ALBUM_SONG_EXTRA);

                    try {

                        Uri songsUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " + MediaStore.Audio.Media.ALBUM_ID + "=?";
                        cursor = getContentResolver().query(songsUri, null, selection, new String[]{String.valueOf(albumInfo != null ? albumInfo.getAlbumID() : null)}, null);

                        List<SongInfo> musicModelList = new ArrayList<>();
                        if (cursor != null) {
                            int i = 0;
                            cursor.moveToFirst();
                            while (!cursor.isAfterLast()) {
                                SongInfo songInfoModel = new SongInfo();
                                songInfoModel.setPath(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                                songInfoModel.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                                songInfoModel.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                                songInfoModel.setId(i);
                                songInfoModel.setAlbum(albumInfo != null ? albumInfo.getAlbumName() : null);
                                songInfoModel.setCover(albumInfo != null ? albumInfo.getAlbumCover() : null);
                                musicModelList.add(songInfoModel);
                                i++;
                                cursor.moveToNext();
                            }
                            cursor.close();

                        }
                        sortMusic(sort, musicModelList);
                        return musicModelList;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return null;
                }

                @Override
                public void deliverResult(List<SongInfo> data) {
                    this.data = data;
                    super.deliverResult(data);
                }
            };
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<List<SongInfo>> loader, List<SongInfo> data) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        if (data != null) {
            if (data.size() > 0) {
                list.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(View.GONE);
            }
            adapter = new MusicAdapter(this, data);
            list.setLayoutManager(new GridLayoutManager(this, getResources().getInteger(R.integer.grid_columns)));
            list.getRecycledViewPool().clear();
            list.setItemAnimator(new DefaultItemAnimator());
            list.setHasFixedSize(true);
            list.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            if (loader.getId() == FAVOURITE_LOADER) {
                if (data.size() == 0) {
                    list.setVisibility(View.GONE);
                    mEmptyView.setText(getResources().getString(R.string.favourite_error));
                    mEmptyView.setVisibility(View.VISIBLE);
                }
                new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        String symbol = adapter.getIDPosition(viewHolder.getAdapterPosition());
                        Uri uri = MusicContract.MusicEntry.CONTENT_URI;
                        uri = uri.buildUpon().appendPath(symbol).build();
                        getContentResolver().delete(uri, null, null);
                        runFavourite();
                    }
                }).attachToRecyclerView(list);
            }
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.music_load_error), Toast.LENGTH_LONG).show();
            list.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onLoaderReset(Loader<List<SongInfo>> loader) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (run) {
            runFavourite();
        }
    }
}
