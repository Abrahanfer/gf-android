package me.abrahanfer.geniusfeed.models;

import java.net.URL;

/**
 * Created by abrahan on 29/09/15.
 */
public class FeedItem {
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

    public Feed getFeed() {
        return feed;
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
    }

    private String pk;
    private String title;
    private URL link;
    private Feed feed;
}
