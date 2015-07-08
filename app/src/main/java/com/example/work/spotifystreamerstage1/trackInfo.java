package com.example.work.spotifystreamerstage1;

/**
 * Created by work on 7/7/15.
 */
public class trackInfo {

    /*
    For each track result you should extract the following data:
    track name
    album name
    Album art thumbnail (large (640px for Now Playing screen) and small (200px for list items)). If the image size does not exist in the API response, you are free to choose whatever size is available.)
    preview url* - This is an HTTP url that you use to stream audio. You won’t need to use this until Stage 2.
     */
    String trackName;
    String albumName;
    String artThumbnail;
    String previewUrl;

    public trackInfo(String trackName, String albumName, String artThumbnail, String previewUrl) {
        this.trackName = trackName;
        this.albumName = albumName;
        this.artThumbnail = artThumbnail;
        this.previewUrl = previewUrl;
    }
}
