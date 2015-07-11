package com.example.work.spotifystreamerstage1;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by work on 7/7/15.
 */
public class TrackInfoAdapter extends ArrayAdapter<trackInfo> {
    List<trackInfo> trackInfos;

    public TrackInfoAdapter(Activity context, List<trackInfo> trackInfos) {
        super(context, 0, trackInfos);
        this.trackInfos = trackInfos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        trackInfo trackInfo = getItem(position);
        //View rootView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_artist, parent, false);

        // prob need to check convertView to see if it is being recycled
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_track, parent, false);
        }

        if (trackInfo.trackName.equals("none") == false) {
            ImageView iconView = (ImageView) convertView.findViewById(R.id.imageViewCD);
            //iconView.setImageResource(R.drawable.ghost);
//            Picasso.with(getContext()).load(trackInfo.artThumbnail).resize(75,75)
            Picasso.with(getContext()).load(trackInfo.artThumbnail).fit().placeholder(R.drawable.ghost).into(iconView);
            TextView track = (TextView) convertView.findViewById(R.id.textViewTrack);
            track.setText(trackInfo.trackName);
            TextView album = (TextView) convertView.findViewById(R.id.textViewAlbum);
            album.setText(trackInfo.albumName);
            //Log.d(LOG_TAG,trackInfo.name);
        } else {
            ImageView iconView = (ImageView) convertView.findViewById(R.id.imageViewCD);
            iconView.setImageResource(android.R.color.transparent);
            TextView track = (TextView) convertView.findViewById(R.id.textViewTrack);
            track.setText("No tracks found");
            TextView album = (TextView) convertView.findViewById(R.id.textViewAlbum);
            album.setText("");
        }

        return convertView;
    }

}