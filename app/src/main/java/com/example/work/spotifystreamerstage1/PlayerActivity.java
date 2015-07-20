package com.example.work.spotifystreamerstage1;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;


public class PlayerActivity extends ActionBarActivity {
    public String artistNameString = null;
    public static String trackNameString = null;
    public static int trackIndex = -1;
    public String albumNameString = null;
    public String artUrlString = null;
    public String previewUrlString = null;
    public String albumIDString = null;
    private static boolean trackPlaying = false;
    private MediaPlayer mediaPlayer;
    private static playerCountDownTimer playTimer = null;
    private long timeRemaining = 0;
    private final long INTERVAL_MS = 100;
    public static SeekBar sb = null;
    public static ImageButton bPrev = null;
    public static ImageButton bNext = null;
    public static TextView trackName = null;
    private boolean trackCompleted = false;
    public static Album spotifyResults = null;

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
        albumIDString = intent.getStringExtra(MainActivityTracks.EXTRA_MESSAGE_ALBUM_ID);

        TextView artistName = (TextView) findViewById(R.id.textViewPlayerArtist);
        artistName.setText(artistNameString);
        trackName = (TextView) findViewById(R.id.textViewPlayerTrack);
        trackName.setText(trackNameString);
        TextView albumName = (TextView) findViewById(R.id.textViewPlayerAlbum);
        albumName.setText(albumNameString);

        // go get album info
        if (albumIDString != null)
            new fetchAlbumInfoTask().execute(albumNameString, albumIDString);

        // need image
        ImageView playerArtView = (ImageView) findViewById(R.id.imageViewPlayerArt);
        Picasso.with(getApplicationContext()).load(artUrlString).fit().placeholder(R.drawable.ghost).into(playerArtView);

        ImageButton b = (ImageButton) findViewById(R.id.imageButtonTrackPlay);
        b.setEnabled(true); // disable until onPrepared gets called
        bPrev = (ImageButton) findViewById(R.id.imageButtonTrackPrev);
        bPrev.setEnabled(false); // disable until onPrepared gets called
        bNext = (ImageButton) findViewById(R.id.imageButtonTrackNext);
        bNext.setEnabled(false); // disable until onPrepared gets called

        sb = (SeekBar) findViewById(R.id.seekBarPlayer);

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (fromUser) {
                    // stop timer
                    if (playTimer != null) {
                        playTimer.cancel();
                    }

                    if (trackPlaying) {
                        mediaPlayer.pause();
                        mediaPlayer.seekTo(progress);
                        // create new timer
                        playTimer = new playerCountDownTimer(mediaPlayer.getDuration() - progress, INTERVAL_MS, sb);
                        playTimer.start();

                        mediaPlayer.start();
                    } else {
                        // paused and just want to move around
                        timeRemaining = mediaPlayer.getDuration() - progress;
                        mediaPlayer.seekTo(progress);
                    }
                }
            }

        });


        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                int duration = 0;
                if (mp.equals(mediaPlayer)) {

                    duration = mediaPlayer.getDuration() / 1000;

                    // could be a problem if the integer can't handle the getDuration()
                    sb.setMax(mediaPlayer.getDuration()); // accept round off

                    TextView trackEnd = (TextView) findViewById(R.id.textViewTrackEnd);
                    if (duration < 60)
                        trackEnd.setText("0:" + Integer.toString(duration));
                    else
                        trackEnd.setText(Integer.toString(duration / 60) + ":" + Integer.toString(duration % 60));

                    mediaPlayer.start();

                    ImageButton b = (ImageButton) findViewById(R.id.imageButtonTrackPlay);
                    b.setEnabled(true);
                    trackPlaying = true;
                    b.setImageResource(android.R.drawable.ic_media_pause);

                    // handle timer and seekbar
                    timeRemaining = 0;
                    playTimer = new playerCountDownTimer(mediaPlayer.getDuration(), INTERVAL_MS, sb);
                    playTimer.start();
                    Log.d("PlayerActivity:", "onPreparedListener");
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
                    timeRemaining = 0;
                    mediaPlayer.seekTo(0);  // rewind in case we want to play again
                }
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("PlayerActivity:", "onStop");
        trackPlaying = false;
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (playTimer != null)
           playTimer.cancel();
    }

    private void stopPlayer(ImageButton b) {
        sb.setProgress(0);
        trackCompleted = false; // reset so we know we have a new track
        trackPlaying = false;
        b.setImageResource(android.R.drawable.ic_media_play);
        if (playTimer != null) {
            timeRemaining = 0;
            playTimer.cancel();
            playTimer = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            mediaPlayer.stop();
            mediaPlayer.reset();  // get ready for the next track
            //mediaPlayer.release();
            //mediaPlayer = null;
        }
    }

    public void onClick(View v) {
        final int id = v.getId();
        boolean found = false;

        ImageButton b = (ImageButton) findViewById(R.id.imageButtonTrackPlay);

        switch (id) {
            case R.id.imageButtonTrackPrev:
                found = false;

                if (trackIndex > 0 ) {
                    trackIndex--;
                    previewUrlString = spotifyResults.tracks.items.get(trackIndex).preview_url;
                    trackNameString = spotifyResults.tracks.items.get(trackIndex).name;
                    // need track name
                    TextView trackName = (TextView) findViewById(R.id.textViewPlayerTrack);
                    trackName.setText(trackNameString+" ("+Integer.toString(trackIndex+1)+" of "+spotifyResults.tracks.total+")");
                    found = true;
                }

                if (found) {
                    stopPlayer(b);
                }
                else {
                    Toast.makeText(getApplicationContext(),"Track not found", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.imageButtonTrackPlay:
                // if we are playing, we just want to pause
                if (trackPlaying) {
                    trackCompleted = false; // reset for next time
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
                    // this is a resume or a new track from the beginning
                    trackPlaying = true;
                    b.setImageResource(android.R.drawable.ic_media_pause);

                    if ((trackCompleted == false) && (timeRemaining == 0)) {
                        // a new track requested to play from the beginning
                        if (mediaPlayer == null) {
                            mediaPlayer = new MediaPlayer();
                            Toast.makeText(getApplicationContext(), "Creating new mediaplayer", Toast.LENGTH_SHORT).show();
                        }

                        try {
                            mediaPlayer.setDataSource(previewUrlString);
                            mediaPlayer.prepareAsync(); // might take long! (for buffering, etc)
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(getApplicationContext(), "Preparing Audio Stream", Toast.LENGTH_SHORT).show();

                    } else if (trackCompleted) {
                        // just played a track and want to play again
                        trackCompleted = false; // reset for next time
                        mediaPlayer.start();
                        // handle timer and seekbar
                        playTimer = new playerCountDownTimer(mediaPlayer.getDuration(), INTERVAL_MS, sb);
                        playTimer.start();
                        //Toast.makeText(getApplicationContext(), "Playing again", Toast.LENGTH_SHORT).show();

                    } else {
                        // we paused and now want to continue
                        //Toast.makeText(getApplicationContext(), "Paused resuming "+Long.toString(timeRemaining), Toast.LENGTH_SHORT).show();
                        playTimer = new playerCountDownTimer(timeRemaining, INTERVAL_MS, sb);
                        playTimer.start();
                        mediaPlayer.start();
                    }
                }
                break;

            case R.id.imageButtonTrackNext:
                found = false;

                // find the position of the current track and get prev
                if ((trackIndex+1) < spotifyResults.tracks.total) {
                    trackIndex++;
                    previewUrlString = spotifyResults.tracks.items.get(trackIndex).preview_url;
                    trackNameString = spotifyResults.tracks.items.get(trackIndex).name;
                    // need track name
                    TextView trackName = (TextView) findViewById(R.id.textViewPlayerTrack);
                    trackName.setText(trackNameString+" ("+Integer.toString(trackIndex+1)+" of "+spotifyResults.tracks.total+")");
                    found = true;
                }

                if (found) {
                    stopPlayer(b);
                }
                else {
                    Toast.makeText(getApplicationContext(),"Track not found", Toast.LENGTH_SHORT).show();
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

    static private class fetchAlbumInfoTask extends AsyncTask<String, String, Album> {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.

        SpotifyApi api = new SpotifyApi();

        private final String asyncTAG = fetchAlbumInfoTask.class.getSimpleName();

         // The system calls this to perform work in a worker thread and
         // delivers it the parameters given to AsyncTask.execute()

        protected Album doInBackground(String... id) {
            Album results;

            if (id[0].equals("none") == false) {
                Log.d(asyncTAG, "album name " + id[0] + "album id "+id[1]);

                publishProgress("50%");
                try {
                    SpotifyService spotify = api.getService();
                    results = spotify.getAlbum(id[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }

                if (results.tracks.total != 0) {
                    return results;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            Log.d(asyncTAG, "onPreExecute ... ");
        }

         // The system calls this to perform work in the UI thread and delivers
         // the result from doInBackground()

        protected void onPostExecute(Album result) {
            if (result != null) {
                spotifyResults = result;

                // find the position of the current track and get prev
                for (int i=0; i<spotifyResults.tracks.total; i++) {
                    String name = spotifyResults.tracks.items.get(i).name;
                    if (trackNameString.equals(name)) {
                        trackIndex = i;
                    }
                }
                trackName.setText(trackNameString+" ("+Integer.toString(trackIndex+1)+" of "+spotifyResults.tracks.total+")");

                bPrev.setEnabled(true); // enable if there is something to play
                bNext.setEnabled(true);

            } else {
                bPrev.setEnabled(false);
                bNext.setEnabled(false);
            }
            publishProgress("100%");

        }

        @Override
        protected void onProgressUpdate(String... progress) {
            Log.d(asyncTAG, progress[0]);
        }

    }

}
