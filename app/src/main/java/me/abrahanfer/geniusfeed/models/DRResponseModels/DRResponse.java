package me.abrahanfer.geniusfeed.models.DRResponseModels;

import java.net.URL;

/**
 * Created by abrahan on 19/03/16.
 */
public class DRResponse {
    private Long count;

    private URL next;

    private URL previous;

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public URL getNext() {
        return next;
    }

    public void setNext(URL next) {
        this.next = next;
    }

    public URL getPrevious() {
        return previous;
    }

    public void setPrevious(URL previous) {
        this.previous = previous;
    }
}
