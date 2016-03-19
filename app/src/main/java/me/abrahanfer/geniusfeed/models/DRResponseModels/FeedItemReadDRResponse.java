package me.abrahanfer.geniusfeed.models.DRResponseModels;

import java.net.URL;

import me.abrahanfer.geniusfeed.models.FeedItemRead;

/**
 * Created by abrahan on 29/09/15.
 */
public class FeedItemReadDRResponse {
    private FeedItemRead[] results;

    public FeedItemRead[] getResults() {
        return results;
    }

    public void setResults(FeedItemRead[] results) {
        this.results = results;
    }
}
