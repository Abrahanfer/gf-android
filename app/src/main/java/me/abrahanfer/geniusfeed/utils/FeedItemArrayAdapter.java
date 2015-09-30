package me.abrahanfer.geniusfeed.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import me.abrahanfer.geniusfeed.R;
import me.abrahanfer.geniusfeed.models.FeedItemRead;

/**
 * Created by abrahanfer on 30/09/15.
 */
public class FeedItemArrayAdapter extends ArrayAdapter<FeedItemRead> {
    public FeedItemArrayAdapter(Context context ,ArrayList<FeedItemRead>
            feedItemReads){
        super(context, 0, feedItemReads);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        FeedItemRead feedItemRead = getItem(position);

        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout
                    .feed_item,parent, false);
        }

        TextView titleText =(TextView) convertView.findViewById(R.id
                .textFeedItemTitle);
        TextView linkText =(TextView) convertView.findViewById(R.id
                .textFeedItemLink);

        titleText.setText(feedItemRead.getFeed_item().getTitle());
        linkText.setText(feedItemRead.getFeed_item().getLink().toString());

        return convertView;
    }
}
