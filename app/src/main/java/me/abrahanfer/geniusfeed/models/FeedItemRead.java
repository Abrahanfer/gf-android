package me.abrahanfer.geniusfeed.models;

import java.net.URL;
import java.util.Date;

/**
 * Created by abrahanfer on 29/09/15.
 */
public class FeedItemRead {

    public FeedItem getFeed_item() {
        return feed_item;
    }

    public void setFeed_item(FeedItem feed_item) {
        this.feed_item = feed_item;
    }

    private FeedItem feed_item;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    private String user;

    public Date getUpdate_date() {
        return update_date;
    }

    public void setUpdate_date(Date update_date) {
        this.update_date = update_date;
    }

    private Date update_date;

    private Boolean read;

    private Boolean fav;

    private String pk;


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
}
