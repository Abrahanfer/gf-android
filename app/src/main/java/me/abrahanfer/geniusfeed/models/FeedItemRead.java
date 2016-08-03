package me.abrahanfer.geniusfeed.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.BoringLayout;

import java.net.URL;
import java.util.Date;

/**
 * Created by abrahanfer on 29/09/15.
 */
public class FeedItemRead implements Parcelable {

    private long pk;

    private Date update_date;

    private Boolean read;

    private Boolean fav;

    private String user;

    private FeedItem feed_item;

    public FeedItem getFeed_item() {
        return feed_item;
    }

    public void setFeed_item(FeedItem feed_item) {
        this.feed_item = feed_item;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Date getUpdate_date() {
        return update_date;
    }

    public void setUpdate_date(Date update_date) {
        this.update_date = update_date;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public Boolean getFav() {
        return fav;
    }

    public void setFav(Boolean fav) {
        this.fav = fav;
    }

    public long getPk() {
        return pk;
    }

    public void setPk(long pk) {
        this.pk = pk;
    }

    public FeedItemRead(Boolean read, Boolean fav, FeedItem feed_item) {
        this.read = read;
        this.fav = fav;
        this.feed_item = feed_item;
        // Default values
        this.update_date = new Date();
    }

    protected FeedItemRead(Parcel in) {
        pk = in.readLong();
        read = in.readByte() != 0;
        fav = in.readByte() != 0;
        update_date = (java.util.Date) in.readSerializable();
        feed_item = in.readParcelable(FeedItem.class.getClassLoader());
    }

    // 99.9% of the time you can just ignore this
    @Override
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(pk);
        out.writeByte((byte) (read ? 1 : 0));
        out.writeByte((byte) (fav ? 1 : 0));
        out.writeSerializable(update_date);
        out.writeParcelable(feed_item, flags);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<FeedItemRead> CREATOR = new Parcelable.Creator<FeedItemRead>() {
        public FeedItemRead createFromParcel(Parcel in) {
            return new FeedItemRead(in);
        }

        public FeedItemRead[] newArray(int size) {
            return new FeedItemRead[size];
        }
    };

    // Override equal method
    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof FeedItemRead))return false;
        FeedItemRead otherFeedItemRead = (FeedItemRead)other;
        return feed_item.equals(otherFeedItemRead.getFeed_item());
    }
}
