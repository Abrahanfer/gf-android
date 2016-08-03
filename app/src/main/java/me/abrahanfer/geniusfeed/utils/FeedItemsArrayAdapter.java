package me.abrahanfer.geniusfeed.utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import me.abrahanfer.geniusfeed.R;
import me.abrahanfer.geniusfeed.models.Feed;
import me.abrahanfer.geniusfeed.models.FeedItem;
import me.abrahanfer.geniusfeed.models.FeedItemRead;

/**
 * Created by abrahan on 19/03/16.
 */
public class FeedItemsArrayAdapter extends ArrayAdapter<FeedItemRead> {
    public FeedItemsArrayAdapter(Context context , ArrayList<FeedItemRead>
            feedItems){
        super(context, 0, feedItems);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        FeedItemRead feedItemRead = getItem(position);

        if (convertView == null){
            if(feedItemRead.getRead()) {
                Log.e("Entra alguno aqui?", "Mirando el log");
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.feed_item, parent, false);
            }else{
                Log.e("Entra alguno aqui? 2", "Mirando el log");
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.feed_item_unread,parent,false);
            }
        }

        TextView titleText =(TextView) convertView.findViewById(R.id
                                                                        .textFeedItemTitle);
        TextView linkText =(TextView) convertView.findViewById(R.id
                                                                       .textFeedItemLink);

        titleText.setText(feedItemRead.getFeed_item().getTitle());
        linkText.setText(feedItemRead.getFeed_item().getLink());

        return convertView;
    }
}
