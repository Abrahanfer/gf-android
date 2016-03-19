package me.abrahanfer.geniusfeed.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import me.abrahanfer.geniusfeed.R;
import me.abrahanfer.geniusfeed.models.Feed;
import me.abrahanfer.geniusfeed.models.FeedItem;

/**
 * Created by abrahan on 19/03/16.
 */
public class FeedItemsArrayAdapter extends ArrayAdapter<FeedItem> {
    public FeedItemsArrayAdapter(Context context , ArrayList<FeedItem>
            feedItems){
        super(context, 0, feedItems);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        FeedItem feedItem = getItem(position);

        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout
                                                                            .feed_item, parent, false);
        }

        TextView titleText =(TextView) convertView.findViewById(R.id
                                                                        .textFeedItemTitle);
        TextView linkText =(TextView) convertView.findViewById(R.id
                                                                       .textFeedItemLink);

        titleText.setText(feedItem.getTitle());
        linkText.setText(feedItem.getLink());

        return convertView;
    }
}
