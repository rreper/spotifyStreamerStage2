package com.example.work.spotifystreamerstage1;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;

/**
 * A placeholder fragment containing a simple view.
 */
public class artistFragment extends Fragment {
    private ArtistPictureAdapter adapter;

    public ArrayList<artistPicture> artistPictures = new ArrayList<artistPicture>();
    public artistPicture noEntries = new artistPicture("none", "none", "none");
    public int totalArtistsFound = 0;
    public int totalArtistsShown = 0;

    public final String TAG = "artistFragment";
    public final static String EXTRA_MESSAGE = "com.example.work.spotifystreamerstage1.MESSAGE";
    public final static String EXTRA_MESSAGE_ID = "com.example.work.spotifystreamerstage1.MESSAGE_ID";

    public EditText artistName;
    public String artistNameString = null;
    public TextView artistCount;

    public artistFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            artistNameString = savedInstanceState.getString("artist");
        }
        else {
            artistNameString = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (artistNameString != null) {
            outState.putString("artist", artistNameString);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            Log.d(TAG, "selected refresh");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        artistName = (EditText)rootView.findViewById(R.id.editText);
        artistCount = (TextView)rootView.findViewById(R.id.textView2);

        artistName.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "text entered " + s.toString());
                String name = artistName.getText().toString();
                // can get null when deleting the last character
                if (name.isEmpty() == false) {
                    artistNameString = name;
                    new fetchArtistTask().execute(name);
                }
                else {
                    artistNameString = "none";
                    new fetchArtistTask().execute("none");
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });

        adapter = new ArtistPictureAdapter(getActivity(), artistPictures);

        ListView weatherView = (ListView) rootView.findViewById(R.id.listViewForecast);
        weatherView.setAdapter(adapter);

        weatherView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                artistPicture value = adapter.getItem(position);
                if (value.name.equals("none") == false) {
                    Toast.makeText(getActivity(), value.name, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), MainActivityTracks.class);
                    intent.putExtra(EXTRA_MESSAGE, value.name);
                    intent.putExtra(EXTRA_MESSAGE_ID, value.id);
                    startActivity(intent);
                }
            }
        });

        if (artistNameString != null) {
            new fetchArtistTask().execute(artistNameString);
        }

        return rootView;

    }

    private class fetchArtistTask extends AsyncTask<String, String, ArrayList<artistPicture>> {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        private final String asyncTAG = fetchArtistTask.class.getSimpleName();
        SpotifyApi api = new SpotifyApi();

        /** The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute() */
        protected ArrayList<artistPicture> doInBackground(String... name) {

            totalArtistsFound = 0;
            totalArtistsShown = 0;
            ArtistsPager results;

            if (name[0].equals("none") == false) {
                //Log.d(asyncTAG, "artist name " + name[0]);
                try {
                    SpotifyService spotify = api.getService();
                    results = spotify.searchArtists(name[0]); // default is 20 returned
                }
                catch (Exception e) {
                    Log.d(TAG,"Spotify service exception caught");
                    return null;
                }

                publishProgress("50%");

                int len = results.artists.total;
                totalArtistsFound = len;
                if (len > 20) len = 20;  // seems like a bug here in the wrapper
                if (len != 0) {
                    String url;
                    totalArtistsShown = len;
                    ArrayList<artistPicture> data = new ArrayList<artistPicture>();
                    Log.d(asyncTAG, "len = " + len);
                    Artist item;
                    for (int i = 0; i < len; i++) {
                        item = results.artists.items.get(i);
                        if (item.images.size() > 0)
                            url = item.images.get(0).url;
                        else
                            url = "none";
                        artistPicture a = new artistPicture( item.name, url, item.id);
                        Log.d(TAG,"found "+item.name);
                        data.add(a);
                    }
                    return data;
                }
                else {
                    ArrayList<artistPicture> data = new ArrayList<artistPicture>();
                    data.add(noEntries);
                    return data;
                }
            }
            else {
                totalArtistsShown = 0;
                totalArtistsFound = 0;
                ArrayList<artistPicture> data = new ArrayList<artistPicture>();
                data.add(noEntries);
                return data;
            }
        }

        @Override
        protected void onPreExecute() {
            Log.d(asyncTAG,"onPreExecute ... ");
        }

        /** The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground() */
        protected void onPostExecute(ArrayList<artistPicture> result) {
            adapter.clear();
            if (result != null) {
                if (result.get(0).toString().equals("none"))
                    Toast.makeText(adapter.getContext(), "No Artists Found", Toast.LENGTH_SHORT).show();
                else
                    adapter.addAll(result);
            }
            else
                Toast.makeText(adapter.getContext(),"Internet Connection problem?", Toast.LENGTH_LONG).show();

            artistCount.setText(Integer.toString(totalArtistsShown)+" of " + Integer.toString(totalArtistsFound));
            publishProgress("100%");

        }

        @Override
        protected void onProgressUpdate(String ... progress) {
            Log.d(asyncTAG, progress[0]);
        }

    }
}