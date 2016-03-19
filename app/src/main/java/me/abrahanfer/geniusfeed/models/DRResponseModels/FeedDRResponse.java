package me.abrahanfer.geniusfeed.models.DRResponseModels;

import me.abrahanfer.geniusfeed.models.Feed;

/**
 * Created by abrahan on 19/03/16.
 */
public class FeedDRResponse extends DRResponse {
    private Feed[] results;

    public Feed[] getResults() {
        return results;
    }

    public void setResults(Feed[] results) {
        this.results = results;
    }
}
