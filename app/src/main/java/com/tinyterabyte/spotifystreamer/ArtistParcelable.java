package com.tinyterabyte.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by e on 8/9/15.
 */
public class ArtistParcelable implements Parcelable {
    private String mImageUrl;
    private String mName;

    public String getId() {
        return mId;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getName() {
        return mName;
    }

    private String mId;

    public ArtistParcelable(String name, String imageUrl, String id) {
        mName = name;
        mImageUrl = imageUrl;
        mId = id;
    }

    private ArtistParcelable(Parcel in) {
        mName = in.readString();
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
        dest.writeString(mImageUrl);
        dest.writeString(mId);
    }

    public final Parcelable.Creator<ArtistParcelable> CREATOR = new Parcelable.Creator<ArtistParcelable>() {
        @Override
        public ArtistParcelable createFromParcel(Parcel source) {
            return new ArtistParcelable(source);
        }

        @Override
        public ArtistParcelable[] newArray(int size) {
            return new ArtistParcelable[size];
        }
    };
}
