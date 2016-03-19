package me.abrahanfer.geniusfeed.models;

import com.einmalfel.earl.AtomEntry;

/**
 * Created by abrahan on 19/03/16.
 */
public class FeedItemAtom extends FeedItem {
    public FeedItemAtom(AtomEntry entry) {
        super(entry);
        mEntry = entry;
    }

    private AtomEntry mEntry;
}
