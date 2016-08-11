package me.abrahanfer.geniusfeed.utils.network.bodyclass;

import java.net.URL;

/**
 * Created by abrahan on 10/08/16.
 */

public class FeedBody {

    public String title;
    public URL link;

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

    public FeedBody(String title, URL link) {
        this.title = title;
        this.link = link;
    }
}
