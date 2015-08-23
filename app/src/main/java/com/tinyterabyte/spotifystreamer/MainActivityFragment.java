package com.tinyterabyte.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;


public class MainActivityFragment extends Fragment implements TaskCompletedListener {
    List<ArtistParcelable> mArtists;
    View mRootView;

    public void setArtistParcelableList(List<ArtistParcelable> artists) {
        mArtists = artists;
        if (artists == null || artists.size() == 0) {
            artists = new ArrayList<>();
            artists.add(new ArtistParcelable("No artists found.", null, null));
        }
        ArtistsAdapter adapter = new ArtistsAdapter(getActivity(), artists);
        ListView listView = (ListView) mRootView.findViewById(R.id.listView);
        listView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        final MainActivityFragment fragment = this;
        EditText editText = (EditText) mRootView.findViewById(R.id.editText);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s == null) {
                    return;
                }
                if (s.toString().equals("")) {
                    // Could empty the ListView here.
                    // But why bother?
                    // Works fine regardless.
                    return;
                }
                // Is network available?
                ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
                    // No network. Display information: There is no connection.
                    Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Search for artists with query `s.toString()`
                final AsyncTask task = new FetchArtistsTask(getActivity(), fragment, mRootView);
                task.execute(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // (Should not call super here)

        mRootView = inflater.inflate(R.layout.fragment_main, container, false);
        EditText editText = (EditText) mRootView.findViewById(R.id.editText);

        if (savedInstanceState != null) {
            setArtistParcelableList(savedInstanceState.<ArtistParcelable>getParcelableArrayList("artists"));
            // Restore listView state.
            ListView listView = (ListView) mRootView.findViewById(R.id.listView);
            listView.onRestoreInstanceState(savedInstanceState.getParcelable("listViewState"));
        } else {
            // All is new...
            editText.setText("");
        }

        final MainActivityFragment fragment = this;
        ListView listView = (ListView) mRootView.findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // User clicked on an artist...
                ArtistParcelable artist = (ArtistParcelable) parent.getAdapter().getItem(position);

                // The "No artists found." row doesn't have an id.
                if (artist.getId() == null) {
                    Toast.makeText(getActivity(), "Try a different search!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check to see if there are any Top Tracks for this artist.
                // We do this here because if there are no tracks,
                // we will show a Toast instead of starting a TopTracksActivity.
                final AsyncTask task = new FetchTopTracksTask(fragment, artist.getId());
                task.execute();
            }
        });



        return mRootView;
    }

    // TaskCompletedListener method for Top Tracks
    @Override
    public void onTaskCompleted(Object o) {
        // Retrieved Top Tracks...
        final List<Track> list = (List<Track>) o;

        // We already checked for null in FetchTopTracksTask, but it won't hurt to make sure.
        if (list == null || list.size() == 0) {
            Toast.makeText(getActivity(), "Artist has no top tracks", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(getActivity(), TopTracksActivity.class);

        // Turn `list` into Parcelable.
        ArrayList<TrackParcelable> trackParcelableArrayList = new ArrayList<>();
        for (Track track: list) {
            String name = track.name;
            String id = track.id;
            String artistName = track.artists.get(0).name;
            String imageUrl = null;
            if (track.album.images.size() > 0) {
                imageUrl = track.album.images.get(0).url;
            }
            trackParcelableArrayList.add(new TrackParcelable(name, artistName, imageUrl, id));
        }

        // Put Parcelable list into bundle.
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("trackList", trackParcelableArrayList);
        intent.putExtras(bundle);
        getActivity().startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("artists", (ArrayList<? extends Parcelable>) mArtists);
        ListView listView = (ListView) mRootView.findViewById(R.id.listView);
        // Save listView state...
        outState.putParcelable("listViewState", listView.onSaveInstanceState());

        super.onSaveInstanceState(outState);
    }
}

class FetchArtistsTask extends AsyncTask {
    private Context mContext;
    private MainActivityFragment mFragment;
    private View mView;

    public FetchArtistsTask(Context context, MainActivityFragment fragment, View view) {
        mContext = context;
        mFragment = fragment;
        mView = view;
    }

    @Override
    protected Object doInBackground(Object[] params) {

        SpotifyApi api = new SpotifyApi();
        SpotifyService spotify = api.getService();

        try {
            ArtistsPager results = spotify.searchArtists(params[0].toString());
            List<Artist> list = results.artists.items;
            ArrayList<ArtistParcelable> artistParcelableArrayList = new ArrayList<ArtistParcelable>();
            for (Artist artist: list) {
                String name = artist.name;
                String id = artist.id;
                List<Image> images = artist.images;
                String thumbnailUrl = null;
                for (int i = 0; i < images.size(); i++) {
                    Image image = images.get(i);
                    // Use image between 200 and 300 pixels wide as thumbnail
                    if (image.width >= 200 && image.width <= 300) {
                        thumbnailUrl = image.url;
                        break;
                    }
                }
                String imageUrl = thumbnailUrl;
                artistParcelableArrayList.add(new ArtistParcelable(name, imageUrl, id));
            }
            return artistParcelableArrayList;
        } catch (Exception e) {
            Log.v("searchArtists", e.toString());
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Object o) {
        super.onPostExecute(o);

        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // If no artists are found, we will let `setArtistParcelableList()` show
                // a row that indicates this. This is a nicer UI than showing a Toast.
                mFragment.setArtistParcelableList((ArrayList<ArtistParcelable>) o);
            }
        });
    }
}
