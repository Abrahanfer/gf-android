package me.abrahanfer.geniusfeed.utils.network.bodyclass;

/**
 * Created by abrahan on 23/07/16.
 */

public class FIReadUpdateBody {
    private Boolean read;
    private Boolean fav;

    public Boolean getFav() {
        return fav;
    }

    public void setFav(Boolean fav) {
        this.fav = fav;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    FIReadUpdateBody(Boolean read, Boolean fav) {
        this.read = read;
        this.fav = fav;
    }
}
