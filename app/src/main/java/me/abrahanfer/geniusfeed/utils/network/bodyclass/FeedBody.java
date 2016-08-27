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
    private List<Category> categories;

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
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

    public FeedBody(String title, URL link, List<Category> categories) {
        this.title = title;
        this.link = link;
        this.categories = categories;
    }
}
