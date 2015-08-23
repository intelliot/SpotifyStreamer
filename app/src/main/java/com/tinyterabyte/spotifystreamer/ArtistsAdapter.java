package com.tinyterabyte.spotifystreamer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by e on 8/23/15.
 */
class ArtistsAdapter extends ArrayAdapter<ArtistParcelable> {
    public ArtistsAdapter(Context context, List<ArtistParcelable> artists) {
        super(context, 0, artists);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ArtistParcelable artist = getItem(position);

        // This holder has references to the views.
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_artist, parent, false);
            holder = new ViewHolder();
            holder.artistName = (TextView) convertView.findViewById(R.id.list_item_artist_textview);
            holder.artistImage = (ImageView) convertView.findViewById(R.id.imageView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.artistName.setText(artist.getName());

        try {
            if (artist.getImageUrl() != null && !artist.getImageUrl().trim().isEmpty()) {
                Picasso.with(getContext()).load(artist.getImageUrl()).into(holder.artistImage);
            }
        } catch (Exception e) {
            Log.e("getView", e.toString());
        }
        return convertView;
    }
}

class ViewHolder {
    TextView artistName;
    ImageView artistImage;
}
