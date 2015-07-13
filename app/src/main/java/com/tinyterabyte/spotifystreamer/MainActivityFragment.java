package com.tinyterabyte.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {

                SpotifyApi api = new SpotifyApi();
                SpotifyService spotify = api.getService();

                try {
                    ArtistsPager results = spotify.searchArtists(params[0].toString());
                    List<Artist> list = results.artists.items;
                    return list;
                } catch (Exception e) {
                    Log.v("searchArtists", e.toString());
                }
                return null;
            }

            @Override
            protected void onPostExecute(final Object o) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        final List<Artist> list = (List<Artist>) o;

                        if (list == null) return;

                        ArtistsAdapter adapter = new ArtistsAdapter(getActivity(), list);

                        ListView listView = (ListView) rootView.findViewById(R.id.listView);
                        listView.setAdapter(adapter);
                    }
                });
            }
        };

        task.execute("Beyonce");

        ListView listView = (ListView) rootView.findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), TopTracksActivity.class);
                startActivity(intent);
                Log.v("OIC", "started");
            }
        });

        return rootView;
    }
}

class ArtistsAdapter extends ArrayAdapter<Artist> {
    public ArtistsAdapter(Context context, List<Artist> artists) {
        super(context, 0, artists);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Artist artist = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_artist, parent, false);
        }

        TextView artistTextView = (TextView) convertView.findViewById(R.id.list_item_artist_textview);
        ImageView artistImageView = (ImageView) convertView.findViewById(R.id.imageView);

        artistTextView.setText(artist.name);
        try {
            if (artist.images.size() > 0) {
                Picasso.with(getContext()).load(artist.images.get(0).url).into(artistImageView);
            }
        } catch (Exception e) {
            Log.e("getView", e.toString());
        }

        return convertView;
    }
}
