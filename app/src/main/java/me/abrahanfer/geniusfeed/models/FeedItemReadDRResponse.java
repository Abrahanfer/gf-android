package me.abrahanfer.geniusfeed.models;

import java.net.URL;

/**
 * Created by abrahan on 29/09/15.
 */
public class FeedItemReadDRResponse {

    private Long count;

    private URL next;

    private URL previous;

    private FeedItemRead[] results;

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

    public FeedItemRead[] getResults() {
        return results;
    }

    public void setResults(FeedItemRead[] results) {
        this.results = results;
    }
}
