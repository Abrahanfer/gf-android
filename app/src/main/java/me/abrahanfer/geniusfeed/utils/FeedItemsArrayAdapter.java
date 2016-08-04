package me.abrahanfer.geniusfeed.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import me.abrahanfer.geniusfeed.R;
import me.abrahanfer.geniusfeed.models.Feed;
import me.abrahanfer.geniusfeed.models.FeedItem;
import me.abrahanfer.geniusfeed.models.FeedItemRead;

/**
 * Created by abrahan on 19/03/16.
 */
public class FeedItemsArrayAdapter extends RecyclerView.Adapter<FeedItemsArrayAdapter.ViewHolder> {

    private ArrayList<FeedItemRead> mFeedItemReadList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View mContainerView;

        public ViewHolder(View v){
            super(v);
            mContainerView = v;
        }
    }

    public FeedItemsArrayAdapter(ArrayList<FeedItemRead>
            feedItems){
        mFeedItemReadList = feedItems;
    }

   /* @Override
    public View getView(int position, View convertView, ViewGroup parent){
        FeedItemRead feedItemRead = getItem(position);

        //if (convertView == null){
            if(feedItemRead.getRead()) {
                Log.e("Entra alguno aqui?", "Mirando el log");
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.feed_item, parent, false);
            }else{
                Log.e("Entra alguno aqui? 2", "Mirando el log");
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.feed_item_unread,parent,false);
            }
        //}

        TextView titleText =(TextView) convertView.findViewById(R.id
                                                                        .textFeedItemTitle);
        TextView linkText =(TextView) convertView.findViewById(R.id
                                                                       .textFeedItemLink);

        titleText.setText(feedItemRead.getFeed_item().getTitle());
        linkText.setText(feedItemRead.getFeed_item().getLink());

        return convertView;
    }*/

    // Create new views (invoked by the layout manager)
    @Override
    public FeedItemsArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.feed_item, parent, false);
        FeedItemsArrayAdapter.ViewHolder vh = new FeedItemsArrayAdapter.ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(FeedItemsArrayAdapter.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        TextView titleText =(TextView) holder.mContainerView.findViewById(R.id.textFeedItemTitle);
        titleText.setText(mFeedItemReadList.get(position).getFeed_item().getTitle());
        FeedItemRead feedItemRead = mFeedItemReadList.get(position);

        if(feedItemRead.getRead()) {
            titleText.setText("Leido");
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mFeedItemReadList.size();
    }
}
