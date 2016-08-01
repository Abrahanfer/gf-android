package me.abrahanfer.geniusfeed.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.einmalfel.earl.RSSEnclosure;
import com.einmalfel.earl.RSSItem;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

/**
 * Created by abrahan on 19/03/16.
 */
public class FeedItemRSS extends FeedItem{
    final static public String FEED_ITEM_RSS_MODEL_TAG = "FeedItemAtomModel";

    private String rssFeedItemURL;

    private String description;

    private String enclosureURL;

    private Integer enclosureLength;

    private String enclosureMIME;

    public String getRssFeedItemURL() {
        return rssFeedItemURL;
    }

    public void setRssFeedItemURL(URL rssFeedItemURL) {
        this.rssFeedItemURL = rssFeedItemURL.toString();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEnclosureURL() {
        return enclosureURL;
    }

    public void setEnclosureURL(String enclosureURL) {
        this.enclosureURL = enclosureURL;
    }

    public Integer getEnclosureLength() {
        return enclosureLength;
    }

    public void setEnclosureLength(Integer enclosureLength) {
        this.enclosureLength = enclosureLength;
    }

    public String getEnclosureMIME() {
        return enclosureMIME;
    }

    public void setEnclosureMIME(String enclosureMIME) {
        this.enclosureMIME = enclosureMIME;
    }

    public FeedItemRSS(RSSItem rssItem){
        super(rssItem);
        super.setPublicationDate(rssItem.getPublicationDate());
        rssFeedItemURL = rssItem.link.toString();
        super.setItem_id(rssItem.guid.toString());
        description = rssItem.getDescription();
        if(rssItem.getEnclosures().size() > 0) {
            RSSEnclosure enclosure = (RSSEnclosure) rssItem.getEnclosures().get(0);
            enclosureURL = enclosure.getLink();
            enclosureLength = enclosure.getLength();
            enclosureMIME = enclosure.getType();
        }else{
            enclosureURL = "";
            enclosureLength = 0;
            enclosureMIME = "";
        }
    }


    private FeedItemRSS(Parcel in) {
        super(in);
        rssFeedItemURL = in.readString();
        description = in.readString();
        enclosureURL = in.readString();
        enclosureLength = new Integer(in.readInt());
        enclosureMIME = in.readString();
    }

    // write your object's data to the passed-in Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(rssFeedItemURL);
        out.writeString(description);
        out.writeString(enclosureURL);
        out.writeInt(enclosureLength.intValue());
        out.writeString(enclosureMIME);
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
