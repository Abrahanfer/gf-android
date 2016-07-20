package me.abrahanfer.geniusfeed.models.DRResponseModels;

import java.util.List;

import me.abrahanfer.geniusfeed.models.Feed;

/**
 * Created by abrahan on 19/03/16.
 */
public class FeedDRResponse extends DRResponse {
    private List<Feed> results;

    public List<Feed> getResults() {
        return results;
    }

    public void setResults(List<Feed> results) {
        this.results = results;
    }
}
