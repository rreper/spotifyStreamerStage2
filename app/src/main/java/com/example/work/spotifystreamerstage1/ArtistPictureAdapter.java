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
 * Created by work on 7/5/15.
 */
public class ArtistPictureAdapter extends ArrayAdapter<artistPicture> {
    private static final String LOG_TAG = ArtistPictureAdapter.class.getSimpleName();
    List<artistPicture> artistPictures;

    public ArtistPictureAdapter(Activity context, List<artistPicture> artistPictures) {
        super(context, 0, artistPictures);
        this.artistPictures = artistPictures;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        artistPicture ArtistPicture = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_artist, parent, false);
        }

        if (ArtistPicture.name.equals("none") == false) {
            ImageView iconView = (ImageView) convertView.findViewById(R.id.imageView);
            Picasso.with(getContext()).load(ArtistPicture.picture).fit().placeholder(R.drawable.ghost).into(iconView);
            TextView ArtistName = (TextView) convertView.findViewById(R.id.textView);
            ArtistName.setText(ArtistPicture.name);
            //Log.d(LOG_TAG,ArtistPicture.name);
        } else {
            ImageView iconView = (ImageView) convertView.findViewById(R.id.imageView);
            iconView.setImageResource(R.drawable.ghost);
//            iconView.setImageResource(android.R.color.transparent); optional way to handle no artist
            TextView ArtistName = (TextView) convertView.findViewById(R.id.textView);
            ArtistName.setText("No Artists Found");
        }

        return convertView;
    }

}