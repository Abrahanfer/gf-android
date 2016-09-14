package me.abrahanfer.geniusfeed.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.einmalfel.earl.Item;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.annotations.RealmClass;
import me.abrahanfer.geniusfeed.FeedActivity;
import me.abrahanfer.geniusfeed.models.realmModels.FeedItemRealm;

/**
 * Created by abrahan on 29/09/15.
 */
public class FeedItem implements Parcelable {

    private String pk;
    private String title;
    private String link;
    private Date publicationDate;
    private Feed feed;
    private String item_id;

    public FeedItem(){}

    public FeedItem(Item item) {
        title = item.getTitle();
        link = item.getLink();

        Log.d(FeedActivity.EARL_TAG, "Mirando las enclosures" +
                item.getEnclosures());
    }

    public FeedItem(FeedItemRealm feedItemRealm) {
        pk = feedItemRealm.getPk();
        title = feedItemRealm.getTitle();
        link = feedItemRealm.getLink();
        publicationDate = feedItemRealm.getPublicationDate();
        item_id = feedItemRealm.getItem_id();
        feed = null;
    }

    protected FeedItem(Parcel in) {
        pk = in.readString();
        title = in.readString();
        link = in.readString();
        item_id = in.readString();
        publicationDate = (java.util.Date) in.readSerializable();
        feed = (Feed)in.readParcelable(Feed.class.getClassLoader());
    }

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Feed getFeed() {
        return feed;
    }

    public void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    // 99.9% of the time you can just ignore this
    @Override
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(pk);
        out.writeString(title);
        out.writeString(link);
        out.writeString(item_id);
        out.writeSerializable(publicationDate);
        out.writeParcelable(feed, flags);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<FeedItem> CREATOR = new Parcelable.Creator<FeedItem>() {
        public FeedItem createFromParcel(Parcel in) {
            return new FeedItem(in);
        }

        public FeedItem[] newArray(int size) {
            return new FeedItem[size];
        }
    };

    // Override equal method
    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof FeedItem))return false;
        FeedItem otherFeedItem = (FeedItem) other;
        if (otherFeedItem.getItem_id().equals(this.getItem_id())) {
            return true;
        } else {
            return false;
        }
    }
}
