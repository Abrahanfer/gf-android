package me.abrahanfer.geniusfeed.models.realmModels;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by abrahan on 13/09/16.
 */

public class FeedItemRealm extends RealmObject {
    @PrimaryKey
    private String pk;
    private String title;
    private String link;
    private Date publicationDate;
    private FeedRealm feed;
    private String item_id;

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

    public Date getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
    }

    public FeedRealm getFeed() {
        return feed;
    }

    public void setFeed(FeedRealm feed) {
        this.feed = feed;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }
}
