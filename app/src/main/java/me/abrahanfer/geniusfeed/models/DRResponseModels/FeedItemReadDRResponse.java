package me.abrahanfer.geniusfeed.models.DRResponseModels;

import java.net.URL;
import java.util.List;

import me.abrahanfer.geniusfeed.models.FeedItemRead;

/**
 * Created by abrahan on 29/09/15.
 */
public class FeedItemReadDRResponse extends DRResponse {

    private List<FeedItemRead> results;

    public List<FeedItemRead> getResults() {
        return results;
    }

    public void setResults(List<FeedItemRead> results) {
        this.results = results;
    }
}
