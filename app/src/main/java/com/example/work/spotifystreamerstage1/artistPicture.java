package com.example.work.spotifystreamerstage1;

import android.media.Image;

/**
 * Created by work on 7/5/15.
 */
public class artistPicture {
    String name;
    String picture; // drawable ref id
    String id;

    public artistPicture(String name, String thumbnail, String id) {
        this.name = name;
        this.picture = thumbnail;
        this.id = id;
    }
}
