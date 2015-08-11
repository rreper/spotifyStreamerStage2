package com.example.work.spotifystreamerstage1;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by work on 7/7/15.
 */
public class trackInfo implements Parcelable {

    /*
    For each track result you should extract the following data:
    track name
    album name
    Album art thumbnail (large (640px for Now Playing screen) and small (200px for list items)). If the image size does not exist in the API response, you are free to choose whatever size is available.)
    preview url* - This is an HTTP url that you use to stream audio. You won’t need to use this until Stage 2.
     */
    String artistName;
    String trackName;
    String albumName;
    String artThumbnail;
    String previewUrl;
    String desiredArt;
    String albumID;

    public trackInfo(String artistName, String trackName, String albumName, String artThumbnail, String previewUrl, String desired, String albumID) {
        this.artistName = artistName;
        this.trackName = trackName;
        this.albumName = albumName;
        this.artThumbnail = artThumbnail;
        this.previewUrl = previewUrl;
        this.desiredArt = desired;
        this.albumID = albumID;
    }

    // implement parcelable
    public int describeContents() {
        return 0;
    }

    private trackInfo(Parcel in) {
        artistName = in.readString();
        trackName = in.readString();
        albumName = in.readString();
        artThumbnail = in.readString();
        previewUrl = in.readString();
        desiredArt = in.readString();
        albumID = in.readString();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(artistName);
        out.writeString(trackName);
        out.writeString(albumName);
        out.writeString(artThumbnail);
        out.writeString(previewUrl);
        out.writeString(desiredArt);
        out.writeString(albumID);
    }

    public static final Parcelable.Creator<trackInfo> CREATOR = new Parcelable.Creator<trackInfo>() {
        public trackInfo createFromParcel(Parcel in) {
            return new trackInfo(in);
        }

        public trackInfo[] newArray(int size) {
            return new trackInfo[size];
        }
    };

}
