package com.tinyterabyte.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

public class TopTracksActivity extends AppCompatActivity {

    public ArrayList<TrackParcelable> mTrackParcelableArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Get tracks from bundle. Be sure to do this before loading the fragment, which uses this list.
        Bundle bundle = this.getIntent().getExtras();
        mTrackParcelableArrayList = bundle.getParcelableArrayList("trackList");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toptracks); // Load fragment view
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
