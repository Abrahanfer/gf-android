package me.abrahanfer.geniusfeed.models.realmModels;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by abrahan on 13/09/16.
 */

public class FeedRealm extends RealmObject {

    private String pk;
    @PrimaryKey
    private String title;
    private String linkURL;
    private RealmList<CategoryRealm> category_set;

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

    public String getLinkURL() {
        return linkURL;
    }

    public void setLinkURL(String linkURL) {
        this.linkURL = linkURL;
    }

    public RealmList<CategoryRealm> getCategory_set() {
        return category_set;
    }

    public void setCategory_set(RealmList<CategoryRealm> category_set) {
        this.category_set = category_set;
    }
}
