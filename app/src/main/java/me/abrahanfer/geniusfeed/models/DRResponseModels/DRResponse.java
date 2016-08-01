package me.abrahanfer.geniusfeed.models.DRResponseModels;

import java.net.URL;

/**
 * Created by abrahan on 19/03/16.
 */
public class DRResponse {
    private long count;

    private String next;

    private String previous;

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }
}
