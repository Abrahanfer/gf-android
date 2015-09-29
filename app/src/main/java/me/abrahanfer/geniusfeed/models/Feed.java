package me.abrahanfer.geniusfeed.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.net.URL;

/**
 * Created by abrahan on 29/09/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Feed {
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

    private String pk;
    private String title;
    private URL link;
}
