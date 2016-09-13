package me.abrahanfer.geniusfeed.models.realmModels;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by abrahan on 13/09/16.
 */

public class CategoryRealm extends RealmObject {
    @PrimaryKey
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
