package com.example.work.spotifystreamerstage1;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class forecastFragment extends Fragment {
    private ArrayAdapter<String> adapter;
    ArrayList<String> weekforecast = new ArrayList<String>();
    public final String TAG = "forecastFragment";
    public final String weatherRequest =
            "http://api.openweathermap.org/data/2.5/forecast/daily?q=80401,us&mode=json&units=metric&cnt=7";

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
            new fetchWeatherTask().execute(weatherRequest);
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

        adapter = new ArrayAdapter<String>(
                getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, weekforecast);

        ListView weatherView = (ListView) rootView.findViewById(R.id.listViewForecast);

        weatherView.setAdapter(adapter);

        return rootView;

    }

    private class fetchWeatherTask extends AsyncTask<String, String, String> {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        private final String asyncTAG = fetchWeatherTask.class.getSimpleName();

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;
        /** The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute() */
        protected String doInBackground(String... urls) {

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=80401,us&mode=json&units=metric&cnt=7");

                publishProgress("10%");

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                publishProgress("50%");

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
            publishProgress("100%");

            return forecastJsonStr;
        }

        @Override
        protected void onPreExecute() {
            Log.d(asyncTAG,"onPreExecute ... ");
        }

        /** The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground() */
        protected void onPostExecute(String result) {
            weekforecast.add(result);
            Log.d(asyncTAG, result);
        }

        @Override
        protected void onProgressUpdate(String ... progress) {
            Log.d(asyncTAG, progress[0]);
        }

    }
}