package org.voidsink.anewjkuapp.rss.lib;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by paul on 17.11.2014.
 */
public class FeedInfoImpl implements FeedInfo {

    public FeedInfoImpl() {

    }

    protected String mTitle;
    protected Uri mLink;
    protected String mDescription;
    protected Integer mTTL;
    protected Uri mImage;

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public Uri getLink() {
        return mLink;
    }

    @Override
    public String getDescription() {
        return mDescription;
    }

    @Override
    public Integer getTTL() {
        return mTTL;
    }

    @Override
    public Uri getImage() {
        return mImage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mTitle);
        out.writeParcelable(mLink, flags);
        out.writeString(mDescription);
        out.writeInt(mTTL);
        out.writeParcelable(mImage, flags);
    }

    private FeedInfoImpl(Parcel in) {
        mTitle = in.readString();
        mLink = in.readParcelable(Uri.class.getClassLoader());
        mDescription = in.readString();
        mTTL = in.readInt();
        mImage = in.readParcelable(Uri.class.getClassLoader());
    }

    public static final Parcelable.Creator<FeedInfoImpl> CREATOR
            = new Parcelable.Creator<FeedInfoImpl>() {
        public FeedInfoImpl createFromParcel(Parcel in) {
            return new FeedInfoImpl(in);
        }

        public FeedInfoImpl[] newArray(int size) {
            return new FeedInfoImpl[size];
        }
    };

}
