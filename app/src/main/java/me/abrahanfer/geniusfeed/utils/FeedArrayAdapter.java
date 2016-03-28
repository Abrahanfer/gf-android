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
import me.abrahanfer.geniusfeed.models.Feed;
import me.abrahanfer.geniusfeed.models.FeedItemRead;

/**
 * Created by abrahanfer on 30/09/15.
 */
public class FeedArrayAdapter extends ArrayAdapter<Feed> {
    public FeedArrayAdapter(Context context ,ArrayList<Feed>
            feeds){
        super(context, 0, feeds);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        Feed feed = getItem(position);

        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout
                    .feed,parent, false);
        }

        TextView titleText =(TextView) convertView.findViewById(R.id
                .textFeedTitle);
        TextView linkText =(TextView) convertView.findViewById(R.id
                .textFeedLink);

        titleText.setText(feed.getTitle());
        linkText.setText(feed.getLink().toString());

        return convertView;
    }
}
