package me.abrahanfer.geniusfeed.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by abrahan on 29/09/15.
 */
public class Feed implements Parcelable {
    final static public String FEED_MODEL_TAG = "FeedModel";

    private String pk;
    private String title;
    private URL link;
    private List<Category> category_set;



    public Feed() {}

    private Feed(Parcel in) {
        pk = in.readString();
        title = in.readString();
        try {
            link = new URL(in.readString());
        } catch (MalformedURLException e) {
            Log.e(FEED_MODEL_TAG, "Exception launched");
            e.printStackTrace();
        }
        category_set = new ArrayList<Category>();
        in.readList(category_set, Category.class.getClassLoader());
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

    public URL getLink() {
        return link;
    }

    public void setLink(URL link) {
        this.link = link;
    }

    public List<Category> getCategory_set() {
        return category_set;
    }

    public void setCategory_set(List<Category> category_set) {
        this.category_set = category_set;
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
        out.writeString(link.toString());
        out.writeList(category_set);
    }

    // this is used to regenerate your object.
    // All Parcelables must have a CREATOR that implements
    // these two methods
    public static final Parcelable.Creator<Feed> CREATOR;

    static {
        CREATOR = new Creator<Feed>() {
            public Feed createFromParcel(Parcel in) {
                return new Feed(in);
            }

            public Feed[] newArray(int size) {
                return new Feed[size];
            }
        };
    }
}
