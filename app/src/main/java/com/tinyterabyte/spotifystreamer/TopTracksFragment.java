package com.tinyterabyte.spotifystreamer;

import android.app.Activity;
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

import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

public class TopTracksFragment extends Fragment {

    String mArtistID;

    public TopTracksFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_toptracks, container, false);
        final AsyncTask task = new FetchTopTracksTask(getActivity(), rootView);
        task.execute();
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
}



//        getFragmentManager().putFragment(savedInstanceState, "TopTracksFragment", this);


//        Intent intent = getIntent();
//        String artistID = intent.getStringExtra("artistID");
//        getFragmentManager().getFragment(savedInstanceState, )

//        String[] array = {
//                "Loading..."
//        };
//        List<String> list = new ArrayList<String>(Arrays.asList(array));
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
//                R.layout.list_item_track,
//                R.id.list_item_track_artist_textview,
//                list);
//
//        ListView listView = (ListView) rootView.findViewById(R.id.listView);
//        listView.setAdapter(adapter);

//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
////                Intent intent = new Intent(MainActivity.this, TopTracksActivity.this);
////                startActivity(intent);
//            }
//        });



class TracksAdapter extends ArrayAdapter<Track> {
    public TracksAdapter(Context context, List<Track> tracks) {
        super(context, 0, tracks);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Track track = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_track, parent, false);
        }

        TextView trackTextView = (TextView) convertView.findViewById(R.id.list_item_track_textview);
        TextView artistTextView = (TextView) convertView.findViewById(R.id.list_item_track_artist_textview);
        ImageView trackImageView = (ImageView) convertView.findViewById(R.id.imageView);

        try {
            Log.v("track.name", track.name);
            String artistName = track.artists.get(0).name;
            Log.v("artistName", artistName);

            trackTextView.setText(track.name);
            artistTextView.setText(artistName);
        } catch (Exception e) {
            Log.e("setText", e.toString());
        }
        try {
            if (track.album.images.size() > 0) {
                Picasso.with(getContext()).load(track.album.images.get(0).url).into(trackImageView);
            }
        } catch (Exception e) {
            Log.e("getView", e.toString());
        }

        return convertView;
    }
}

class FetchTopTracksTask extends AsyncTask {
    private Context mContext;
    private View mView;
    private String mArtistID;

    public FetchTopTracksTask(Context context, View view) {
        mContext = context;
        mView = view;

        Activity activity = (Activity) context;

        mArtistID = activity.getIntent().getStringExtra("artistID");
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
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {

                final List<Track> list = (List<Track>) o;

                if (list == null) return;

                TracksAdapter adapter = new TracksAdapter(mContext, list);

                ListView listView = (ListView) mView.findViewById(R.id.listView);
                listView.setAdapter(adapter);
            }
        });
    }

}
