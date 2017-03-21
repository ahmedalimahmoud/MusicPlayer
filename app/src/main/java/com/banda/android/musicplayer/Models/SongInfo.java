package com.banda.android.musicplayer.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

public class SongInfo implements Parcelable {
    private String path;
    private String title;
    private String artist;
    private String album;
    private String cover;
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private SongInfo(Parcel in) {
        path = in.readString();
        title = in.readString();
        artist = in.readString();
        album = in.readString();
        cover = in.readString();
        id = in.readInt();

    }

    public static final Creator<SongInfo> CREATOR = new Creator<SongInfo>() {
        @Override
        public SongInfo createFromParcel(Parcel in) {
            return new SongInfo(in);
        }

        @Override
        public SongInfo[] newArray(int size) {
            return new SongInfo[size];
        }
    };

    public SongInfo() {

    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(path);
        parcel.writeString(title);
        parcel.writeString(artist);
        parcel.writeString(album);
        parcel.writeString(cover);
        parcel.writeInt(id);

    }

    public static final Comparator<SongInfo> SongTitleComparator
            = new Comparator<SongInfo>() {

        public int compare(SongInfo title1, SongInfo title2) {

            String song_title1 = title1.getTitle().toUpperCase();
            String song_title2 = title2.getTitle().toUpperCase();
            return song_title1.compareTo(song_title2);

        }

    };
    public static final Comparator<SongInfo> SongArtistComparator
            = new Comparator<SongInfo>() {

        public int compare(SongInfo artist1, SongInfo artist2) {

            String song_artist1 = artist1.getArtist().toUpperCase();
            String song_artist2 = artist2.getArtist().toUpperCase();
            return song_artist1.compareTo(song_artist2);

        }

    };
    public static final Comparator<SongInfo> SongAlbumComparator
            = new Comparator<SongInfo>() {

        public int compare(SongInfo album1, SongInfo album2) {

            String song_album1 = album1.getAlbum().toUpperCase();
            String song_album2 = album2.getAlbum().toUpperCase();
            return song_album1.compareTo(song_album2);

        }

    };

}

