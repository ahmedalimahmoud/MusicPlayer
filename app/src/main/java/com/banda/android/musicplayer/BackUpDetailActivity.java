package com.banda.android.musicplayer;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.banda.android.musicplayer.Models.SongInfo;
import com.banda.android.musicplayer.data.MusicContract;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class BackUpDetailActivity extends AppCompatActivity {
    private static final String NAME_INDEX = "NAME_INDEX";
    private static final String EMAIL_INDEX = "EMAIL_INDEX";
    private static final String ID_INDEX = "ID_INDEX";
    private static String name;
    private static String email;
    private static String id;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_back_up_detail);
        dialog = new ProgressDialog(this);
        Bundle intent = getIntent().getExtras();
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(ID_INDEX)) {
                //noinspection ConstantConditions
                name = savedInstanceState.getString(ID_INDEX).replace(" ", ">");
                email = savedInstanceState.getString(EMAIL_INDEX);
                id = savedInstanceState.getString(ID_INDEX);
                checkUser(name, email, id);

            }
        } else {
            //noinspection ConstantConditions
            name = intent.get("name").toString().replace(" ", ">");
            //noinspection ConstantConditions
            email = intent.get("email").toString();
            //noinspection ConstantConditions
            id = intent.get("id").toString();
            checkUser(name, email, id);

        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ID_INDEX, id);
        outState.putString(NAME_INDEX, name);
        outState.putString(EMAIL_INDEX, email);
    }

    private void AddUser(String name, String email, String id) {
        final String Url = "http://formorenetwork.com/new1.php?do=add&&name=" + name + "&&email=" + email + "&&id=" + id;
        new AsyncTask<String, String, String>() {

            String NewsData = "";

            protected void onPreExecute() {
                NewsData = "";

            }

            @Override
            protected String doInBackground(String... params) {
                publishProgress(getResources().getString(R.string.open_connection));
                try {
                    URL u = new URL(Url);
                    HttpURLConnection uc = (HttpURLConnection) u.openConnection();
                    InputStream in = new BufferedInputStream(uc.getInputStream());
                    publishProgress(getResources().getString(R.string.start_buffering));
                    NewsData = Steam2String(in);
                    in.close();

                } catch (Exception e) {
                    publishProgress(getResources().getString(R.string.connection_error));
                }
                return null;
            }

            protected void onPostExecute(String result2) {
                Log.v("Add User", "User Added :" + result2);

            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void AddSongs(String title, String artist, String album, String path, String cover, String song_id, String id) {
        final String Url = "http://formorenetwork.com/new1.php?do=insert&&title=" + title + "&&artist=" + artist + "&&album=" + album + "&&path=" + path + "&&cover=" + cover + "&&songid=" + song_id + "&&id=" + id;
       Log.v("insert ","http://formorenetwork.com/new1.php?do=insert&&title=" + title + "&&artist=" + artist + "&&album=" + album + "&&path=" + path + "&&cover=" + cover + "&&songid=" + song_id + "&&id=" + id) ;
        new AsyncTask<String, String, String>() {

            String NewsData = "";

            protected void onPreExecute() {
                NewsData = "";
                dialog.setMessage(getResources().getString(R.string.back_up_progress));
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

            }

            @Override
            protected String doInBackground(String... params) {
                publishProgress(getResources().getString(R.string.open_connection));
                try {
                    URL u = new URL(Url);
                    HttpURLConnection uc = (HttpURLConnection) u.openConnection();
                    InputStream in = new BufferedInputStream(uc.getInputStream());
                    publishProgress(getResources().getString(R.string.start_buffering));
                    NewsData = Steam2String(in);
                    in.close();

                } catch (Exception e) {
                    publishProgress(getResources().getString(R.string.connection_error));
                }
                return null;
            }

            protected void onPostExecute(String result2) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void DeleteSongs(String id) {
        final String Url = "http://formorenetwork.com/new1.php?do=delete&&id=" + id;
        new AsyncTask<String, String, String>() {

            String NewsData = "";

            protected void onPreExecute() {
                NewsData = "";
                dialog.setMessage(getResources().getString(R.string.delete_progress));
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }

            @Override
            protected String doInBackground(String... params) {
                publishProgress(getResources().getString(R.string.open_connection));
                try {
                    URL u = new URL(Url);
                    HttpURLConnection uc = (HttpURLConnection) u.openConnection();
                    InputStream in = new BufferedInputStream(uc.getInputStream());
                    publishProgress(getResources().getString(R.string.start_buffering));
                    NewsData = Steam2String(in);
                    in.close();

                } catch (Exception e) {
                    publishProgress(getResources().getString(R.string.connection_error));
                }
                return null;
            }

            protected void onPostExecute(String result2) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private String Steam2String(InputStream inputStream) {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String t = "";
        try {
            while ((line = br.readLine()) != null) {
                t += line;
            }
            inputStream.close();
        } catch (Exception ex) {
            ex.getMessage();
        }
        return t;
    }

    private void checkUser(final String name, final String email, final String userID) {
        final String URI = "http://formorenetwork.com/new1.php?do=login&&email=" + email + "&&user_id=" + userID;
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                HttpURLConnection connection;
                BufferedReader reader;
                URL url;
                try {
                    url = new URL(URI);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    InputStream stream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuilder buffer = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    String result = buffer.toString();
                    return !result.equals("");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Boolean check) {

                if (check) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.welcome_back) + name.replace(">", " "), Toast.LENGTH_SHORT).show();
                } else {
                    AddUser(name, email, userID);

                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.welcome) + name.replace(">", " "), Toast.LENGTH_SHORT).show();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public void backUp(@SuppressWarnings("UnusedParameters") View view) {
        DeleteSongs(id);
        Cursor res = getContentResolver().query(MusicContract.MusicEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
        assert res != null;
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String song_id = String.valueOf(res.getInt(res.getColumnIndex(MusicContract.MusicEntry.SONG_ID))).replace(" ", ">");
            String title = res.getString(res.getColumnIndex(MusicContract.MusicEntry.COLUMN_TITLE)).replace(" ", ">");
            String album = res.getString(res.getColumnIndex(MusicContract.MusicEntry.COLUMN_ALBUM)).replace(" ", ">");
            String artist = res.getString(res.getColumnIndex(MusicContract.MusicEntry.COLUMN_ARTIST)).replace(" ", ">");
            String path = res.getString(res.getColumnIndex(MusicContract.MusicEntry.COLUMN_PATH)).replace(" ", ">");
            String cover;
            if (res.getString(res.getColumnIndex(MusicContract.MusicEntry.COLUMN_COVER)) != null) {
                cover = res.getString(res.getColumnIndex(MusicContract.MusicEntry.COLUMN_COVER)).replace(" ", ">");
            } else {
                cover = res.getString(res.getColumnIndex(MusicContract.MusicEntry.COLUMN_COVER));
            }
            AddSongs(title, artist, album, path, cover, song_id, id);
            res.moveToNext();
        }
        res.close();
    }

    public void recover(@SuppressWarnings("UnusedParameters")View view) {
        Uri uri = MusicContract.MusicEntry.CONTENT_URI;
        getContentResolver().delete(uri, "song_id >=?", new String[]{"0"});
        String URL = "http://formorenetwork.com/new1.php?do=view&id=" + id;
        CheckSongs(URL);
    }

    private void CheckSongs(String url) {
        final String URI = url;
        new AsyncTask<Void, Void, List<SongInfo>>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog.setMessage(getResources().getString(R.string.recover_progress));
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }

            @Override
            protected List<SongInfo> doInBackground(Void... params) {
                HttpURLConnection connection;
                BufferedReader reader;
                URL url;
                try {
                    url = new URL(URI);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    InputStream stream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuilder buffer = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    String result = buffer.toString();
                    if (result.equals("")) {
                        return null;
                    } else {
                        try {
                            JSONArray jsonArray = new JSONArray(result);
                            List<SongInfo> musicModelList = new ArrayList<>();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject json = jsonArray.getJSONObject(i);
                                SongInfo songInfo = new SongInfo();
                                songInfo.setId(Integer.valueOf(json.getString("song_id").replace(">", " ")));
                                songInfo.setTitle(json.getString("title").replace(">", " "));
                                songInfo.setAlbum(json.getString("album").replace(">", " "));
                                songInfo.setArtist(json.getString("artist").replace(">", " "));
                                songInfo.setPath(json.getString("path").replace(">", " "));
                                songInfo.setCover(json.getString("cover").replace(">", " "));
                                musicModelList.add(songInfo);
                            }
                            return musicModelList;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(List<SongInfo> list) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                recoverSongs(list);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void recoverSongs(List<SongInfo> list) {
        for (int i = 0; i < list.size(); i++) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MusicContract.MusicEntry.SONG_ID, list.get(i).getId());
            contentValues.put(MusicContract.MusicEntry.COLUMN_TITLE, list.get(i).getTitle());
            contentValues.put(MusicContract.MusicEntry.COLUMN_ALBUM, list.get(i).getAlbum());
            contentValues.put(MusicContract.MusicEntry.COLUMN_ARTIST, list.get(i).getArtist());
            contentValues.put(MusicContract.MusicEntry.COLUMN_PATH, list.get(i).getPath());
            contentValues.put(MusicContract.MusicEntry.COLUMN_COVER, list.get(i).getCover());
            Uri uri = getContentResolver().insert(MusicContract.MusicEntry.CONTENT_URI, contentValues);
            if (uri != null) {
                Log.v("recover", getResources().getString(R.string.recover_progress) + " " + list.get(i).getTitle());
            }
        }

    }


}
