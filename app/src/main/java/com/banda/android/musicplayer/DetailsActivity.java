package com.banda.android.musicplayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.banda.android.musicplayer.data.MusicContract;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsActivity extends Activity {
    public static final String EXTRA_SONGS = "EXTRA_SONGS";
    private static final String EXTRA_INDEX = "INDEX";
    private static int intValue;
    private AnimatedVectorDrawable emptyHeart;
    private AnimatedVectorDrawable fillHeart;
    private boolean full = false;
    private int progress;
    private static Boolean s = true;
    private static final MediaPlayer mediaPlayer = new MediaPlayer();

    @SuppressWarnings({"WeakerAccess", "CanBeFinal"})
    @BindView(R.id.title)
    TextView title;
    @SuppressWarnings({"WeakerAccess", "CanBeFinal"})
    @BindView(R.id.artist)
    TextView artist;
    @SuppressWarnings({"WeakerAccess", "CanBeFinal"})
    @BindView(R.id.duration)
    TextView duration;
    @SuppressWarnings({"WeakerAccess", "CanBeFinal"})
    @BindView(R.id.time)
    TextView time;
    @SuppressWarnings({"WeakerAccess", "CanBeFinal"})
    @BindView(R.id.avatar)
    ImageView cover;
    @SuppressWarnings({"WeakerAccess", "CanBeFinal"})
    @BindView(R.id.play)
    ImageButton play;
    @SuppressWarnings({"WeakerAccess", "CanBeFinal"})
    @BindView(R.id.next)
    ImageButton next;
    @SuppressWarnings({"WeakerAccess", "CanBeFinal"})
    @BindView(R.id.previous)
    ImageButton previous;
    @SuppressWarnings({"WeakerAccess", "CanBeFinal"})
    @BindView(R.id.share_fab)
    FloatingActionButton fab;
    @SuppressWarnings({"WeakerAccess", "CanBeFinal"})
    @BindView(R.id.heart)
    ImageButton heart;
    @SuppressWarnings({"WeakerAccess", "CanBeFinal"})
    @BindView(R.id.seekbar)
    SeekBar seekBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        emptyHeart = (AnimatedVectorDrawable) getDrawable(R.drawable.avd_heart_empty);
        fillHeart = (AnimatedVectorDrawable) getDrawable(R.drawable.avd_heart_fill);
        Intent mIntent = getIntent();
        intValue = mIntent.getIntExtra(EXTRA_SONGS, 0);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(EXTRA_INDEX)) {
                intValue = savedInstanceState.getInt(EXTRA_INDEX);
                playSong(intValue);

            }
        } else {
            if (intValue >= 0) {
                playSong(intValue);
            }
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(progress);
            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (s) {
                    play.setImageResource(R.drawable.ic_play_circle_filled_48px);
                    mediaPlayer.pause();
                    s = false;

                } else {
                    play.setImageResource(R.drawable.ic_pause_circle_filled_48px);
                    mediaPlayer.start();
                    s = true;
                }
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (intValue < MusicAdapter.songInfoList.size() - 1) {
                    playSong(intValue + 1);
                    intValue++;
                }
            }
        });
        previous.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (intValue > 0) {
                    playSong(intValue - 1);
                    intValue--;
                }
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareSong(MusicAdapter.songInfoList.get(intValue).getPath());
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (intValue < MusicAdapter.songInfoList.size() - 1) {

                    playSong(intValue + 1);
                    intValue++;
                }
            }
        });

        MyThread thread = new MyThread();
        thread.start();
    }

    private void shareSong(String path) {
        String mimeType = "audio/mp3";
        String title = getResources().getString(R.string.share);
        ShareCompat.IntentBuilder
                .from(this)
                .setType(mimeType)
                .setChooserTitle(title)
                .setStream(Uri.parse(path))
                .startChooser();
    }

    private boolean check(String path) {

        String[] selectionArgs = new String[]{path};
        Cursor res = getContentResolver().query(MusicContract.MusicEntry.CONTENT_URI,
                null,
                "path=?",
                selectionArgs,
                null);
        assert res != null;
        res.close();
        return res.getCount() != 0;
    }

    @SuppressLint("DefaultLocale")
    private void playSong(final int songIndex) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getResources().getString(R.string.pref_now_playing_key), songIndex);
        editor.apply();

        final boolean x = check(MusicAdapter.songInfoList.get(intValue).getPath());
        if (x) {
            heart.setImageResource(R.drawable.ic_haertfull);
            full = true;

        } else {
            heart.setImageResource(R.drawable.ic_heart);
            full = false;
        }
        heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!full) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MusicContract.MusicEntry.SONG_ID, MusicAdapter.songInfoList.get(songIndex).getId());
                    contentValues.put(MusicContract.MusicEntry.COLUMN_TITLE, MusicAdapter.songInfoList.get(songIndex).getTitle());
                    contentValues.put(MusicContract.MusicEntry.COLUMN_ALBUM, MusicAdapter.songInfoList.get(songIndex).getAlbum());
                    contentValues.put(MusicContract.MusicEntry.COLUMN_ARTIST, MusicAdapter.songInfoList.get(songIndex).getArtist());
                    contentValues.put(MusicContract.MusicEntry.COLUMN_PATH, MusicAdapter.songInfoList.get(songIndex).getPath());
                    contentValues.put(MusicContract.MusicEntry.COLUMN_COVER, MusicAdapter.songInfoList.get(songIndex).getCover());
                    Uri uri = getContentResolver().insert(MusicContract.MusicEntry.CONTENT_URI, contentValues);
                    if (uri != null) {
                        Toast.makeText(getBaseContext(), MusicAdapter.songInfoList.get(songIndex).getTitle()+" "+
                                getResources().getString(R.string.add_favourite), Toast.LENGTH_SHORT).show();
                        AnimatedVectorDrawable drawable = fillHeart;
                        heart.setImageDrawable(fillHeart);
                        drawable.start();
                        full = !full;
                    }
                } else {
                    Uri uri = MusicContract.MusicEntry.CONTENT_URI;
                    getContentResolver().delete(uri, "path=?", new String[]{MusicAdapter.songInfoList.get(songIndex).getPath()});
                    AnimatedVectorDrawable drawable = emptyHeart;
                    heart.setImageDrawable(emptyHeart);
                    drawable.start();
                    full = !full;
                }
            }
        });
        title.setText(MusicAdapter.songInfoList.get(songIndex).getTitle());
        artist.setText(MusicAdapter.songInfoList.get(songIndex).getArtist());
        if (MusicAdapter.songInfoList.get(songIndex).getCover() != null) {
            Picasso.
                    with(this).
                    load("file://" + Uri.parse(MusicAdapter.songInfoList.get(songIndex).getCover())).noFade().error(R.drawable.music1)
                    .into(cover);
        } else {
            Picasso.with(this).load(R.drawable.music1).placeholder(R.drawable.loading).into(cover);
        }
        try {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(MusicAdapter.songInfoList.get(songIndex).getPath());
            mediaPlayer.prepare();
            seekBar.setMax(mediaPlayer.getDuration());
            mediaPlayer.start();
            play.setImageResource(R.drawable.ic_pause_circle_filled_48px);
            s = true;

            duration.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes((long) mediaPlayer.getDuration()),
                    TimeUnit.MILLISECONDS.toSeconds((long) mediaPlayer.getDuration()) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                    mediaPlayer.getDuration())))
            );
            time.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes((long) mediaPlayer.getCurrentPosition()),
                    TimeUnit.MILLISECONDS.toSeconds((long) mediaPlayer.getCurrentPosition()) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                    mediaPlayer.getCurrentPosition())))
            );
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int index = intValue;
        outState.putInt(EXTRA_INDEX, index);
    }
    private class MyThread extends Thread {
        public void run() {
            while (mediaPlayer != null) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                seekBar.post(new Runnable() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void run() {

                        seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        time.setText(String.format("%d:%d",
                                TimeUnit.MILLISECONDS.toMinutes((long) mediaPlayer.getCurrentPosition()),
                                TimeUnit.MILLISECONDS.toSeconds((long) mediaPlayer.getCurrentPosition()) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                                mediaPlayer.getCurrentPosition())))
                        );
                    }
                });
            }
        }
    }

}
