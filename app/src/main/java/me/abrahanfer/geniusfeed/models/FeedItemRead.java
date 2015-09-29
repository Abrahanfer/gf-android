package me.abrahanfer.geniusfeed.models;

import java.net.URL;
import java.util.Date;

/**
 * Created by abrahanfer on 29/09/15.
 */
public class FeedItemRead {

    private URL feedItem;

    private Date updateDate;

    private Boolean read;

    private Boolean fav;

    public URL getFeedItem(){
        return feedItem;
    }

    public void setFeedItem(URL urlFeedItem){
        this.feedItem = urlFeedItem;
    }

    public Date getUpdateDate(){
        return updateDate;
    }

    public void setUpdateDate(Date updateDate){
        this.updateDate = updateDate;
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
}
