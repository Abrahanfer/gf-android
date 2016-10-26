package me.abrahanfer.geniusfeed.utils;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.realm.RealmResults;
import me.abrahanfer.geniusfeed.R;
import me.abrahanfer.geniusfeed.models.Feed;
import me.abrahanfer.geniusfeed.models.FeedItemRead;
import me.abrahanfer.geniusfeed.models.realmModels.FeedItemReadRealm;

/**
 * Created by abrahan on 14/09/16.
 */

public class FavFeedArrayAdapter extends RecyclerView.Adapter<FavFeedArrayAdapter.ViewHolder> {
    private RealmResults<FeedItemReadRealm> mFavFeedArrayList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View mContainerView;

        public ViewHolder(View v){
            super(v);
            mContainerView = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public FavFeedArrayAdapter(RealmResults<FeedItemReadRealm> feedArrayList) {
        mFavFeedArrayList = feedArrayList;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public FavFeedArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.fav_feed_item, parent, false);
        FavFeedArrayAdapter.ViewHolder vh = new FavFeedArrayAdapter.ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(FavFeedArrayAdapter.ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        TextView titleText = (TextView) holder.mContainerView.findViewById(R.id.textFavFeedItemTitle);
        titleText.setText(mFavFeedArrayList.get(position).getFeed_item().getTitle());

        Log.e("Mirando realm ", "postion: " + position + " " + mFavFeedArrayList.get(position).getPk());
        TextView pubDateText = (TextView) holder.mContainerView.findViewById(R.id.textFavFeedItemPubDate);
        Date pubDate = mFavFeedArrayList.get(position).getFeed_item().getPublicationDate();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        if(pubDate != null)
            pubDateText.setText(dateFormat.format(pubDate));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mFavFeedArrayList.size();
    }
}
