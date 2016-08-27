package me.abrahanfer.geniusfeed.utils.network.bodyclass;

import java.net.URL;
import java.util.List;

import me.abrahanfer.geniusfeed.models.Category;

/**
 * Created by abrahan on 10/08/16.
 */

public class FeedBody {

    public String title;
    public URL link;
    private List<Category> category_set;

    public List<Category> getCategory_set() {
        return category_set;
    }

    public void setCategory_set(List<Category> category_set) {
        this.category_set = category_set;
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

    public FeedBody(String title, URL link, List<Category> category_set) {
        this.title = title;
        this.link = link;
        this.category_set = category_set;
    }
}
