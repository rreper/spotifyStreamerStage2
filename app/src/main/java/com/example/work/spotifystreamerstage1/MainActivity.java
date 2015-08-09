package com.example.work.spotifystreamerstage1;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


// consulted stackoverflow.com and developer.android.com for reference material on java
// https://guides.codepath.com/android/Using-an-ArrayAdapter-with-ListView
// also consulted commonsware - Busy Android Coder's Guide
// and of course the Sunshine App and Google-Udacity videos

public class MainActivity extends ActionBarActivity {
    public boolean mTwoPane = false;
    public final String ARTIST_DETAIL_TAG = "ADT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.artist_detail_container) != null) {
            //Toast.makeText(getApplicationContext(), "artist detail container found", Toast.LENGTH_SHORT).show();
            mTwoPane = true;
        } else {
            mTwoPane = false;
        }
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_forecast, new artistFragment())
                    .commit();

            if (mTwoPane) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.artist_detail_container, new MainActivityTracks.PlaceholderFragment(),ARTIST_DETAIL_TAG)
                        .commit();
            }

        }




    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

}
