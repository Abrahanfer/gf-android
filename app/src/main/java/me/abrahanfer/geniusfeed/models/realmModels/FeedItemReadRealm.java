package me.abrahanfer.geniusfeed.models.realmModels;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by abrahan on 13/09/16.
 */

public class FeedItemReadRealm extends RealmObject {
    @PrimaryKey
    private long pk;
    private Date update_date;
    private Boolean read;
    private Boolean fav;
    private String user;
    private FeedItemRealm feed_item;

    public long getPk() {
        return pk;
    }

    public void setPk(long pk) {
        this.pk = pk;
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public FeedItemRealm getFeed_item() {
        return feed_item;
    }

    public void setFeed_item(FeedItemRealm feed_item) {
        this.feed_item = feed_item;
    }
}
