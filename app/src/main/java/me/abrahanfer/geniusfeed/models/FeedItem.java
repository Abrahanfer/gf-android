package me.abrahanfer.geniusfeed.models;

import android.util.Log;

import com.einmalfel.earl.Item;

import java.net.MalformedURLException;
import java.net.URL;

import me.abrahanfer.geniusfeed.FeedActivity;

/**
 * Created by abrahan on 29/09/15.
 */
public class FeedItem {

    public FeedItem(Item item) {
        pk = item.getPublicationDate().toString();
        title = item.getTitle();
        link = item.getLink();

        Log.d(FeedActivity.EARL_TAG, "Mirando las enclosures" + item.getEnclosures());
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

    public void setFeed(Feed feed) {
        this.feed = feed;
    }

    private String pk;
    private String title;
    private String link;
    private Feed feed;
}
