package com.tinyterabyte.spotifystreamer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

public class TopTracksFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_toptracks, container, false);

        TopTracksActivity activity = (TopTracksActivity) getActivity();

        if (activity.mTrackParcelableArrayList != null && activity.mTrackParcelableArrayList.size() > 0) {
            // Display the tracks.
            TracksAdapter adapter = new TracksAdapter(activity, activity.mTrackParcelableArrayList);
            ListView listView = (ListView) rootView.findViewById(R.id.listView);
            listView.setAdapter(adapter);
        } else {
            // This shouldn't happen because we checked for this earlier.
            Toast.makeText(activity, "No tracks available", Toast.LENGTH_LONG).show();
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}

class TracksAdapter extends ArrayAdapter<TrackParcelable> {
    public TracksAdapter(Context context, ArrayList<TrackParcelable> tracks) {
        super(context, 0, tracks);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TrackParcelable track = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_track, parent, false);
        }

        TextView trackTextView = (TextView) convertView.findViewById(R.id.list_item_track_textview);
        TextView artistTextView = (TextView) convertView.findViewById(R.id.list_item_track_artist_textview);
        ImageView trackImageView = (ImageView) convertView.findViewById(R.id.imageView);

        try {
            trackTextView.setText(track.getName());
            artistTextView.setText(track.getArtistName());
        } catch (Exception e) {
            Log.e("setText", e.toString());
        }
        try {
            if (!track.getImageUrl().trim().isEmpty()) {
                Picasso.with(getContext()).load(track.getImageUrl()).into(trackImageView);
            }
        } catch (Exception e) {
            Log.e("getView", e.toString());
        }

        return convertView;
    }
}

class FetchTopTracksTask extends AsyncTask {
    private TaskCompletedListener mListener;
    private String mArtistID;

    // listener and artistID are both required.
    public FetchTopTracksTask(TaskCompletedListener listener, String artistID) {
        mListener = listener;
        mArtistID = artistID;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        SpotifyApi api = new SpotifyApi();
        SpotifyService spotify = api.getService();

        try {
            HashMap map = new HashMap();
            map.put("country", "us");
            Tracks results = spotify.getArtistTopTrack(mArtistID, map);
            List<Track> list = results.tracks;
            return list;
        } catch (Exception e) {
            Log.v("getArtistTopTrack", e.toString());
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Object o) {
        final List<Track> list = (List<Track>) o;

        // If artist has no Top Tracks, list will have size() == 0.
        // list should not be null - but just return if it is.
        if (list == null) return;

        // Call onTaskCompleted() in MainActivityFragment.
        if (mListener != null) {
            mListener.onTaskCompleted(o);
        }
    }
}
