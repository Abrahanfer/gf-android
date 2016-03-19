package me.abrahanfer.geniusfeed.models;

import com.einmalfel.earl.RSSItem;

/**
 * Created by abrahan on 19/03/16.
 */
public class FeedItemRSS extends FeedItem{
    public FeedItemRSS(RSSItem rssItem){
        super(rssItem);
        mRssItem = rssItem;
    }

    private RSSItem mRssItem;
}
