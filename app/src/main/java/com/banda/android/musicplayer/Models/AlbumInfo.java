package com.banda.android.musicplayer.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class AlbumInfo implements Parcelable {
    private String AlbumName;
    private String AlbumID;
    private String AlbumCover;

    private AlbumInfo(Parcel in) {
        AlbumName = in.readString();
        AlbumID = in.readString();
        AlbumCover = in.readString();
    }

    public static final Creator<AlbumInfo> CREATOR = new Creator<AlbumInfo>() {
        @Override
        public AlbumInfo createFromParcel(Parcel in) {
            return new AlbumInfo(in);
        }

        @Override
        public AlbumInfo[] newArray(int size) {
            return new AlbumInfo[size];
        }
    };

    public AlbumInfo() {

    }

    public String getAlbumName() {
        return AlbumName;
    }

    public void setAlbumName(String albumName) {
        AlbumName = albumName;
    }

    public String getAlbumID() {
        return AlbumID;
    }

    public void setAlbumID(String albumID) {
        AlbumID = albumID;
    }

    public String getAlbumCover() {
        return AlbumCover;
    }

    public void setAlbumCover(String albumCover) {
        AlbumCover = albumCover;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(AlbumName);
        parcel.writeString(AlbumID);
        parcel.writeString(AlbumCover);
    }
}
