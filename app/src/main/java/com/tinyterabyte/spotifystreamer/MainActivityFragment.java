package com.tinyterabyte.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;

public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArtistParcelable artist = (ArtistParcelable) parent.getAdapter().getItem(position);
                Intent intent = new Intent(getActivity(), TopTracksActivity.class);
                intent.putExtra("artistID", artist.getId());
                startActivity(intent);
            }
        });

        EditText editText = (EditText) rootView.findViewById(R.id.editText);
        editText.setText("");
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s == null) { return; }
                if (s.toString().equals("")) {
                    // Could empty the ListView here.
                    // But why bother?
                    // Works fine regardless.
                } else {
                    // Search for s.toString()
                    final AsyncTask task = new FetchArtistsTask(getActivity(), rootView);
                    task.execute(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        return rootView;
    }
}

class ArtistsAdapter extends ArrayAdapter<ArtistParcelable> {
    public ArtistsAdapter(Context context, List<ArtistParcelable> artists) {
        super(context, 0, artists);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ArtistParcelable artist = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_artist, parent, false);
        }
        TextView artistTextView = (TextView) convertView.findViewById(R.id.list_item_artist_textview);
        ImageView artistImageView = (ImageView) convertView.findViewById(R.id.imageView);
        artistTextView.setText(artist.getName());
        try {
//            if (artist.images.size() > 0) {
//                int positionOfSmallestImage = artist.images.size() - 1;
//                Picasso.with(getContext()).load(artist.images.get(positionOfSmallestImage).url).into(artistImageView);
//            }
            if (artist.getImageUrl() != null) {
                Picasso.with(getContext()).load(artist.getImageUrl()).into(artistImageView);
            }
        } catch (Exception e) {
            Log.e("getView", e.toString());
        }
        return convertView;
    }
}

class FetchArtistsTask extends AsyncTask {
    private Context mContext;
    private View mView;

    public FetchArtistsTask(Context context, View view) {
        mContext = context;
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
                List<ArtistParcelable> artists = (List<ArtistParcelable>) o;
                if (artists == null) return;
                ArtistsAdapter adapter = new ArtistsAdapter(mContext, artists);
                ListView listView = (ListView) mView.findViewById(R.id.listView);
                listView.setAdapter(adapter);
            }
        });
    }

//    @Override
//    protected void onPostExecute(final ArrayList<ArtistParcelable> artists) {
//        ((Activity) mContext).runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (artists == null) return;
//                ArtistsAdapter adapter = new ArtistsAdapter(mContext, artists);
//                ListView listView = (ListView) mView.findViewById(R.id.listView);
//                listView.setAdapter(adapter);
//            }
//        });
//    }
}
