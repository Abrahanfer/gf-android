package me.abrahanfer.geniusfeed.models;

import android.text.BoringLayout;

import java.net.URL;
import java.util.Date;

/**
 * Created by abrahanfer on 29/09/15.
 */
public class FeedItemRead {

    private String pk;

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

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public FeedItemRead(Boolean read, Boolean fav, FeedItem feed_item) {
        this.read = read;
        this.fav = fav;
        this.feed_item = feed_item;
        // Default values
        this.update_date = new Date();
    }
}
