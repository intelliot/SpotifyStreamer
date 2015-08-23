package com.tinyterabyte.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by e on 8/9/15.
 */
public class TrackParcelable implements Parcelable {
    private String mName;
    private String mArtistName;
    private String mImageUrl;
    private String mId;

    public String getName() {
        return mName;
    }

    public String getArtistName() {
        return mArtistName;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getId() {
        return mId;
    }

    public TrackParcelable(String name, String artistName, String imageUrl, String id) {
        mName = name;
        mArtistName = artistName;
        mImageUrl = imageUrl;
        mId = id;
    }

    private TrackParcelable(Parcel in) {
        mName = in.readString();
        mArtistName = in.readString();
        mImageUrl = in.readString();
        mId = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mArtistName);
        dest.writeString(mImageUrl);
        dest.writeString(mId);
    }

    public static final Creator<TrackParcelable> CREATOR = new Creator<TrackParcelable>() {
        @Override
        public TrackParcelable createFromParcel(Parcel source) {
            return new TrackParcelable(source);
        }

        @Override
        public TrackParcelable[] newArray(int size) {
            return new TrackParcelable[size];
        }
    };
}
