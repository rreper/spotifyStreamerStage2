package com.example.work.spotifystreamerstage1;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Pager;

/**
 * A placeholder fragment containing a simple view.
 */
public class forecastFragment extends Fragment {
    private ArrayAdapter<String> adapter;
    ArrayList<String> weekforecast = new ArrayList<String>();
    public final String TAG = "forecastFragment";
    public final String weatherRequest =
            "http://api.openweathermap.org/data/2.5/forecast/daily?q=80401,us&mode=json&units=metric&cnt=7";
    public final static String EXTRA_MESSAGE = "com.example.work.spotifystreamerstage1.MESSAGE";

    public EditText artistName;

    public forecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            Log.d(TAG,"selected refresh");

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

/*

            String[] weatherStrings;
            weatherStrings = new String[] {
                "Today - Sunny - 88/63",
                "Tomorrow - Sunny - 88/63",
                "Tuesday - Sunny - 88/63",
                "Wednesday - Sunny - 88/63",
                "Thursday - Sunny - 88/63",
                "Friday - Sunny - 88/63",
                "Saturday - Sunny - 88/63"
            };

            ArrayList<String> weekforecast = new ArrayList<String>(Arrays.asList(weatherStrings));

            adapter = new ArrayAdapter<String>(
                    getActivity(), R.layout.list_item_forecast,R.id.list_item_forecast_textview,weekforecast);

//            TextView weatherView = (TextView)getActivity().findViewById(R.id.list_item_forecast_textview);

            ListView weatherView = (ListView)rootView.findViewById(R.id.listViewForecast);

            weatherView.setAdapter(adapter);

            return rootView;
*/

        artistName = (EditText)rootView.findViewById(R.id.editText);

        artistName.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                Log.d(TAG,"text entered " + s.toString());
                String name = artistName.getText().toString();
                // can get null when deleting the last character
                if (name.isEmpty() == false)
                   new fetchWeatherTask().execute(name);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });

        adapter = new ArrayAdapter<String>(
                getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, weekforecast);

        ListView weatherView = (ListView) rootView.findViewById(R.id.listViewForecast);
        weatherView.setAdapter(adapter);

        weatherView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String value = adapter.getItem(position);
                Toast.makeText(getActivity(),value,Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), detailActivty.class);
                intent.putExtra(EXTRA_MESSAGE, value);
                startActivity(intent);
            }
        });

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

    private class fetchWeatherTask extends AsyncTask<String, String, String[]> {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.

        SpotifyApi api = new SpotifyApi();

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        /* weather app
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        int numDays = 7;
        **** weather app */
        private final String asyncTAG = fetchWeatherTask.class.getSimpleName();

        /** The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute() */
        protected String[] doInBackground(String... name) {

//            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast

                if (name[0] != null) {
                    Log.d(asyncTAG, "artist name " + name[0]);

                    SpotifyService spotify = api.getService();
                    ArtistsPager results = spotify.searchArtists(name[0]);

                /* weather app
                //http://api.openweathermap.org/data/2.5/forecast/daily?q=80401,us&mode=json&units=metric&cnt=7
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .authority("api.openweathermap.org")
                        .appendPath("data")
                        .appendPath("2.5")
                        .appendPath("forecast")
                        .appendPath("daily")
                        .appendQueryParameter("q", zipCode[0])
                        .appendQueryParameter("mode", "json")
                        .appendQueryParameter("units", "metric")
                        .appendQueryParameter("cnt", Integer.toString(numDays));
                String myUrl = builder.build().toString();
                Log.d(TAG, myUrl);
                publishProgress("10%");
                URL url = new URL(myUrl);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                *** weather app */

                    publishProgress("50%");

                /* weather app
                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();

                **** weather app */

            /* weather app
            } catch (IOException e) {

                Log.e(asyncTAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(asyncTAG, "Error closing stream", e);
                    }
                }
            }
            **** weather app */

            /* weather app
            try {
                String data[] = getWeatherDataFromJson(forecastJsonStr, numDays);
                publishProgress("100%");
                return data;
            } catch (org.json.JSONException e) {
                Log.e(TAG, "Error ", e);
                publishProgress("95%");
                return null;
            }
            *** weather app */

                    //return forecastJsonStr;
                    int len = results.artists.total;
                    if (len > 20) len = 20;
                    if (len != 0) {
                        String[] data = new String[len];
//            Log.d(asyncTAG, results.toString());
                        Log.d(asyncTAG, "len = " + len);
//            for (Artist item : results.artists.items) {
                        Artist item;
                        for (int i = 0; i < len; i++) {
                            item = results.artists.items.get(i);
                            Log.d(asyncTAG, "Found " + item.name);
                            data[i] = item.name;
                        }
                        return data;
                    }
                    else {
                        len = 1; // so we can say none
                        String[] data = new String[len];
                        data[0] = "none";
                        return data;
                    }
            }
            else {
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            Log.d(asyncTAG,"onPreExecute ... ");
        }

        /** The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground() */
        protected void onPostExecute(String[] result) {
            adapter.clear();
            if (result != null)
                if (result[0].equals("none"))
                    Toast.makeText(adapter.getContext(),"No Artists Found", Toast.LENGTH_SHORT).show();
                else
                    adapter.addAll(result);
            // with arrayAdapter, it internally calls notifyDataSetChanged

            //for (String item : result) {
            //    adapter.add(item);
                //Log.d(asyncTAG, item);
            //}

            else
                Toast.makeText(adapter.getContext(),"Internet Connection problem?", Toast.LENGTH_LONG).show();

            publishProgress("100%");

        }

        @Override
        protected void onProgressUpdate(String ... progress) {
            Log.d(asyncTAG, progress[0]);
        }

    }
}