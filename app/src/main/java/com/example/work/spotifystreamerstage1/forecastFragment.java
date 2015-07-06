package com.example.work.spotifystreamerstage1;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Pager;

/**
 * A placeholder fragment containing a simple view.
 */
public class forecastFragment extends Fragment {
    private ArtistPictureAdapter adapter;

    public ArrayList<artistPicture> artistPictures = new ArrayList<artistPicture>();
    public artistPicture noEntries = new artistPicture("none", "none", "none");
    public int totalArtistsFound = 0;
    public int totalArtistsShown = 0;
    public int totalTracksFound = 0;

    ArrayList<String> weekforecast = new ArrayList<String>();
    public final String TAG = "forecastFragment";
    public final String weatherRequest =
            "http://api.openweathermap.org/data/2.5/forecast/daily?q=80401,us&mode=json&units=metric&cnt=7";
    public final static String EXTRA_MESSAGE = "com.example.work.spotifystreamerstage1.MESSAGE";

    public EditText artistName;
    public String artistNameString = null;
    public TextView artistCount;

    public forecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.d("Spotify","onCreate");
        if (savedInstanceState != null) {
            Log.d("Spotify","onCreate not null");
            artistNameString = savedInstanceState.getString("artist");
            Log.d("Spotify", "onCreate " + artistNameString);
        }
        else {
            artistNameString = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("Spotify","onSaveInstance");
        if (artistNameString != null) {
            Log.d("Spotify","onSaveInstance "+artistNameString);
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
            String name = artistName.getText().toString();
            new fetchWeatherTask().execute(name);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        Log.d("Spotify","onCreateView");

        artistName = (EditText)rootView.findViewById(R.id.editText);
        artistCount = (TextView)rootView.findViewById(R.id.textView2);

        artistName.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "text entered " + s.toString());
                String name = artistName.getText().toString();
                // can get null when deleting the last character
                if (name.isEmpty() == false) {
                    artistNameString = name;
                    new fetchWeatherTask().execute(name);
                }
                else {
                    artistNameString = "none";
                    new fetchWeatherTask().execute("none");
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });
/*

        adapter = new ArrayAdapter<String>(
//                getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, weekforecast);
                getActivity(), R.layout.list_item_artist, R.id.textView, weekforecast);
 */
        adapter = new ArtistPictureAdapter(getActivity(), artistPictures);

        ListView weatherView = (ListView) rootView.findViewById(R.id.listViewForecast);
        weatherView.setAdapter(adapter);

        weatherView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                artistPicture value = adapter.getItem(position);
                Toast.makeText(getActivity(),value.name,Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), detailActivty.class);
                intent.putExtra(EXTRA_MESSAGE, value.name);
                startActivity(intent);
            }
        });

        if (artistNameString != null) {
            new fetchWeatherTask().execute(artistNameString);
        }

        return rootView;

    }

 /* The date/time conversion code is going to be moved outside the asynctask later,
 * so for convenience we're breaking it out into its own method now.
 */
    private String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        // OWM returns daily forecasts based upon the local time of the city that is being
        // asked for, which means that we need to know the GMT offset to translate this data
        // properly.

        // Since this data is also sent in-order and the first day is always the
        // current day, we're going to take advantage of that to get a nice
        // normalized UTC date for all of our weather.

        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        String[] resultStrs = new String[numDays];
        for(int i = 0; i < weatherArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;
            String description;
            String highAndLow;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime;
            // Cheating to convert this to UTC time, which is what we want anyhow
            dateTime = dayTime.setJulianDay(julianStartDay+i);
            day = getReadableDateString(dateTime);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }

        for (String s : resultStrs) {
            Log.v(TAG, "Forecast entry: " + s);
        }
        return resultStrs;

    }

    private class fetchWeatherTask extends AsyncTask<String, String, ArrayList<artistPicture>> {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.

        SpotifyApi api = new SpotifyApi();

        private final String asyncTAG = fetchWeatherTask.class.getSimpleName();

        /** The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute() */
        protected ArrayList<artistPicture> doInBackground(String... name) {

                totalArtistsFound = 0;
                totalArtistsShown = 0;

                if (name[0].equals("none") == false) {
                    Log.d(asyncTAG, "artist name " + name[0]);

                    SpotifyService spotify = api.getService();
                    ArtistsPager results = spotify.searchArtists(name[0]);

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
                        data.clear();
                        for (int i = 0; i < len; i++) {
                            item = results.artists.items.get(i);
                            if (item.images.size() > 0)
                                url = item.images.get(0).url.toString();
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

                // adapter.notifyDataSetChanged(); makes no difference - it should have been called in addAll

                // with arrayAdapter, it internally calls notifyDataSetChanged

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