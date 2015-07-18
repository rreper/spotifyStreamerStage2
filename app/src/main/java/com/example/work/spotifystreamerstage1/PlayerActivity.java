package com.example.work.spotifystreamerstage1;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


public class PlayerActivity extends ActionBarActivity {
    public String artistNameString;
    public String trackNameString;
    public String albumNameString;
    public String artUrlString;
    public String previewUrlString;
    private static boolean trackPlaying = false;
    private MediaPlayer mediaPlayer;
    private static playerCountDownTimer playTimer = null;
    private long timeRemaining = 0;
    private final long INTERVAL_MS = 100;
    public static SeekBar sb = null;
    private boolean trackCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        ActionBar myBar = getSupportActionBar();
        myBar.setTitle("Spotify Streamer");

        // need to just pass the item so all tracks are available.
        Intent intent = getIntent();
        artistNameString = intent.getStringExtra(MainActivityTracks.EXTRA_MESSAGE_ARTIST);
        trackNameString = intent.getStringExtra(MainActivityTracks.EXTRA_MESSAGE_TRACK);
        albumNameString = intent.getStringExtra(MainActivityTracks.EXTRA_MESSAGE_ALBUM);
        artUrlString = intent.getStringExtra(MainActivityTracks.EXTRA_MESSAGE_ART);
        previewUrlString = intent.getStringExtra(MainActivityTracks.EXTRA_MESSAGE_PREVIEW);

        TextView artistName = (TextView) findViewById(R.id.textViewPlayerArtist);
        artistName.setText(artistNameString);
        TextView trackName = (TextView) findViewById(R.id.textViewPlayerTrack);
        trackName.setText(trackNameString);
        TextView albumName = (TextView) findViewById(R.id.textViewPlayerAlbum);
        albumName.setText(albumNameString);

        // need image
        ImageView playerArtView = (ImageView) findViewById(R.id.imageViewPlayerArt);
        Picasso.with(getApplicationContext()).load(artUrlString).fit().placeholder(R.drawable.ghost).into(playerArtView);

        ImageButton b = (ImageButton) findViewById(R.id.imageButtonTrackPlay);
        b.setEnabled(false); // disable until onPrepared gets called
        ImageButton bPrev = (ImageButton) findViewById(R.id.imageButtonTrackPrev);
        bPrev.setEnabled(false); // disable until onPrepared gets called
        ImageButton bNext = (ImageButton) findViewById(R.id.imageButtonTrackNext);
        bNext.setEnabled(false); // disable until onPrepared gets called

        sb = (SeekBar) findViewById(R.id.seekBarPlayer);

        // need to handle when user drags seekbar to new point


        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                int duration = 0;
                if (mp.equals(mediaPlayer)) {
                    ImageButton b = (ImageButton) findViewById(R.id.imageButtonTrackPlay);
                    b.setEnabled(true);

                    duration = mediaPlayer.getDuration() / 1000;

                    // could be a problem if the integer can't handle the getDuration()
                    sb.setMax(mediaPlayer.getDuration()); // accept round off

                    TextView trackEnd = (TextView) findViewById(R.id.textViewTrackEnd);
                    if (duration < 60)
                        trackEnd.setText("0:" + Integer.toString(duration));
                    else
                        trackEnd.setText(Integer.toString(duration / 60) + ":" + Integer.toString(duration % 60));
                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mp.equals(mediaPlayer)) {
                    sb.setProgress(sb.getMax());  // finish with the bar complete
                    ImageButton b = (ImageButton) findViewById(R.id.imageButtonTrackPlay);
                    trackPlaying = false;
                    b.setImageResource(android.R.drawable.ic_media_play);
                    Log.d("PlayerActivity:", "onCompletion");
                    trackCompleted = true;
                    //mediaPlayer.seekTo(0);  // rewind or prev track?
                }
            }
        });

        try {
            mediaPlayer.setDataSource(previewUrlString);
            mediaPlayer.prepareAsync(); // might take long! (for buffering, etc)
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("PlayerActivity:", "onStop");
        trackPlaying = false;
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        if (playTimer != null)
           playTimer.cancel();
    }

    public void onClick(View v) {
        final int id = v.getId();

        ImageButton b = (ImageButton) findViewById(R.id.imageButtonTrackPlay);

        switch (id) {
            case R.id.imageButtonTrackPrev:

                timeRemaining = 0;
                b.setEnabled(false);

                Toast.makeText(getApplicationContext(), "imageButtonTrackPrev", Toast.LENGTH_SHORT).show();
                try {
                    mediaPlayer.setDataSource(previewUrlString);
                    mediaPlayer.prepareAsync(); // might take long! (for buffering, etc)
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            case R.id.imageButtonTrackPlay:

                //Toast.makeText(getApplicationContext(), "imageButtonTrackPlay", Toast.LENGTH_SHORT).show();
                if (trackPlaying) {
                    trackPlaying = false;
                    b.setImageResource(android.R.drawable.ic_media_play);
                    mediaPlayer.pause();
                    if (playTimer != null) {
                        timeRemaining = playTimer.timeRemaining;
                        playTimer.cancel();
                        playTimer = null;
                    }
                }
                else {
                    trackPlaying = true;
                    b.setImageResource(android.R.drawable.ic_media_pause);
                    if (trackCompleted) {
                        mediaPlayer.seekTo(0);
                        trackCompleted = false;
                    }
                    mediaPlayer.start();
                    if (timeRemaining == 0) {
                        playTimer = new playerCountDownTimer(mediaPlayer.getDuration(), INTERVAL_MS, sb);
                    } else {
                        playTimer = new playerCountDownTimer(timeRemaining, INTERVAL_MS, sb);
                    }
                    playTimer.start();
                }
                break;

            case R.id.imageButtonTrackNext:

                timeRemaining = 0;
                b.setEnabled(false);

                Toast.makeText(getApplicationContext(), "imageButtonTrackNext", Toast.LENGTH_SHORT).show();
                try {
                    mediaPlayer.setDataSource(previewUrlString);
                    mediaPlayer.prepareAsync(); // might take long! (for buffering, etc)
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

/*
    static private class fetchAlbumInfoTask extends AsyncTask<String, String, ArrayList<trackInfo>> {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.

        SpotifyApi api = new SpotifyApi();

        private final String asyncTAG = fetchAlbumInfoTask.class.getSimpleName();

         // The system calls this to perform work in a worker thread and
         // delivers it the parameters given to AsyncTask.execute()

        protected ArrayList<trackInfo> doInBackground(String... id) {
            Album results;

            totalTracksFound = 0;
            totalTracksShown = 0;

            if (id[0].equals("none") == false) {
                Log.d(asyncTAG, "artist name " + id[0] + "album name "+id[1]);

                publishProgress("50%");
                try {
                    SpotifyService spotify = api.getService();
//                    final Map<String, Object> options = new HashMap<String, Object>();
//                    options.put(SpotifyService.COUNTRY, "US");
//                    results = spotify.getArtistTopTrack(id[1], options);
                    results = spotify.getAlbum(id[1]);
                    results.tracks.items.get(0).id;  // get the id at the mainactivitytracks and store it with name then use it here?
                } catch (Exception e) {
                    totalTracksShown = 0;
                    totalTracksFound = 0;
                    ArrayList<trackInfo> data = new ArrayList<trackInfo>();
                    data.add(noEntries);
                    e.printStackTrace();
                    return data;
                }

                int len = results.tracks.size();
                totalTracksFound = len;
                if (len > 10) len = 10;  // list just the top 10
                if (len != 0) {
                    totalTracksShown = len;
                    ArrayList<trackInfo> data = new ArrayList<trackInfo>();
                    Log.d(asyncTAG, "len = " + len);
                    Track item;
                    int desiredArt = 0;
                    int maxFound = 0;
                    for (int i = 0; i < len; i++) {
                        item = results.tracks.get(i);
                        for (int j=0; j<item.album.images.size(); j++) {
                            //Log.d(asyncTAG, "image size h " + item.album.images.get(j).height + " w " + item.album.images.get(j).width);
                            if (item.album.images.get(j).width > maxFound) { desiredArt = j; maxFound = item.album.images.get(j).width; }
                        }
                        trackInfo a = new trackInfo(artistNameString, item.name, item.album.name,
                                item.album.images.get(0).url, item.preview_url, item.album.images.get(desiredArt).url);
                        data.add(a);
                    }
                    return data;
                } else {
                    ArrayList<trackInfo> data = new ArrayList<trackInfo>();
                    data.add(noEntries);
                    return data;
                }
            } else {
                totalTracksShown = 0;
                totalTracksFound = 0;
                ArrayList<trackInfo> data = new ArrayList<trackInfo>();
                data.add(noEntries);
                return data;
            }
        }

        @Override
        protected void onPreExecute() {
            Log.d(asyncTAG, "onPreExecute ... ");
        }

         // The system calls this to perform work in the UI thread and delivers
         // the result from doInBackground()

        protected void onPostExecute(ArrayList<trackInfo> result) {
            adapter.clear();
            if (result != null) {
                if (result.get(0).toString().equals("none"))
                    Toast.makeText(adapter.getContext(), "No Artists Found", Toast.LENGTH_SHORT).show();
                else
                    adapter.addAll(result);

            } else
                Toast.makeText(adapter.getContext(), "Internet Connection problem?", Toast.LENGTH_LONG).show();

            Log.d(TAG,Integer.toString(totalTracksShown) + " of " + Integer.toString(totalTracksFound));
            publishProgress("100%");

        }

        @Override
        protected void onProgressUpdate(String... progress) {
            Log.d(asyncTAG, progress[0]);
        }

    }

    */

}
