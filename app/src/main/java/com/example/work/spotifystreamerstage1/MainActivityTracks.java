package com.example.work.spotifystreamerstage1;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivityTracks extends ActionBarActivity {
    public final String TAG = "ActivityTracks";
    private static TrackInfoAdapter adapter;

    public static ArrayList<trackInfo> trackInfos = new ArrayList<trackInfo>();
    public static trackInfo noEntries = new trackInfo("none", "none", "none","none");
    public int totalTracksFound = 0;
    public int totalTracksShown = 0;

    public static String artistNameString = null;
    public TextView artistCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity_tracks);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        Intent intent = getIntent();
        String message = intent.getStringExtra(forecastFragment.EXTRA_MESSAGE);
        artistNameString = message;

        ActionBar myBar = getSupportActionBar();
        myBar.setTitle("Top 10 Tracks");
        myBar.setSubtitle(artistNameString);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_activity_tracks, menu);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_activity_tracks, container, false);

            trackInfo toby1 = new trackInfo("Toby Keith", "Beer For My Horses", "dummy","dummy");
            trackInfo toby2 = new trackInfo("Toby Keith", "Good As I Once Was", "dummy","dummy");
            trackInfos.add(toby1);
            trackInfos.add(toby2);
            adapter = new TrackInfoAdapter(getActivity(), trackInfos);

            ListView trackView = (ListView) rootView.findViewById(R.id.listViewTracks);
            trackView.setAdapter(adapter);

            trackView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    trackInfo value = adapter.getItem(position);
                    Toast.makeText(getActivity(), value.trackName, Toast.LENGTH_SHORT).show();
                }
            });

            if (artistNameString != null) {
                //new fetchWeatherTask().execute(artistNameString);
            }

            return rootView;
        }
    }
}
