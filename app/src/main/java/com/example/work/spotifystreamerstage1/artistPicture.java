package com.example.work.spotifystreamerstage1;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by work on 7/5/15.
 */
public class artistPicture implements Parcelable{
    String name;
    String picture; // drawable ref id
    String id;


    public artistPicture(String name, String thumbnail, String id) {
        this.name = name;
        this.picture = thumbnail;
        this.id = id;
    }

    //     implement parcelable
    public int describeContents() {
        return 0;
    }

    private artistPicture(Parcel in) {
        name = in.readString();
        picture = in.readString();
        id = in.readString();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeString(picture);
        out.writeString(id);
    }

    public static final Parcelable.Creator<artistPicture> CREATOR = new Parcelable.Creator<artistPicture>() {
        public artistPicture createFromParcel(Parcel in) {
            return new artistPicture(in);
        }

        public artistPicture[] newArray(int size) {
            return new artistPicture[size];
        }
    };

}
