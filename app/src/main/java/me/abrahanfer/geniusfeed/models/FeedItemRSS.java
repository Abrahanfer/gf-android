package me.abrahanfer.geniusfeed.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.einmalfel.earl.RSSItem;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by abrahan on 19/03/16.
 */
public class FeedItemRSS extends FeedItem{
    final static public String FEED_ITEM_RSS_MODEL_TAG = "FeedItemAtomModel";

    public String getRssFeedItemURL() {
        return rssFeedItemURL;
    }

    public void setRssFeedItemURL(URL rssFeedItemURL) {
        this.rssFeedItemURL = rssFeedItemURL.toString();
    }

    private String rssFeedItemURL;

    public FeedItemRSS(RSSItem rssItem){
        super(rssItem);
        rssFeedItemURL = rssItem.link.toString();
    }


    private FeedItemRSS(Parcel in) {
        super(in);
        rssFeedItemURL = in.readString();
    }

    // write your object's data to the passed-in Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(rssFeedItemURL);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<FeedItem> CREATOR = new Parcelable.Creator<FeedItem>() {
        public FeedItemRSS createFromParcel(Parcel in) {
            return new FeedItemRSS(in);
        }

        public FeedItemRSS[] newArray(int size) {
            return new FeedItemRSS[size];
        }
    };

}
