package com.banda.android.musicplayer;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.banda.android.musicplayer.Models.AlbumInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class AlbumActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<List<AlbumInfo>>, AlbumAdapter.AlbumAdapterOnClickHandler {
    private static final String ALBUM_EXTRA = "album";
    private static final int ALBUM_LOADER = 25;

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
        setContentView(R.layout.activity_album);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //noinspection deprecation
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        runAlbums();
    }

    private void runAlbums() {
        ((CollapsingToolbarLayout) findViewById(R.id.toolbar_layout)).setTitle(getResources().getString(R.string.album));
        ((ImageView) findViewById(R.id.collapsingImageView)).setImageResource(R.drawable.image3);
        Bundle queryBundle = new Bundle();
        queryBundle.putString(ALBUM_EXTRA, getResources().getString(R.string.album));
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> githubSearchLoader = loaderManager.getLoader(ALBUM_LOADER);
        if (githubSearchLoader == null) {
            loaderManager.initLoader(ALBUM_LOADER, queryBundle, this);
        } else {
            loaderManager.restartLoader(ALBUM_LOADER, queryBundle, this);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.now) {
            if (MainActivity.now != getResources().getInteger(R.integer.now_playing)) {
                Intent intent = new Intent(this, DetailsActivity.class);
                intent.putExtra(DetailsActivity.EXTRA_SONGS, MainActivity.now);
                startActivity(intent);
            } else {
                Toast.makeText(this, getResources().getString(R.string.empty_now), Toast.LENGTH_LONG).show();
            }
        } else if (id == R.id.all) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.favourite) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(getResources().getString(R.string.favourite), getResources().getString(R.string.favourite));
            startActivity(intent);

        } else if (id == R.id.album) {
            runAlbums();
        } else if (id == R.id.backup) {
            Intent intent = new Intent(this, BackUpActivity.class);
            startActivity(intent);
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public Loader<List<AlbumInfo>> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<List<AlbumInfo>>(this) {
            List<AlbumInfo> data;

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
            public List<AlbumInfo> loadInBackground() {
                String searchQueryUrlString = args.getString(ALBUM_EXTRA);
                if (searchQueryUrlString == null || TextUtils.isEmpty(searchQueryUrlString)) {
                    return null;
                }
                try {
                    List<AlbumInfo> AlbumModelList = new ArrayList<>();
                    Uri smusicUri = android.provider.MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
                    Cursor music = getContentResolver().query(smusicUri, null
                            , null,
                            null, null);
                    assert music != null;
                    music.moveToFirst();
                    while (!music.isAfterLast()) {
                        AlbumInfo albumInfo = new AlbumInfo();
                        albumInfo.setAlbumID(music.getString(music.getColumnIndex(MediaStore.Audio.Albums._ID)));
                        albumInfo.setAlbumName(music.getString(music.getColumnIndex(MediaStore.Audio.Albums.ALBUM)));
                        albumInfo.setAlbumCover(music.getString(music.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)));
                        AlbumModelList.add(albumInfo);
                        music.moveToNext();
                    }
                    music.close();
                    return AlbumModelList;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            public void deliverResult(List<AlbumInfo> data) {
                this.data = data;
                super.deliverResult(data);
            }
        };

    }

    @Override
    public void onLoadFinished(Loader<List<AlbumInfo>> loader, List<AlbumInfo> data) {

        if (data != null) {
            AlbumAdapter adapter = new AlbumAdapter(this, data);
            list.setLayoutManager(new GridLayoutManager(this, getResources().getInteger(R.integer.grid_columns_album)));
            list.setItemAnimator(new SlideInUpAnimator());
            list.getRecycledViewPool().clear();
            list.setAdapter(adapter);
            list.setHasFixedSize(true);
            adapter.notifyDataSetChanged();
            list.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);

        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.album_load_error), Toast.LENGTH_LONG).show();
            list.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<AlbumInfo>> loader) {
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode != KeyEvent.KEYCODE_BACK && super.onKeyDown(keyCode, event);

    }

}
